@file:Suppress("UNCHECKED_CAST")

package com.github.ixtf.persistence.arcade.internal

import com.arcadedb.database.Database
import com.arcadedb.database.MutableDocument
import com.arcadedb.query.sql.executor.Result
import com.arcadedb.schema.DocumentType
import com.arcadedb.schema.Type
import com.github.ixtf.persistence.arcade.ArcadeEntity
import com.github.ixtf.persistence.arcade.Jarcade
import com.github.ixtf.persistence.arcade.Jarcade.Companion.DELETED_COL
import com.github.ixtf.persistence.arcade.Jarcade.Companion.ID_COL
import java.lang.reflect.ParameterizedType
import kotlin.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * ArcadeDB 记录仓库基类，通用 CRUD 已全部实现。
 *
 * ArcadeDB 无 ORM，泛型映射通过 codec 挂钩完成：
 * - [toEntity]：查询结果 -> 领域对象
 * - [toProperties]：领域对象 -> 记录属性
 *
 * 子类只需提供两个钩子：
 * - [ensureSchema]：类型创建（VertexType / DocumentType）
 * - [newMutable]：新记录（[Database.newVertex] / [Database.newDocument]）
 */
abstract class CurdRepositoryJarcade<T : ArcadeEntity>(val jarcade: Jarcade) {
  val db: Database = jarcade.db

  @Suppress("UNCHECKED_CAST")
  val entityClass by lazy {
    val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    parameterizedType.actualTypeArguments[0] as Class<T>
  }

  /** 记录类型名，默认取实体类名。entityClass 是基类自己的 lazy，基类 init 时已就绪。 */
  protected open val recordType: String
    get() = entityClass.simpleName

  protected open val idIndexName: String
    get() = "${recordType}_${ID_COL}"

  init {
    db.ensureSchema()
  }

  /** 子类钩子：确保类型与索引存在 */
  protected abstract fun Database.ensureSchema()

  /** 子类钩子：创建可变新记录（Vertex / Document） */
  protected abstract fun Database.newMutable(): MutableDocument

  protected fun Database.ensureIndex(
    type: DocumentType,
    indexName: String,
    propertyName: String,
    propertyType: Type,
    unique: Boolean = false,
  ) {
    if (schema.existsIndex(indexName)) return
    type.getOrCreateProperty(propertyName, propertyType)
    command(
      "sql",
      "CREATE INDEX $indexName ON ${type.name} ($propertyName) ${if (unique) "UNIQUE" else "NOTUNIQUE"}",
    )
  }

  protected fun Database.dropIndex(indexName: String) {
    if (schema.existsIndex(indexName)) command("sql", "DROP INDEX $indexName")
  }

  // region codec 挂钩
  protected abstract fun Result.toEntity(): T

  protected abstract fun T.toProperties(): Map<String, Any?>

  protected open fun T.modifyProperties(): Map<String, Any?> = toProperties() - ID_COL

  // endregion

  open suspend fun findOrNull(id: String): T? =
    withContext(Dispatchers.IO) {
      db.query("sql", "SELECT FROM $recordType WHERE $ID_COL = :id", mapOf(ID_COL to id)).use {
        it.nextIfAvailable()?.toEntity()
      }
    }

  open suspend fun find(id: String): T =
    findOrNull(id) ?: throw NoSuchElementException("$recordType[$id] 不存在")

  open fun listByIds(ids: Iterable<String>): Flow<T> {
    val idSet = ids.toSet()
    if (idSet.isEmpty()) return emptyFlow()
    return flow {
        db
          .query("sql", "SELECT FROM $recordType WHERE $ID_COL in :ids", mapOf("ids" to idSet))
          .use { rs -> rs.forEach { emit(it.toEntity()) } }
      }
      .flowOn(Dispatchers.IO)
  }

  open fun list(): Flow<T> = flow {
    db.query("sql", "SELECT FROM $recordType WHERE $DELETED_COL = false").use { rs ->
      rs.forEach { emit(it.toEntity()) }
    }
  }
    .flowOn(Dispatchers.IO)

  open suspend fun save(entity: T) {
    withContext(Dispatchers.IO) {
      db.transaction {
        val existing =
          db
            .query("sql", "SELECT FROM $recordType WHERE $ID_COL = :id", mapOf(ID_COL to entity.id))
            .use { rs -> rs.nextIfAvailable()?.toElement() }
        if (existing == null) {
          db.newMutable().fromMap(entity.toProperties()).save()
        } else {
          // fromMap 是全量替换（会抹掉未包含的属性，如 id），更新必须用 merge 增量合并
          val mutable = existing.modify()
          mutable.merge(entity.modifyProperties())
          mutable.save()
        }
      }
    }
  }

  open suspend fun purge(entity: T) {
    withContext(Dispatchers.IO) {
      db.transaction {
        db
          .query("sql", "SELECT FROM $recordType WHERE $ID_COL = :id", mapOf(ID_COL to entity.id))
          .use { it.nextIfAvailable()?.toElement()?.delete() }
      }
    }
  }
}
