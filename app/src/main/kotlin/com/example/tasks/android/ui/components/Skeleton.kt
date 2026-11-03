package com.example.tasks.android.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme

/** M7: opacity 0.45 <-> 1, 1000 ms per half cycle, ease-in-out, repeat + reverse. Static under reduced motion. */
@Composable
fun rememberSkeletonAlpha(): Float {
    if (LocalReduceMotion.current) return 1f
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(Motion.pulseHalfCycle, easing = Motion.easeInOut), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    return alpha
}

/** A `surface2` block that pulses (list rows, stats hero and tiles). */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = TasksShapes.pill) {
    val alpha = rememberSkeletonAlpha()
    Box(modifier.alpha(alpha).clip(shape).background(TasksTheme.colors.surface2))
}

/** Skeleton row (§2): TaskCard shape; circle 26, line 14 h (65-80%), line 10 h (40-50%). */
@Composable
fun SkeletonRow(titleFraction: Float, dueFraction: Float, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(TasksShapes.l)
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(Modifier.size(26.dp), CircleShape)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(Modifier.fillMaxWidth(titleFraction).height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(dueFraction).height(10.dp))
        }
    }
}

@Composable
fun SkeletonBlockBox(height: Dp, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier) {
    SkeletonBlock(modifier.height(height), shape)
}
