package com.github.ixtf.persistence.mongo.internal.kit

import cn.hutool.core.util.RandomUtil
import cn.hutool.log.Log
import com.github.ixtf.persistence.Sort
import com.github.ixtf.persistence.SortStart
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idEq
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlin.time.Duration.Companion.microseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.conversions.Bson

fun MongoCollection<*>.ensureIndex(bson: Bson, options: IndexOptions) {
  mono {
      delay(RandomUtil.randomLong(3000).microseconds)
      listIndexes().firstOrNull { it.getString("name") == options.name }
        ?: createIndex(bson, options.background(true))
    }
    .retry(1)
    .doOnError { Log.get().error(it) }
    .subscribe()
}

suspend fun MongoCollection<*>.count(condition: Bson? = null): Long =
  condition?.let { countDocuments(it) } ?: countDocuments()

suspend fun MongoCollection<*>.exists(id: Any): Boolean = countDocuments(idEq(id)) > 0

suspend fun <T : Any> MongoCollection<T>.singleOrNull(id: Any): T? = find(idEq(id)).singleOrNull()

suspend fun <T : Any> MongoCollection<T>.single(id: Any): T = find(idEq(id)).single()

fun MongoCollection<*>.asBson(o: Any): BsonDocument =
  BsonDocument().also { bsonDocument ->
    BsonDocumentWriter(bsonDocument).use { writer ->
      val codec = codecRegistry.get(o.javaClass)
      codec.encode(writer, o, EncoderContext.builder().build())
    }
  }

inline fun <reified T : Any> MongoCollection<*>.fromBson(bson: BsonDocument): T =
  BsonDocumentReader(bson).use { reader ->
    val codec = codecRegistry.get(T::class.java)
    return codec.decode(reader, DecoderContext.builder().build())
  }

inline fun <reified T : Any> MongoCollection<*>.fromBson(bson: String): T =
  fromBson(BsonDocument.parse(bson))

suspend fun MongoCollection<*>.count(condition: Flow<Bson?>) = count(condition.asFilter())

suspend fun MongoCollection<*>.count(vararg condition: Bson?) = count(condition.asFlow())

suspend fun MongoCollection<*>.count(condition: Iterable<Bson?>) = count(condition.asFlow())

fun <T : Any> MongoCollection<T>.list(condition: Bson? = null): FindFlow<T> {
  val findFlow = condition?.let { find(it) } ?: find()
  // return findFlow.allowDiskUse(true).batchSize(DEFAULT_BATCH_SIZE)
  return findFlow.allowDiskUse(true)
}

suspend fun <T : Any> MongoCollection<T>.list(condition: Flow<Bson?>) = list(condition.asFilter())

suspend fun <T : Any> MongoCollection<T>.list(vararg condition: Bson?) = list(condition.asFlow())

suspend fun <T : Any> MongoCollection<T>.list(condition: Iterable<Bson?>) = list(condition.asFlow())

suspend fun <T : Any> FindFlow<T>.sort(sort: Flow<Sort>?): FindFlow<T> = sort(sort?.asSort())

suspend fun <T : Any> FindFlow<T>.sort(vararg sort: Sort) = sort(sort.asFlow())

suspend fun <T : Any> FindFlow<T>.sort(sort: Iterable<Sort>?) = sort(sort?.asFlow())

suspend fun Flow<Bson?>.asFilter(): Bson? =
  filterNotNull().toList().let {
    when (it.size) {
      0 -> null
      1 -> it.first()
      else -> and(it)
    }
  }

suspend fun Flow<Sort>.asSort(): Bson? =
  filterNotNull()
    .distinctUntilChangedBy { it.id }
    .toList()
    .map { if (it.start == SortStart.asc) Sorts.ascending(it.id) else Sorts.descending(it.id) }
    .let {
      when (it.size) {
        0 -> null
        1 -> it.first()
        else -> Sorts.orderBy(it)
      }
    }
