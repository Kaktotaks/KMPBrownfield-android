package com.example.tasks.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/** Access to the design tokens: `TasksTheme.colors.accent`. */
object TasksTheme {
    val colors: TasksColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTasksColors.current
}

/**
 * App theme. Dynamic color is OFF on purpose: the brand palette wins (DESIGN_HANDOFF §1).
 * Tokens live in [TasksColors]; the M3 `colorScheme` is mapped from them so stock M3 parts
 * (menus, dialogs, bottom sheet) pick up the same colors.
 */
@Composable
fun TasksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkTasksColors else LightTasksColors
    val colorScheme = remember(colors) { colors.toMaterial() }
    val reduceMotion = rememberReduceMotion()
    CompositionLocalProvider(
        LocalTasksColors provides colors,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTasksTypography,
            shapes = Shapes(
                extraSmall = TasksShapes.xs,
                small = TasksShapes.s,
                medium = TasksShapes.m,
                large = TasksShapes.l,
                extraLarge = TasksShapes.xl,
            ),
            content = content,
        )
    }
}

// Same named-argument mapping for both appearances (DESIGN_HANDOFF §1.1 "colorScheme mapping").
private fun TasksColors.toMaterial() =
    if (isDark) {
        darkColorScheme(
            primary = accent, onPrimary = onAccent,
            secondary = inverse, onSecondary = onInverse,
            background = bg, onBackground = textPrimary,
            surface = surface, onSurface = textPrimary,
            surfaceVariant = surface2, onSurfaceVariant = textSecondary,
            surfaceContainer = surface2, surfaceContainerLow = surface, surfaceContainerLowest = surface,
            surfaceContainerHigh = surface2, surfaceContainerHighest = surface2,
            outline = outline, outlineVariant = border,
            error = danger, errorContainer = dangerContainer, onErrorContainer = onDangerContainer,
            scrim = scrim,
        )
    } else {
        lightColorScheme(
            primary = accent, onPrimary = onAccent,
            secondary = inverse, onSecondary = onInverse,
            background = bg, onBackground = textPrimary,
            surface = surface, onSurface = textPrimary,
            surfaceVariant = surface2, onSurfaceVariant = textSecondary,
            surfaceContainer = surface2, surfaceContainerLow = surface, surfaceContainerLowest = surface,
            surfaceContainerHigh = surface2, surfaceContainerHighest = surface2,
            outline = outline, outlineVariant = border,
            error = danger, errorContainer = dangerContainer, onErrorContainer = onDangerContainer,
            scrim = scrim,
        )
    }
