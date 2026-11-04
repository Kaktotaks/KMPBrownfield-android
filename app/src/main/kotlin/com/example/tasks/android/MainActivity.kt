package com.example.tasks.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tasks.android.ui.navigation.TasksRoot
import com.example.tasks.android.ui.voice.MicPermissionHost
import com.example.tasks.android.voice.DebugHooks
import com.example.tasks.android.ui.theme.TasksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as TasksApplication).container
        DebugHooks.onMainActivityCreated(this, container) // no-op in release
        setContent {
            // Bridges the system RECORD_AUDIO dialog (Activity Result API) to the shared MicPermissionProvider.
            MicPermissionHost(container.voiceAdapters.micPermission)
            TasksTheme {
                TasksRoot()
            }
        }
    }
}
