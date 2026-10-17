package com.github.ixtf.persistence.mongo.internal

import com.gitee.ixtf.mongo.codec.IOperatorCodecProvider
import com.gitee.ixtf.mongo.codec.JmongoRefCodecProvider
import com.github.ixtf.persistence.mongo.MongoRuntimeModuleConfig
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@Module
object MongoBackwardModule {
  @Provides
  @Singleton
  fun backwardCodecRegistry(config: MongoRuntimeModuleConfig): CodecRegistry =
    CodecRegistries.fromProviders(JmongoRefCodecProvider(config), IOperatorCodecProvider)
}
