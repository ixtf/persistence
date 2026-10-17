package com.github.ixtf.persistence

import com.google.common.collect.Lists
import java.util.Collections

abstract class AbstractUnitOfWork : UnitOfWork {
  protected val saveList: MutableList<IEntity> =
    Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val newList: MutableList<IEntity> =
    Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val dirtyList: MutableList<IEntity> =
    Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val deleteList: MutableList<IEntity> =
    Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val purgeList: MutableList<IEntity> =
    Collections.synchronizedList(Lists.newArrayList<IEntity>())

  @Synchronized
  override fun registerSave(o: IEntity) {
    o.takeUnless { saveList.contains(o) }?.let { saveList.add(it) }
  }

  @Synchronized
  override fun registerNew(o: IEntity) {
    o.takeUnless { newList.contains(o) }?.let { newList.add(it) }
  }

  @Synchronized
  override fun registerDirty(o: IEntity) {
    o.takeUnless { dirtyList.contains(o) }?.let { dirtyList.add(it) }
  }

  @Synchronized
  override fun registerDelete(o: IEntity) {
    o.takeUnless { deleteList.contains(o) }?.let { deleteList.add(it) }
  }

  @Synchronized
  override fun registerPurge(o: IEntity) {
    o.takeUnless { purgeList.contains(o) }?.let { purgeList.add(it) }
  }
}
