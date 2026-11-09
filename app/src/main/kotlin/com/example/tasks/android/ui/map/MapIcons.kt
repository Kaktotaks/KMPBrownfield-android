package com.example.tasks.android.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.strokeIcon

/** Icons of the Map screens: the path data is copied verbatim from `the design-handoff/map component files` (24 grid). */
internal object MapIcons {
    private fun fillIcon(name: String, d: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(pathData = PathParser().parsePathString(d).toNodes(), fill = SolidColor(Color.Black))
            .build()

    /** Tab icon + task-sheet address icon (circle of the pin drawn as an arc path). */
    val Pin: ImageVector by lazy {
        strokeIcon(
            "mapPin", 2.2f,
            "M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z",
            "M9.6 10a2.4 2.4 0 1 0 4.8 0a2.4 2.4 0 1 0 -4.8 0",
        )
    }
    val PinPlus: ImageVector by lazy {
        strokeIcon("mapPinPlus", 2.2f, "M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z", "M12 7.5v5M9.5 10h5")
    }
    val Home: ImageVector by lazy { strokeIcon("home", 2.4f, "M4 11l8-6.5 8 6.5M6.5 9.5V19h11V9.5M10 19v-5h4v5") }
    val HomeSmall: ImageVector by lazy { strokeIcon("homeSmall", 2.6f, "M4 11l8-6.5 8 6.5M6.5 9.5V19h11V9.5") }
    val Car: ImageVector by lazy { strokeIcon("car", 2.2f, "M5 16V11l2-5h10l2 5v5M5 16h14M5 16v2M19 16v2M5 11h14") }
    val Recenter: ImageVector by lazy {
        strokeIcon(
            "recenter", 2.2f,
            "M5.5 12a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0 -13 0",
            "M10.2 12a1.8 1.8 0 1 0 3.6 0a1.8 1.8 0 1 0 -3.6 0",
            "M12 2.5v3M12 18.5v3M2.5 12h3M18.5 12h3",
        )
    }
    val Move: ImageVector by lazy {
        strokeIcon(
            "move", 2.2f,
            "M12 3v18M3 12h18M12 3l-2.5 2.5M12 3l2.5 2.5M12 21l-2.5-2.5M12 21l2.5-2.5M3 12l2.5-2.5M3 12l2.5 2.5M21 12l-2.5-2.5M21 12l-2.5 2.5",
        )
    }
    val ChevronLeft: ImageVector by lazy { strokeIcon("chevronLeft", 2.4f, "M15 5l-7 7 7 7") }
    val ChevronRight: ImageVector by lazy { strokeIcon("chevronRight", 2.4f, "M9 5l7 7-7 7") }
    val Info: ImageVector by lazy {
        strokeIcon("info", 2.4f, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 7.5v5.5M12 16.5v.01")
    }
    val WifiOff: ImageVector by lazy {
        strokeIcon(
            "wifiOff", 2.3f,
            "M3 3l18 18M8.5 16.5a5 5 0 0 1 7 0M5 13a10 10 0 0 1 5-2.6M19 13a10 10 0 0 0-2.2-1.5M2 9.5a15 15 0 0 1 4.3-2.6M22 9.5A15 15 0 0 0 11 6M12 20h.01",
        )
    }
    val Flame: ImageVector by lazy {
        fillIcon("flame", "M12 2.5c1 3.6 5.5 5.6 5.5 10.6a5.5 5.5 0 0 1-11 0c0-2.4 1.3-4 2.6-5 .2 1.7 1.1 2.8 2.3 3.1-.6-3.1.1-6.1.6-8.7z")
    }
    val Star: ImageVector by lazy {
        fillIcon("star", "M12 3.2l2.7 5.5 6 .9-4.35 4.25 1.03 6-5.38-2.83-5.38 2.83 1.03-6L3.3 9.6l6-.9z")
    }
    val Leaf: ImageVector by lazy {
        strokeIcon("leaf", 2.4f, "M5 19.5C5 11 10 5.5 19.5 4.5 19.5 14 14 19 5.5 19M5 19.5l8.5-8.5")
    }
    val PinCheck: ImageVector by lazy { strokeIcon("pinCheck", 3f, "M5 12.5l4.5 4.5L19 7.5") }
    val Check: ImageVector by lazy { strokeIcon("check", 2.8f, "M5 12.5l4.5 4.5L19 7.5") }
    val ClockSmall: ImageVector by lazy { strokeIcon("clockSmall", 2.3f, "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0", "M12 7.5V12l3 2") }
}
