package com.github.ixtf.persistence

import com.github.ixtf.core.kit.date
import com.github.ixtf.core.kit.ldt
import java.time.Duration
import java.time.LocalDateTime
import java.util.Date

data class ProcessDuration(
  val startDateTime: Date = Date(),
  val endDateTime: Date? = null,
  val seconds: Long? = null,
) {
  fun onEnd(): ProcessDuration {
    val endLdt = LocalDateTime.now()
    val startLdt = startDateTime.ldt()
    val between = Duration.between(startLdt, endLdt)
    return copy(endDateTime = endLdt.date(), seconds = between.seconds)
  }
}
