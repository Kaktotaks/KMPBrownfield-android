package com.example.tasks.android.ui.components

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.tasks.android.AppContainer
import com.example.tasks.android.TasksApplication
import com.example.tasks.android.ui.navigation.TaskDetailRoute
import com.example.tasks.shared.presentation.MapViewModel
import com.example.tasks.shared.presentation.PlaceSearchViewModel
import com.example.tasks.shared.presentation.StatsViewModel
import com.example.tasks.shared.presentation.TaskDetailViewModel
import com.example.tasks.shared.presentation.TaskListViewModel
import com.example.tasks.shared.presentation.VoiceNoteTarget
import com.example.tasks.shared.presentation.VoiceNoteViewModel
import com.example.tasks.android.voice.VoiceTargets
import com.example.tasks.shared.domain.VoiceNote

// ViewModel logic and UiState types live in `shared` (com.example.tasks.shared.presentation).
// On Android they are ordinary androidx ViewModels: ViewModelProvider creates them, they survive
// configuration changes and viewModelScope is cancelled in onCleared(). Only these factories are
// Android-specific: they pull dependencies from the manual DI container (AppContainer) and read
// navigation arguments from the SavedStateHandle that Navigation Compose puts into CreationExtras.

/** Pulls the manual DI container out of CreationExtras inside `viewModelFactory { initializer { ... } }`. */
fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as TasksApplication).container

val TaskListViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = appContainer()
        TaskListViewModel(container.taskRepository, container.dateProvider)
    }
}

/** The shared VM takes a plain `taskId`; the route argument is decoded here, not inside the VM. */
val TaskDetailViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = appContainer()
        TaskDetailViewModel(
            taskId = createSavedStateHandle().toRoute<TaskDetailRoute>().taskId,
            repository = container.taskRepository,
            dateProvider = container.dateProvider,
        )
    }
}

val StatsViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = appContainer()
        StatsViewModel(container.taskRepository, container.dateProvider)
    }
}

/** App language for Nominatim's `accept-language`: Ukrainian or English (the two supported languages). */
fun appLanguage(): String = if (java.util.Locale.getDefault().language == "uk") "uk" else "en"

/** The Map tab VM: shared routing / geocoding, and the app clock (frozen only by the debug screenshot hook). */
val MapViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = appContainer()
        MapViewModel(container.taskRepository, container.createRouting(), container.createGeocoder(appLanguage()), container.clock)
    }
}

/** Address search of the editor's Place row. */
fun placeSearchViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { PlaceSearchViewModel(container.createGeocoder(appLanguage()), appLanguage()) }
}

/**
 * The voice-note VM gets its own recorder/player (Detail and the editor sheet can be alive at once), the one shared
 * microphone-permission provider, and `files` = the repository's own VoiceNoteFiles (API contract §5).
 */
fun AppContainer.createVoiceNoteViewModel(target: VoiceNoteTarget): VoiceNoteViewModel = VoiceNoteViewModel(
    target = target,
    repository = taskRepository,
    files = voiceFiles,
    recorder = voiceAdapters.newRecorder(),
    player = voiceAdapters.newPlayer(),
    permission = voiceAdapters.permissionProvider(),
)

/** Detail: the target comes from the route (`ExistingTask(taskId)`); scoped to the Detail back-stack entry. */
val VoiceNoteDetailViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val taskId = createSavedStateHandle().toRoute<TaskDetailRoute>().taskId
        appContainer().createVoiceNoteViewModel(VoiceTargets.forTask(taskId))
    }
}

/** Editor sheet: a `Draft(initial)` VM owned by the sheet, bound to the editor VM by the caller (`bindTo`). */
fun voiceNoteDraftViewModelFactory(container: AppContainer, initial: VoiceNote?): ViewModelProvider.Factory =
    viewModelFactory {
        initializer { container.createVoiceNoteViewModel(VoiceTargets.forEditor(initial)) }
    }
