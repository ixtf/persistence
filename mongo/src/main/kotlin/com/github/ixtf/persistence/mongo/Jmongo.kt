@file:Suppress("UNCHECKED_CAST", "unused")

package com.github.ixtf.persistence.mongo

import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.dto.EntityDTO
import com.github.ixtf.persistence.mongo.internal.MongoEntityMetadata
import com.github.ixtf.persistence.mongo.internal.MongoRuntimeModuleConfig
import com.github.ixtf.persistence.mongo.internal.MongoUnitOfWork
import com.github.ixtf.persistence.mongo.internal.codec.JmongoRef
import com.github.ixtf.persistence.mongo.internal.kit.asBson
import com.github.ixtf.persistence.mongo.internal.kit.count
import com.github.ixtf.persistence.mongo.internal.kit.fromBson
import com.github.ixtf.persistence.mongo.internal.kit.list
import com.github.ixtf.persistence.mongo.internal.kit.single
import com.github.ixtf.persistence.mongo.internal.kit.singleOrNull
import com.google.common.collect.Maps
import com.mongodb.client.model.Filters.*
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import java.security.Principal
import kotlinx.coroutines.flow.*
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class Jmongo(
  val client: MongoClient,
  val codecRegistry: CodecRegistry,
  config: MongoRuntimeModuleConfig,
) {
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

  val database = client.getDatabase(config.database)
  private val metadataCache = Maps.newConcurrentMap<Class<out MongoEntity>, MongoEntityMetadata>()

  fun <T : MongoEntity> metadata(clazz: Class<T>): MongoEntityMetadata =
    metadataCache.computeIfAbsent(clazz) {
      MongoEntityMetadata(this, it)
    }

  fun <T : MongoEntity> database(clazz: Class<T>) = metadata(clazz).database

  fun <T : MongoEntity> collection(clazz: Class<T>) = metadata(clazz).collection

  inline fun <reified T : MongoEntity> collection(): MongoCollection<Document> =
    metadata(T::class.java).collection

  inline fun <reified T : MongoEntity> bsonCollection(): MongoCollection<BsonDocument> =
    metadata(T::class.java).bsonCollection

  inline fun <reified T : MongoEntity> entityCollection(): MongoCollection<T> =
    metadata(T::class.java).entityCollection as MongoCollection<T>

  fun asBson(o: Any): BsonDocument = codecRegistry.asBson(o)

  inline fun <reified T : Any> fromBson(bson: BsonDocument): T = codecRegistry.fromBson(bson)

  inline fun <reified T : Any> fromBson(bson: String): T = codecRegistry.fromBson(bson)

  fun uow() = MongoUnitOfWork(this)

  suspend inline fun uow(crossinline block: suspend MongoUnitOfWork.() -> Unit): Unit =
    uow().apply { block() }.commit()

  inline fun <reified T : MongoEntity> asRef(id: String): JmongoRef<T> =
    collection(T::class.java).namespace.run {
      JmongoRef(databaseName = databaseName, collectionName = collectionName, id = id)
    }

  suspend inline fun <reified T : MongoEntity> count(condition: Flow<Bson?>): Long =
    collection<T>().count(asFilter(condition))

  suspend inline fun <reified T : MongoEntity> count(vararg condition: Bson?): Long =
    count<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> count(condition: Iterable<Bson?>): Long =
    count<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> list(condition: Flow<Bson?>): FindFlow<T> =
    entityCollection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntity> list(vararg condition: Bson?): FindFlow<T> =
    list(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> list(condition: Iterable<Bson?>): FindFlow<T> =
    list(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> listDoc(condition: Flow<Bson?>) =
    collection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntity> listDoc(vararg condition: Bson?) =
    listDoc<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> listDoc(condition: Iterable<Bson?>) =
    listDoc<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> listBson(condition: Flow<Bson?>) =
    bsonCollection<T>().list(asFilter(condition))

  suspend inline fun <reified T : MongoEntity> listBson(vararg condition: Bson?) =
    listBson<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> listBson(condition: Iterable<Bson?>) =
    listBson<T>(condition.asFlow())

  suspend inline fun <reified T : MongoEntity> singleOrNull(id: String) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntity> single(id: String) = entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntity> singleOrNull(id: ObjectId) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntity> single(id: ObjectId) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntity> singleOrNull(id: EntityDTO) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntity> single(id: EntityDTO) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntity> singleOrNull(id: Principal) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntity> single(id: Principal) =
    entityCollection<T>().single(id)

  suspend inline fun <reified T : MongoEntity> singleOrNull(id: JmongoRef<*>) =
    entityCollection<T>().singleOrNull(id)

  suspend inline fun <reified T : MongoEntity> single(id: JmongoRef<*>) =
    entityCollection<T>().single(id)
}
