package com.github.ixtf.persistence

import java.io.Serializable

interface IOperator : Serializable {
  val id: String
  val name: String
}
