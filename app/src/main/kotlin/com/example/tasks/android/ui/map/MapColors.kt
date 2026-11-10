package com.example.tasks.android.ui.map

import androidx.compose.ui.graphics.Color
import com.example.tasks.android.ui.theme.TasksColors
import com.example.tasks.shared.domain.Priority

/**
 * Colours the Map components use on top of the shared tokens (`TasksColors`). Values are copied from the
 * `renderVals()` blocks of `the design-handoff/map component files`; everything else comes from `TasksTheme.colors`.
 */
internal object MapColors {
    /** 2 px ring around pins / order badges: white in light, black in dark. */
    fun ring(c: TasksColors) = if (c.isDark) Color(0xFF000000) else Color(0xFFFFFFFF)

    /** Order badge of a done stop (pin). */
    fun doneOrderBg(c: TasksColors) = if (c.isDark) Color(0xFF3A3A36) else Color(0xFF8C8C85)

    /** Lime halo of a selected pin (accent @ 55 %). */
    val halo = Color(0xFFE3F140).copy(alpha = 0.55f)

    /** Start marker ring + label border. */
    fun startRing(c: TasksColors) = if (c.isDark) Color(0xFF3A3A36) else Color(0xFFFFFFFF)
    fun startLabelBorder(c: TasksColors) = if (c.isDark) Color(0xFF3A3A36) else Color(0xFF0A0A0A)
    val startBg = Color(0xFF0A0A0A)

    /** Banner / dark pill surfaces (MovePinBanner is always dark). */
    val bannerBg = Color(0xFF0A0A0A)
    val bannerButton = Color(0xFF232323)
    fun bannerBorder(c: TasksColors) = if (c.isDark) Color(0xFF3A3A36) else Color(0xFF0A0A0A)

    fun sheetGrabber(@Suppress("UNUSED_PARAMETER") c: TasksColors) = Color(0xFF8C8C85) // Android drag handle, both themes

    fun sheetEdge(c: TasksColors) = c.border

    /** Route-row connector line (3a3a36 dark / e2e2da light). */
    fun connector(c: TasksColors) = if (c.isDark) Color(0xFF3A3A36) else Color(0xFFE2E2DA)

    fun shadowAlpha(c: TasksColors, light: Float) = if (c.isDark) minOf(light * 4.2f, 0.6f) else light
}

/** Tinted ("past") priority badge: container, text, border (RouteStopRow / TaskMapSheet `F` table; text ≥ 4.5:1). */
internal data class FadedBadge(val container: Color, val content: Color, val border: Color)

internal fun fadedBadge(priority: Priority, dark: Boolean): FadedBadge = when (priority) {
    Priority.HIGH ->
        if (dark) FadedBadge(Color(0xFF3A1512), Color(0xFFFFB4AD), Color(0xFF6B2A24)) else FadedBadge(Color(0xFFFDE8E6), Color(0xFFB42323), Color(0xFFF4C2BD))
    Priority.MEDIUM ->
        if (dark) FadedBadge(Color(0xFF33280F), Color(0xFFFFD58A), Color(0xFF5C4613)) else FadedBadge(Color(0xFFFFF1D6), Color(0xFF7A5200), Color(0xFFF2D9A6))
    Priority.LOW ->
        if (dark) FadedBadge(Color(0xFF1C1C1A), Color(0xFFB3B3AD), Color(0xFF3A3A36)) else FadedBadge(Color(0xFFF2F2EC), Color(0xFF4F4F4A), Color(0xFFE2E2DA))
}

/** Parses the `#RRGGBB` strings of shared's `MapStyle.routePalette`. */
internal fun parseHex(hex: String): Color = Color(("FF" + hex.removePrefix("#")).toLong(16))
