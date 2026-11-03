package com.example.tasks.android.ui.tasklist

import com.example.tasks.android.R
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.SortOrder
import com.example.tasks.shared.domain.StatusFilter
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskListContent
import com.example.tasks.shared.presentation.TaskListUiState
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskListUiModelTest {

    private val today = LocalDate(2026, 10, 29)

    private fun task(id: Int, due: LocalDate, isDone: Boolean = false) =
        Task(id, "Task $id", "", Priority.HIGH, due, isDone)

    private fun state(content: TaskListContent, status: StatusFilter = StatusFilter.ALL, sort: SortOrder = SortOrder.DUE_DATE) =
        TaskListUiState(query = "milk", status = status, sort = sort, today = today, content = content)

    @Test
    fun loadedTasksBecomeFormattedRows() {
        val model = state(
            TaskListContent.Loaded(
                listOf(
                    task(1, LocalDate(2026, 10, 28)),
                    task(2, LocalDate(2026, 10, 28), isDone = true),
                    task(3, today),
                ),
            ),
        ).toUiModel()

        val rows = (model.content as TaskListUiModel.Content.Loaded).rows
        assertEquals(listOf(1, 2, 3), rows.map { it.id })
        assertEquals("28 Oct 2026", rows[0].dueDate)
        assertEquals("29 Oct 2026", rows[2].dueDate)
        assertTrue("past due + active = overdue", rows[0].isOverdue)
        assertFalse("done tasks are never overdue", rows[1].isOverdue)
        assertFalse("due today is not overdue", rows[2].isOverdue)
        assertFalse(rows[0].isDone)
        assertTrue(rows[1].isDone)
        assertEquals(Priority.HIGH, rows[0].priority)
    }

    @Test
    fun emptyListWithQueryOrFilterIsNoMatch() {
        // `state()` has the query "milk": an active query means "No matching tasks".
        assertEquals(
            TaskListUiModel.Content.Empty(hasActiveFilter = true),
            state(TaskListContent.Loaded(emptyList())).toUiModel().content,
        )
        assertEquals(
            TaskListUiModel.Content.Empty(hasActiveFilter = true),
            state(TaskListContent.Loaded(emptyList()), status = StatusFilter.DONE).toUiModel().content,
        )
    }

    @Test
    fun emptyListWithoutQueryAndFilterIsNoTasksYet() {
        val noQuery = TaskListUiState("  ", StatusFilter.ALL, SortOrder.DUE_DATE, today, TaskListContent.Loaded(emptyList()))
        assertEquals(TaskListUiModel.Content.Empty(hasActiveFilter = false), noQuery.toUiModel().content)
    }

    @Test
    fun loadingAndErrorPassThrough() {
        assertEquals(TaskListUiModel.Content.Loading, state(TaskListContent.Loading).toUiModel().content)
        assertEquals(TaskListUiModel.Content.Error, state(TaskListContent.Error).toUiModel().content)
    }

    @Test
    fun optionsCarryLabelResourcesAndSelection() {
        val model = state(TaskListContent.Loading, status = StatusFilter.DONE, sort = SortOrder.PRIORITY).toUiModel()

        assertEquals("milk", model.query)
        assertEquals(
            listOf(R.string.filter_all, R.string.filter_active, R.string.filter_done),
            model.statusOptions.map { it.labelRes },
        )
        assertEquals(listOf(StatusFilter.DONE), model.statusOptions.filter { it.isSelected }.map { it.value })
        assertEquals(
            listOf(R.string.sort_due, R.string.sort_priority, R.string.sort_title),
            model.sortOptions.map { it.labelRes },
        )
        assertEquals(listOf(SortOrder.PRIORITY), model.sortOptions.filter { it.isSelected }.map { it.value })
        assertEquals(R.string.sort_priority, model.sortLabelRes)
    }
}
