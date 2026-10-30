package com.example.tasks.android.ui.stats

import com.example.tasks.shared.domain.TaskStats
import com.example.tasks.shared.presentation.StatsUiState

/** Everything [StatsScreen] draws, already formatted. Plain Kotlin: no Compose, unit-tested on the JVM. */
sealed interface StatsUiModel {
    data object Loading : StatsUiModel
    data object Error : StatsUiModel

    data class Loaded(
        /** "75%". */
        val completionText: String,
        /** 0f..1f for the progress bar. */
        val progress: Float,
        /** Total / Done / Active / Overdue. */
        val summaryRows: List<StatRowUi>,
        /** "By priority": High / Medium / Low. */
        val priorityRows: List<StatRowUi>,
    ) : StatsUiModel
}

data class StatRowUi(val label: String, val value: String)

/** Shared VM state -> UI model. Pure function (called from [StatsScreenRoute] inside `remember(state)`). */
fun StatsUiState.toUiModel(): StatsUiModel = when (this) {
    StatsUiState.Loading -> StatsUiModel.Loading
    StatsUiState.Error -> StatsUiModel.Error
    is StatsUiState.Loaded -> stats.toUiModel()
}

fun TaskStats.toUiModel(): StatsUiModel.Loaded = StatsUiModel.Loaded(
    completionText = "$completionPercent%",
    progress = completionPercent / 100f,
    summaryRows = listOf(
        StatRowUi("Total", total.toString()),
        StatRowUi("Done", done.toString()),
        StatRowUi("Active", active.toString()),
        StatRowUi("Overdue", overdue.toString()),
    ),
    priorityRows = listOf(
        StatRowUi("High", high.toString()),
        StatRowUi("Medium", medium.toString()),
        StatRowUi("Low", low.toString()),
    ),
)
