package com.example.tasks.android.data

import kotlinx.serialization.Serializable

// JSON shape of jsonplaceholder /todos. Parsing stays in the app; the mapping rule
// (priority / dueDate from id) now lives in the shared KMP module: TodoMapper.map(...).
@Serializable
data class TodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean,
)
