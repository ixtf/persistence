package com.github.ixtf.persistence.mongo

import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.IEntityLoggable
import com.github.ixtf.persistence.dto.DefaultOperator
import java.time.Instant
import org.bson.codecs.pojo.annotations.BsonId

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MongoEntityCollection(val database: String = "", val collection: String = "")

abstract class MongoEntity : IEntity {
  @BsonId override lateinit var id: String
  override var deleted = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as MongoEntity
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}

abstract class MongoEntityLoggable : MongoEntity(), IEntityLoggable {
  override var creator: DefaultOperator? = null
  override var createDateTime: Instant? = null
  override var modifier: DefaultOperator? = null
  override var modifyDateTime: Instant? = null
}
