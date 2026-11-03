package com.example.tasks.android.ui.taskeditor

import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.ValidationError
import com.example.tasks.shared.presentation.TaskEditorUiState
import kotlinx.datetime.LocalDate

/** Everything [TaskEditorScreen] draws, already mapped. Plain Kotlin: no Compose, unit-tested on the JVM. */
data class TaskEditorUiModel(
    /** "New Task" / "Edit Task" (from shared). */
    val screenTitle: String,
    val title: String,
    val notes: String,
    val priorityOptions: List<SelectableOption<Priority>>,
    /** "29 Oct 2026" — value of the DateField. */
    val dueDateText: String,
    /** Due date as UTC-midnight epoch millis: the unit Material3 `DatePicker` works in. */
    val dueDateMillis: Long,
    /** Validation messages (text from shared), mapped to the field they belong to. */
    val titleError: String?,
    val notesError: String?,
    val dueDateError: String?,
    val canSave: Boolean,
) {
    /** Number of fields with an error: "Fix N fields to save this task." */
    val errorFieldCount: Int
        get() = listOfNotNull(titleError, notesError, dueDateError).size
}

/** Shared VM state -> UI model. Pure function (called from [TaskEditorSheet] inside `remember(state)`). */
fun TaskEditorUiState.toUiModel(): TaskEditorUiModel = TaskEditorUiModel(
    screenTitle = screenTitle,
    title = title,
    notes = notes,
    priorityOptions = Priority.entries.map { SelectableOption(it, it == priority, label = it.displayName) },
    dueDateText = DateFormatting.format(dueDate),
    dueDateMillis = dueDate.toUtcMillis(),
    titleError = errors.firstOrNull { it == ValidationError.TITLE_TOO_SHORT || it == ValidationError.TITLE_TOO_LONG }?.message,
    notesError = errors.firstOrNull { it == ValidationError.NOTES_TOO_LONG }?.message,
    dueDateError = errors.firstOrNull { it == ValidationError.DUE_DATE_IN_PAST }?.message,
    canSave = canSave,
)

// DatePicker <-> kotlinx.datetime.LocalDate via epoch days (UTC midnight = whole days since 1970-01-01).
private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

fun LocalDate.toUtcMillis(): Long = toEpochDays() * MILLIS_PER_DAY

/** Reverse of [toUtcMillis]; any millis inside a UTC day map to that day (floor, also before 1970). */
fun utcMillisToLocalDate(millis: Long): LocalDate =
    LocalDate.fromEpochDays(millis.floorDiv(MILLIS_PER_DAY))
