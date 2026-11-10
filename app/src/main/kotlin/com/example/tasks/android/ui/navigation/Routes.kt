package com.example.tasks.android.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes.

/** Tab graph: Tasks (list -> detail). */
@Serializable
data object TasksGraph

/** Tab graph: Map (the selected day's route). */
@Serializable
data object MapGraph

/** Tab graph: Stats. */
@Serializable
data object StatsGraph

@Serializable
data object TaskListRoute

@Serializable
data class TaskDetailRoute(val taskId: Int)

@Serializable
data object MapRoute

@Serializable
data object StatsRoute
