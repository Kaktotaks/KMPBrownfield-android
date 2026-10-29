package com.example.tasks.android.data

import com.example.tasks.android.domain.Priority
import com.example.tasks.android.domain.Task
import kotlinx.serialization.Serializable
import java.time.LocalDate

// KMP-shared candidate: DTO + deterministic mapping, identical on iOS.
@Serializable
data class TodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean,
)

private val PRIORITY_BY_INDEX = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)

/** Mapping from the spec: priority = [low, medium, high][id % 3], dueDate = today + ((id % 14) - 3) days. */
fun TodoDto.toTask(today: LocalDate): Task = Task(
    id = id,
    title = title,
    notes = "",
    priority = PRIORITY_BY_INDEX[Math.floorMod(id, 3)],
    dueDate = today.plusDays((Math.floorMod(id, 14) - 3).toLong()),
    isDone = completed,
)
