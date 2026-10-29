package com.example.tasks.android.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsCalculatorTest {

    @Test
    fun emptyListGivesZeros() {
        assertEquals(TaskStats.EMPTY, StatsCalculator.calculate(emptyList(), fixedClock.today()))
    }

    @Test
    fun countsAndOverdue() {
        val tasks = listOf(
            task(1, priority = Priority.LOW, dueDate = TODAY.minusDays(1)), // overdue
            task(2, priority = Priority.HIGH, dueDate = TODAY.minusDays(3), isDone = true), // done, not overdue
            task(3, priority = Priority.HIGH, dueDate = TODAY), // due today, not overdue
            task(4, priority = Priority.MEDIUM, dueDate = TODAY.plusDays(4), isDone = true),
        )
        val stats = StatsCalculator.calculate(tasks, fixedClock.today())
        assertEquals(4, stats.total)
        assertEquals(2, stats.done)
        assertEquals(2, stats.active)
        assertEquals(50, stats.completionPercent)
        assertEquals(1, stats.low)
        assertEquals(1, stats.medium)
        assertEquals(2, stats.high)
        assertEquals(1, stats.overdue)
    }

    @Test
    fun completionPercentRoundsHalfUp() {
        // 1/3 = 33.33 -> 33
        assertEquals(33, percent(done = 1, total = 3))
        // 2/3 = 66.67 -> 67
        assertEquals(67, percent(done = 2, total = 3))
        // 1/8 = 12.5 -> 13 (half up)
        assertEquals(13, percent(done = 1, total = 8))
        // 3/8 = 37.5 -> 38
        assertEquals(38, percent(done = 3, total = 8))
        assertEquals(100, percent(done = 5, total = 5))
    }

    private fun percent(done: Int, total: Int): Int {
        val tasks = (1..total).map { task(it, isDone = it <= done, dueDate = TODAY.plusDays(1)) }
        return StatsCalculator.calculate(tasks, fixedClock.today()).completionPercent
    }
}
