package com.example.tasks.android.ui.tasklist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.EmptyState
import com.example.tasks.android.ui.components.EmptyStateKind
import com.example.tasks.android.ui.components.FloatingAddButton
import com.example.tasks.android.ui.components.PillSearchField
import com.example.tasks.android.ui.components.PillSegmentedControl
import com.example.tasks.android.ui.components.PrimaryButton
import com.example.tasks.android.ui.components.SecondaryButton
import com.example.tasks.android.ui.components.SelectableOption
import com.example.tasks.android.ui.components.SkeletonRow
import com.example.tasks.android.ui.components.SortPill
import com.example.tasks.android.ui.components.TaskCard
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter

/** Bottom inset so the last card clears the FloatingAddButton: 16 + 56 + 16 (§8). */
private val ListBottomInset = 88.dp

/** Stateless Tasks list (§3.1): layout only. All data comes mapped in [model]; all actions go out via lambdas. */
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
    val colors = TasksTheme.colors
    val isLoading = model.content == TaskListUiModel.Content.Loading
    Box(Modifier.fillMaxSize().background(colors.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                stringResource(R.string.tasks_title),
                style = TasksType.largeTitle,
                color = colors.textPrimary,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp)
                    .semantics { heading() },
            )
            PillSearchField(
                value = model.query,
                onValueChange = onQueryChange,
                enabled = !isLoading,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillSegmentedControl(
                    options = model.statusOptions,
                    onSelect = onStatusChange,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                )
                SortPill(
                    label = stringResource(model.sortLabelRes),
                    options = model.sortOptions,
                    onSelect = onSortChange,
                    enabled = !isLoading,
                )
            }
            when (val content = model.content) {
                TaskListUiModel.Content.Loading -> LoadingRows()
                TaskListUiModel.Content.Error -> EmptyState(
                    title = stringResource(R.string.error_title),
                    body = stringResource(R.string.error_body),
                    kind = EmptyStateKind.Error,
                    action = { SecondaryButton(stringResource(R.string.retry), onRetry, icon = TasksIcons.Refresh) },
                )
                is TaskListUiModel.Content.Empty ->
                    if (content.hasActiveFilter) {
                        EmptyState(stringResource(R.string.no_match_title), stringResource(R.string.no_match_body))
                    } else {
                        EmptyState(
                            title = stringResource(R.string.empty_title),
                            body = stringResource(R.string.empty_body),
                            action = { PrimaryButton(stringResource(R.string.add_task), onNewTask, icon = TasksIcons.Plus) },
                        )
                    }
                is TaskListUiModel.Content.Loaded -> TaskRows(content.rows, onToggle, onOpenTask)
            }
        }
        FloatingAddButton(
            onClick = onNewTask,
            contentDescription = stringResource(R.string.add_task),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

/** Loading (§3.1): 4 skeleton rows, then a 16 dp spinner + "Loading tasks…". */
@Composable
private fun LoadingRows() {
    val colors = TasksTheme.colors
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SkeletonRow(titleFraction = 0.8f, dueFraction = 0.45f)
        SkeletonRow(titleFraction = 0.65f, dueFraction = 0.4f)
        SkeletonRow(titleFraction = 0.75f, dueFraction = 0.5f)
        SkeletonRow(titleFraction = 0.7f, dueFraction = 0.45f)
        Row(
            Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(Modifier.size(16.dp), color = colors.textSecondary, strokeWidth = 2.dp)
            Text(stringResource(R.string.loading), style = TasksType.subhead, color = colors.textSecondary)
        }
    }
}

