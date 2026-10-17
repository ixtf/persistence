package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import org.bson.BsonDocument
import org.bson.Document

class MongoEntityMetadata(private val jmongo: Jmongo, private val clazz: Class<*>) {
  private val annoMongoEntity by lazy {
    if (clazz.isAnnotationPresent(MongoEntity::class.java))
      clazz.getAnnotation(MongoEntity::class.java)
    else null
  }
  val database by lazy {
    annoMongoEntity?.database?.takeIf { it.isNotBlank() }?.let { jmongo.client.getDatabase(it) }
      ?: jmongo.database
  }
  private val collectionName by lazy {
    annoMongoEntity?.collection?.takeIf { it.isNotBlank() } ?: "T_${clazz.simpleName}"
  }
  val collection by lazy { database.getCollection<Document>(collectionName) }
  val bsonCollection by lazy { database.getCollection<BsonDocument>(collectionName) }
  val entityCollection by lazy { database.getCollection(collectionName, clazz) }
}
