package com.github.ixtf.persistence.dto

import com.github.ixtf.persistence.IOperator
import jakarta.validation.constraints.NotBlank

data class DefaultOperator(@NotBlank override val id: String, @NotBlank override val name: String) :
  IOperator {
  constructor(o: IOperator) : this(id = o.id, name = o.name)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as DefaultOperator
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
