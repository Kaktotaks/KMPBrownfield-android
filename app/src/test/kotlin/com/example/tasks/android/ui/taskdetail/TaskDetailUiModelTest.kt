package com.example.tasks.android.ui.taskdetail

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.TaskDetailUiState
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskDetailUiModelTest {

    private val today = LocalDate(2026, 10, 29)

    @Test
    fun missingTaskIsNotFound() {
        assertEquals(TaskDetailUiModel.NotFound, TaskDetailUiState(task = null, today = today).toUiModel())
    }

    @Test
    fun activeOverdueTaskWithoutNotes() {
        val task = Task(7, "Pay rent", "", Priority.LOW, LocalDate(2026, 10, 1), isDone = false)

        assertEquals(
            TaskDetailUiModel.Loaded(
                title = "Pay rent",
                statusText = "Active",
                priority = Priority.LOW,
                dueText = "Due 1 Oct 2026",
                isOverdue = true,
                notesText = "No notes",
                hasNotes = false,
                toggleButtonText = "Mark as done",
            ),
            TaskDetailUiState(task, today).toUiModel(),
        )
    }

    @Test
    fun doneTaskWithNotes() {
        val task = Task(7, "Pay rent", "Bank transfer", Priority.LOW, LocalDate(2026, 10, 1), isDone = true)
        val model = TaskDetailUiState(task, today).toUiModel() as TaskDetailUiModel.Loaded

        assertEquals("Done", model.statusText)
        assertEquals(false, model.isOverdue)
        assertEquals("Bank transfer", model.notesText)
        assertEquals(true, model.hasNotes)
        assertEquals("Mark as active", model.toggleButtonText)
    }
}
