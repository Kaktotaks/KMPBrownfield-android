package com.example.tasks.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Radius tokens (DESIGN_HANDOFF §1.3). Same names and values on iOS (`Radius`). */
object Radius {
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 20.dp
    val xl = 28.dp
    val pill = 999.dp
}

/** Shapes built from [Radius]. */
object TasksShapes {
    val xs = RoundedCornerShape(Radius.xs)
    val s = RoundedCornerShape(Radius.s)
    val m = RoundedCornerShape(Radius.m)
    val l = RoundedCornerShape(Radius.l)
    val xl = RoundedCornerShape(Radius.xl)
    val sheetTop = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl)
    val pill = RoundedCornerShape(Radius.pill)
}

/** Spacing scale 4, 8, 12, 16, 20, 24, 32, 48 (same names on iOS `Spacing`). */
object Spacing {
    val s4 = 4.dp
    val s8 = 8.dp
    val s12 = 12.dp
    val s16 = 16.dp
    val s20 = 20.dp
    val s24 = 24.dp
    val s32 = 32.dp
    val s48 = 48.dp

    /** Screen gutter. */
    val gutter = s16
    /** Gap between task cards. */
    val cardGap = s8
    /** Gap between sections. */
    val sectionGap = s12
    /** Minimum touch target (48 dp on Android, 44 pt on iOS). */
    val minTarget = 48.dp
}
