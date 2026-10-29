package com.example.tasks.android.domain

import java.time.LocalDate

// KMP-shared candidate. java.time is JVM-only: in KMP commonMain this becomes kotlinx-datetime LocalDate.
data class Task(
    val id: Int,
    val title: String,
    val notes: String,
    val priority: Priority,
    val dueDate: LocalDate,
    val isDone: Boolean,
) {
    /** Overdue: not done and due date strictly before today. */
    fun isOverdue(today: LocalDate): Boolean = !isDone && dueDate < today
}
