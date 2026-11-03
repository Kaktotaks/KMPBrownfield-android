package com.example.tasks.android.ui.components

import androidx.compose.ui.graphics.Color
import com.example.tasks.android.ui.theme.TasksColors
import com.example.tasks.shared.domain.Priority

/** Badge background for a priority (§1.1). */
fun TasksColors.priorityFill(priority: Priority): Color = when (priority) {
    Priority.HIGH -> priorityHigh
    Priority.MEDIUM -> priorityMedium
    Priority.LOW -> priorityLow
}

/** Text on [priorityFill]. */
fun TasksColors.onPriority(priority: Priority): Color = when (priority) {
    Priority.HIGH -> onPriorityHigh
    Priority.MEDIUM -> onPriorityMedium
    Priority.LOW -> onPriorityLow
}

/** Bar fill and segment dot: Low uses `outline`, not its pale badge color (§1.1). */
fun TasksColors.priorityBar(priority: Priority): Color = when (priority) {
    Priority.HIGH -> priorityHigh
    Priority.MEDIUM -> priorityMedium
    Priority.LOW -> outline
}
