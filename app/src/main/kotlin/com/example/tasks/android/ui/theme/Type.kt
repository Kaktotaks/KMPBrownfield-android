package com.example.tasks.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R

/** Inter 400/500/600/700 and Inter Tight 800, static TTFs (SIL OFL, see assets/OFL.txt). */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)
val InterTight = FontFamily(Font(R.font.inter_tight_extrabold, FontWeight.ExtraBold))

private const val TABULAR = "tnum"

/**
 * Type scale (DESIGN_HANDOFF §1.2): same token names, sizes, line heights and tracking as iOS `Font.tk`.
 * `overline` and `badge` are UPPERCASE: callers apply `.uppercase()` to the text.
 */
object TasksType {
    val largeTitle = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.03).em)
    val display = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-0.04).em, fontFeatureSettings = TABULAR)
    val statNumber = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.03).em, fontFeatureSettings = TABULAR)
    val heroTitle = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 33.sp, letterSpacing = (-0.03).em)
    val title = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.02).em)
    val sheetTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp)
    val headline = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp)
    val body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val button = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
    val subhead = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
    val caption = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
    val overline = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.06.em)
    val badge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.04.em)

    // Variants the handoff names inline (§1.2 usage, §2 components).
    /** `body` 500: Cancel in the editor header, unselected segments. */
    val bodyMedium = body.copy(fontWeight = FontWeight.Medium)
    /** `body` 600: info-row values. */
    val bodyStrong = body.copy(fontWeight = FontWeight.SemiBold)
    /** PriorityBar label ("body-15" 600). */
    val label15 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp)
    /** "N tasks" / "N of M tasks done" ("subhead-15"). */
    val subhead15 = subhead.copy(fontSize = 15.sp)
    /** SavePill: Inter 700 15. */
    val savePill = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp)
    /** Tabular figures for any changing number. */
    val tabular = TextStyle(fontFeatureSettings = TABULAR)
}

/** Material typography mapped to the same tokens so stock M3 parts (menus, dialogs) match. */
internal val MaterialTasksTypography = Typography(
    displayLarge = TasksType.display,
    headlineLarge = TasksType.largeTitle,
    headlineMedium = TasksType.heroTitle,
    headlineSmall = TasksType.title,
    titleLarge = TasksType.sheetTitle,
    titleMedium = TasksType.headline,
    titleSmall = TasksType.subhead,
    bodyLarge = TasksType.body,
    bodyMedium = TasksType.subhead,
    bodySmall = TasksType.caption,
    labelLarge = TasksType.button,
    labelMedium = TasksType.subhead,
    labelSmall = TasksType.badge,
)
