package com.example.tasks.android

import android.app.Application
import com.example.tasks.shared.data.KtorTaskApi
import com.example.tasks.shared.data.TaskApi
import com.example.tasks.shared.data.TaskRepository
import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.SystemDateProvider
import com.example.tasks.shared.domain.TaskValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Simple manual DI container: one instance of each dependency for the whole app. */
class AppContainer {
    val dateProvider: DateProvider = SystemDateProvider
    val taskApi: TaskApi = KtorTaskApi() // shared Ktor client (OkHttp engine on Android)
    val taskRepository: TaskRepository = TaskRepository(taskApi, dateProvider)
    val taskValidator: TaskValidator = TaskValidator(dateProvider)
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
