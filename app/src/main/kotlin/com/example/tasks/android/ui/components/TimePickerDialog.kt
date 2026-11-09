package com.example.tasks.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import kotlinx.datetime.LocalTime

/**
 * M3 `TimePicker` in a dialog (24-hour dial, like the rest of the Map copy). OK confirms, Cancel dismisses,
 * "No time" clears the optional time. Lime is fill-only: selected values sit on the accent container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initial: LocalTime?,
    defaultTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TasksTheme.colors
    val start = initial ?: defaultTime
    val state = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = true)
    val pickerColors = TimePickerDefaults.colors(
        containerColor = colors.surface,
        clockDialColor = colors.surface2,
        selectorColor = colors.accent,
        clockDialSelectedContentColor = colors.onAccent,
        clockDialUnselectedContentColor = colors.textPrimary,
        timeSelectorSelectedContainerColor = colors.accent,
        timeSelectorSelectedContentColor = colors.onAccent,
        timeSelectorUnselectedContainerColor = colors.surface2,
        timeSelectorUnselectedContentColor = colors.textPrimary,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = TasksShapes.xl,
        title = { Text(stringResource(R.string.editor_set_time), style = TasksType.sheetTitle, color = colors.textPrimary) },
        text = { TimePicker(state = state, colors = pickerColors) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(state.hour, state.minute)) }) {
                Text(stringResource(R.string.editor_time_ok), style = TasksType.button, color = colors.textPrimary)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text(stringResource(R.string.editor_no_time), style = TasksType.button, color = colors.textPrimary) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.map_cancel), style = TasksType.button, color = colors.textPrimary) }
            }
        },
    )
}
