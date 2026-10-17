package com.github.ixtf.persistence.mongo

import com.mongodb.MongoClientSettings

interface MongoClientHook {
  fun build(builder: MongoClientSettings.Builder)
}
