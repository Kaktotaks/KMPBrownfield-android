package com.example.tasks.android.ui.components

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tasks.android.AppContainer
import com.example.tasks.android.TasksApplication

/** Pulls the manual DI container out of CreationExtras inside `viewModelFactory { initializer { ... } }`. */
fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as TasksApplication).container
