package com.example.tasks.android.ui.voice

import kotlin.math.abs
import kotlin.math.sin

/** Fixtures for the @Previews and tests: one value per card state. */
object VoicePreviewData {
    val waveform: List<Float> = List(40) { i -> (0.15f + 0.85f * abs(sin(i * 0.55f + 0.4f))).coerceIn(0.08f, 1f) }

    private val liveLevels: List<Float> = List(32) { i -> if (i < 6) 0.1f else (0.15f + 0.85f * abs(sin(i * 0.6f))).coerceIn(0f, 1f) }

    val player = VoiceCardModel.Player(
        waveform = waveform, progress = 0.29f, isPlaying = false,
        positionText = "0:12", durationText = "0:42", speedLabel = "1×",
        spokenDuration = SpokenDuration(0, 42), seekSteps = 7,
    )
    val playerPlaying = player.copy(isPlaying = true, speedLabel = "1.5×")
    val recording = VoiceCardModel.Recording(timerText = "0:07", isWarning = false, timeLeftText = null, levels = liveLevels)
    val recordingWarning = VoiceCardModel.Recording(timerText = "1:51", isWarning = true, timeLeftText = "0:09", levels = liveLevels)
}
