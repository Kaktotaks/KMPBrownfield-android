package com.example.tasks.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Semantic colors shared with the iOS twin (system gray / orange / red). */
object TaskColors {
    val Low = Color(0xFF8E8E93)
    val Medium = Color(0xFFFF9500)
    val High = Color(0xFFFF3B30)
    val Overdue = Color(0xFFFF3B30)
}

@Composable
fun TasksTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors, content = content)
}
