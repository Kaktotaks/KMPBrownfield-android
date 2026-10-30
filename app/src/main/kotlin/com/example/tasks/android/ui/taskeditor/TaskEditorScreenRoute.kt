package com.example.tasks.android.ui.taskeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.TaskEditorViewModelFactory
import com.example.tasks.shared.presentation.TaskEditorViewModel

/**
 * Stateful entry point of the editor sheet: collects the shared [TaskEditorViewModel], maps to
 * [TaskEditorUiModel], converts DatePicker millis back to `LocalDate`, dismisses after a successful save.
 */
@Composable
fun TaskEditorScreenRoute(
    onDismiss: () -> Unit,
    viewModel: TaskEditorViewModel = viewModel(factory = TaskEditorViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }

    TaskEditorScreen(
        model = model,
        onTitleChange = viewModel::onTitleChange,
        onNotesChange = viewModel::onNotesChange,
        onPriorityChange = viewModel::onPriorityChange,
        onDueDateSelected = { millis -> viewModel.onDueDateChange(utcMillisToLocalDate(millis)) },
        onSave = { if (viewModel.save()) onDismiss() },
        onCancel = onDismiss,
    )
}
