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
    val Mic: ImageVector by lazy {
        strokeIcon("mic", 2.2f,
            "M12 2 C10.34 2 9 3.34 9 5 L9 12 C9 13.66 10.34 15 12 15 C13.66 15 15 13.66 15 12 L15 5 C15 3.34 13.66 2 12 2 Z",
            "M19 10 L19 12 C19 15.87 15.87 19 12 19 C8.13 19 5 15.87 5 12 L5 10",
            "M12 19 L12 22")
    }
    val MicOff: ImageVector by lazy {
        strokeIcon("micOff", 2.2f,
            "M2 2 L22 22",
            "M18.89 13.23 C18.96 12.83 19 12.42 19 12 L19 10",
            "M5 10 L5 12 C5 15.87 8.13 19 12 19 C13.62 19 15.11 18.45 16.3 17.53",
            "M15 9.34 L15 5 C15 3.34 13.66 2 12 2 C10.82 2 9.8 2.68 9.32 3.67",
            "M9 9 L9 12 C9 13.66 10.34 15 12 15 C12.8 15 13.52 14.69 14.06 14.18",
            "M12 19 L12 22")
    }
    val ExternalLink: ImageVector by lazy {
        strokeIcon("externalLink", 2.2f,
            "M18 13 L18 19 C18 20.1 17.1 21 16 21 L5 21 C3.9 21 3 20.1 3 19 L3 8 C3 6.9 3.9 6 5 6 L11 6",
            "M15 3 L21 3 L21 9",
            "M10 14 L21 3")
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
    val Pause: ImageVector by lazy { filledIcon("pause", "M7 5 L10 5 L10 19 L7 19 Z", "M14 5 L17 5 L17 19 L14 19 Z") }

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
