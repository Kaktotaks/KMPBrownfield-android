package com.example.tasks.android.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasks.android.ui.components.StatsViewModelFactory
import com.example.tasks.shared.presentation.StatsViewModel

/** Stateful entry point of the Stats tab: collects the shared [StatsViewModel] and maps to [StatsUiModel]. */
@Composable
fun StatsScreenRoute(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModelFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toUiModel() }

    StatsScreen(model = model, onRetry = viewModel::onRetry)
}
