package com.github.ixtf.persistence.mongo.internal

import com.github.ixtf.persistence.mongo.Jmongo
import com.github.ixtf.persistence.mongo.internal.codec.IOperatorCodecProvider
import com.github.ixtf.persistence.mongo.internal.codec.JmongoRefCodecProvider
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import jakarta.inject.Singleton
import java.util.Optional
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@Module
abstract class MongoRuntimeModule {
  @Multibinds abstract fun custom(): Set<CodecRegistry>

  @BindsOptionalOf abstract fun hook(): MongoClientHook

  companion object {
    @JvmStatic
    @Singleton
    @Provides
    fun jmongo(
      config: MongoRuntimeModuleConfig,
      custom: Set<CodecRegistry>,
      hook: Optional<MongoClientHook>,
    ): Jmongo =
      MongoClientSettings.builder().run {
        applyConnectionString(config.connectionString)
        serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
        val registries = buildList {
          add(backwardCodecRegistry(config))
          addAll(custom)
          add(MongoClientSettings.getDefaultCodecRegistry())
        }
        codecRegistry(CodecRegistries.fromRegistries(registries))
        retryWrites(true)
        retryReads(true)
        hook.ifPresent { it.build(this) }
        Jmongo(MongoClient.create(build()), config.database)
      }

    private fun backwardCodecRegistry(config: MongoRuntimeModuleConfig): CodecRegistry =
      CodecRegistries.fromProviders(JmongoRefCodecProvider(config), IOperatorCodecProvider)
  }
}

data class MongoRuntimeModuleConfig(
  val connectionString: ConnectionString,
  val database: String,
)

interface MongoClientHook {
  fun build(builder: MongoClientSettings.Builder)
}
