package com.example.tasks.android.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlin.math.roundToInt

/**
 * Stats animations S1-S5 (§4.1): a value that runs 0 -> [target] when first composed (the composable only exists
 * while the content is Loaded, so tab re-entry replays it), then animates current -> new on data changes with
 * no delay and no reset to 0. [delayMillis] applies to the first run only (S4 stagger). Snaps under reduced motion.
 */
@Composable
fun rememberTargetAnimation(target: Float, durationMillis: Int, delayMillis: Int = 0): Float {
    val animatable = remember { Animatable(0f) }
    val firstRun = remember { booleanArrayOf(true) }
    val enterSpec = emphasizedSpec<Float>(durationMillis, delayMillis)
    val changeSpec = emphasizedSpec<Float>(durationMillis, 0)
    LaunchedEffect(target) {
        val spec = if (firstRun[0]) enterSpec else changeSpec
        firstRun[0] = false
        animatable.animateTo(target, spec)
    }
    return animatable.value
}

/** Integer shown for an animating value; the final frame is the exact [targetInt], not a rounded float. */
fun animatedInt(value: Float, target: Float, targetInt: Int, scale: Float = 1f): Int =
    if (value == target) targetInt else (value * scale).roundToInt()
