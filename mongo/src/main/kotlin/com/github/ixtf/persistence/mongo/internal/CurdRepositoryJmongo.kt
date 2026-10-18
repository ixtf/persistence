package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.EntitiesDTO
import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.Jmongo.Companion.asFilter
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idIn
import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.internal.kit.count
import com.github.ixtf.persistence.mongo.internal.kit.exists
import com.github.ixtf.persistence.mongo.internal.kit.list
import com.github.ixtf.persistence.mongo.internal.kit.single
import com.github.ixtf.persistence.mongo.internal.kit.singleOrNull
import java.lang.reflect.ParameterizedType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.bson.conversions.Bson

abstract class CurdRepositoryJmongo<T : MongoEntity>(val jmongo: Jmongo) {
  @Suppress("UNCHECKED_CAST")
  val entityClass by lazy {
    val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    parameterizedType.actualTypeArguments[0] as Class<T>
  }
  val entityCollection by lazy { jmongo.metadata.entityCollection(entityClass) }
  val collection by lazy { jmongo.metadata.collection(entityClass) }
  val bsonCollection by lazy { jmongo.metadata.bsonCollection(entityClass) }

  suspend fun save(entity: T) =
    if (entityCollection.exists(entity.id)) update(entity) else insert(entity)

  suspend fun insert(entity: T): Unit = jmongo.uow { registerNew(entity) }

  suspend fun update(entity: T): Unit = jmongo.uow { registerDirty(entity) }

  suspend fun delete(entity: T): Unit = jmongo.uow { registerDelete(entity) }

  suspend fun purge(entity: T): Unit = jmongo.uow { registerPurge(entity) }

  suspend fun findOrNull(id: Any): T? = entityCollection.singleOrNull(id)

  suspend fun find(id: Any): T = entityCollection.single(id)

  fun listByIds(ids: Iterable<String>) = entityCollection.find(idIn(ids))

  fun listBy(ids: EntitiesDTO) = listByIds(ids.ids)

  suspend fun count(condition: Flow<Bson?>) = entityCollection.count(asFilter(condition))

  suspend fun count(vararg condition: Bson?) = count(condition.asFlow())

  suspend fun count(condition: Iterable<Bson?>) = count(condition.asFlow())

  suspend fun list(condition: Flow<Bson?>) = entityCollection.list(asFilter(condition))

  suspend fun list(vararg condition: Bson?) = list(condition.asFlow())

  suspend fun list(condition: Iterable<Bson?>) = list(condition.asFlow())
}
