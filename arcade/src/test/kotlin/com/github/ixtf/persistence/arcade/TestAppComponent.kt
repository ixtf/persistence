package com.github.ixtf.persistence.arcade

import com.github.ixtf.persistence.arcade.domain.PersonRepository
import com.github.ixtf.persistence.arcade.infrastructure.PersonRepositoryJarcade
import com.github.ixtf.persistence.arcade.internal.ArcadeRuntimeModule
import com.github.ixtf.persistence.arcade.internal.ArcadeRuntimeModuleConfig
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import jakarta.inject.Singleton

@Singleton
@Component(modules = [ArcadeRuntimeModule::class, TestModule::class])
interface TestAppComponent {
  val jarcade: Jarcade

  val personRepository: PersonRepository

  @Component.Factory
  interface Factory {
    fun create(
      @BindsInstance config: ArcadeRuntimeModuleConfig = ArcadeRuntimeModuleConfig.Local("db")
    ): TestAppComponent
  }
}

@Module
abstract class TestModule {
  @Binds
  abstract fun bindPersonRepository(impl: PersonRepositoryJarcade): PersonRepository
}