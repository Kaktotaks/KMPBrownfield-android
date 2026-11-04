package com.example.tasks.android.voice

import com.example.tasks.shared.domain.MicPermission
import kotlin.math.abs
import kotlin.math.sin

/** Debug-only launch extras for deterministic screenshots (parsed once per MainActivity launch). */
data class VoiceDebugConfig(
    val fakeAudio: Boolean = false,
    val micPermission: MicPermission? = null,
    val denyOnRequest: Boolean = false,
    val forceError: ForceError = ForceError.NONE,
    /** taskId -> duration ms of the voice note to attach at launch. */
    val seed: Map<Int, Long> = emptyMap(),
) {
    enum class ForceError { NONE, RECORD_FAILED, UNAVAILABLE }

    companion object {
        const val EXTRA_FAKE_AUDIO = "fakeVoiceAudio"
        const val EXTRA_MIC_PERMISSION = "fakeMicPermission"
        const val EXTRA_MIC_DENY_ON_REQUEST = "fakeMicDenyOnRequest"
        const val EXTRA_FORCE_ERROR = "forceVoiceError"
        const val EXTRA_SEED_VOICE = "seedVoice"

        fun parse(fakeAudio: Boolean, micPermission: String?, denyOnRequest: Boolean, forceError: String?, seedVoice: String?) =
            VoiceDebugConfig(
                fakeAudio = fakeAudio,
                micPermission = when (micPermission?.lowercase()) {
                    "notdetermined" -> MicPermission.NOT_DETERMINED
                    "denied" -> MicPermission.DENIED
                    "granted" -> MicPermission.GRANTED
                    else -> null
                },
                denyOnRequest = denyOnRequest,
                forceError = when (forceError?.lowercase()) {
                    "recordfailed" -> ForceError.RECORD_FAILED
                    "unavailable" -> ForceError.UNAVAILABLE
                    else -> ForceError.NONE
                },
                seed = seedVoice.orEmpty().split(',').mapNotNull { pair ->
                    val (id, ms) = pair.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
                    (id.trim().toIntOrNull() ?: return@mapNotNull null) to (ms.trim().toLongOrNull() ?: return@mapNotNull null)
                }.toMap(),
            )

        /** 40 deterministic bars in 0.08..1. */
        fun seedWaveform(): List<Float> = List(40) { i -> (0.12f + 0.88f * abs(sin(i * 0.55f + 0.4f))).coerceIn(0.08f, 1f) }
    }
}
