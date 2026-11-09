package com.example.tasks.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tasks.android.ui.map.MapDebug
import com.example.tasks.android.ui.navigation.TasksRoot
import com.example.tasks.android.ui.splash.SplashDebug
import com.example.tasks.android.ui.splash.SplashOverlay
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.rememberReduceMotion
import com.example.tasks.android.ui.voice.MicPermissionHost
import com.example.tasks.android.voice.DebugHooks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // System launch frame (plain #0A0A0A, Theme.App.Starting). No keep condition: hands over to SplashOverlay at once.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as TasksApplication).container
        DebugHooks.onMainActivityCreated(this, container) // no-op in release
        // Debug-only screenshot hooks (frozen "now", demo day, OSRM host): they must run BEFORE the first load, because the
        // repository seeds the demo route relative to its clock. No-op in release.
        MapDebug.configure(intent, container)
        (application as TasksApplication).startLoading()

        val frozenAtMs = SplashDebug.frozenAtMs(intent) // null in release
        // Cold start only: not after a rotation / process restore (saved state), not for a deep link,
        // not when the instrumentation tests pass `disableSplash`.
        val coldStart = savedInstanceState == null &&
            intent?.data == null &&
            !intent.getBooleanExtra(EXTRA_DISABLE_SPLASH, false)

        setContent {
            // Bridges the system RECORD_AUDIO dialog (Activity Result API) to the shared MicPermissionProvider.
            MicPermissionHost(container.voiceAdapters.micPermission)
            TasksTheme {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                var listSettled by rememberSaveable { mutableStateOf(false) }
                val showSplash = frozenAtMs != null || (coldStart && !splashDone)
                Box(Modifier.fillMaxSize()) {
                    // While the overlay is up the list is composed (it loads underneath) but hidden from TalkBack.
                    Box(if (showSplash) Modifier.fillMaxSize().clearAndSetSemantics {} else Modifier.fillMaxSize()) {
                        TasksRoot(onListSettled = { listSettled = true })
                    }
                    if (showSplash) {
                        SplashOverlay(
                            listSettled = listSettled,
                            reduceMotion = rememberReduceMotion(),
                            frozenAtMs = frozenAtMs,
                            onFinished = { splashDone = true },
                        )
                    }
                }
            }
        }
    }

    companion object {
        /** Boolean intent extra that skips the splash overlay (instrumentation / UI tests). */
        const val EXTRA_DISABLE_SPLASH = "disableSplash"
    }
}
