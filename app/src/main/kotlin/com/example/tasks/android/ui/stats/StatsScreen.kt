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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.ErrorContent
import com.example.tasks.android.ui.components.LoadingContent
import com.example.tasks.android.ui.components.StatsViewModelFactory
import com.example.tasks.shared.domain.TaskStats
import com.example.tasks.shared.presentation.StatsUiState
import com.example.tasks.shared.presentation.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Stats") }) }) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (val s = state) {
            StatsUiState.Loading -> LoadingContent(contentModifier)
            StatsUiState.Error -> ErrorContent(onRetry = viewModel::onRetry, modifier = contentModifier)
            is StatsUiState.Loaded -> StatsContent(s.stats, contentModifier)
        }
    }
}

@Composable
private fun StatsContent(stats: TaskStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Completed", style = MaterialTheme.typography.titleMedium)
        Text(
            "${stats.completionPercent}%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        LinearProgressIndicator(
            progress = { stats.completionPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        StatRow("Total", stats.total)
        StatRow("Done", stats.done)
        StatRow("Active", stats.active)
        StatRow("Overdue", stats.overdue)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("By priority", style = MaterialTheme.typography.titleMedium)
        StatRow("High", stats.high)
        StatRow("Medium", stats.medium)
        StatRow("Low", stats.low)
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value.toString(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
