package com.github.ixtf.persistence.uof

import com.github.ixtf.persistence.IEntity

interface UnitOfWork {
  fun registerSave(o: IEntity)

  fun registerNew(o: IEntity)

  fun registerDirty(o: IEntity)

  fun registerDelete(o: IEntity)

  fun registerPurge(o: IEntity)

  suspend fun commit()

  suspend fun rollback()
}
