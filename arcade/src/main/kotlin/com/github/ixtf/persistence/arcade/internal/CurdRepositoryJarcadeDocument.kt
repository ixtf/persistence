package com.github.ixtf.persistence.arcade.internal

import com.arcadedb.database.Database
import com.arcadedb.database.MutableDocument
import com.arcadedb.schema.Type
import com.github.ixtf.persistence.arcade.ArcadeEntity
import com.github.ixtf.persistence.arcade.Jarcade
import com.github.ixtf.persistence.arcade.Jarcade.Companion.ID_COL

/** 文档（非图）仓库基类 */
abstract class CurdRepositoryJarcadeDocument<T : ArcadeEntity>(jarcade: Jarcade) :
  CurdRepositoryJarcade<T>(jarcade) {

  override fun Database.ensureSchema() {
    ensureIndex(
      schema.getOrCreateDocumentType(recordType),
      idIndexName,
      ID_COL,
      Type.STRING,
      unique = true,
    )
  }

  override fun Database.newMutable(): MutableDocument = newDocument(recordType)
}
