package com.example.tasks.android.ui.splash

import android.app.Activity
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.motion.MotionSpec
import com.example.tasks.shared.motion.SplashFrame
import kotlin.math.hypot

// Splash-only colours (design-handoff/splash §3): fixed in light and dark, not theme tokens.
private val WordmarkColor = Color(0xFFFFFFFF)
private val CaptionColor = Color(0xFFB3B3AD) // = textSecondary (dark)
private const val HOLE_MARGIN_DP = 2f // "+ 2" past the farthest corner distance
private const val MS_PER_NANO_DIV = 1_000_000L

/**
 * Animated splash drawn above the root content (design-handoff/splash §3).
 * All timing/geometry comes from [MotionSpec.Splash]; this only measures time and draws the [SplashFrame].
 *
 * @param listSettled true once the task list state left Loading (Loaded or Error).
 * @param frozenAtMs debug hook: render this fixed elapsed time, no clock, never finishes.
 * @param onFinished called once when the overlay is done and should be removed.
 */
@Composable
fun SplashOverlay(
    listSettled: Boolean,
    reduceMotion: Boolean,
    frozenAtMs: Long?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = MotionSpec.Splash
    var elapsedMs by remember { mutableLongStateOf(frozenAtMs ?: 0L) }
    var readyAtMs by remember { mutableStateOf<Long?>(null) }
    var exitStart by remember {
        mutableStateOf(
            // Frozen: a time past the minimum hold means the exit began at the minimum hold.
            if (frozenAtMs != null && frozenAtMs >= spec.MIN_HOLD_MS) spec.MIN_HOLD_MS else null,
        )
    }
    val settled by rememberUpdatedState(listSettled)
    val finish by rememberUpdatedState(onFinished)

    if (frozenAtMs == null) {
        LaunchedEffect(Unit) {
            var startNanos = -1L
            while (true) {
                withFrameNanos { now ->
                    if (startNanos < 0) startNanos = now
                    val t = (now - startNanos) / MS_PER_NANO_DIV
                    elapsedMs = t
                    if (readyAtMs == null && settled) readyAtMs = t
                    if (exitStart == null) exitStart = spec.exitStartMs(readyAtMs, t)
                }
                if (spec.frameAt(elapsedMs, exitStart, reduceMotion).finished) {
                    finish()
                    return@LaunchedEffect
                }
            }
        }
    }

    val frame = spec.frameAt(elapsedMs, exitStart, reduceMotion)

    LightSystemBarIcons()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Swallow every touch while visible.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .clearAndSetSemantics {},
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val blockTopDp = (maxHeight.value - spec.BLOCK_HEIGHT) / 2f
        val markLeftDp = (maxWidth.value - spec.MARK_WIDTH) / 2f
        val (discX, discY) = spec.discCenterInMark()
        // Disc centre in screen coordinates (px).
        val centre = with(density) { Offset((markLeftDp + discX).dp.toPx(), (blockTopDp + discY).dp.toPx()) }
        val holeMaxRadius = hypot(
            maxOf(centre.x, widthPx - centre.x),
            maxOf(centre.y, heightPx - centre.y),
        ) + with(density) { HOLE_MARGIN_DP.dp.toPx() }
        val holeRadius = frame.revealFraction * holeMaxRadius

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = frame.overlayAlpha
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    if (holeRadius > 0f) {
                        drawCircle(Color.Black, radius = holeRadius, center = centre, blendMode = BlendMode.Clear)
                    }
                }
                .background(colorResource(R.color.splash_bg)),
        ) {
            SplashContent(frame, centre, widthPx, heightPx, blockTopDp, markLeftDp)
        }
    }
}

@Composable
private fun SplashContent(
    frame: SplashFrame,
    centre: Offset,
    widthPx: Float,
    heightPx: Float,
    blockTopDp: Float,
    markLeftDp: Float,
) {
    val spec = MotionSpec.Splash
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = frame.contentAlpha
                scaleX = frame.contentScale
                scaleY = frame.contentScale
                transformOrigin = TransformOrigin(centre.x / widthPx, centre.y / heightPx)
            },
    ) {
        Canvas(
            Modifier
                .offset(x = markLeftDp.dp, y = blockTopDp.dp)
                .size(spec.MARK_WIDTH.dp, spec.MARK_HEIGHT.dp),
        ) { drawSplashMark(frame) }

        Text(
            text = stringResource(R.string.splash_wordmark),
            style = TasksType.largeTitle,
            color = WordmarkColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .offset(y = (blockTopDp + spec.WORDMARK_TOP + frame.wordmarkOffsetY).dp)
                .graphicsLayer { alpha = frame.wordmarkAlpha },
        )
        Text(
            text = stringResource(R.string.splash_caption),
            style = TasksType.subhead15,
            color = CaptionColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .offset(y = (blockTopDp + spec.CAPTION_TOP + frame.captionOffsetY).dp)
                .graphicsLayer { alpha = frame.captionAlpha },
        )
    }
}

/** Light system-bar icons while the overlay is up; the previous appearance is restored when it leaves. */
@Composable
private fun LightSystemBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(window, view)
            val previousStatus = controller.isAppearanceLightStatusBars
            val previousNav = controller.isAppearanceLightNavigationBars
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            onDispose {
                controller.isAppearanceLightStatusBars = previousStatus
                controller.isAppearanceLightNavigationBars = previousNav
                // Let TalkBack land on the list that is revealed.
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
            }
        }
    }
}
