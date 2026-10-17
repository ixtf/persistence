package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idEq
import com.github.ixtf.persistence.uof.AbstractUnitOfWork
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection

class MongoUnitOfWork(private val jmongo: Jmongo) : AbstractUnitOfWork() {
  @Suppress("UNCHECKED_CAST")
  private fun collection(entity: IEntity) =
    jmongo.entityCollection(entity::class.java) as MongoCollection<Any>

  /** 所有写操作均幂等(upsert / 条件替换 / 按 id 删除),commit 中途失败后可整体安全重试; 列表仅在全部成功后清空。非事务模式,跨实体不做原子性保证。 */
  override suspend fun commit() {
    saveList.forEach { upsert(it) }
    newList.forEach { upsert(it) }
    dirtyList.forEach { upsert(it) }
    deleteList.forEach { replace(it) }
    purgeList.forEach { delete(it) }
    clear()
  }

  /** 非事务模式无法撤销已写入的数据,rollback 仅丢弃尚未 commit 的变更。 */
  override suspend fun rollback() {
    clear()
  }

  private suspend fun upsert(entity: IEntity) {
    val result = collection(entity).replaceOne(idEq(entity), entity, ReplaceOptions().upsert(true))
    require(result.wasAcknowledged()) { "upsert[$entity]" }
  }

  /** 软删除:仅当文档存在时替换,不创建新文档。 */
  private suspend fun replace(entity: IEntity) {
    val result = collection(entity).replaceOne(idEq(entity), entity)
    require(result.wasAcknowledged()) { "replaceOne[$entity]" }
  }

  private suspend fun delete(entity: IEntity) {
    val result = collection(entity).deleteOne(idEq(entity))
    require(result.wasAcknowledged()) { "purgeOne[$entity]" }
  }

  @Synchronized
  private fun clear() {
    saveList.clear()
    newList.clear()
    dirtyList.clear()
    deleteList.clear()
    purgeList.clear()
  }
}
