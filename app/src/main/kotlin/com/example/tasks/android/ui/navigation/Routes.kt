package com.example.tasks.android.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes.

/** Tab graph: Tasks (list -> detail). */
@Serializable
data object TasksGraph

/** Tab graph: Stats. */
@Serializable
data object StatsGraph

@Serializable
data object TaskListRoute

@Serializable
data class TaskDetailRoute(val taskId: Int)

/** Editor "sheet". [taskId] == [NEW_TASK_ID] means "New Task", otherwise "Edit Task". */
@Serializable
data class TaskEditorRoute(val taskId: Int = NEW_TASK_ID) {
    companion object {
        const val NEW_TASK_ID = -1
    }
}

@Serializable
data object StatsRoute
