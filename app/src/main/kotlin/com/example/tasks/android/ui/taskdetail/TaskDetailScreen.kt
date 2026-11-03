package com.example.tasks.android.ui.taskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.DestructiveButton
import com.example.tasks.android.ui.components.EmptyState
import com.example.tasks.android.ui.components.HeroCard
import com.example.tasks.android.ui.components.InfoRow
import com.example.tasks.android.ui.components.InfoRowsCard
import com.example.tasks.android.ui.components.PriorityBadge
import com.example.tasks.android.ui.components.PrimaryButton
import com.example.tasks.android.ui.components.SecondaryButton
import com.example.tasks.android.ui.components.StatusBadge
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.Priority

/** Stateless Task detail (§3.2): layout only. Chrome is the back arrow only; no Edit in the bar. */
@Composable
fun TaskDetailScreen(
    model: TaskDetailUiModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean = false,
) {
    val colors = TasksTheme.colors
    // Purely visual state: the delete confirmation dialog.
    var confirmDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(colors.bg).statusBarsPadding()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = colors.textPrimary)
        }
        when {
            isDeleting -> Box(Modifier.fillMaxSize())
            model is TaskDetailUiModel.NotFound -> EmptyState(
                title = stringResource(R.string.not_found_title),
                body = stringResource(R.string.not_found_body),
            )
            model is TaskDetailUiModel.Loaded -> TaskDetailContent(model, onEdit, onToggle) { confirmDelete = true }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.surface,
            title = { Text(stringResource(R.string.delete_confirm_title), style = TasksType.sheetTitle, color = colors.textPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.delete), style = TasksType.button, color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel), style = TasksType.button, color = colors.textPrimary)
                }
            },
        )
    }
}

@Composable
private fun TaskDetailContent(
    model: TaskDetailUiModel.Loaded,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val colors = TasksTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeroCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PriorityBadge(model.priority)
                StatusBadge(model.isDone)
            }
            Text(model.title, style = TasksType.heroTitle, color = colors.heroText, modifier = Modifier.padding(top = 14.dp))
            Row(
                Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val overdue = model.isOverdue && !model.isDone
                val tint = if (overdue) colors.danger else colors.heroTextSecondary
                Icon(TasksIcons.Calendar, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                val due = stringResource(R.string.due_format, model.dueDate)
                Text(
                    text = if (overdue) "$due ${stringResource(R.string.overdue_suffix)}" else due,
                    style = TasksType.subhead.let { if (overdue) it.copy(fontWeight = FontWeight.SemiBold) else it },
                    color = tint,
                )
            }
        }

        // Hidden when the notes are blank.
        if (model.notes != null) {
            Column(
                Modifier.fillMaxWidth().clip(TasksShapes.l).background(colors.surface).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(stringResource(R.string.notes).uppercase(), style = TasksType.overline, color = colors.textSecondary)
                Text(model.notes, style = TasksType.body, color = colors.textPrimary)
            }
        }

        InfoRowsCard(
            listOf(
                InfoRow(TasksIcons.Flag, stringResource(R.string.priority), model.priority.displayName),
                InfoRow(TasksIcons.Calendar, stringResource(R.string.due_date), model.dueDate),
                InfoRow(
                    TasksIcons.StatusRing,
                    stringResource(R.string.status),
                    stringResource(if (model.isDone) R.string.status_done else R.string.status_active),
                ),
            ),
        )

        if (model.isDone) {
            SecondaryButton(stringResource(R.string.mark_active), onToggle, Modifier.fillMaxWidth())
        } else {
            PrimaryButton(stringResource(R.string.mark_done), onToggle, Modifier.fillMaxWidth(), icon = TasksIcons.Check)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(stringResource(R.string.edit), onEdit, Modifier.weight(1f), icon = TasksIcons.Pencil)
            DestructiveButton(stringResource(R.string.delete), onDeleteClick, Modifier.weight(1f), icon = TasksIcons.Trash)
        }
    }
}

// region Previews

private val previewLoaded = TaskDetailUiModel.Loaded(
    title = "Prepare sprint demo slides",
    isDone = false,
    priority = Priority.HIGH,
    dueDate = "5 Nov 2026",
    isOverdue = false,
    notes = "Cover the offline sync work and the new Stats tab. Pull the latest crash-free numbers from the dashboard before Thursday.",
)

@Composable
private fun PreviewScreen(model: TaskDetailUiModel, dark: Boolean) {
    TasksTheme(darkTheme = dark) {
        TaskDetailScreen(model = model, onBack = {}, onEdit = {}, onToggle = {}, onDelete = {})
    }
}

@Preview(name = "Detail light", showBackground = true, heightDp = 800)
@Composable
private fun TaskDetailLightPreview() = PreviewScreen(previewLoaded, dark = false)

@Preview(name = "Detail dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskDetailDarkPreview() = PreviewScreen(previewLoaded, dark = true)

@Preview(name = "Detail overdue done light", showBackground = true, heightDp = 800)
@Composable
private fun TaskDetailOverdueLightPreview() =
    PreviewScreen(previewLoaded.copy(isOverdue = true, notes = null, dueDate = "31 Oct 2026"), dark = false)

@Preview(name = "Not found light", showBackground = true, heightDp = 800)
@Composable
private fun TaskDetailNotFoundLightPreview() = PreviewScreen(TaskDetailUiModel.NotFound, dark = false)

@Preview(name = "Not found dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskDetailNotFoundDarkPreview() = PreviewScreen(TaskDetailUiModel.NotFound, dark = true)

// endregion
