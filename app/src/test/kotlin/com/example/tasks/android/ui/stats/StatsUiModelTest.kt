package com.example.tasks.android.ui.stats

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.TaskStats
import com.example.tasks.shared.presentation.StatsUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsUiModelTest {

    @Test
    fun loadingAndErrorPassThrough() {
        assertEquals(StatsUiModel.Loading, StatsUiState.Loading.toUiModel())
        assertEquals(StatsUiModel.Error, StatsUiState.Error.toUiModel())
    }

    @Test
    fun loadedStatsKeepSharedNumbersAndBarFractionsAreCountOverTotal() {
        val stats = TaskStats(total = 4, done = 3, active = 1, completionPercent = 75, low = 1, medium = 2, high = 1, overdue = 1)
        val model = StatsUiState.Loaded(stats).toUiModel() as StatsUiModel.Loaded

        // The animation must land exactly on shared's completionPercent: it is passed through untouched.
        assertEquals(75, model.completionPercent)
        assertEquals(listOf(4, 3, 1, 1), listOf(model.total, model.done, model.active, model.overdue))
        assertEquals(listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW), model.priorityRows.map { it.priority })
        assertEquals(listOf(1, 2, 1), model.priorityRows.map { it.count })
        assertEquals(listOf(0.25f, 0.5f, 0.25f), model.priorityRows.map { it.fraction })
    }

    @Test
    fun zeroTasksHaveEmptyBarsWithoutDivisionByZero() {
        val model = StatsUiState.Loaded(TaskStats.EMPTY).toUiModel() as StatsUiModel.Loaded

        assertEquals(0, model.completionPercent)
        assertEquals(listOf(0f, 0f, 0f), model.priorityRows.map { it.fraction })
    }
}
