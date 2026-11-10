package com.example.tasks.android.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.Inter
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.domain.Priority

/** Pin geometry (components/TaskPin.dc.html): 56 x 76 hit box, anchor (28, 46). */
internal object PinGeometry {
    val width = 56.dp
    val height = 76.dp
    const val ANCHOR_X = 28f
    const val ANCHOR_Y = 46f
}

/**
 * Outline of the whole pin body (56 x 50 dp box): the 40 dp ring circle at (8, 2) united with the tail ring, the 10 dp tail
 * square grown by 2 dp on every side (14 dp, corner 3 dp) and rotated 45 degrees about (28, 38).
 */
private fun pinOutlinePath(d: Float): Path {
        val circle = Path().apply { addOval(Rect(8f * d, 2f * d, 48f * d, 42f * d)) }
        val tail = Path().apply { addRoundRect(RoundRect(Rect(-7f * d, -7f * d, 7f * d, 7f * d), CornerRadius(3f * d))) }
        tail.transform(Matrix().apply { rotateZ(45f) })
        tail.translate(Offset(28f * d, 38f * d))
        return Path.combine(PathOperation.Union, circle, tail)
}

private val pinSmallText = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, fontFeatureSettings = "tnum")
private val pinOrderText = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, fontFeatureSettings = "tnum")

/**
 * TaskPin: a real accessible view (role button, label "09:30, Title, Low priority, done, stop 1"), 56 x 76 dp.
 * Selected = scale 1.25 from the anchor + a lime halo drawn above the faded body; lifted = translateY -14 + scale 1.25
 * + a shadow. The overdue ring and the halo live in their own full-opacity layer above the faded body (§5).
 */
@Composable
internal fun TaskPinView(
    pin: PinModel,
    description: String,
    showTime: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = TasksTheme.colors
    val ring = MapColors.ring(colors)
    val reduce = LocalReduceMotion.current
    val sel = pin.selected
    val scale by animateFloatAsState(
        targetValue = if (pin.lifted || sel) 1.25f else 1f,
        animationSpec = if (reduce) tween(0) else tween(MapMotion.MOVE_LIFT_MS, easing = MapMotion.emphasized),
        label = "pinScale",
    )
    val lift by animateFloatAsState(
        targetValue = if (pin.lifted) -14f else 0f,
        animationSpec = if (reduce) tween(0) else tween(MapMotion.MOVE_LIFT_MS, easing = MapMotion.emphasized),
        label = "pinLift",
    )
    val priority = pin.priority
    val bg = if (pin.done) colors.surface2 else when (priority) {
        Priority.HIGH -> colors.priorityHigh
        Priority.MEDIUM -> colors.priorityMedium
        Priority.LOW -> colors.priorityLow
    }
    val fg = if (pin.done) colors.textTertiary else when (priority) {
        Priority.HIGH -> colors.onPriorityHigh
        Priority.MEDIUM -> colors.onPriorityMedium
        Priority.LOW -> colors.onPriorityLow
    }
    val clickable = if (onClick != null) Modifier.clickable(onClickLabel = null, role = Role.Button, onClick = onClick) else Modifier
    Box(
        modifier
            .size(PinGeometry.width, PinGeometry.height)
            .then(clickable)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            },
    ) {
        if (pin.lifted) {
            Box(Modifier.offset(18.dp, 44.dp).size(20.dp, 8.dp).background(Color.Black.copy(alpha = 0.28f), CircleShape))
        }
        // Head layer: 56 x 50, scaled / lifted around the tip of the tail (28, 48).
        Box(
            Modifier
                .size(56.dp, 50.dp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(28f / 56f, 48f / 50f)
                    scaleX = scale
                    scaleY = scale
                    translationY = lift.dp.toPx()
                },
        ) {
            // Status ring + selection halo: always full opacity, above the faded body.
            if (pin.overdue || sel) {
                val danger = colors.danger
                Canvas(Modifier.size(56.dp, 50.dp)) {
                    val c = Offset(28.dp.toPx(), 22.dp.toPx())
                    if (sel) drawCircle(MapColors.halo, radius = (if (pin.overdue) 30 else 28).dp.toPx(), center = c)
                    if (pin.overdue) {
                        drawCircle(danger, radius = 23.dp.toPx(), center = c)
                        drawCircle(ring, radius = 20.dp.toPx(), center = c)
                    }
                }
            }
            Box(Modifier.size(56.dp, 50.dp).alpha(pin.bodyOpacity)) {
                // One teardrop: the 2 dp ring and the drop shadow follow the union of the head and the tail, drawn
                // first; the coloured tail and head sit on top, so the tail reads as part of the bubble.
                // The shadow is painted with a blur layer instead of an elevation shadow: an elevation shadow of a
                // non-convex outline inside a faded (alpha) layer draws a grey rectangle at the box corner.
                Canvas(Modifier.size(56.dp, 50.dp)) {
                    val path = pinOutlinePath(density)
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = ring
                            asFrameworkPaint().setShadowLayer(
                                (if (pin.lifted) 9.dp else 3.dp).toPx(),
                                0f,
                                (if (pin.lifted) 10.dp else 2.dp).toPx(),
                                Color.Black.copy(alpha = if (pin.lifted) 0.30f else 0.22f).toArgb(),
                            )
                        }
                        canvas.drawPath(path, paint)
                    }
                }
                Canvas(Modifier.size(56.dp, 50.dp)) {
                    val s = 10.dp.toPx()
                    val tl = Offset(23.dp.toPx(), 33.dp.toPx())
                    rotate(45f, Offset(28.dp.toPx(), 38.dp.toPx())) {
                        drawRoundRect(bg, tl, Size(s, s), CornerRadius(2.dp.toPx()))
                    }
                }
                // Head: 36 dp disc.
                Box(
                    Modifier.offset(10.dp, 4.dp).size(36.dp).background(bg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            pin.done -> MapIcons.PinCheck
                            priority == Priority.HIGH -> MapIcons.Flame
                            priority == Priority.MEDIUM -> MapIcons.Star
                            else -> MapIcons.Leaf
                        },
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (pin.order != null) {
                    val orderBg = if (pin.done) MapColors.doneOrderBg(colors) else colors.textPrimary
                    val orderFg = if (pin.done) Color.White else colors.bg
                    Box(
                        Modifier
                            .offset(36.dp, 0.dp)
                            .height(18.dp)
                            .defaultMinSize(minWidth = 18.dp)
                            .background(ring, CircleShape)
                            .padding(2.dp)
                            .background(orderBg, CircleShape)
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(pin.order.toString(), style = pinOrderText, color = orderFg)
                    }
                }
            }
        }
        if (showTime && pin.timeText != null) {
            val danger = colors.danger
            val timeColor = when {
                pin.overdue -> danger
                pin.done -> colors.textTertiary
                else -> colors.textPrimary
            }
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 54.dp)
                    .alpha(pin.timeOpacity)
                    .height(18.dp)
                    .background(if (pin.overdue) danger else colors.border, CircleShape)
                    .padding(1.dp)
                    .background(colors.surface, CircleShape)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(pin.timeText, style = pinSmallText, color = timeColor, maxLines = 1)
            }
        }
    }
}

