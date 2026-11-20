package com.github.ixtf.persistence.arcade.domain

import com.github.ixtf.core.J
import com.github.ixtf.persistence.arcade.ArcadeEntity

class Person(
  override var id: String = J.objectId(),
  override var deleted: Boolean = false,
  var name: String = "",
  var age: Int = 0,
) : ArcadeEntity()
