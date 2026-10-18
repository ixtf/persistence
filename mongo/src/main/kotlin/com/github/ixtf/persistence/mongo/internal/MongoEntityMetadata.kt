package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.MongoEntityCollection
import com.google.common.collect.Maps
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.BsonDocument
import org.bson.Document

class MongoEntityMetadata internal constructor(private val jmongo: Jmongo) {
  private val cache = Maps.newConcurrentMap<Class<*>, Metadata>()

  private fun metadata(clazz: Class<*>) = cache.computeIfAbsent(clazz) { Metadata(it) }

  private inner class Metadata(private val clazz: Class<*>) {
    private val anno: MongoEntityCollection?
      get() = clazz.getAnnotation(MongoEntityCollection::class.java)

    private val collectionName
      get() = anno?.collection?.takeIf { it.isNotBlank() } ?: "T_${clazz.simpleName}"

    private val database
      get() =
        anno?.database?.takeIf { it.isNotBlank() }?.let { jmongo.client.getDatabase(it) }
          ?: jmongo.database

    val collection by lazy { database.getCollection<Document>(collectionName) }
    val bsonCollection by lazy { collection.withDocumentClass<BsonDocument>() }
    val entityCollection by lazy { collection.withDocumentClass(clazz) }
  }

  fun <T : MongoEntity> collection(clazz: Class<T>) = metadata(clazz).collection

  fun <T : MongoEntity> bsonCollection(clazz: Class<T>) = metadata(clazz).bsonCollection

  fun <T : MongoEntity> entityCollection(clazz: Class<T>) =
    metadata(clazz).entityCollection as MongoCollection<T>
}
