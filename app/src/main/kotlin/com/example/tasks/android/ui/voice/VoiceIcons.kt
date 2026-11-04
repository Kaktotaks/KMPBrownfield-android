package com.example.tasks.android.ui.voice

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.components.strokeIcon

/** Voice-note icons on the same 24 grid / stroke 2.2 / round caps as `TasksIcons` (tint with `Icon(tint = ...)`). */
object VoiceIcons {
    // Same drawings as iOS `VoiceIcon` (24 grid; see ios-app/.../UI/Voice/VoiceIcon.swift).
    val Mic: ImageVector by lazy { strokeIcon("mic", 2.2f, MIC_CAPSULE, MIC_CRADLE, MIC_STAND) }
    val MicOff: ImageVector by lazy { strokeIcon("micOff", 2.2f, MIC_CAPSULE, MIC_CRADLE, MIC_STAND, "M4 4 L20 20") }
    val ExternalLink: ImageVector by lazy {
        strokeIcon("externalLink", 2.2f,
            "M10 5 L7 5 C5.9 5 5 5.9 5 7 L5 17 C5 18.1 5.9 19 7 19 L17 19 C18.1 19 19 18.1 19 17 L19 14",
            "M14 4.5 L19.5 4.5 L19.5 10",
            "M19.5 4.5 L11 13")
    }
    val AlertTriangle: ImageVector by lazy {
        strokeIcon("alertTriangle", 2.2f,
            "M21.73 18 L13.73 4 C13.2 3.1 12.1 2.8 11.2 3.3 C10.9 3.5 10.6 3.7 10.45 4 L2.45 18 C1.9 18.9 2.2 20 3.1 20.55 C3.4 20.7 3.7 20.8 4 20.8 L20 20.8 C21.05 20.8 21.9 19.95 21.9 18.9 C21.9 18.6 21.85 18.3 21.73 18 Z",
            "M12 9 L12 13",
            "M12 17 L12.01 17")
    }
    val Close: ImageVector get() = TasksIcons.Close

    /** Filled triangle with rounded joins. */
    val Play: ImageVector by lazy { filledIcon("play", "M8 5.5 L18.5 12 L8 18.5 Z") }

    /** Two filled bars. */
    val Pause: ImageVector by lazy { filledIcon("pause", roundRect(7f, 5.5f, 3.5f, 13f, 1f), roundRect(13.5f, 5.5f, 3.5f, 13f, 1f)) }

    /** Filled rounded square. */
    val Stop: ImageVector by lazy { filledIcon("stop", roundRect(6.5f, 6.5f, 11f, 11f, 2f)) }

    private const val MIC_CAPSULE =
        "M9 5.5 C9 3.843 10.343 2.5 12 2.5 C13.657 2.5 15 3.843 15 5.5 L15 10.5 C15 12.157 13.657 13.5 12 13.5 C10.343 13.5 9 12.157 9 10.5 Z"
    private const val MIC_CRADLE = "M5.5 11 C5.5 14.6 8.4 17.5 12 17.5 C15.6 17.5 18.5 14.6 18.5 11"
    private const val MIC_STAND = "M12 17.5 L12 21"

    private fun roundRect(x: Float, y: Float, w: Float, h: Float, r: Float): String {
        val k = r * 0.4477f // 1 - 0.5523: control-point offset of a quarter circle
        fun f(v: Float) = "%.3f".format(java.util.Locale.ROOT, v)
        return "M ${f(x + r)} ${f(y)} L ${f(x + w - r)} ${f(y)} C ${f(x + w - k)} ${f(y)} ${f(x + w)} ${f(y + k)} ${f(x + w)} ${f(y + r)} " +
            "L ${f(x + w)} ${f(y + h - r)} C ${f(x + w)} ${f(y + h - k)} ${f(x + w - k)} ${f(y + h)} ${f(x + w - r)} ${f(y + h)} " +
            "L ${f(x + r)} ${f(y + h)} C ${f(x + k)} ${f(y + h)} ${f(x)} ${f(y + h - k)} ${f(x)} ${f(y + h - r)} " +
            "L ${f(x)} ${f(y + r)} C ${f(x)} ${f(y + k)} ${f(x + k)} ${f(y)} ${f(x + r)} ${f(y)} Z"
    }

    private fun filledIcon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                paths.forEach { d ->
                    addPath(
                        pathData = PathParser().parsePathString(d).toNodes(),
                        fill = SolidColor(Color.Black),
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    )
                }
            }.build()
}
