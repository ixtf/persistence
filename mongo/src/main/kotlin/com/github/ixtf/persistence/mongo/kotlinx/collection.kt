package com.github.ixtf.persistence.mongo.kotlinx

import cn.hutool.log.Log
import com.github.ixtf.persistence.Sort
import com.github.ixtf.persistence.SortStart
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idEq
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.conversions.Bson
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry

fun MongoCollection<*>.ensureIndex(bson: Bson, options: IndexOptions) {
  Mono.delay(Duration.ofSeconds(3))
    .subscribeOn(Schedulers.boundedElastic())
    .flatMap {
      mono(Dispatchers.IO) {
          listIndexes().firstOrNull { it.getString("name") == options.name }
            ?: createIndex(bson, options.background(true))
        }
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
    }
    .doOnError { Log.get().error(it) }
    .subscribe()
}

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

suspend fun MongoCollection<*>.exists(id: Any): Boolean = countDocuments(idEq(id)) > 0

suspend fun <T : Any> MongoCollection<T>.singleOrNull(id: Any): T? = find(idEq(id)).singleOrNull()

suspend fun <T : Any> MongoCollection<T>.single(id: Any): T = find(idEq(id)).single()

suspend fun MongoCollection<*>.count(condition: Bson? = null): Long =
  condition?.let { countDocuments(it) } ?: countDocuments()

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