/** StartMarker (components/StartMarker.dc.html): 40 dp black disc with a lime home icon, ring, and the "Start · …" label. */
@Composable
internal fun StartMarkerView(modifier: Modifier = Modifier, showLabel: Boolean = true) {
    val colors = TasksTheme.colors
    val description = stringResource(R.string.map_a11y_start, stringResource(R.string.map_start_address_long))
    Box(modifier.size(40.dp).semantics(mergeDescendants = true) { contentDescription = description; role = Role.Image }) {
        Box(
            Modifier
                .size(46.dp)
                .offset((-3).dp, (-3).dp)
                .shadow(4.dp, CircleShape, clip = false)
                .background(MapColors.startRing(colors), CircleShape)
                .padding(3.dp)
                .background(MapColors.startBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(MapIcons.Home, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        if (showLabel) {
            Row(
                Modifier
                    .offset((-96).dp, 46.dp)
                    .wrapContentSize(unbounded = true, align = Alignment.TopStart)
                    .clearAndSetSemantics { }
                    .height(24.dp)
                    .background(MapColors.startLabelBorder(colors), CircleShape)
                    .padding(1.5.dp)
                    .background(MapColors.startBg, CircleShape)
                    .padding(horizontal = 8.5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
                Text(stringResource(R.string.map_start), style = style, color = colors.accent, maxLines = 1, softWrap = false)
                Text(" · " + stringResource(R.string.map_start_address), style = style, color = Color.White, maxLines = 1, softWrap = false)
            }
        }
    }
}

/** The temporary pin of a long-press: lime disc, two translucent halos and (unless reduced motion) a looping pulse ring. */
@Composable
internal fun DroppedPinView(description: String, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val reduce = LocalReduceMotion.current
    val ringColor = MapColors.ring(colors)
    // Pin drop: falls 16 px + fades in with a small overshoot, 250 ms.
    val drop = remember { Animatable(if (reduce) 0f else -16f) }
    val fade = remember { Animatable(if (reduce) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduce) {
            fade.animateTo(1f, tween(MapMotion.PIN_DROP_MS, easing = MapMotion.emphasized))
        }
    }
    LaunchedEffect(Unit) {
        if (!reduce) drop.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
    }
    val pulse = if (reduce) null else rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(MapMotion.PULSE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseValue",
    )
    Box(
        modifier
            .size(76.dp)
            .graphicsLayer { translationY = drop.value.dp.toPx(); alpha = fade.value }
            .semantics { contentDescription = description; role = Role.Image },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(76.dp).clearAndSetSemantics { }) {
            val c = center
            drawCircle(Color(0xFFE3F140).copy(alpha = 0.22f), radius = 38.dp.toPx(), center = c)
            drawCircle(Color(0xFFE3F140).copy(alpha = 0.45f), radius = 24.dp.toPx(), center = c)
            pulse?.let {
                val p = it.value
                drawCircle(
                    Color(0xFFE3F140).copy(alpha = (1f - p) * 0.8f),
                    radius = 11.dp.toPx() * (1f + 1.2f * p),
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                )
            }
        }
        Box(
            Modifier
                .size(26.dp)
                .background(ringColor, CircleShape)
                .padding(2.dp)
                .background(Color(0xFF0A0A0A), CircleShape)
                .padding(3.dp)
                .background(Color(0xFFE3F140), CircleShape),
        )
    }
}

/** Dashed ghost of the pin's original place during Move (components/MapScreen.dc.html `s.move`). */
@Composable
internal fun MoveGhostView(modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val fill = if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color(0xFF0A0A0A).copy(alpha = 0.04f)
    val stroke = colors.textSecondary
    Canvas(modifier.size(36.dp).clearAndSetSemantics { }) {
        drawCircle(fill)
        drawCircle(
            stroke,
            radius = size.minDimension / 2 - 1.dp.toPx(),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            ),
        )
    }
}
