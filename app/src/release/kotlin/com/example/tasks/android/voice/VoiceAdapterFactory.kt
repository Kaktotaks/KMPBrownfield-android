package com.example.tasks.android.voice

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.tasks.android.AppContainer
import com.example.tasks.shared.domain.AudioPlayer
import com.example.tasks.shared.domain.AudioRecorder
import com.example.tasks.shared.domain.MicPermissionProvider

/**
 * Release variant: always the real adapters. The debug source set has a same-named class that can swap in fakes
 * (see `app/src/debug/.../voice/VoiceAdapterFactory.kt`); none of that code exists in release builds.
 */
class VoiceAdapterFactory(private val context: Context) {
    /** The real provider; the composable bridge (`MicPermissionHost`) attaches the Activity launcher to it. */
    val micPermission = RuntimeMicPermission(context)

    // One recorder / player per VoiceNoteViewModel: Detail and the editor sheet can be alive at once.
    fun newRecorder(): AudioRecorder = MediaVoiceRecorder(context)
    fun newPlayer(): AudioPlayer = MediaVoicePlayer(context)
    fun permissionProvider(): MicPermissionProvider = micPermission
}

/** No-op in release. */
object DebugHooks {
    @Suppress("UNUSED_PARAMETER")
    fun onMainActivityCreated(activity: ComponentActivity, container: AppContainer) = Unit
}
