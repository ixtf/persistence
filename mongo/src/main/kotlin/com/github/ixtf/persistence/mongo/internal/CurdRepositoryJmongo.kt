package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.EntitiesDTO
import com.github.ixtf.persistence.dto.EntityDTO
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
import java.security.Principal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.bson.conversions.Bson
import org.bson.types.ObjectId

abstract class CurdRepositoryJmongo<T : MongoEntity>(protected val jmongo: Jmongo) {
  @Suppress("UNCHECKED_CAST")
  protected val entityClass by lazy {
    val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    parameterizedType.actualTypeArguments[0] as Class<T>
  }
  protected val entityCollection by lazy { jmongo.metadata.entityCollection(entityClass) }
  protected val collection by lazy { jmongo.metadata.collection(entityClass) }
  protected val bsonCollection by lazy { jmongo.metadata.bsonCollection(entityClass) }

  suspend fun save(entity: T) =
    if (entityCollection.exists(entity.id)) update(entity) else insert(entity)

  suspend fun insert(entity: T): Unit = jmongo.uow { registerNew(entity) }

  suspend fun update(entity: T): Unit = jmongo.uow { registerDirty(entity) }

  suspend fun delete(entity: T): Unit = jmongo.uow { registerDelete(entity) }

  suspend fun purge(entity: T): Unit = jmongo.uow { registerPurge(entity) }

  suspend fun findOrNull(id: ObjectId): T? = entityCollection.singleOrNull(id)

  suspend fun find(id: ObjectId): T = entityCollection.single(id)

  suspend fun findOrNull(id: String): T? = entityCollection.singleOrNull(id)

  suspend fun find(id: String): T = entityCollection.single(id)

  suspend fun findOrNull(id: EntityDTO): T? = entityCollection.singleOrNull(id)

  suspend fun find(id: EntityDTO): T = entityCollection.single(id)

  suspend fun findOrNull(id: Principal): T? = entityCollection.singleOrNull(id)

  suspend fun find(id: Principal): T = entityCollection.single(id)

  suspend fun <ID : Any> findOrCreate(id: ID, block: suspend (ID) -> T) =
    entityCollection.singleOrNull(id) ?: block(id)

  suspend fun listByIds(ids: Iterable<String>) = entityCollection.find(idIn(ids))

  suspend fun listBy(ids: EntitiesDTO) = listByIds(ids.ids)

  suspend fun count(condition: Flow<Bson?>) = entityCollection.count(asFilter(condition))

  suspend fun count(vararg condition: Bson?) = count(condition.asFlow())

  suspend fun count(condition: Iterable<Bson?>) = count(condition.asFlow())

  suspend fun list(condition: Flow<Bson?>) = entityCollection.list(asFilter(condition))

  suspend fun list(vararg condition: Bson?) = list(condition.asFlow())

  suspend fun list(condition: Iterable<Bson?>) = list(condition.asFlow())
}
