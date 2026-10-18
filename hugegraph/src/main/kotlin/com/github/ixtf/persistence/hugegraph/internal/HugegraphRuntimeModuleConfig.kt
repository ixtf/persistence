package com.github.ixtf.persistence.hugegraph.internal

import java.nio.file.Path

sealed interface HugegraphRuntimeModuleConfig {
  data class Local(val dir: Path) : HugegraphRuntimeModuleConfig
}
