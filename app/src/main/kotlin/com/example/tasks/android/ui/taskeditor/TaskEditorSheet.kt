package com.example.tasks.android.ui.taskeditor

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tasks.android.TasksApplication
import com.example.tasks.android.ui.components.voiceNoteDraftViewModelFactory
import com.example.tasks.android.ui.voice.VoiceCardVariant
import com.example.tasks.android.ui.voice.VoiceNoteSection
import com.example.tasks.shared.presentation.VoiceNoteUiState
import com.example.tasks.shared.presentation.VoiceNoteViewModel
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.presentation.TaskEditorViewModel

/** `taskId` of a new task (the sentinel shared defines). */
const val NEW_TASK_ID = TaskEditorViewModel.NEW_TASK_ID

/**
 * Stateful entry point of the editor (§3.3): a `ModalBottomSheet` (skipPartiallyExpanded, top radius `xl`,
 * `bg` container). It owns exactly one shared [TaskEditorViewModel] per presentation: the ViewModel lives in
 * its own `ViewModelStore`, cleared when the sheet leaves the composition. Save -> `save()`, dismiss on success.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(taskId: Int, onDismiss: () -> Unit) {
    val container = (LocalContext.current.applicationContext as TasksApplication).container
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val viewModel: TaskEditorViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = remember(taskId) {
            viewModelFactory {
                initializer {
                    TaskEditorViewModel(
                        taskId = taskId,
                        repository = container.taskRepository,
                        validator = container.taskValidator,
                        dateProvider = container.dateProvider,
                    )
                }
            }
        },
    )
    // The sheet owns a Draft(initial) voice VM in the same store (cleared with the sheet: it cancels a running
    // recording and deletes its draft file) and binds it to the editor VM, which commits it on save().
    val voiceViewModel: VoiceNoteViewModel = viewModel(
        viewModelStoreOwner = owner,
        key = "voice",
        factory = remember(container) {
            voiceNoteDraftViewModelFactory(container, viewModel.uiState.value.voiceNote)
        },
    )
    DisposableEffect(voiceViewModel, viewModel) {
        voiceViewModel.bindTo(viewModel)
        onDispose {}
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }
    val colors = TasksTheme.colors

    // Cancel / swipe-down: delete this session's recordings, keep the original note (API contract §6).
    val cancel = {
        viewModel.onCancel()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = cancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = TasksShapes.sheetTop,
        containerColor = colors.bg,
        scrimColor = colors.scrim,
    ) {
        TaskEditorScreen(
            model = model,
            onTitleChange = viewModel::onTitleChange,
            onNotesChange = viewModel::onNotesChange,
            onPriorityChange = viewModel::onPriorityChange,
            onDueDateSelected = { millis -> viewModel.onDueDateChange(utcMillisToLocalDate(millis)) },
            onSave = {
                // Save mid-recording: stop and keep the clip first, then hand the note to the editor VM explicitly
                // (bindTo forwards it asynchronously).
                if (voiceViewModel.uiState.value is VoiceNoteUiState.Recording) {
                    voiceViewModel.onStop()
                    viewModel.onVoiceNoteChange(voiceViewModel.currentNote())
                }
                if (viewModel.save()) onDismiss()
            },
            onCancel = cancel,
            voiceSection = { VoiceNoteSection(voiceViewModel, VoiceCardVariant.Editor) },
        )
    }
}
