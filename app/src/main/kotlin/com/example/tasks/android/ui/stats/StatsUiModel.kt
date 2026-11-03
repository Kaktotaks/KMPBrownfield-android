package com.example.tasks.android.ui.stats

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.TaskStats
import com.example.tasks.shared.presentation.StatsUiState

/** Everything [StatsScreen] draws, already mapped. Plain Kotlin: no Compose, unit-tested on the JVM. */
sealed interface StatsUiModel {
    data object Loading : StatsUiModel
    data object Error : StatsUiModel

    data class Loaded(
        /** `completionPercent` from shared, untouched: the animation must land exactly on it. */
        val completionPercent: Int,
        val total: Int,
        val done: Int,
        val active: Int,
        val overdue: Int,
        /** "By priority": High, Medium, Low. */
        val priorityRows: List<PriorityRowUi>,
    ) : StatsUiModel
}

/** One PriorityBar: [fraction] = count / total (0 when there are no tasks). */
data class PriorityRowUi(val priority: Priority, val count: Int, val fraction: Float)

/** Shared VM state -> UI model. Pure function (called from [StatsScreenRoute] inside `remember(state)`). */
fun StatsUiState.toUiModel(): StatsUiModel = when (this) {
    StatsUiState.Loading -> StatsUiModel.Loading
    StatsUiState.Error -> StatsUiModel.Error
    is StatsUiState.Loaded -> stats.toUiModel()
}

fun TaskStats.toUiModel(): StatsUiModel.Loaded {
    fun row(priority: Priority, count: Int) =
        PriorityRowUi(priority, count, if (total == 0) 0f else count.toFloat() / total)
    return StatsUiModel.Loaded(
        completionPercent = completionPercent,
        total = total,
        done = done,
        active = active,
        overdue = overdue,
        priorityRows = listOf(row(Priority.HIGH, high), row(Priority.MEDIUM, medium), row(Priority.LOW, low)),
    )
}
