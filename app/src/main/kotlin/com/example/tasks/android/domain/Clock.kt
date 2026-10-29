package com.example.tasks.android.domain

import java.time.LocalDate

// KMP-shared candidate. In KMP this becomes kotlinx-datetime (Clock.System.todayIn(TimeZone.currentSystemDefault())).
fun interface Clock {
    fun today(): LocalDate
}

/** Real clock using the device's default time zone. */
object SystemClock : Clock {
    override fun today(): LocalDate = LocalDate.now()
}
