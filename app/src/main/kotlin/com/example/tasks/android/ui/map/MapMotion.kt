package com.example.tasks.android.ui.map

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.shared.motion.MotionSpec

/** Map motion (MAP_HANDOFF §6). The easing comes from shared `MotionSpec.emphasized`, the same curve as everywhere else. */
internal object MapMotion {
    val emphasized = CubicBezierEasing(
        MotionSpec.emphasized.x1,
        MotionSpec.emphasized.y1,
        MotionSpec.emphasized.x2,
        MotionSpec.emphasized.y2,
    )

    const val LONG_PRESS_MS = 500
    const val PIN_DROP_MS = 250
    const val PULSE_MS = 1200
    const val ROUTE_DRAW_MS = 600
    const val CAMERA_FIT_MS = 400
    const val DAY_FADE_MS = 200
    const val DAY_FADE_REDUCED_MS = 150
    const val TAP_PIN_MS = 200
    const val SHEET_SLIDE_MS = 300
    const val MOVE_LIFT_MS = 150

    /** Tween with the emphasized curve, or an instant jump under reduced motion. */
    @Composable
    fun <T> spec(durationMillis: Int): FiniteAnimationSpec<T> =
        if (LocalReduceMotion.current) snap() else tween(durationMillis, easing = emphasized)
}
