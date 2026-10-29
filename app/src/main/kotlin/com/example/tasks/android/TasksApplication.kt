package com.example.tasks.android

import android.app.Application
import com.example.tasks.android.data.HttpTaskApi
import com.example.tasks.android.data.TaskApi
import com.example.tasks.android.data.TaskRepository
import com.example.tasks.android.domain.Clock
import com.example.tasks.android.domain.SystemClock
import com.example.tasks.android.domain.TaskValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Simple manual DI container: one instance of each dependency for the whole app. */
class AppContainer {
    val clock: Clock = SystemClock
    val taskApi: TaskApi = HttpTaskApi()
    val taskRepository: TaskRepository = TaskRepository(taskApi, clock)
    val taskValidator: TaskValidator = TaskValidator(clock)
}

class TasksApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
        // Spec: tasks are loaded once on app start by TaskRepository.
        appScope.launch { container.taskRepository.load() }
    }
}
