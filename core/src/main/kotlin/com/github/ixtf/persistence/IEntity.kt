package com.github.ixtf.persistence

import java.io.Serializable

interface IEntity : Serializable {
  val id: String
  val deleted: Boolean
}
