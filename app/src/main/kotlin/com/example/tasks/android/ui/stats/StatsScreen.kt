package com.example.tasks.android.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.components.ErrorContent
import com.example.tasks.android.ui.components.LoadingContent
import com.example.tasks.android.ui.theme.TasksTheme

/** Stateless Stats tab: layout only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(model: StatsUiModel, onRetry: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Stats") }) }) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (model) {
            StatsUiModel.Loading -> LoadingContent(contentModifier)
            StatsUiModel.Error -> ErrorContent(onRetry = onRetry, modifier = contentModifier)
            is StatsUiModel.Loaded -> StatsContent(model, contentModifier)
        }
    }
}

@Composable
private fun StatsContent(model: StatsUiModel.Loaded, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Completed", style = MaterialTheme.typography.titleMedium)
        Text(
            model.completionText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        LinearProgressIndicator(
            progress = { model.progress },
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        model.summaryRows.forEach { StatRow(it) }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("By priority", style = MaterialTheme.typography.titleMedium)
        model.priorityRows.forEach { StatRow(it) }
    }
}

@Composable
private fun StatRow(row: StatRowUi) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(row.label, style = MaterialTheme.typography.bodyLarge)
        Text(row.value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    TasksTheme {
        StatsScreen(
            model = StatsUiModel.Loaded(
                completionText = "40%",
                progress = 0.4f,
                summaryRows = listOf(
                    StatRowUi("Total", "20"), StatRowUi("Done", "8"),
                    StatRowUi("Active", "12"), StatRowUi("Overdue", "3"),
                ),
                priorityRows = listOf(StatRowUi("High", "5"), StatRowUi("Medium", "9"), StatRowUi("Low", "6")),
            ),
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenErrorPreview() {
    TasksTheme {
        StatsScreen(model = StatsUiModel.Error, onRetry = {})
    }
}

// endregion
