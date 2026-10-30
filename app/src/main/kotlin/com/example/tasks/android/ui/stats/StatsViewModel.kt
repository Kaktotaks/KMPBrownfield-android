package com.example.tasks.android.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tasks.shared.data.LoadState
import com.example.tasks.shared.data.TaskRepository
import com.example.tasks.android.ui.components.appContainer
import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.StatsCalculator
import com.example.tasks.shared.domain.TaskStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data object Error : StatsUiState
    data class Loaded(val stats: TaskStats) : StatsUiState
}

class StatsViewModel(
    private val repository: TaskRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = repository.state
        .map { state ->
            when (state) {
                LoadState.Loading -> StatsUiState.Loading
                LoadState.Error -> StatsUiState.Error
                is LoadState.Loaded -> StatsUiState.Loaded(StatsCalculator.calculate(state.tasks, dateProvider.today()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState.Loading)

    fun onRetry() {
        viewModelScope.launch { repository.load() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                StatsViewModel(container.taskRepository, container.dateProvider)
            }
        }
    }
}
