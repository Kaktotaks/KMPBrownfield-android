package com.example.tasks.android.ui.taskdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.TaskDetailViewModelFactory
import com.example.tasks.android.ui.components.VoiceNoteDetailViewModelFactory
import com.example.tasks.android.ui.voice.VoiceCardVariant
import com.example.tasks.android.ui.voice.VoiceNoteSection
import com.example.tasks.shared.presentation.VoiceNoteViewModel
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
    // The voice-note VM (target = ExistingTask(route.taskId)) lives in the same back-stack entry as the detail VM.
    voiceViewModel: VoiceNoteViewModel = viewModel(key = "voice", factory = VoiceNoteDetailViewModelFactory),
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
        voiceSection = { VoiceNoteSection(voiceViewModel, VoiceCardVariant.Detail) },
    )
    if (showEditor) {
        TaskEditorSheet(taskId = viewModel.taskId, onDismiss = { showEditor = false })
    }
}
