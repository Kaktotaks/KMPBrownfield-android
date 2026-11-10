package com.example.tasks.android.ui.map

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class RouteSheetTapTest {
    @Test
    fun mapTapCollapsesOnlyAnExpandedSheetWithNothingElseActive() {
        assertTrue(collapsesOnMapTap(expanded = true, hasSelection = false, moving = false, hasDraft = false))
        assertFalse(collapsesOnMapTap(expanded = false, hasSelection = false, moving = false, hasDraft = false))
        assertFalse(collapsesOnMapTap(expanded = true, hasSelection = true, moving = false, hasDraft = false))
        assertFalse(collapsesOnMapTap(expanded = true, hasSelection = false, moving = true, hasDraft = false))
        assertFalse(collapsesOnMapTap(expanded = true, hasSelection = false, moving = false, hasDraft = true))
    }
}
