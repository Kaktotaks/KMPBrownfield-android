package com.example.tasks.android.ui.map

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MapFormatTest {
    @Test
    fun hoursAndMinutes() {
        assertEquals(1 to 2, MapFormat.hoursAndMinutes(62))
        assertEquals(0 to 14, MapFormat.hoursAndMinutes(14))
        assertEquals(2 to 0, MapFormat.hoursAndMinutes(120))
    }

    @Test
    fun kilometersUseTheLocaleSeparator() {
        assertEquals("14.2", MapFormat.kilometers(14_200.0, Locale.US))
        assertEquals("14,2", MapFormat.kilometers(14_200.0, Locale.forLanguageTag("uk")))
        assertEquals("0.9", MapFormat.kilometers(949.0, Locale.US))
    }

    @Test
    fun timeIsTwentyFourHourPadded() {
        assertEquals("09:05", formatTime(LocalTime(9, 5)))
        assertEquals("18:00", formatTime(LocalTime(18, 0)))
    }
}
