package com.example.tasks.android.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.stats.StatsScreenRoute
import com.example.tasks.android.ui.taskdetail.TaskDetailScreenRoute
import com.example.tasks.android.ui.tasklist.TaskListScreenRoute
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import kotlin.reflect.KClass

private data class Tab(
    val labelRes: Int,
    val icon: ImageVector,
    val graph: Any,
    val graphClass: KClass<*>,
)

private val tabs = listOf(
    Tab(R.string.tasks_title, TasksIcons.ListChecks, TasksGraph, TasksGraph::class),
    Tab(R.string.stats_title, TasksIcons.BarChart, StatsGraph, StatsGraph::class),
)

/** Root: Scaffold + bottom NavigationBar (2 tabs) + NavHost. The editor is a sheet hosted by its screens. */
@Composable
fun TasksRoot() {
    val colors = TasksTheme.colors
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = colors.bg,
        // Each screen draws its own status-bar inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = colors.border)
                // M3 NavigationBar 80 dp: indicator = accent, selected icon = onAccent (§0).
                NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
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
                            label = { Text(stringResource(tab.labelRes), style = TasksType.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.onAccent,
                                selectedTextColor = colors.textPrimary,
                                indicatorColor = colors.accent,
                                unselectedIconColor = colors.textTertiary,
                                unselectedTextColor = colors.textTertiary,
                            ),
                        )
                    }
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
                    TaskListScreenRoute(onOpenTask = { id -> navController.navigate(TaskDetailRoute(id)) })
                }
                composable<TaskDetailRoute> {
                    TaskDetailScreenRoute(onBack = { navController.popBackStack() })
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
