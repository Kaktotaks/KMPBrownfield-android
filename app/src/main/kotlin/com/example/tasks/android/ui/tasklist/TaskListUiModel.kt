package com.example.tasks.android.ui.tasklist

import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskListContent
import com.example.tasks.shared.presentation.TaskListUiState
import kotlinx.datetime.LocalDate

/** Everything [TaskListScreen] draws, already formatted. Plain Kotlin: no Compose, unit-tested on the JVM. */
data class TaskListUiModel(
    val query: String,
    val statusOptions: List<SelectableOption<StatusFilter>>,
    val sortOptions: List<SelectableOption<SortOrder>>,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content
        data object Error : Content
        data object Empty : Content
        data class Loaded(val rows: List<TaskRowUi>) : Content
    }
}

data class TaskRowUi(
    val id: Int,
    val title: String,
    /** "Due 29 Oct 2026". */
    val dueText: String,
    val isOverdue: Boolean,
    val isDone: Boolean,
    /** Accessibility label of the leading toggle: the action it performs. */
    val toggleLabel: String,
    val priority: Priority,
)

/** Shared VM state -> UI model. Pure function (called from [TaskListScreenRoute] inside `remember(state)`). */
fun TaskListUiState.toUiModel(): TaskListUiModel = TaskListUiModel(
    query = query,
    statusOptions = StatusFilter.entries.map { SelectableOption(it, it.displayName, it == status) },
    sortOptions = SortOrder.entries.map { SelectableOption(it, it.displayName, it == sort) },
    content = when (val c = content) {
        TaskListContent.Loading -> TaskListUiModel.Content.Loading
        TaskListContent.Error -> TaskListUiModel.Content.Error
        is TaskListContent.Loaded ->
            if (c.tasks.isEmpty()) {
                TaskListUiModel.Content.Empty
            } else {
                TaskListUiModel.Content.Loaded(c.tasks.map { it.toRowUi(today) })
            }
    },
)

fun Task.toRowUi(today: LocalDate): TaskRowUi = TaskRowUi(
    id = id,
    title = title,
    dueText = "Due ${DateFormatting.format(dueDate)}",
    isOverdue = isOverdue(today),
    isDone = isDone,
    toggleLabel = if (isDone) "Mark as active" else "Mark as done",
    priority = priority,
)
