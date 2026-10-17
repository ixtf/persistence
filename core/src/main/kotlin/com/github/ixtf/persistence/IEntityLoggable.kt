package com.github.ixtf.persistence

import com.github.ixtf.persistence.dto.DefaultOperator
import java.time.Instant

interface IEntityLoggable : IEntity {
  var creator: DefaultOperator?
  var createDateTime: Instant?
  var modifier: DefaultOperator?
  var modifyDateTime: Instant?

  fun log(o: IOperator, date: Instant = Instant.now()) {
    modifier = o as? DefaultOperator ?: DefaultOperator(o)
    modifyDateTime = date
    if (creator == null || creator == modifier) creator = modifier
    if (createDateTime == null) createDateTime = date
  }

  fun log(o: IEntityLoggable) {
    creator = o.creator
    createDateTime = o.createDateTime
    modifier = o.modifier
    modifyDateTime = o.modifyDateTime
  }
}
