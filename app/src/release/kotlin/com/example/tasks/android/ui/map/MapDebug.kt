package com.example.tasks.android.ui.map

import android.content.Intent
import com.example.tasks.android.AppContainer
import com.example.tasks.shared.presentation.MapViewModel

/** Release: no screenshot hooks (the debug source set has a same-named object with the real ones). */
object MapDebug {
    const val forceOffline: Boolean = false
    const val startOnMap: Boolean = false

    @Suppress("UNUSED_PARAMETER")
    fun configure(intent: Intent?, container: AppContainer) = Unit

    @Suppress("UNUSED_PARAMETER")
    suspend fun apply(viewModel: MapViewModel) = Unit
}
