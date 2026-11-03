package com.example.tasks.android.ui.taskdetail

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskDetailUiState
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskDetailUiModelTest {

    private val today = LocalDate(2026, 10, 29)

    @Test
    fun missingTaskIsNotFound() {
        assertEquals(TaskDetailUiModel.NotFound, TaskDetailUiState(task = null, today = today).toUiModel())
    }

    @Test
    fun activeOverdueTaskWithoutNotesHidesTheNotesCard() {
        val task = Task(7, "Pay rent", "", Priority.LOW, LocalDate(2026, 10, 1), isDone = false)

        assertEquals(
            TaskDetailUiModel.Loaded(
                title = "Pay rent",
                isDone = false,
                priority = Priority.LOW,
                dueDate = "1 Oct 2026",
                isOverdue = true,
                notes = null,
            ),
            TaskDetailUiState(task, today).toUiModel(),
        )
    }

    @Test
    fun blankNotesCountAsNoNotes() {
        val task = Task(7, "Pay rent", "  \n ", Priority.LOW, LocalDate(2026, 10, 1), isDone = false)
        assertNull((TaskDetailUiState(task, today).toUiModel() as TaskDetailUiModel.Loaded).notes)
    }

    @Test
    fun doneTaskWithNotes() {
        val task = Task(7, "Pay rent", "Bank transfer", Priority.LOW, LocalDate(2026, 10, 1), isDone = true)
        val model = TaskDetailUiState(task, today).toUiModel() as TaskDetailUiModel.Loaded

        assertEquals(true, model.isDone)
        assertEquals(false, model.isOverdue)
        assertEquals("Bank transfer", model.notes)
    }
}
