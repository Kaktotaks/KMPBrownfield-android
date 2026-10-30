package com.example.tasks.android.data

import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.Priority
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

// Domain rules are tested in ../shared (commonTest). Here: only the app-specific repository logic.
class TaskRepositoryTest {

    private val today = LocalDate(2026, 10, 29)
    private val fixedDate = DateProvider { today }

    private class FakeTaskApi(
        private val todos: List<TodoDto> = emptyList(),
        private val fail: Boolean = false,
    ) : TaskApi {
        override suspend fun fetchTodos(): List<TodoDto> {
            if (fail) throw IOException("offline")
            return todos
        }
    }

    private fun todo(id: Int, completed: Boolean = false) = TodoDto(userId = 1, id = id, title = "Todo $id", completed = completed)

    private fun loadedTasks(repo: TaskRepository) = (repo.state.value as LoadState.Loaded).tasks

    @Test
    fun load_mapsDtosWithSharedMapper() = runTest {
        val repo = TaskRepository(FakeTaskApi(listOf(todo(1), todo(2, completed = true))), fixedDate)
        repo.load()
        val tasks = loadedTasks(repo)
        assertEquals(listOf(1, 2), tasks.map { it.id })
        // id 1: priority [low, medium, high][1 % 3] = MEDIUM, due = today + (1 - 3) days
        assertEquals(Priority.MEDIUM, tasks[0].priority)
        assertEquals(LocalDate(2026, 10, 27), tasks[0].dueDate)
        assertTrue(tasks[1].isDone)
    }

    @Test
    fun load_failure_setsError() = runTest {
        val repo = TaskRepository(FakeTaskApi(fail = true), fixedDate)
        repo.load()
        assertEquals(LoadState.Error, repo.state.value)
    }

    @Test
    fun add_usesMaxIdPlusOne_andTrimsTitle() = runTest {
        val repo = TaskRepository(FakeTaskApi(listOf(todo(3), todo(7))), fixedDate)
        repo.load()
        val added = repo.add("  New task  ", "notes", Priority.HIGH, today)
        assertEquals(8, added.id)
        assertEquals("New task", added.title)
        assertFalse(added.isDone)
        assertEquals(added, repo.task(8))
    }

    @Test
    fun add_toEmptyList_startsAtOne() = runTest {
        val repo = TaskRepository(FakeTaskApi(emptyList()), fixedDate)
        repo.load()
        assertEquals(1, repo.add("First", "", Priority.LOW, today).id)
    }

    @Test
    fun toggle_update_delete() = runTest {
        val repo = TaskRepository(FakeTaskApi(listOf(todo(1), todo(2))), fixedDate)
        repo.load()
        repo.toggle(1)
        assertTrue(repo.task(1)!!.isDone)
        repo.update(repo.task(2)!!.copy(title = "  Renamed "))
        assertEquals("Renamed", repo.task(2)!!.title)
        repo.delete(1)
        assertNull(repo.task(1))
        assertEquals(listOf(2), loadedTasks(repo).map { it.id })
    }
}
