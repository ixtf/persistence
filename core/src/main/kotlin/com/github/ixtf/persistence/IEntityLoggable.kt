package com.github.ixtf.persistence

import java.util.Date

interface IEntityLoggable : IEntity {
  var creator: DefaultOperator?
  var createDateTime: Date?
  var modifier: DefaultOperator?
  var modifyDateTime: Date?

  fun log(o: IOperator, date: Date = Date()) {
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
