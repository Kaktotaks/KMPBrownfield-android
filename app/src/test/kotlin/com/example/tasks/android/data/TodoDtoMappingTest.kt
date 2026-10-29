package com.example.tasks.android.data

import com.example.tasks.android.domain.Priority
import com.example.tasks.android.domain.TODAY
import org.junit.Assert.assertEquals
import org.junit.Test

class TodoDtoMappingTest {

    @Test
    fun mappingFollowsSpec() {
        val task = TodoDto(userId = 1, id = 1, title = "delectus", completed = true).toTask(TODAY)
        assertEquals(1, task.id)
        assertEquals("delectus", task.title)
        assertEquals("", task.notes)
        assertEquals(true, task.isDone)
        assertEquals(Priority.MEDIUM, task.priority) // [low, medium, high][1 % 3]
        assertEquals(TODAY.minusDays(2), task.dueDate) // (1 % 14) - 3 = -2
    }

    @Test
    fun priorityAndDueDateCycle() {
        val t3 = TodoDto(1, 3, "x", false).toTask(TODAY)
        assertEquals(Priority.LOW, t3.priority)
        assertEquals(TODAY, t3.dueDate)
        val t14 = TodoDto(1, 14, "x", false).toTask(TODAY)
        assertEquals(Priority.HIGH, t14.priority)
        assertEquals(TODAY.minusDays(3), t14.dueDate)
    }
}
