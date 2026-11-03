package com.example.tasks.android.ui.taskdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.TaskDetailViewModelFactory
import com.example.tasks.android.ui.taskeditor.TaskEditorSheet
import com.example.tasks.shared.presentation.TaskDetailViewModel

/**
 * Stateful entry point of Task detail: collects the shared [TaskDetailViewModel], maps to [TaskDetailUiModel]
 * and wires the events. Delete = VM delete + pop back. Hosts the edit sheet.
 */
@Composable
fun TaskDetailScreenRoute(
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }
    // Purely visual state: the edit sheet, and "deleted" so "Task not found" doesn't flash during the pop.
    var showEditor by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    TaskDetailScreen(
        model = model,
        isDeleting = isDeleting,
        onBack = onBack,
        onEdit = { showEditor = true },
        onToggle = viewModel::onToggle,
        onDelete = {
            isDeleting = true
            viewModel.onDelete()
            onBack()
        },
    )
    if (showEditor) {
        TaskEditorSheet(taskId = viewModel.taskId, onDismiss = { showEditor = false })
    }
}
