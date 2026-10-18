package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.MongoEntityCollection
import org.bson.BsonDocument
import org.bson.Document

class MongoEntityMetadata(
  private val jmongo: Jmongo,
  private val clazz: Class<out MongoEntity>,
) {
  private val anno: MongoEntityCollection?
    get() = clazz.getAnnotation(MongoEntityCollection::class.java)

  val database by lazy {
    anno?.database?.takeIf { it.isNotBlank() }?.let { jmongo.client.getDatabase(it) }
      ?: jmongo.database
  }
  val collection by lazy {
    val collectionName = anno?.collection?.takeIf { it.isNotBlank() } ?: "T_${clazz.simpleName}"
    database.getCollection<Document>(collectionName)
  }
  val bsonCollection by lazy {
    collection.withDocumentClass<BsonDocument>()
  }
  val entityCollection by lazy {
    collection.withDocumentClass(clazz)
  }
}
