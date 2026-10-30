package com.example.tasks.android.ui.taskeditor

import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.TaskEditorUiState
import kotlinx.datetime.LocalDate

/** Everything [TaskEditorScreen] draws, already formatted. Plain Kotlin: no Compose, unit-tested on the JVM. */
data class TaskEditorUiModel(
    /** "New Task" / "Edit Task". */
    val screenTitle: String,
    val title: String,
    val notes: String,
    val priorityOptions: List<SelectableOption<Priority>>,
    /** "29 Oct 2026" — label of the due-date button. */
    val dueDateText: String,
    /** Due date as UTC-midnight epoch millis: the unit Material3 `DatePicker` works in. */
    val dueDateMillis: Long,
    /** Live validation messages, shown in red under the form. */
    val errorMessages: List<String>,
    val canSave: Boolean,
)

/** Shared VM state -> UI model. Pure function (called from [TaskEditorScreenRoute] inside `remember(state)`). */
fun TaskEditorUiState.toUiModel(): TaskEditorUiModel = TaskEditorUiModel(
    screenTitle = screenTitle,
    title = title,
    notes = notes,
    priorityOptions = Priority.entries.map { SelectableOption(it, it.displayName, it == priority) },
    dueDateText = DateFormatting.format(dueDate),
    dueDateMillis = dueDate.toUtcMillis(),
    errorMessages = errors.map { it.message },
    canSave = canSave,
)

// DatePicker <-> kotlinx.datetime.LocalDate via epoch days (UTC midnight = whole days since 1970-01-01).
private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

fun LocalDate.toUtcMillis(): Long = toEpochDays() * MILLIS_PER_DAY

/** Reverse of [toUtcMillis]; any millis inside a UTC day map to that day (floor, also before 1970). */
fun utcMillisToLocalDate(millis: Long): LocalDate =
    LocalDate.fromEpochDays(millis.floorDiv(MILLIS_PER_DAY))
