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
import com.example.tasks.android.ui.navigation.TaskEditorRoute
import com.example.tasks.shared.presentation.StatsViewModel
import com.example.tasks.shared.presentation.TaskDetailViewModel
import com.example.tasks.shared.presentation.TaskEditorViewModel
import com.example.tasks.shared.presentation.TaskListViewModel

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

/** `TaskEditorRoute.NEW_TASK_ID` == `TaskEditorViewModel.NEW_TASK_ID`, so the id is forwarded as is. */
val TaskEditorViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = appContainer()
        TaskEditorViewModel(
            taskId = createSavedStateHandle().toRoute<TaskEditorRoute>().taskId,
            repository = container.taskRepository,
            validator = container.taskValidator,
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
