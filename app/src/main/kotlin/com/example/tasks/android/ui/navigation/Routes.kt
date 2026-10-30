package com.example.tasks.android.ui.navigation

import com.example.tasks.shared.presentation.TaskEditorViewModel
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
        /** Forwarded from the shared VM so the route and the ViewModel agree on the sentinel. */
        const val NEW_TASK_ID = TaskEditorViewModel.NEW_TASK_ID
    }
}

@Serializable
data object StatsRoute
