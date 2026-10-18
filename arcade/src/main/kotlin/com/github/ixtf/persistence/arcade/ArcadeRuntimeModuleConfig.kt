package com.github.ixtf.persistence.arcade

import java.nio.file.Path

sealed interface ArcadeRuntimeModuleConfig {
  data class Local(val dir: Path) : ArcadeRuntimeModuleConfig
}
