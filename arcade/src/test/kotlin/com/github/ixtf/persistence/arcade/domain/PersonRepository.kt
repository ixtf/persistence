package com.github.ixtf.persistence.arcade.domain

import kotlinx.coroutines.flow.Flow

interface PersonRepository {
  suspend fun findOrNull(id: String): Person?

  suspend fun find(id: String): Person

  fun listByIds(ids: Iterable<String>): Flow<Person>

  fun list(): Flow<Person>

  suspend fun save(entity: Person)
}
