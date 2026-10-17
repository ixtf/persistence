package com.github.ixtf.persistence

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.io.Serializable

@JvmRecord data class EntitiesDTO(@NotEmpty val ids: Set<@Valid @NotBlank String>) : Serializable
