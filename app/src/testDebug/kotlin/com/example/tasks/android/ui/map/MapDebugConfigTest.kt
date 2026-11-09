package com.example.tasks.android.ui.map

import com.example.tasks.shared.domain.GeoPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapDebugConfigTest {
    private fun parse(
        now: String? = null, day: String? = null, select: Int? = null, expanded: Boolean = false,
        move: Boolean = false, newHere: String? = null, offline: Boolean = false, osrm: String? = null,
    ) = MapDebugConfig.parse(now, day, select, expanded, move, newHere, offline, osrm)

    @Test
    fun noExtrasLeavesTheAppAlone() {
        val c = parse()
        assertFalse(c.opensMap)
        assertNull(c.frozenNow)
        assertNull(c.osrmBaseUrl)
    }

    @Test
    fun screenshotClockAndDay() {
        val c = parse(now = "2026-11-04T12:40", day = "2026-11-04")
        assertEquals(LocalDateTime(2026, 11, 4, 12, 40), c.frozenNow)
        assertEquals(LocalDate(2026, 11, 4), c.day)
        assertTrue(c.opensMap)
    }

    @Test
    fun garbageIsIgnored() {
        val c = parse(now = "yesterday", day = "soon", select = 0, newHere = "48.46")
        assertNull(c.frozenNow)
        assertNull(c.day)
        assertNull(c.selectOrder)
        assertNull(c.newHere)
        assertFalse(c.opensMap)
    }

    @Test
    fun invalidOsrmHostShortcut() {
        assertEquals("https://invalid.invalid", parse(osrm = "invalid").osrmBaseUrl)
        assertEquals("https://osrm.example.com", parse(osrm = " https://osrm.example.com ").osrmBaseUrl)
    }

    @Test
    fun newHerePoint() {
        assertEquals(GeoPoint(48.4647, 35.0462), parse(newHere = "48.4647, 35.0462").newHere)
    }
}
