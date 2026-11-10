package com.example.tasks.android.ui.map

import com.example.tasks.shared.domain.GeoPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Debug-only launch extras of the Map tab, parsed once per MainActivity launch (pure Kotlin, unit-tested). They freeze
 * "now", pick the demo day / state and point OSRM at a dead host, so screenshots match `design-handoff/map/reference`:
 *
 *   adb shell pm clear com.example.tasks.android
 *   adb shell am start -n com.example.tasks.android/.MainActivity --ez disableSplash true --es mapFrozenNow 2026-11-04T12:40 --es mapDay 2026-11-04
 *
 * Extras (all optional; any of them opens the Map tab):
 * - `mapFrozenNow`  ISO local date-time the app clock is frozen at, e.g. `2026-11-04T12:40` (Wed 4 Nov 2026 12:40)
 * - `mapDay`        ISO date to show, e.g. `2026-11-04`
 * - `mapSelect`     route order of the stop to select (3 = "Dentist check-up", 2 = the overdue passport photos)
 * - `mapExpanded`   true: expand the route sheet
 * - `mapMove`       true: start "Move pin" for the selected stop (use with `mapSelect`)
 * - `mapNewHere`    `lat,lng`: long-press there ("New task here")
 * - `mapOffline`    true: show the "No connection" chip (and keep it)
 * - `mapOsrmHost`   base URL of the OSRM host; `invalid` = https://invalid.invalid (routing fails -> straight dashed lines)
 */
data class MapDebugConfig(
    val frozenNow: LocalDateTime? = null,
    val day: LocalDate? = null,
    val selectOrder: Int? = null,
    val expanded: Boolean = false,
    val move: Boolean = false,
    val newHere: GeoPoint? = null,
    val offline: Boolean = false,
    val osrmBaseUrl: String? = null,
) {
    /** Any hook set: the app starts on the Map tab. */
    val opensMap: Boolean
        get() = frozenNow != null || day != null || selectOrder != null || expanded || move || newHere != null || offline || osrmBaseUrl != null

    companion object {
        const val EXTRA_NOW = "mapFrozenNow"
        const val EXTRA_DAY = "mapDay"
        const val EXTRA_SELECT = "mapSelect"
        const val EXTRA_EXPANDED = "mapExpanded"
        const val EXTRA_MOVE = "mapMove"
        const val EXTRA_NEW_HERE = "mapNewHere"
        const val EXTRA_OFFLINE = "mapOffline"
        const val EXTRA_OSRM_HOST = "mapOsrmHost"
        const val INVALID_HOST = "https://invalid.invalid"

        fun parse(
            now: String?,
            day: String?,
            select: Int?,
            expanded: Boolean,
            move: Boolean,
            newHere: String?,
            offline: Boolean,
            osrmHost: String?,
        ) = MapDebugConfig(
            frozenNow = now?.let { runCatching { LocalDateTime.parse(it.trim()) }.getOrNull() },
            day = day?.let { runCatching { LocalDate.parse(it.trim()) }.getOrNull() },
            selectOrder = select?.takeIf { it > 0 },
            expanded = expanded,
            move = move,
            newHere = newHere?.split(',')?.map { it.trim().toDoubleOrNull() }?.takeIf { it.size == 2 && it.all { v -> v != null } }
                ?.let { GeoPoint(it[0]!!, it[1]!!) },
            offline = offline,
            osrmBaseUrl = osrmHost?.trim()?.takeIf { it.isNotEmpty() }?.let { if (it == "invalid") INVALID_HOST else it },
        )
    }
}
