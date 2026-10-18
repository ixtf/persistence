package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.MongoEntityBase
import org.bson.BsonDocument
import org.bson.Document

class MongoEntityMetadata(
  private val jmongo: Jmongo,
  private val clazz: Class<out MongoEntityBase>,
) {
  private val annoMongoEntity
    get() = clazz.getAnnotation(MongoEntity::class.java)

  private val collectionName
    get() = annoMongoEntity?.collection?.takeIf { it.isNotBlank() } ?: "T_${clazz.simpleName}"

  internal val database by lazy {
    annoMongoEntity?.database?.takeIf { it.isNotBlank() }?.let { jmongo.client.getDatabase(it) }
      ?: jmongo.database
  }
  val collection by lazy { database.getCollection<Document>(collectionName) }
  val bsonCollection by lazy { database.getCollection<BsonDocument>(collectionName) }
  val entityCollection by lazy { database.getCollection(collectionName, clazz) }
}
