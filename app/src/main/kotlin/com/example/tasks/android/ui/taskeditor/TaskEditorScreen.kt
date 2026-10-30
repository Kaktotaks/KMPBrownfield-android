package com.example.tasks.android.ui.taskeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.domain.Priority

/** Stateless New/Edit task form: layout only. Dates cross this boundary as DatePicker millis. */
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
) {
    // Purely visual state: whether the date picker dialog is shown.
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(model.screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = model.canSave) { Text("Save") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = model.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = model.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Priority", style = MaterialTheme.typography.labelLarge)
            PrioritySelector(options = model.priorityOptions, onSelect = onPriorityChange)

            Text("Due date", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Text(model.dueDateText, modifier = Modifier.padding(start = 8.dp))
            }

            ValidationErrors(model.errorMessages)
        }
    }

    if (showDatePicker) {
        DueDatePickerDialog(
            initialMillis = model.dueDateMillis,
            onConfirm = { millis ->
                millis?.let(onDueDateSelected)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySelector(options: List<SelectableOption<Priority>>, onSelect: (Priority) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.isSelected,
                onClick = { onSelect(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(option.label) },
            )
        }
    }
}

/** Validation errors, live, in red under the form. */
@Composable
private fun ValidationErrors(messages: List<String>) {
    messages.forEach { message ->
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Material3 DatePicker works in UTC millis at start of day; the conversion lives in [TaskEditorUiModel]'s file. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(initialMillis: Long, onConfirm: (Long?) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.selectedDateMillis) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun TaskEditorScreenPreview() {
    TasksTheme {
        TaskEditorScreen(
            model = TaskEditorUiModel(
                screenTitle = "New Task",
                title = "Hi",
                notes = "",
                priorityOptions = Priority.entries.map { SelectableOption(it, it.displayName, it == Priority.MEDIUM) },
                dueDateText = "30 Oct 2026",
                dueDateMillis = 0L,
                errorMessages = listOf("Title must be at least 3 characters."),
                canSave = false,
            ),
            onTitleChange = {}, onNotesChange = {}, onPriorityChange = {},
            onDueDateSelected = {}, onSave = {}, onCancel = {},
        )
    }
}

// endregion
