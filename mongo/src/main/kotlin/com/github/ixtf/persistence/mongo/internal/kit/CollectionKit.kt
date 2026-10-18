package com.github.ixtf.persistence.mongo.internal.kit

import cn.hutool.core.util.RandomUtil
import cn.hutool.log.Log
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idEq
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.IndexOptions
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlin.time.Duration.Companion.microseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.bson.BsonDocument
import org.bson.conversions.Bson

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

suspend fun Flow<Bson?>.asFilter(): Bson? =
  filterNotNull().toList().let {
    when (it.size) {
      0 -> null
      1 -> it.first()
      else -> and(it)
    }
  }

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

fun MongoCollection<*>.asBson(o: Any): BsonDocument = codecRegistry.asBson(o)

inline fun <reified T : Any> MongoCollection<*>.fromBson(bson: BsonDocument): T =
  codecRegistry.fromBson(bson)

inline fun <reified T : Any> MongoCollection<*>.fromBson(bson: String): T =
  codecRegistry.fromBson(bson)
