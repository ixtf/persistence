package com.github.ixtf.persistence.mongo

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MongoEntity(val database: String = "", val collection: String = "")
