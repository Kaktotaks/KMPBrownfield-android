package com.example.tasks.android.ui.taskeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.DateField
import com.example.tasks.android.ui.components.ErrorBanner
import com.example.tasks.android.ui.components.PillSegmentedControl
import com.example.tasks.android.ui.components.SavePill
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.android.ui.components.TasksTextField
import com.example.tasks.android.ui.components.TasksVisibility
import com.example.tasks.android.ui.components.priorityBar
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.android.ui.voice.VoiceCardActions
import com.example.tasks.android.ui.voice.VoiceCardModel
import com.example.tasks.android.ui.voice.VoiceCardVariant
import com.example.tasks.android.ui.voice.VoiceNoteCard
import com.example.tasks.android.ui.voice.VoicePreviewData
import com.example.tasks.shared.domain.Priority

/**
 * Stateless editor content (§3.3): header (Cancel / title / SavePill) + scrolling body.
 * Hosted by [TaskEditorSheet]'s `ModalBottomSheet`; the field order is the focus order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    model: TaskEditorUiModel,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onDueDateSelected: (Long) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    /** The voice-note section under Due date; supplied by the sheet (stateful) or a preview. */
    voiceSection: @Composable () -> Unit = {},
) {
    val colors = TasksTheme.colors
    // Purely visual state: the system date picker.
    var showDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
        // Header: 3-column grid.
        Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp)) {
            Text(
                stringResource(R.string.cancel),
                style = TasksType.bodyMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onCancel)
                    .padding(vertical = 12.dp),
            )
            Text(model.screenTitle, style = TasksType.sheetTitle, color = colors.textPrimary, modifier = Modifier.align(Alignment.Center))
            SavePill(
                text = stringResource(R.string.save),
                onClick = onSave,
                enabled = model.canSave,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AnimatedVisibility(model.errorFieldCount > 0, enter = TasksVisibility.enter, exit = TasksVisibility.exit) {
                ErrorBanner(pluralStringResource(R.plurals.fix_fields, model.errorFieldCount, model.errorFieldCount))
            }
            TasksTextField(
                label = stringResource(R.string.title_label),
                value = model.title,
                onValueChange = onTitleChange,
                placeholder = stringResource(R.string.title_placeholder),
                error = model.titleError,
            )
            TasksTextField(
                label = stringResource(R.string.notes),
                value = model.notes,
                onValueChange = onNotesChange,
                minLines = 3,
                error = model.notesError,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.priority).uppercase(), style = TasksType.overline, color = colors.textSecondary)
                PillSegmentedControl(
                    options = model.priorityOptions,
                    onSelect = onPriorityChange,
                    segmentHeight = 36.dp,
                    dotColor = { colors.priorityBar(it) },
                )
            }
            DateField(
                label = stringResource(R.string.due_date),
                valueText = model.dueDateText,
                onClick = { showDatePicker = true },
                error = model.dueDateError,
            )
            voiceSection()
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = model.dueDateMillis)
        // Lime is fill-only (§1.1): selected day = accent container, "today" and buttons use textPrimary.
        val pickerColors = DatePickerDefaults.colors(
            containerColor = colors.surface,
            selectedDayContainerColor = colors.accent,
            selectedDayContentColor = colors.onAccent,
            todayContentColor = colors.textPrimary,
            todayDateBorderColor = colors.textPrimary,
            selectedYearContainerColor = colors.accent,
            selectedYearContentColor = colors.onAccent,
            currentYearContentColor = colors.textPrimary,
            navigationContentColor = colors.textPrimary,
            dividerColor = colors.border,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = pickerColors,
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onDueDateSelected)
                    showDatePicker = false
                }) { Text(stringResource(R.string.save), style = TasksType.button, color = colors.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel), style = TasksType.button, color = colors.textPrimary)
                }
            },
        ) {
            DatePicker(
                state = pickerState,
                colors = pickerColors,
            )
        }
    }
}

// region Previews

private fun previewModel(withErrors: Boolean) = TaskEditorUiModel(
    screenTitle = "New task",
    title = "",
    notes = "Ask about the express option and bring the old passport.",
    priorityOptions = Priority.entries.map { SelectableOption(it, it == Priority.HIGH, label = it.displayName) },
    dueDateText = "2 Nov 2026",
    dueDateMillis = 0L,
    titleError = if (withErrors) "Title must be at least 3 characters." else null,
    notesError = null,
    dueDateError = if (withErrors) "Due date can't be in the past." else null,
    canSave = !withErrors,
)

@Composable
private fun PreviewScreen(model: TaskEditorUiModel, dark: Boolean, voice: VoiceCardModel? = null) {
    TasksTheme(darkTheme = dark) {
        Box(Modifier.fillMaxWidth().background(TasksTheme.colors.bg)) {
            TaskEditorScreen(
                model, {}, {}, {}, {}, {}, {},
                voiceSection = { if (voice != null) VoiceNoteCard(voice, VoiceCardActions.None, variant = VoiceCardVariant.Editor) },
            )
        }
    }
}

@Preview(name = "Editor + empty voice light", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorVoiceEmptyLightPreview() = PreviewScreen(previewModel(false), dark = false, voice = VoiceCardModel.Empty)

@Preview(name = "Editor + recorded voice light", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorVoiceRecordedLightPreview() = PreviewScreen(previewModel(false), dark = false, voice = VoicePreviewData.player)

@Preview(name = "Editor + recording warning light", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorVoiceWarningLightPreview() = PreviewScreen(previewModel(false), dark = false, voice = VoicePreviewData.recordingWarning)

@Preview(name = "Editor + recorded voice dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorVoiceRecordedDarkPreview() = PreviewScreen(previewModel(false), dark = true, voice = VoicePreviewData.player)

@Preview(name = "Editor errors light", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorErrorsLightPreview() = PreviewScreen(previewModel(withErrors = true), dark = false)

@Preview(name = "Editor errors dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorErrorsDarkPreview() = PreviewScreen(previewModel(withErrors = true), dark = true)

@Preview(name = "Editor valid light", showBackground = true, heightDp = 800)
@Composable
private fun TaskEditorValidLightPreview() = PreviewScreen(previewModel(withErrors = false), dark = false)

// endregion
