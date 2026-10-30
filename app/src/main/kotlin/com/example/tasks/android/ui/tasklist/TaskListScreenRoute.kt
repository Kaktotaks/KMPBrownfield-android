package com.example.tasks.android.ui.tasklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.TaskListViewModelFactory
import com.example.tasks.shared.presentation.TaskListViewModel

/**
 * Stateful entry point of the Tasks list: the only place that talks to the shared [TaskListViewModel].
 * Collects its state, maps it to [TaskListUiModel] and wires the events into the stateless [TaskListScreen].
 * (Named `...ScreenRoute` because `TaskListRoute` is the type-safe navigation destination.)
 */
@Composable
fun TaskListScreenRoute(
    onOpenTask: (Int) -> Unit,
    onNewTask: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = TaskListViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }

    TaskListScreen(
        model = model,
        onQueryChange = viewModel::onQueryChange,
        onStatusChange = viewModel::onStatusChange,
        onSortChange = viewModel::onSortChange,
        onToggle = viewModel::onToggle,
        onRetry = viewModel::onRetry,
        onOpenTask = onOpenTask,
        onNewTask = onNewTask,
    )
}
