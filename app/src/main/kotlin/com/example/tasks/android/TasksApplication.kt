package com.example.tasks.android

import android.app.Application
import android.content.Context
import com.example.tasks.android.voice.VoiceAdapterFactory
import com.example.tasks.shared.TasksAndroid
import com.example.tasks.shared.data.KtorTaskApi
import com.example.tasks.shared.data.TaskApi
import com.example.tasks.shared.data.TaskRepository
import com.example.tasks.shared.data.VoiceNoteFiles
import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.SystemDateProvider
import com.example.tasks.shared.domain.TaskValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Simple manual DI container: one instance of each dependency for the whole app. */
class AppContainer(context: Context) {
    val dateProvider: DateProvider = SystemDateProvider
    val taskApi: TaskApi = KtorTaskApi() // shared Ktor client (OkHttp engine on Android)

    // One repository per process (offline-first, SQLDelight "tasks.db"; voice files in filesDir/voice_notes).
    val taskRepository: TaskRepository =
        TasksAndroid.createRepository(context, api = taskApi, dateProvider = dateProvider)

    /** THE VoiceNoteFiles instance of the repository; the voice-note VMs must use this one. */
    val voiceFiles: VoiceNoteFiles get() = taskRepository.voiceFiles

    /** Recorder / player / microphone-permission adapters (real ones; fakes in debug builds only). */
    val voiceAdapters = VoiceAdapterFactory(context)

    val taskValidator: TaskValidator = TaskValidator(dateProvider)
}

class TasksApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Spec: tasks are loaded once on app start by TaskRepository.
        appScope.launch { container.taskRepository.load() }
    }
}
