package com.example.tasks.android.ui.components

import androidx.compose.ui.graphics.Color
import com.example.tasks.android.ui.theme.TaskColors
import com.example.tasks.shared.domain.Priority

/** Priority -> semantic color (iOS twin: gray / orange / red). Presentation-only, no Compose runtime needed. */
fun Priority.badgeColor(): Color = when (this) {
    Priority.LOW -> TaskColors.Low
    Priority.MEDIUM -> TaskColors.Medium
    Priority.HIGH -> TaskColors.High
}
