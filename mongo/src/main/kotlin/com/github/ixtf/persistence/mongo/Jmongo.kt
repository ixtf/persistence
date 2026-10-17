@file:Suppress("UNCHECKED_CAST", "unused")

package com.github.ixtf.persistence.mongo

import com.gitee.ixtf.mongo.codec.JmongoRef
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.dto.EntityDTO
import com.mongodb.client.model.Filters.*
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import java.security.Principal
import kotlinx.coroutines.flow.*
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class Jmongo(val client: MongoClient, database: String) {
  companion object {
    const val ID_COL = "_id"
    const val DELETED_COL = "deleted"
    // const val DEFAULT_BATCH_SIZE = 10_000
    private val DELETED_FALSE = eq(DELETED_COL, false)

    fun idIn(o: Iterable<String>): Bson = `in`(ID_COL, o)

    fun idEq(o: Any): Bson =
      when (o) {
        is String,
        is ByteArray,
        is Long,
        is Int,
        is Short,
        is ObjectId -> eq(ID_COL, o)
        is EntityDTO -> eq(ID_COL, o.id)
        is Principal -> eq(ID_COL, o.name)
        is IEntity -> eq(ID_COL, o.id)
        is JmongoRef<*> -> eq(ID_COL, o.id)
        else -> throw IllegalArgumentException("Unsupported type: $o")
      }

    fun asFilter(condition: Flow<Bson?>): Flow<Bson?> = condition.onStart { emit(DELETED_FALSE) }
  }

  val database by lazy { client.getDatabase(database) }
  private val metadataCache: LoadingCache<Class<*>, MongoEntityMetadata> by lazy {
    Caffeine.newBuilder().build { MongoEntityMetadata(this, it) }
  }

  fun metadata(clazz: Class<*>): MongoEntityMetadata = metadataCache.get(clazz)

  fun database(clazz: Class<*>) = metadata(clazz).database

  fun collection(clazz: Class<*>) = metadata(clazz).collection

  inline fun <reified T : MongoEntityBase> collection(): MongoCollection<Document> =
    metadata(T::class.java).collection

  fun bsonCollection(clazz: Class<*>) = metadata(clazz).bsonCollection

  inline fun <reified T : MongoEntityBase> bsonCollection(): MongoCollection<BsonDocument> =
    metadata(T::class.java).bsonCollection

  fun <T : Any> entityCollection(clazz: Class<T>) =
    metadata(clazz).entityCollection as MongoCollection<T>

  inline fun <reified T : MongoEntityBase> entityCollection(): MongoCollection<T> =
    metadata(T::class.java).entityCollection as MongoCollection<T>

  fun uow() = MongoUnitOfWork(this)

  suspend fun uow(block: suspend MongoUnitOfWork.() -> Unit): Unit =
    uow().apply { block() }.commit()

  inline fun <reified T : MongoEntityBase> asRef(id: String): JmongoRef<T> =
    collection(T::class.java).namespace.run {
      JmongoRef(databaseName = databaseName, collectionName = collectionName, id = id)
    }

  inline fun <reified T : MongoEntityBase> fromBson(bson: BsonDocument): T =
    collection<T>().fromBson<T>(bson)

  inline fun <reified T : MongoEntityBase> fromBson(bson: String): T =
    fromBson(BsonDocument.parse(bson))

  suspend inline fun <reified T : MongoEntityBase> count(condition: Flow<Bson?>): Long =
    collection<T>().count(asFilter(condition))

  suspend inline fun <reified T : MongoEntityBase> count(vararg condition: Bson?): Long =
    count<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> count(condition: Iterable<Bson?>): Long =
    count<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> list(condition: Flow<Bson?>): FindFlow<T> =
    entityCollection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntityBase> list(vararg condition: Bson?): FindFlow<T> =
    list(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> list(condition: Iterable<Bson?>): FindFlow<T> =
    list(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> listDoc(condition: Flow<Bson?>) =
    collection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntityBase> listDoc(vararg condition: Bson?) =
    listDoc<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> listDoc(condition: Iterable<Bson?>) =
    listDoc<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> listBson(condition: Flow<Bson?>) =
    bsonCollection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntityBase> listBson(vararg condition: Bson?) =
    listBson<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> listBson(condition: Iterable<Bson?>) =
    listBson<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntityBase> singleOrNull(id: String) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntityBase> single(id: String) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntityBase> singleOrNull(id: ObjectId) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntityBase> single(id: ObjectId) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntityBase> singleOrNull(id: EntityDTO) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntityBase> single(id: EntityDTO) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntityBase> singleOrNull(id: Principal) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntityBase> single(id: Principal) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntityBase> singleOrNull(id: JmongoRef<*>) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntityBase> single(id: JmongoRef<*>) =
    entityCollection<T>().single(id)
}
