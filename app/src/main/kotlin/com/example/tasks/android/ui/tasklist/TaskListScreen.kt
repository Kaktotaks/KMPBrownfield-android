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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.EmptyContent
import com.example.tasks.android.ui.components.ErrorContent
import com.example.tasks.android.ui.components.LoadingContent
import com.example.tasks.android.ui.components.PriorityBadge
import com.example.tasks.android.ui.components.TaskListViewModelFactory
import com.example.tasks.android.ui.theme.TaskColors
import com.example.tasks.shared.domain.DateFormatting
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskListContent
import com.example.tasks.shared.presentation.TaskListViewModel
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onOpenTask: (Int) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = TaskListViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.displayName) },
                                onClick = {
                                    viewModel.onSortChange(order)
                                    sortMenuOpen = false
                                },
                                trailingIcon = {
                                    if (order == state.sort) Icon(Icons.Filled.Check, contentDescription = null)
                                },
                            )
                        }
                    }
                    IconButton(onClick = onAddTask) {
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
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search tasks") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                val options = StatusFilter.entries
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = state.status == option,
                        onClick = { viewModel.onStatusChange(option) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        label = { Text(option.displayName) },
                    )
                }
            }

            when (val content = state.content) {
                TaskListContent.Loading -> LoadingContent()
                TaskListContent.Error -> ErrorContent(onRetry = viewModel::onRetry)
                is TaskListContent.Loaded ->
                    if (content.tasks.isEmpty()) {
                        EmptyContent("No tasks")
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(content.tasks, key = { it.id }) { task ->
                                TaskRow(
                                    task = task,
                                    today = state.today,
                                    onToggle = { viewModel.onToggle(task.id) },
                                    onClick = { onOpenTask(task.id) },
                                )
                                HorizontalDivider(Modifier.padding(start = 64.dp))
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    today: LocalDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading circle toggle: toggles isDone without opening detail.
        IconButton(onClick = onToggle, modifier = Modifier.padding(horizontal = 4.dp)) {
            if (task.isDone) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Mark as active",
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Mark as done",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Due ${DateFormatting.format(task.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (task.isOverdue(today)) {
                    Text("Overdue", style = MaterialTheme.typography.bodySmall, color = TaskColors.Overdue)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        PriorityBadge(task.priority)
    }
}
