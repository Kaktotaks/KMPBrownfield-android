package com.example.tasks.android.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.tasks.android.ui.stats.StatsScreenRoute
import com.example.tasks.android.ui.taskdetail.TaskDetailScreenRoute
import com.example.tasks.android.ui.taskeditor.TaskEditorScreenRoute
import com.example.tasks.android.ui.tasklist.TaskListScreenRoute
import kotlin.reflect.KClass

private data class Tab(
    val label: String,
    val icon: ImageVector,
    val graph: Any,
    val graphClass: KClass<*>,
)

private val tabs = listOf(
    Tab("Tasks", Icons.AutoMirrored.Filled.List, TasksGraph, TasksGraph::class),
    Tab("Stats", Icons.Filled.BarChart, StatsGraph, StatsGraph::class),
)

/** Root: Scaffold + bottom NavigationBar (2 tabs) + NavHost. */
@Composable
fun TasksRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        // Each screen draws its own TopAppBar (which handles the status bar inset).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.graphClass) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.graph) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        // Destinations host the stateful *ScreenRoute composables (shared VM -> UI model -> stateless Screen).
        NavHost(
            navController = navController,
            startDestination = TasksGraph,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            navigation<TasksGraph>(startDestination = TaskListRoute) {
                composable<TaskListRoute> {
                    TaskListScreenRoute(
                        onOpenTask = { id -> navController.navigate(TaskDetailRoute(id)) },
                        onNewTask = { navController.navigate(TaskEditorRoute()) },
                    )
                }
                composable<TaskDetailRoute> {
                    TaskDetailScreenRoute(
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(TaskEditorRoute(id)) },
                    )
                }
                // Closest match to an iOS modal sheet: a full-screen dialog destination
                // with "Cancel" / "Save" in its top app bar.
                dialog<TaskEditorRoute>(
                    dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    TaskEditorScreenRoute(onDismiss = { navController.popBackStack() })
                }
            }
            navigation<StatsGraph>(startDestination = StatsRoute) {
                composable<StatsRoute> {
                    StatsScreenRoute()
                }
            }
        }
    }
}
