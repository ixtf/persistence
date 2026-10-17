package com.github.ixtf.persistence.mongo

import com.github.ixtf.persistence.IEntityLoggable
import com.github.ixtf.persistence.dto.DefaultOperator
import java.time.Instant

abstract class MongoEntityLoggable : MongoEntity(), IEntityLoggable {
  override var creator: DefaultOperator? = null
  override var createDateTime: Instant? = null
  override var modifier: DefaultOperator? = null
  override var modifyDateTime: Instant? = null
}
