package com.example.tasks.android.voice

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.tasks.android.AppContainer
import com.example.tasks.shared.data.LoadState
import com.example.tasks.shared.domain.AudioPlayer
import com.example.tasks.shared.domain.AudioRecorder
import com.example.tasks.shared.domain.MicPermissionProvider
import com.example.tasks.shared.domain.VoiceNote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/**
 * DEBUG variant (this class does not exist in release builds): same API as the release one, but the adapters
 * can be replaced by deterministic fakes, driven by launch-intent extras (see [VoiceDebugConfig] and the README):
 *
 *   adb shell am start -S -n com.example.tasks.android/.MainActivity --ez fakeVoiceAudio true --es fakeMicPermission notDetermined
 */
class VoiceAdapterFactory(private val context: Context) {
    val micPermission = RuntimeMicPermission(context)

    fun newRecorder(): AudioRecorder {
        val c = VoiceDebug.config
        return if (c.fakeAudio || c.forceError == VoiceDebugConfig.ForceError.RECORD_FAILED) {
            FakeVoiceRecorder(failOnStart = c.forceError == VoiceDebugConfig.ForceError.RECORD_FAILED)
        } else {
            MediaVoiceRecorder(context)
        }
    }

    fun newPlayer(): AudioPlayer {
        val c = VoiceDebug.config
        return if (c.fakeAudio || c.forceError == VoiceDebugConfig.ForceError.UNAVAILABLE) {
            FakeVoicePlayer(failOnPrepare = c.forceError == VoiceDebugConfig.ForceError.UNAVAILABLE)
        } else {
            MediaVoicePlayer(context)
        }
    }

    fun permissionProvider(): MicPermissionProvider = VoiceDebug.fakeMic ?: micPermission
}

/** Process-wide debug state, replaced on every MainActivity launch. */
object VoiceDebug {
    @Volatile
    var config = VoiceDebugConfig()
        private set

    @Volatile
    var fakeMic: FakeMicPermission? = null
        private set

    fun apply(newConfig: VoiceDebugConfig) {
        config = newConfig
        fakeMic = newConfig.micPermission?.let { FakeMicPermission(it, newConfig.denyOnRequest) }
    }
}

object DebugHooks {
    fun onMainActivityCreated(activity: ComponentActivity, container: AppContainer) {
        val intent = activity.intent
        val config = VoiceDebugConfig.parse(
            fakeAudio = intent.getBooleanExtra(VoiceDebugConfig.EXTRA_FAKE_AUDIO, false),
            micPermission = intent.getStringExtra(VoiceDebugConfig.EXTRA_MIC_PERMISSION),
            denyOnRequest = intent.getBooleanExtra(VoiceDebugConfig.EXTRA_MIC_DENY_ON_REQUEST, false),
            forceError = intent.getStringExtra(VoiceDebugConfig.EXTRA_FORCE_ERROR),
            seedVoice = intent.getStringExtra(VoiceDebugConfig.EXTRA_SEED_VOICE),
        )
        VoiceDebug.apply(config)
        if (config.seed.isEmpty()) return
        activity.lifecycleScope.launch {
            val repository = container.taskRepository
            repository.state.first { it is LoadState.Loaded }
            config.seed.forEach { (taskId, durationMs) ->
                if (repository.task(taskId) == null) return@forEach
                val name = "note-debug-seed-$taskId.m4a"
                // "unavailable" mode leaves the file out, so shared starts the card in Error(unavailable).
                if (config.forceError != VoiceDebugConfig.ForceError.UNAVAILABLE) {
                    File(container.voiceFiles.pathOf(name)).writeText(durationMs.toString())
                }
                repository.setVoiceNote(taskId, VoiceNote(name, durationMs, VoiceDebugConfig.seedWaveform()))
            }
        }
    }
}
