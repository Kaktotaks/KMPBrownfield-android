package com.example.tasks.android.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens, color (DESIGN_HANDOFF §1.1). Token names are identical on iOS (`Color.tk.<name>`).
 * Views read them through `TasksTheme.colors`; no hex literals outside this file.
 */
@Immutable
data class TasksColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val inverse: Color,
    val onInverse: Color,
    val accent: Color,
    val onAccent: Color,
    val onAccentMuted: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val priorityHigh: Color,
    val onPriorityHigh: Color,
    val priorityMedium: Color,
    val onPriorityMedium: Color,
    val priorityLow: Color,
    val onPriorityLow: Color,
    val heroBg: Color,
    val heroText: Color,
    val heroTextSecondary: Color,
    val ringTrack: Color,
    val scrim: Color,
    val isDark: Boolean,
)

val LightTasksColors = TasksColors(
    bg = Color(0xFFF5F5F0),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEBEBE4),
    border = Color(0xFFE2E2DA),
    outline = Color(0xFF8C8C85),
    textPrimary = Color(0xFF0A0A0A),
    textSecondary = Color(0xFF4F4F4A),
    textTertiary = Color(0xFF6E6E68),
    inverse = Color(0xFF0A0A0A),
    onInverse = Color(0xFFFFFFFF),
    accent = Color(0xFFE3F140),
    onAccent = Color(0xFF0A0A0A),
    onAccentMuted = Color(0xFF2E3200),
    danger = Color(0xFFDC3232),
    dangerContainer = Color(0xFFFDE8E6),
    onDangerContainer = Color(0xFFB42323),
    priorityHigh = Color(0xFFDC3232),
    onPriorityHigh = Color(0xFFFFFFFF),
    priorityMedium = Color(0xFFFFB020),
    onPriorityMedium = Color(0xFF0A0A0A),
    priorityLow = Color(0xFFE2E2DA),
    onPriorityLow = Color(0xFF3A3A36),
    heroBg = Color(0xFF0A0A0A),
    heroText = Color(0xFFFFFFFF),
    heroTextSecondary = Color(0xFFB3B3AD),
    ringTrack = Color(0xFF2A2A2A),
    scrim = Color(0x660A0A0A), // #0A0A0A @ 40%
    isDark = false,
)

val DarkTasksColors = TasksColors(
    bg = Color(0xFF000000),
    surface = Color(0xFF151515),
    surface2 = Color(0xFF232323),
    border = Color(0xFF2A2A2A),
    outline = Color(0xFF8A8A84),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB3B3AD),
    textTertiary = Color(0xFF8A8A84),
    inverse = Color(0xFFFFFFFF),
    onInverse = Color(0xFF0A0A0A),
    accent = Color(0xFFE3F140),
    onAccent = Color(0xFF0A0A0A),
    onAccentMuted = Color(0xFF2E3200),
    danger = Color(0xFFFF5A4E),
    dangerContainer = Color(0xFF3A1512),
    onDangerContainer = Color(0xFFFFB4AD),
    priorityHigh = Color(0xFFFF5A4E),
    onPriorityHigh = Color(0xFF000000),
    priorityMedium = Color(0xFFFFB020),
    onPriorityMedium = Color(0xFF0A0A0A),
    priorityLow = Color(0xFF3A3A36),
    onPriorityLow = Color(0xFFE5E5E0),
    heroBg = Color(0xFF151515),
    heroText = Color(0xFFFFFFFF),
    heroTextSecondary = Color(0xFFB3B3AD),
    ringTrack = Color(0xFF2A2A2A),
    scrim = Color(0x99000000), // #000000 @ 60%
    isDark = true,
)

val LocalTasksColors = staticCompositionLocalOf { LightTasksColors }
