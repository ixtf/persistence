package com.github.ixtf.persistence.arcade.internal

sealed interface ArcadeRuntimeModuleConfig {
  data class Local(val path: String) : ArcadeRuntimeModuleConfig
}
