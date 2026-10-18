package com.github.ixtf.persistence.arcade.internal

import java.nio.file.Path

sealed interface ArcadeRuntimeModuleConfig {
  data class Local(val dir: Path) : ArcadeRuntimeModuleConfig
}
