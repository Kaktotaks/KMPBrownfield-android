package com.example.tasks.android.ui.stats

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
    fun loadedStatsBecomeRowsInSpecOrder() {
        val stats = TaskStats(total = 4, done = 3, active = 1, completionPercent = 75, low = 1, medium = 2, high = 1, overdue = 1)
        val model = StatsUiState.Loaded(stats).toUiModel() as StatsUiModel.Loaded

        assertEquals("75%", model.completionText)
        assertEquals(0.75f, model.progress, 0.0001f)
        assertEquals(
            listOf(StatRowUi("Total", "4"), StatRowUi("Done", "3"), StatRowUi("Active", "1"), StatRowUi("Overdue", "1")),
            model.summaryRows,
        )
        assertEquals(
            listOf(StatRowUi("High", "1"), StatRowUi("Medium", "2"), StatRowUi("Low", "1")),
            model.priorityRows,
        )
    }
}
