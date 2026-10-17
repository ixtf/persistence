package com.github.ixtf.persistence.mongo

import com.github.ixtf.persistence.IEntity
import org.bson.codecs.pojo.annotations.BsonId

abstract class MongoEntity : IEntity {
  @BsonId
  override lateinit var id: String
  override var deleted = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as MongoEntity
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
