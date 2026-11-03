package com.example.tasks.android.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Motion tokens (DESIGN_HANDOFF §4). Durations in ms, one easing everywhere: `emphasized`.
 * Same values on iOS (`Motion`).
 */
object Motion {
    const val press = 100
    const val short = 150
    const val medium = 250
    const val long = 600
    const val extraLong = 900
    const val stagger = 80

    /** Skeleton pulse: one half-cycle (M7). */
    const val pulseHalfCycle = 1000

    /** cubic-bezier(0.2, 0, 0, 1). */
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easeOut = LinearOutSlowInEasing
    val easeInOut = FastOutSlowInEasing
}

/** True when the user turned animations off (`ANIMATOR_DURATION_SCALE == 0`). Read once in [TasksTheme]. */
val LocalReduceMotion = compositionLocalOf { false }

@Composable
internal fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}
