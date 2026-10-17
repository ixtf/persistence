package com.github.ixtf.persistence

import jakarta.validation.constraints.NotBlank
import java.io.Serializable

@JvmRecord data class EntityDTO(@NotBlank val id: String) : Serializable
