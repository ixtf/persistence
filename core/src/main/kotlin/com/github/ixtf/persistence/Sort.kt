package com.github.ixtf.persistence

import java.io.Serializable

@JvmRecord data class Sort(val id: String, val start: SortStart) : Serializable
