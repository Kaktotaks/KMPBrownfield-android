package com.example.tasks.android.ui.tasklist

import androidx.annotation.StringRes
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskListContent
import com.example.tasks.shared.presentation.TaskListUiState
import kotlinx.datetime.LocalDate

/** Everything [TaskListScreen] draws, already mapped. Plain Kotlin: no Compose, unit-tested on the JVM. */
data class TaskListUiModel(
    val query: String,
    val statusOptions: List<SelectableOption<StatusFilter>>,
    val sortOptions: List<SelectableOption<SortOrder>>,
    /** Label of the SortPill: the current sort. */
    @StringRes val sortLabelRes: Int,
    val content: Content,
) {
    sealed interface Content {
        data object Loading : Content
        data object Error : Content

        /** Loaded, nothing to show. [hasActiveFilter] picks "No matching tasks" over "No tasks yet". */
        data class Empty(val hasActiveFilter: Boolean) : Content
        data class Loaded(val rows: List<TaskRowUi>) : Content
    }
}

data class TaskRowUi(
    val id: Int,
    val title: String,
    /** "5 Nov 2026" (from shared `DateFormatting`); the "Due %s" copy is applied by the UI. */
    val dueDate: String,
    val isOverdue: Boolean,
    val isDone: Boolean,
    val priority: Priority,
)

@StringRes
fun StatusFilter.labelRes(): Int = when (this) {
    StatusFilter.ALL -> R.string.filter_all
    StatusFilter.ACTIVE -> R.string.filter_active
    StatusFilter.DONE -> R.string.filter_done
}

@StringRes
fun SortOrder.labelRes(): Int = when (this) {
    SortOrder.DUE_DATE -> R.string.sort_due
    SortOrder.PRIORITY -> R.string.sort_priority
    SortOrder.TITLE -> R.string.sort_title
}

/** Shared VM state -> UI model. Pure function (called from [TaskListScreenRoute] inside `remember(state)`). */
fun TaskListUiState.toUiModel(): TaskListUiModel = TaskListUiModel(
    query = query,
    statusOptions = StatusFilter.entries.map { SelectableOption(it, it == status, labelRes = it.labelRes()) },
    sortOptions = SortOrder.entries.map { SelectableOption(it, it == sort, labelRes = it.labelRes()) },
    sortLabelRes = sort.labelRes(),
    content = when (val c = content) {
        TaskListContent.Loading -> TaskListUiModel.Content.Loading
        TaskListContent.Error -> TaskListUiModel.Content.Error
        is TaskListContent.Loaded ->
            if (c.tasks.isEmpty()) {
                // shared exposes no "total", so "nothing at all" = loaded, empty, and no query or filter on.
                TaskListUiModel.Content.Empty(hasActiveFilter = query.isNotBlank() || status != StatusFilter.ALL)
            } else {
                TaskListUiModel.Content.Loaded(c.tasks.map { it.toRowUi(today) })
            }
    },
)

fun Task.toRowUi(today: LocalDate): TaskRowUi = TaskRowUi(
    id = id,
    title = title,
    dueDate = DateFormatting.format(dueDate),
    isOverdue = isOverdue(today),
    isDone = isDone,
    priority = priority,
)
