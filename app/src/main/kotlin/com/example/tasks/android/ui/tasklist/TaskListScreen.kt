package com.example.tasks.android.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.EmptyContent
import com.example.tasks.android.ui.components.ErrorContent
import com.example.tasks.android.ui.components.LoadingContent
import com.example.tasks.android.ui.components.PriorityBadge
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.android.ui.theme.TaskColors
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter

/** Stateless Tasks list: layout only. All data comes pre-formatted in [model]; all actions go out via lambdas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    model: TaskListUiModel,
    onQueryChange: (String) -> Unit,
    onStatusChange: (StatusFilter) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onToggle: (Int) -> Unit,
    onRetry: () -> Unit,
    onOpenTask: (Int) -> Unit,
    onNewTask: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    SortMenu(options = model.sortOptions, onSelect = onSortChange)
                    IconButton(onClick = onNewTask) {
                        Icon(Icons.Filled.Add, contentDescription = "New Task")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(query = model.query, onQueryChange = onQueryChange)
            StatusFilterRow(options = model.statusOptions, onSelect = onStatusChange)
            when (val content = model.content) {
                TaskListUiModel.Content.Loading -> LoadingContent()
                TaskListUiModel.Content.Error -> ErrorContent(onRetry = onRetry)
                TaskListUiModel.Content.Empty -> EmptyContent("No tasks")
                is TaskListUiModel.Content.Loaded -> TaskRows(
                    rows = content.rows,
                    onToggle = onToggle,
                    onOpenTask = onOpenTask,
                )
            }
        }
    }
}

@Composable
private fun SortMenu(options: List<SelectableOption<SortOrder>>, onSelect: (SortOrder) -> Unit) {
    // Purely visual state: whether the dropdown is open.
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    onSelect(option.value)
                    open = false
                },
                trailingIcon = {
                    if (option.isSelected) Icon(Icons.Filled.Check, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search tasks") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(options: List<SelectableOption<StatusFilter>>, onSelect: (StatusFilter) -> Unit) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
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

@Composable
private fun TaskRows(rows: List<TaskRowUi>, onToggle: (Int) -> Unit, onOpenTask: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(rows, key = { it.id }) { row ->
            TaskRow(
                row = row,
                onToggle = { onToggle(row.id) },
                onClick = { onOpenTask(row.id) },
            )
            HorizontalDivider(Modifier.padding(start = 64.dp))
        }
    }
}

@Composable
private fun TaskRow(row: TaskRowUi, onToggle: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading circle toggle: toggles isDone without opening detail.
        IconButton(onClick = onToggle, modifier = Modifier.padding(horizontal = 4.dp)) {
            Icon(
                imageVector = if (row.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = row.toggleLabel,
                tint = if (row.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (row.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (row.isDone) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = row.dueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (row.isOverdue) {
                    Text("Overdue", style = MaterialTheme.typography.bodySmall, color = TaskColors.Overdue)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        PriorityBadge(row.priority)
    }
}

// region Previews

private fun previewModel(content: TaskListUiModel.Content) = TaskListUiModel(
    query = "",
    statusOptions = StatusFilter.entries.map { SelectableOption(it, it.displayName, it == StatusFilter.ALL) },
    sortOptions = SortOrder.entries.map { SelectableOption(it, it.displayName, it == SortOrder.DUE_DATE) },
    content = content,
)

@Preview(showBackground = true)
@Composable
private fun TaskListScreenLoadedPreview() {
    TasksTheme {
        TaskListScreen(
            model = previewModel(
                TaskListUiModel.Content.Loaded(
                    listOf(
                        TaskRowUi(1, "Buy milk", "Due 28 Oct 2026", isOverdue = true, isDone = false, toggleLabel = "Mark as done", priority = Priority.HIGH),
                        TaskRowUi(2, "Write report", "Due 30 Oct 2026", isOverdue = false, isDone = false, toggleLabel = "Mark as done", priority = Priority.MEDIUM),
                        TaskRowUi(3, "Call mom", "Due 1 Nov 2026", isOverdue = false, isDone = true, toggleLabel = "Mark as active", priority = Priority.LOW),
                    ),
                ),
            ),
            onQueryChange = {}, onStatusChange = {}, onSortChange = {}, onToggle = {},
            onRetry = {}, onOpenTask = {}, onNewTask = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenEmptyPreview() {
    TasksTheme {
        TaskListScreen(
            model = previewModel(TaskListUiModel.Content.Empty),
            onQueryChange = {}, onStatusChange = {}, onSortChange = {}, onToggle = {},
            onRetry = {}, onOpenTask = {}, onNewTask = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenErrorPreview() {
    TasksTheme {
        TaskListScreen(
            model = previewModel(TaskListUiModel.Content.Error),
            onQueryChange = {}, onStatusChange = {}, onSortChange = {}, onToggle = {},
            onRetry = {}, onOpenTask = {}, onNewTask = {},
        )
    }
}

// endregion
