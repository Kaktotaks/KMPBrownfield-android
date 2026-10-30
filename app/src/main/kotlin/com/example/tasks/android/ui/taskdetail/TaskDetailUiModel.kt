package com.example.tasks.android.ui.taskdetail

import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.TaskDetailUiState

/** Everything [TaskDetailScreen] draws, already formatted. Plain Kotlin: no Compose, unit-tested on the JVM. */
sealed interface TaskDetailUiModel {
    /** The task is gone (e.g. deleted) — the screen shows the empty state. */
    data object NotFound : TaskDetailUiModel

    data class Loaded(
        val title: String,
        /** "Done" / "Active". */
        val statusText: String,
        val priority: Priority,
        /** "Due 29 Oct 2026". */
        val dueText: String,
        val isOverdue: Boolean,
        /** The notes, or "No notes" when empty ([hasNotes] == false selects the muted style). */
        val notesText: String,
        val hasNotes: Boolean,
        /** "Mark as active" / "Mark as done". */
        val toggleButtonText: String,
    ) : TaskDetailUiModel
}

/** Shared VM state -> UI model. Pure function (called from [TaskDetailScreenRoute] inside `remember(state)`). */
fun TaskDetailUiState.toUiModel(): TaskDetailUiModel {
    val task = task ?: return TaskDetailUiModel.NotFound
    return TaskDetailUiModel.Loaded(
        title = task.title,
        statusText = if (task.isDone) "Done" else "Active",
        priority = task.priority,
        dueText = "Due ${DateFormatting.format(task.dueDate)}",
        isOverdue = task.isOverdue(today),
        notesText = task.notes.ifEmpty { "No notes" },
        hasNotes = task.notes.isNotEmpty(),
        toggleButtonText = if (task.isDone) "Mark as active" else "Mark as done",
    )
}
