package com.example.tasks.android.data

import com.example.tasks.android.domain.Clock
import com.example.tasks.android.domain.Priority
import com.example.tasks.android.domain.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

// KMP-shared candidate: single in-memory source of truth, no Android dependencies.
sealed interface LoadState {
    data object Loading : LoadState
    data object Error : LoadState
    data class Loaded(val tasks: List<Task>) : LoadState
}

class TaskRepository(
    private val api: TaskApi,
    private val clock: Clock,
) {
    private val _state = MutableStateFlow<LoadState>(LoadState.Loading)
    val state: StateFlow<LoadState> = _state.asStateFlow()

    /** Loads tasks from the network. After a successful load, changes live in memory only. */
    suspend fun load() {
        _state.value = LoadState.Loading
        _state.value = try {
            val today = clock.today()
            LoadState.Loaded(api.fetchTodos().map { it.toTask(today) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadState.Error
        }
    }

    fun task(id: Int): Task? = currentTasks().firstOrNull { it.id == id }

    /** Adds a task with id = max existing id + 1 (or 1 if empty) and returns it. */
    fun add(title: String, notes: String, priority: Priority, dueDate: LocalDate): Task {
        val newTask = Task(
            id = (currentTasks().maxOfOrNull { it.id } ?: 0) + 1,
            title = title.trim(),
            notes = notes,
            priority = priority,
            dueDate = dueDate,
            isDone = false,
        )
        mutate { it + newTask }
        return newTask
    }

    fun update(task: Task) {
        val saved = task.copy(title = task.title.trim())
        mutate { list -> list.map { if (it.id == saved.id) saved else it } }
    }

    fun toggle(id: Int) {
        mutate { list -> list.map { if (it.id == id) it.copy(isDone = !it.isDone) else it } }
    }

    fun delete(id: Int) {
        mutate { list -> list.filterNot { it.id == id } }
    }

    private fun currentTasks(): List<Task> = (_state.value as? LoadState.Loaded)?.tasks.orEmpty()

    private fun mutate(transform: (List<Task>) -> List<Task>) {
        _state.update { state ->
            if (state is LoadState.Loaded) LoadState.Loaded(transform(state.tasks)) else state
        }
    }
}
