package com.example.tasks.android.ui.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.EmptyContent
import com.example.tasks.android.ui.components.PriorityBadge
import com.example.tasks.android.ui.theme.TaskColors
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.domain.Priority

/** Stateless Task detail: layout only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    model: TaskDetailUiModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    // Purely visual state: the delete confirmation dialog.
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (model is TaskDetailUiModel.Loaded) {
                        TextButton(onClick = onEdit) { Text("Edit") }
                    }
                },
            )
        },
    ) { padding ->
        when (model) {
            TaskDetailUiModel.NotFound -> EmptyContent("No tasks", Modifier.padding(padding))
            is TaskDetailUiModel.Loaded -> TaskDetailContent(
                model = model,
                onToggle = onToggle,
                onDeleteClick = { confirmDelete = true },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (confirmDelete) {
        DeleteConfirmationDialog(
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun TaskDetailContent(
    model: TaskDetailUiModel.Loaded,
    onToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(model.title, style = MaterialTheme.typography.headlineMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PriorityBadge(model.priority)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(model.dueText, style = MaterialTheme.typography.bodyLarge)
            if (model.isOverdue) {
                Text("Overdue", style = MaterialTheme.typography.bodyLarge, color = TaskColors.Overdue)
            }
        }
        HorizontalDivider()
        if (model.hasNotes) {
            Text(model.notesText, style = MaterialTheme.typography.bodyLarge)
        } else {
            Text(model.notesText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
        Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
            Text(model.toggleButtonText)
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Delete task")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete task?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenPreview() {
    TasksTheme {
        TaskDetailScreen(
            model = TaskDetailUiModel.Loaded(
                title = "Write report",
                statusText = "Active",
                priority = Priority.HIGH,
                dueText = "Due 28 Oct 2026",
                isOverdue = true,
                notesText = "Quarterly numbers for the team.",
                hasNotes = true,
                toggleButtonText = "Mark as done",
            ),
            onBack = {}, onEdit = {}, onToggle = {}, onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenNotFoundPreview() {
    TasksTheme {
        TaskDetailScreen(model = TaskDetailUiModel.NotFound, onBack = {}, onEdit = {}, onToggle = {}, onDelete = {})
    }
}

// endregion
