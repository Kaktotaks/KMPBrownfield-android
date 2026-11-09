package com.example.tasks.android.ui.splash

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.tasks.shared.motion.MotionSpec
import com.example.tasks.shared.motion.SplashFrame

// App-icon art in the 1024 icon space. These values are only documented in comments in MotionSpec.Splash
// (they are not constants there), so they live here, and only here.
private const val CARD_X = 196f
private const val CARD_Y = 334f
private const val CARD_SIZE = 440f
private const val CARD_CORNER = 124f
private const val CARD_STROKE = 40f
private const val DISC_RADIUS = 250f
private const val CHECK_STROKE = 66f
private val CARD_COLOR = Color(0xFFFFFFFF)
private val DISC_COLOR = Color(0xFFE3F140)
private val CHECK_COLOR = Color(0xFF0A0A0A)
private val CHECK_P0 = Offset(486f, 438f)
private val CHECK_P1 = Offset(574f, 526f)
private val CHECK_P2 = Offset(736f, 346f)

/** Card outline: clockwise from the top edge just after the top-left corner, like an SVG `rect`. */
private fun cardPath(): Path {
    val l = CARD_X
    val t = CARD_Y
    val r = CARD_X + CARD_SIZE
    val b = CARD_Y + CARD_SIZE
    val k = CARD_CORNER
    val d = 2 * k
    return Path().apply {
        moveTo(l + k, t)
        lineTo(r - k, t)
        arcTo(Rect(r - d, t, r, t + d), -90f, 90f, false)
        lineTo(r, b - k)
        arcTo(Rect(r - d, b - d, r, b), 0f, 90f, false)
        lineTo(l + k, b)
        arcTo(Rect(l, b - d, l + d, b), 90f, 90f, false)
        lineTo(l, t + k)
        arcTo(Rect(l, t, l + d, t + d), 180f, 90f, false)
        close()
    }
}

private fun checkPath(): Path = Path().apply {
    moveTo(CHECK_P0.x, CHECK_P0.y)
    lineTo(CHECK_P1.x, CHECK_P1.y)
    lineTo(CHECK_P2.x, CHECK_P2.y)
}

private val cardFull = cardPath()
private val checkFull = checkPath()

private fun DrawScope.drawTrimmed(full: Path, trim: Float, color: Color, stroke: Stroke) {
    // Fully hidden at trim 0 so round caps leave no dots.
    if (trim <= 0f) return
    val measure = PathMeasure().apply { setPath(full, false) }
    val dst = Path()
    measure.getSegment(0f, measure.length * trim.coerceAtMost(1f), dst, true)
    drawPath(dst, color, style = stroke)
}

/** Draws the mark in icon space, mapped from the viewBox to the current draw size (132x120 dp). */
internal fun DrawScope.drawSplashMark(frame: SplashFrame) {
    val s = MotionSpec.Splash
    scale(size.width / s.VIEWBOX_W, size.height / s.VIEWBOX_H, pivot = Offset.Zero) {
        translate(-s.VIEWBOX_X, -s.VIEWBOX_Y) {
            drawTrimmed(cardFull, frame.cardTrim, CARD_COLOR, Stroke(width = CARD_STROKE))
            val centre = Offset(s.DISC_CX, s.DISC_CY)
            scale(frame.discScale, pivot = centre) {
                drawCircle(DISC_COLOR, radius = DISC_RADIUS, center = centre)
            }
            drawTrimmed(
                checkFull, frame.checkTrim, CHECK_COLOR,
                Stroke(width = CHECK_STROKE, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
