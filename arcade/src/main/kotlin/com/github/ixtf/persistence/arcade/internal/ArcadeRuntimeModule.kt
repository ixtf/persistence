package com.github.ixtf.persistence.arcade.internal

import com.arcadedb.database.Database
import com.arcadedb.database.DatabaseFactory
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton

@Module
abstract class ArcadeRuntimeModule {
  companion object {
    @Singleton
    @Provides
    fun db(config: ArcadeRuntimeModuleConfig): Database =
      when (config) {
        is ArcadeRuntimeModuleConfig.Local -> {
          val factory = DatabaseFactory(config.path)
          if (factory.exists()) factory.open() else factory.create()
        }
      }
  }
}
