package com.github.ixtf.persistence

@JvmRecord
data class PageQueryResult<T>(
  val first: Int,
  val pageSize: Int,
  val count: Long,
  val data: Collection<T>,
)
