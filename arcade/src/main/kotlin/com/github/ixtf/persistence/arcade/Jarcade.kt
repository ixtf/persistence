package com.github.ixtf.persistence.arcade

import com.arcadedb.database.Database
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class Jarcade @Inject constructor(val db: Database) {

  companion object {
    const val ID_COL = "id"
    const val DELETED_COL = "deleted"
  }
}
