package com.example.tasks.android.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion

/** Tween with the one `emphasized` easing; snaps to the final value under reduced motion (§4.3). */
@Composable
fun <T> emphasizedSpec(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
    if (LocalReduceMotion.current) snap() else tween(durationMillis, delayMillis, Motion.emphasized)

/** M5: scale 1 -> 0.96 while pressed, 100 ms ease-out. */
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(Motion.press, easing = Motion.easeOut),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** M6: fade + height expand for banners and field errors, 250 ms emphasized. */
object TasksVisibility {
    val enter: EnterTransition =
        fadeIn(tween(Motion.medium, easing = Motion.emphasized)) +
            expandVertically(tween(Motion.medium, easing = Motion.emphasized))
    val exit: ExitTransition =
        fadeOut(tween(Motion.medium, easing = Motion.emphasized)) +
            shrinkVertically(tween(Motion.medium, easing = Motion.emphasized))
}
