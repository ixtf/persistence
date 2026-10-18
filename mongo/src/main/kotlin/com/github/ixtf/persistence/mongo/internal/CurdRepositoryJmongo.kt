package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import java.lang.reflect.ParameterizedType

abstract class CurdRepositoryJmongo<T : MongoEntity>(protected val jmongo: Jmongo) {
  @Suppress("UNCHECKED_CAST")
  protected val entityClass by lazy {
    val parameterizedType = javaClass.genericSuperclass as ParameterizedType
    parameterizedType.actualTypeArguments[0] as Class<T>
  }
  protected val entityCollection by lazy { jmongo.metadata.entityCollection(entityClass) }
  protected val collection by lazy { jmongo.metadata.collection(entityClass) }
  protected val bsonCollection by lazy { jmongo.metadata.bsonCollection(entityClass) }
}
