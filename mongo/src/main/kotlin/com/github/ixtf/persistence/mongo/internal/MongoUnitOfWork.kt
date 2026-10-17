package com.github.ixtf.persistence.mongo.internal

import com.gitee.ixtf.mongo.kotlinx.exists
import com.github.ixtf.persistence.IEntity
import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.Jmongo.Companion.idEq
import com.github.ixtf.persistence.uof.AbstractUnitOfWork
import com.mongodb.kotlin.client.coroutine.MongoCollection

class MongoUnitOfWork(private val jmongo: Jmongo) : AbstractUnitOfWork() {
  @Suppress("UNCHECKED_CAST")
  private fun collection(entity: IEntity) =
    jmongo.entityCollection(entity::class.java) as MongoCollection<Any>

  override suspend fun commit() {
    saveList.forEach { entity ->
      if (collection(entity).exists(entity.id)) registerDirty(entity) else registerNew(entity)
    }
    deleteList.forEach { entity ->
      registerDirty(entity)
    }
    newList.forEach { entity ->
      val result = collection(entity).insertOne(entity)
      require(result.wasAcknowledged()) { "insertOne[$entity]" }
    }
    dirtyList.forEach { entity ->
      val result = collection(entity).replaceOne(idEq(entity), entity)
      require(result.wasAcknowledged()) { "replaceOne[$entity]" }
    }
    purgeList.forEach { entity ->
      val result = collection(entity).deleteOne(idEq(entity))
      require(result.wasAcknowledged()) { "purgeOne[$entity]" }
    }
  }

  override suspend fun rollback() {
    TODO("Not yet implemented")
  }
}
