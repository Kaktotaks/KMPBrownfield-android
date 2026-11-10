package com.example.tasks.android.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.R
import com.example.tasks.android.TasksApplication
import com.example.tasks.android.ui.components.PillSegmentedControl
import com.example.tasks.android.ui.components.SavePill
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.android.ui.components.TasksTextField
import com.example.tasks.android.ui.components.TimeField
import com.example.tasks.android.ui.components.TimePickerDialog
import com.example.tasks.android.ui.components.priorityBar
import com.example.tasks.android.ui.components.voiceNoteDraftViewModelFactory
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.android.ui.voice.VoiceCardVariant
import com.example.tasks.android.ui.voice.VoiceNoteSection
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.ValidationError
import com.example.tasks.shared.domain.VoiceNote
import com.example.tasks.shared.presentation.DayLabel
import com.example.tasks.shared.presentation.MapViewModel
import com.example.tasks.shared.presentation.NewTaskDraft
import com.example.tasks.shared.presentation.VoiceNoteUiState
import com.example.tasks.shared.presentation.VoiceNoteViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Title / notes errors of the new-task form (messages come from shared's validator). */
internal fun List<ValidationError>.titleError(): String? =
    firstOrNull { it == ValidationError.TITLE_TOO_SHORT || it == ValidationError.TITLE_TOO_LONG }?.message

internal fun List<ValidationError>.notesError(): String? = firstOrNull { it == ValidationError.NOTES_TOO_LONG }?.message

/**
 * NewTaskAtLocationSheet (components/NewTaskAtLocationSheet.dc.html), opened by a long-press: the Editor's fields
 * (TasksTextField, PillSegmentedControl), a place card, Time and the voice note (the same VoiceNoteCard / VoiceRecorder /
 * VoicePlayer as the Editor, driven by a Draft [VoiceNoteViewModel] bound to the Map view model). Save stays disabled until
 * the title is valid (the same validator, via [MapViewModel.validateNewTask]).
 */
@Composable
internal fun NewTaskAtLocationSheet(
    draft: NewTaskDraft,
    day: LocalDate,
    dayLabel: DayLabel,
    mapViewModel: MapViewModel,
    maxHeight: Dp,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TasksTheme.colors
    val container = (LocalContext.current.applicationContext as TasksApplication).container

    // The draft voice VM lives in its own store: cleared with the sheet (cancels a recording, deletes unsaved clips).
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val voiceViewModel: VoiceNoteViewModel = viewModel(
        viewModelStoreOwner = owner,
        key = "voice-new",
        factory = remember(container) { voiceNoteDraftViewModelFactory(container, null) },
    )
    DisposableEffect(voiceViewModel, mapViewModel) {
        voiceViewModel.bindTo(mapViewModel)
        onDispose {}
    }

    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var priorityName by rememberSaveable { mutableStateOf(Priority.MEDIUM.name) }
    var timeMinutes by rememberSaveable { mutableStateOf<Int?>(draft.defaultTime.hour * 60 + draft.defaultTime.minute) }
    var showTimePicker by remember { mutableStateOf(false) }
    var titleTouched by rememberSaveable { mutableStateOf(false) }
    val priority = Priority.valueOf(priorityName)
    val time = timeMinutes?.let { LocalTime(it / 60, it % 60) }

    val errors = mapViewModel.validateNewTask(title, notes)
    val canSave = errors.isEmpty()

    val save = {
        if (voiceViewModel.uiState.value is VoiceNoteUiState.Recording) voiceViewModel.onStop()
        val note: VoiceNote? = voiceViewModel.currentNote()
        if (mapViewModel.saveNewTask(title, notes, priority, time, note)) onSaved()
    }

    SheetSurface(modifier.fillMaxWidth()) {
        Column(Modifier.imePadding().navigationBarsPadding()) {
            // Header: Cancel · title · Save.
            Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.map_cancel),
                    style = TasksType.bodyMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = onCancel)
                        .padding(vertical = 12.dp),
                )
                Text(
                    stringResource(R.string.map_new_here),
                    style = TasksType.sheetTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.align(Alignment.Center).semantics { heading() },
                )
                SavePill(stringResource(R.string.map_save), onClick = save, enabled = canSave, modifier = Modifier.align(Alignment.CenterEnd))
            }
            Column(
                Modifier
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PlaceCard(draft)
                TasksTextField(
                    label = stringResource(R.string.title_label),
                    value = title,
                    onValueChange = { title = it; titleTouched = true },
                    placeholder = stringResource(R.string.title_placeholder),
                    error = if (titleTouched) errors.titleError() else null,
                )
                val dayWord = when (dayLabel) {
                    DayLabel.TODAY -> stringResource(R.string.map_day_today)
                    DayLabel.TOMORROW -> stringResource(R.string.map_day_tomorrow)
                    DayLabel.YESTERDAY -> stringResource(R.string.map_day_yesterday)
                    DayLabel.NONE -> null
                }
                val dateText = dateText(day)
                val dayText = if (dayWord != null) stringResource(R.string.map_when_day_time, dayWord, dateText) else dateText
                val timeValue: AnnotatedString = if (time != null) {
                    buildAnnotatedString {
                        append("$dayText · ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")) { append(formatTime(time)) }
                    }
                } else {
                    AnnotatedString(stringResource(R.string.editor_no_time))
                }
                TimeField(
                    label = stringResource(R.string.map_time),
                    valueText = timeValue,
                    onClick = { showTimePicker = true },
                    isPlaceholder = time == null,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.priority).uppercase(), style = TasksType.overline, color = colors.textSecondary)
                    PillSegmentedControl(
                        options = Priority.entries.map { p ->
                            SelectableOption(
                                p,
                                p == priority,
                                labelRes = when (p) {
                                    Priority.HIGH -> R.string.map_priority_high
                                    Priority.MEDIUM -> R.string.map_priority_medium
                                    Priority.LOW -> R.string.map_priority_low
                                },
                            )
                        },
                        onSelect = { priorityName = it.name },
                        segmentHeight = 36.dp,
                        dotColor = { colors.priorityBar(it) },
                    )
                }
                TasksTextField(
                    label = stringResource(R.string.notes),
                    value = notes,
                    onValueChange = { notes = it },
                    error = errors.notesError(),
                )
                VoiceNoteSection(voiceViewModel, VoiceCardVariant.Editor)
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initial = time,
            defaultTime = draft.defaultTime,
            onConfirm = { timeMinutes = it.hour * 60 + it.minute; showTimePicker = false },
            onClear = { timeMinutes = null; showTimePicker = false },
            onDismiss = { showTimePicker = false },
        )
    }
}

/** The place of the long-press: lime pin disc, the address (or "Finding address…") and "Dnipro · dropped pin". */
@Composable
private fun PlaceCard(draft: NewTaskDraft) {
    val colors = TasksTheme.colors
    val line = draft.address ?: if (draft.isResolvingAddress) stringResource(R.string.map_finding_address) else "%.4f, %.4f".format(draft.point.lat, draft.point.lng)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(colors.accent, CircleShape), contentAlignment = Alignment.Center) {
            Icon(MapIcons.Pin, null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(line, style = TasksType.button.copy(lineHeight = 21.sp), color = colors.textPrimary)
            Text(stringResource(R.string.map_dropped_pin), style = TasksType.caption, color = colors.textSecondary)
        }
    }
}
