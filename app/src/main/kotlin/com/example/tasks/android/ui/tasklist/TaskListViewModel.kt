package com.example.tasks.android.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tasks.android.data.LoadState
import com.example.tasks.android.data.TaskRepository
import com.example.tasks.android.domain.Clock
import com.example.tasks.android.domain.SortOrder
import com.example.tasks.android.domain.StatusFilter
import com.example.tasks.android.domain.Task
import com.example.tasks.android.domain.TaskFilter
import com.example.tasks.android.ui.components.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface TaskListContent {
    data object Loading : TaskListContent
    data object Error : TaskListContent
    data class Loaded(val tasks: List<Task>) : TaskListContent
}

data class TaskListUiState(
    val query: String = "",
    val status: StatusFilter = StatusFilter.ALL,
    val sort: SortOrder = SortOrder.DUE_DATE,
    val today: LocalDate,
    val content: TaskListContent = TaskListContent.Loading,
)

private data class FilterInput(val query: String, val status: StatusFilter, val sort: SortOrder)

class TaskListViewModel(
    private val repository: TaskRepository,
    private val clock: Clock,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val status = MutableStateFlow(StatusFilter.ALL)
    private val sort = MutableStateFlow(SortOrder.DUE_DATE)

    private val filterInput = combine(query, status, sort) { q, s, o -> FilterInput(q, s, o) }

    val uiState: StateFlow<TaskListUiState> =
        combine(repository.state, filterInput) { loadState, input ->
            val content = when (loadState) {
                LoadState.Loading -> TaskListContent.Loading
                LoadState.Error -> TaskListContent.Error
                is LoadState.Loaded -> TaskListContent.Loaded(
                    TaskFilter.apply(loadState.tasks, input.query, input.status, input.sort),
                )
            }
            TaskListUiState(
                query = input.query,
                status = input.status,
                sort = input.sort,
                today = clock.today(),
                content = content,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskListUiState(today = clock.today()),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusChange(value: StatusFilter) {
        status.value = value
    }

    fun onSortChange(value: SortOrder) {
        sort.value = value
    }

    fun onToggle(id: Int) = repository.toggle(id)

    fun onRetry() {
        viewModelScope.launch { repository.load() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                TaskListViewModel(container.taskRepository, container.clock)
            }
        }
    }
}
