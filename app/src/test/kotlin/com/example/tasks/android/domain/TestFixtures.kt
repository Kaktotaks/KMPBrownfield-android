package com.example.tasks.android.domain

import java.time.LocalDate

val TODAY: LocalDate = LocalDate.of(2026, 10, 29)

val fixedClock = Clock { TODAY }

fun task(
    id: Int,
    title: String = "Task $id",
    priority: Priority = Priority.MEDIUM,
    dueDate: LocalDate = TODAY,
    isDone: Boolean = false,
    notes: String = "",
) = Task(id, title, notes, priority, dueDate, isDone)
