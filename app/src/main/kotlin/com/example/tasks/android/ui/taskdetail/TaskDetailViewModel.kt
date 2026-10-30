package com.example.tasks.android.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.tasks.shared.data.LoadState
import com.example.tasks.shared.data.TaskRepository
import com.example.tasks.android.ui.components.appContainer
import com.example.tasks.android.ui.navigation.TaskDetailRoute
import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

data class TaskDetailUiState(
    val task: Task?,
    val today: LocalDate,
)

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    val taskId: Int = savedStateHandle.toRoute<TaskDetailRoute>().taskId

    val uiState: StateFlow<TaskDetailUiState> = repository.state
        .map { state ->
            val task = (state as? LoadState.Loaded)?.tasks?.firstOrNull { it.id == taskId }
            TaskDetailUiState(task = task, today = dateProvider.today())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState(task = repository.task(taskId), today = dateProvider.today()),
        )

    fun onToggle() = repository.toggle(taskId)

    fun onDelete() = repository.delete(taskId)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                TaskDetailViewModel(createSavedStateHandle(), container.taskRepository, container.dateProvider)
            }
        }
    }
}
