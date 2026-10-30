package com.example.tasks.android.ui.taskdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.TaskDetailViewModelFactory
import com.example.tasks.shared.presentation.TaskDetailViewModel

/**
 * Stateful entry point of Task detail: collects the shared [TaskDetailViewModel], maps to [TaskDetailUiModel]
 * and wires the events. Delete = VM delete + pop back (SPEC: "Delete -> confirmation -> pop back to list").
 */
@Composable
fun TaskDetailScreenRoute(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }

    TaskDetailScreen(
        model = model,
        onBack = onBack,
        onEdit = { onEdit(viewModel.taskId) },
        onToggle = viewModel::onToggle,
        onDelete = {
            viewModel.onDelete()
            onBack()
        },
    )
}
