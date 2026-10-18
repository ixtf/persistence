package com.github.ixtf.persistence.mongo.internal.kit

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.internal.codec.JmongoRef

inline fun <reified T : MongoEntity> Jmongo.asRef(id: String): JmongoRef<T> =
  collection<T>().namespace.run {
    JmongoRef(databaseName = databaseName, collectionName = collectionName, id = id)
  }
