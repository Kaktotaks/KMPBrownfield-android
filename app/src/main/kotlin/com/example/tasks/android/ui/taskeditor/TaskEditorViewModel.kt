package com.example.tasks.android.ui.taskeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.tasks.android.data.TaskRepository
import com.example.tasks.android.domain.Clock
import com.example.tasks.android.domain.Priority
import com.example.tasks.android.domain.Task
import com.example.tasks.android.domain.TaskValidator
import com.example.tasks.android.domain.ValidationError
import com.example.tasks.android.ui.components.appContainer
import com.example.tasks.android.ui.navigation.TaskEditorRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class TaskEditorUiState(
    val isNew: Boolean,
    val title: String,
    val notes: String,
    val priority: Priority,
    val dueDate: LocalDate,
    val errors: List<ValidationError>,
) {
    val screenTitle: String get() = if (isNew) "New Task" else "Edit Task"
    val canSave: Boolean get() = errors.isEmpty()
}

class TaskEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    private val validator: TaskValidator,
    clock: Clock,
) : ViewModel() {

    private val original: Task? = savedStateHandle.toRoute<TaskEditorRoute>().taskId
        .takeIf { it != TaskEditorRoute.NEW_TASK_ID }
        ?.let { repository.task(it) }

    private val _uiState = MutableStateFlow(
        validated(
            TaskEditorUiState(
                isNew = original == null,
                title = original?.title ?: "",
                notes = original?.notes ?: "",
                priority = original?.priority ?: Priority.MEDIUM,
                dueDate = original?.dueDate ?: clock.today(),
                errors = emptyList(),
            ),
        ),
    )
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) = edit { it.copy(title = value) }

    fun onNotesChange(value: String) = edit { it.copy(notes = value) }

    fun onPriorityChange(value: Priority) = edit { it.copy(priority = value) }

    fun onDueDateChange(value: LocalDate) = edit { it.copy(dueDate = value) }

    /** Returns true when saved (caller dismisses the sheet). */
    fun save(): Boolean {
        val state = validated(_uiState.value)
        _uiState.value = state
        if (!state.canSave) return false
        val existing = original
        if (existing == null) {
            repository.add(state.title, state.notes, state.priority, state.dueDate)
        } else {
            repository.update(
                existing.copy(
                    title = state.title.trim(),
                    notes = state.notes,
                    priority = state.priority,
                    dueDate = state.dueDate,
                ),
            )
        }
        return true
    }

    private fun edit(transform: (TaskEditorUiState) -> TaskEditorUiState) {
        _uiState.update { validated(transform(it)) }
    }

    // Live validation as the user types.
    private fun validated(state: TaskEditorUiState): TaskEditorUiState = state.copy(
        errors = validator.validate(
            title = state.title,
            notes = state.notes,
            dueDate = state.dueDate,
            originalDueDate = original?.dueDate,
        ),
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                TaskEditorViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = container.taskRepository,
                    validator = container.taskValidator,
                    clock = container.clock,
                )
            }
        }
    }
}
