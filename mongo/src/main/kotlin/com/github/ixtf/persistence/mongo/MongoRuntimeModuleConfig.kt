package com.github.ixtf.persistence.mongo

import com.mongodb.ConnectionString

data class MongoRuntimeModuleConfig(
  val connectionString: ConnectionString,
  val database: String,
)
