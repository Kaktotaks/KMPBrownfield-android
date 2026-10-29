package com.example.tasks.android.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Shared date format "d MMM yyyy" (en_US), e.g. "29 Oct 2026".
// In KMP this becomes a small expect/actual or a kotlinx-datetime format.
object DateFormatting {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)

    fun format(date: LocalDate): String = formatter.format(date)
}
