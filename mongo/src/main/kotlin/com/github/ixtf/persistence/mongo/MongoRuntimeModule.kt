package com.github.ixtf.persistence.mongo

import dagger.Module
import dagger.multibindings.Multibinds
import kotlin.reflect.KClass

@Module
abstract class MongoRuntimeModule {
  @Multibinds abstract fun batchPublishers(): Set<KClass<MongoEntity>>
}
