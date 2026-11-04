package com.example.tasks.android.ui.voice

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksTheme
import kotlin.math.max

private val BarWidth = 3.dp
private val BarRadius = 2.dp
private val MinBar = 4.dp

/**
 * WaveformView, static mode (§ notes board): bars 3 wide, spread over the available width, r2. Played bars =
 * `textPrimary` (waveformPlayed), the rest `waveformTrack`; a playhead line sits at [progress]. Tapping or dragging
 * calls [onSeek] with 0..1. Decorative for accessibility: the player container carries the semantics.
 */
@Composable
fun WaveformView(
    bars: List<Float>,
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
) {
    val colors = TasksTheme.colors
    val seek by rememberUpdatedState(onSeek)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures { offset -> seek((offset.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    seek((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
    ) {
        val n = bars.size
        if (n == 0) return@Canvas
        val barW = BarWidth.toPx()
        val gap = if (n > 1) max(0f, (size.width - n * barW) / (n - 1)) else 0f
        val played = progress.coerceIn(0f, 1f) * n
        bars.forEachIndexed { i, value ->
            val h = max(MinBar.toPx(), value.coerceIn(0f, 1f) * size.height)
            drawRoundRect(
                color = if (i < played) colors.textPrimary else colors.waveformTrack,
                topLeft = Offset(i * (barW + gap), (size.height - h) / 2),
                size = Size(barW, h),
                cornerRadius = CornerRadius(BarRadius.toPx()),
            )
        }
        if (progress > 0f) {
            val x = (progress.coerceIn(0f, 1f) * size.width).coerceIn(1.dp.toPx(), size.width - 1.dp.toPx())
            drawLine(colors.textPrimary, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
        }
    }
}

/**
 * WaveformView, live mode: the last 32 input levels, newest on the right; older bars fade to 25 % opacity.
 * Shared updates [levels] every 50 ms (~20 fps); every bar eases to its new height in 80 ms.
 * Reduced motion: bars are static at mid height.
 */
@Composable
fun LiveWaveform(levels: List<Float>, modifier: Modifier = Modifier, height: Dp = 56.dp) {
    val reduceMotion = LocalReduceMotion.current
    val count = levels.size.coerceAtLeast(1)
    Row(
        modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        levels.forEachIndexed { index, level ->
            val target = if (reduceMotion) height / 2 else maxOf(MinBar, height * level.coerceIn(0f, 1f))
            val barHeight by animateDpAsState(target, tween(LIVE_BAR_SMOOTHING_MS, easing = Motion.emphasized), label = "liveBar")
            Box(
                Modifier
                    .width(BarWidth)
                    .height(barHeight)
                    .alpha(0.25f + 0.75f * (index + 1) / count)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(BarRadius))
                    .background(TasksTheme.colors.textPrimary),
            )
        }
    }
}

/** Per-bar smoothing of the live bars (notes board: "~20 fps, 80 ms smoothing"). */
const val LIVE_BAR_SMOOTHING_MS = 80
