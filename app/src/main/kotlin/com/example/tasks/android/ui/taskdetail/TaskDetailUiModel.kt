package com.example.tasks.android.ui.taskdetail

import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.TaskDetailUiState

/** Everything [TaskDetailScreen] draws, already mapped. Plain Kotlin: no Compose, unit-tested on the JVM. */
sealed interface TaskDetailUiModel {
    /** The task is gone (e.g. deleted) — the screen shows "Task not found". */
    data object NotFound : TaskDetailUiModel

    data class Loaded(
        val title: String,
        val isDone: Boolean,
        val priority: Priority,
        /** "5 Nov 2026"; the "Due %s" copy is applied by the UI. */
        val dueDate: String,
        val isOverdue: Boolean,
        /** The notes, or null when blank (the notes card is hidden then). */
        val notes: String?,
    ) : TaskDetailUiModel
}

/** Shared VM state -> UI model. Pure function (called from [TaskDetailScreenRoute] inside `remember(state)`). */
fun TaskDetailUiState.toUiModel(): TaskDetailUiModel {
    val task = task ?: return TaskDetailUiModel.NotFound
    return TaskDetailUiModel.Loaded(
        title = task.title,
        isDone = task.isDone,
        priority = task.priority,
        dueDate = DateFormatting.format(task.dueDate),
        isOverdue = task.isOverdue(today),
        notes = task.notes.takeIf { it.isNotBlank() },
    )
}
