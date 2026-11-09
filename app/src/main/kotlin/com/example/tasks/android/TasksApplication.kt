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
import com.example.tasks.shared.domain.TaskValidator
import com.example.tasks.shared.geocoding.Geocoder
import com.example.tasks.shared.geocoding.NominatimGeocoder
import com.example.tasks.shared.routing.OsrmRoutingApi
import com.example.tasks.shared.routing.RoutingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/** Simple manual DI container: one instance of each dependency for the whole app. */
class AppContainer(context: Context) {
    /** "Now" for everything: the system clock, except when the debug screenshot hook freezes it. */
    val clock = AppClock()
    val dateProvider: DateProvider = clock
    val taskApi: TaskApi = KtorTaskApi() // shared Ktor client (OkHttp engine on Android)

    // One repository per process (offline-first, SQLDelight "tasks.db"; voice files in filesDir/voice_notes).
    val taskRepository: TaskRepository =
        TasksAndroid.createRepository(context, api = taskApi, dateProvider = dateProvider)

    /** THE VoiceNoteFiles instance of the repository; the voice-note VMs must use this one. */
    val voiceFiles: VoiceNoteFiles get() = taskRepository.voiceFiles

    /** Recorder / player / microphone-permission adapters (real ones; fakes in debug builds only). */
    val voiceAdapters = VoiceAdapterFactory(context)

    val taskValidator: TaskValidator = TaskValidator(dateProvider)

    /** OSRM host of the Map tab. Only the debug hook overrides it (an invalid host forces the "route unavailable" state). */
    @Volatile
    var osrmBaseUrl: String = OsrmRoutingApi.DEFAULT_BASE_URL

    fun createRouting(): RoutingApi = OsrmRoutingApi(osrmBaseUrl)

    /** Nominatim reverse geocoding in the app language (search takes its own language argument). */
    fun createGeocoder(language: String): Geocoder = NominatimGeocoder(language)
}

class TasksApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    private val loadStarted = AtomicBoolean(false)

    /**
     * Spec: tasks are loaded once on app start by TaskRepository. MainActivity calls this right after the debug hooks
     * ran (so a frozen clock is in place before the first seed and the demo route is dated from it); idempotent.
     */
    fun startLoading() {
        if (loadStarted.compareAndSet(false, true)) appScope.launch { container.taskRepository.load() }
    }
}