@Composable
private fun TaskRows(rows: List<TaskRowUi>, onToggle: (Int) -> Unit, onOpenTask: (Int) -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = ListBottomInset),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rows, key = { it.id }) { row ->
            TaskCard(
                title = row.title,
                dueDate = row.dueDate,
                priority = row.priority,
                isDone = row.isDone,
                isOverdue = row.isOverdue,
                onClick = { onOpenTask(row.id) },
                onToggle = { onToggle(row.id) },
                // M4: items fade in/out and move, 250 ms emphasized (snaps under reduced motion).
                modifier = if (reduceMotion) {
                    Modifier.animateItem(fadeInSpec = null, placementSpec = null, fadeOutSpec = null)
                } else {
                    Modifier.animateItem(
                        fadeInSpec = tween(Motion.medium, easing = Motion.emphasized),
                        placementSpec = tween(Motion.medium, easing = Motion.emphasized),
                        fadeOutSpec = tween(Motion.medium, easing = Motion.emphasized),
                    )
                },
            )
        }
    }
}

// region Previews

private fun previewModel(content: TaskListUiModel.Content, query: String = "") = TaskListUiModel(
    query = query,
    statusOptions = StatusFilter.entries.map { SelectableOption(it, it == StatusFilter.ALL, labelRes = it.labelRes()) },
    sortOptions = SortOrder.entries.map { SelectableOption(it, it == SortOrder.DUE_DATE, labelRes = it.labelRes()) },
    sortLabelRes = SortOrder.DUE_DATE.labelRes(),
    content = content,
)

private val previewRows = TaskListUiModel.Content.Loaded(
    listOf(
        TaskRowUi(1, "Renew passport photos", "31 Oct 2026", isOverdue = true, isDone = false, priority = Priority.HIGH),
        TaskRowUi(2, "Pay electricity bill", "1 Nov 2026", isOverdue = false, isDone = true, priority = Priority.LOW),
        TaskRowUi(3, "Review pull request for offline sync", "2 Nov 2026", isOverdue = false, isDone = true, priority = Priority.MEDIUM),
        TaskRowUi(4, "Call the dentist to reschedule", "4 Nov 2026", isOverdue = false, isDone = false, priority = Priority.MEDIUM),
        TaskRowUi(5, "Prepare sprint demo slides", "5 Nov 2026", isOverdue = false, isDone = false, priority = Priority.HIGH),
    ),
)

@Composable
private fun PreviewScreen(content: TaskListUiModel.Content, dark: Boolean, query: String = "") {
    TasksTheme(darkTheme = dark) {
        TaskListScreen(
            model = previewModel(content, query),
            onQueryChange = {}, onStatusChange = {}, onSortChange = {}, onToggle = {},
            onRetry = {}, onOpenTask = {}, onNewTask = {},
        )
    }
}

@Preview(name = "List light", showBackground = true, heightDp = 800)
@Composable
private fun TaskListLightPreview() = PreviewScreen(previewRows, dark = false)

@Preview(name = "List dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskListDarkPreview() = PreviewScreen(previewRows, dark = true)

@Preview(name = "Loading light", showBackground = true, heightDp = 800)
@Composable
private fun TaskListLoadingLightPreview() = PreviewScreen(TaskListUiModel.Content.Loading, dark = false)

@Preview(name = "Loading dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskListLoadingDarkPreview() = PreviewScreen(TaskListUiModel.Content.Loading, dark = true)

@Preview(name = "Empty light", showBackground = true, heightDp = 800)
@Composable
private fun TaskListEmptyLightPreview() = PreviewScreen(TaskListUiModel.Content.Empty(hasActiveFilter = false), dark = false)

@Preview(name = "Empty dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskListEmptyDarkPreview() = PreviewScreen(TaskListUiModel.Content.Empty(hasActiveFilter = false), dark = true)

@Preview(name = "No match light", showBackground = true, heightDp = 800)
@Composable
private fun TaskListNoMatchLightPreview() = PreviewScreen(TaskListUiModel.Content.Empty(hasActiveFilter = true), dark = false, query = "zzz")

@Preview(name = "Error light", showBackground = true, heightDp = 800)
@Composable
private fun TaskListErrorLightPreview() = PreviewScreen(TaskListUiModel.Content.Error, dark = false)

@Preview(name = "Error dark", showBackground = true, heightDp = 800)
@Composable
private fun TaskListErrorDarkPreview() = PreviewScreen(TaskListUiModel.Content.Error, dark = true)

// endregion
