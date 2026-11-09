package com.example.tasks.android.ui.map

import java.util.Locale

/** Number formatting of the route sheet. Pure JVM: the surrounding words come from string resources. */
object MapFormat {
    /** Splits whole minutes into hours and minutes: 62 -> (1, 2). */
    fun hoursAndMinutes(totalMinutes: Int): Pair<Int, Int> = totalMinutes / 60 to totalMinutes % 60

    /** Distance in km with one decimal and the locale's separator: 14200.0 -> "14.2" (en), "14,2" (uk). */
    fun kilometers(meters: Double, locale: Locale): String = String.format(locale, "%.1f", meters / 1000.0)
}
