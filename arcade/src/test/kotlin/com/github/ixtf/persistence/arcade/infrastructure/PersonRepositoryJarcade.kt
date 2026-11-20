package com.github.ixtf.persistence.arcade.infrastructure

import com.arcadedb.query.sql.executor.Result
import com.github.ixtf.persistence.arcade.Jarcade
import com.github.ixtf.persistence.arcade.Jarcade.Companion.DELETED_COL
import com.github.ixtf.persistence.arcade.Jarcade.Companion.ID_COL
import com.github.ixtf.persistence.arcade.domain.Person
import com.github.ixtf.persistence.arcade.domain.PersonRepository
import com.github.ixtf.persistence.arcade.internal.CurdRepositoryJarcadeGraph
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PersonRepositoryJarcade @Inject constructor(jarcade: Jarcade) :
  CurdRepositoryJarcadeGraph<Person>(jarcade), PersonRepository {

  override fun Result.toEntity(): Person =
    Person(
      id = getProperty(ID_COL),
      deleted = getProperty(DELETED_COL) ?: false,
      name = getProperty(Person::name.name),
      age = getProperty(Person::age.name),
    )

  override fun Person.toProperties(): Map<String, Any?> =
    mapOf(ID_COL to id, DELETED_COL to deleted, Person::name.name to name, Person::age.name to age)
}
