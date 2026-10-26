package com.github.ixtf.persistence.arcade

import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.IEntityLoggable
import com.github.ixtf.persistence.dto.DefaultOperator
import java.time.Instant

abstract class ArcadeEntity : IEntity {
  override lateinit var id: String
  override var deleted = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ArcadeEntity
    return id == other.id
  }

  override fun hashCode() = id.hashCode()
}

abstract class ArcadeEntityLoggable : ArcadeEntity(), IEntityLoggable {
  override var creator: DefaultOperator? = null
  override var createDateTime: Instant? = null
  override var modifier: DefaultOperator? = null
  override var modifyDateTime: Instant? = null
}
