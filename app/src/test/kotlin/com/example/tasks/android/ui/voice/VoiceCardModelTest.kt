package com.example.tasks.android.ui.voice

import com.example.tasks.shared.domain.VoiceNote
import com.example.tasks.shared.presentation.VoiceNoteErrorKind
import com.example.tasks.shared.presentation.VoiceNoteUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceCardModelTest {
    private val waveform = List(40) { 0.5f }
    private val note = VoiceNote("note-1.m4a", 42_900, waveform)

    @Test
    fun simpleStatesMapOneToOne() {
        assertEquals(VoiceCardModel.Empty, VoiceNoteUiState.Empty.toCardModel())
        assertEquals(VoiceCardModel.PermissionExplainer, VoiceNoteUiState.PermissionExplainer.toCardModel())
        assertEquals(VoiceCardModel.PermissionDenied, VoiceNoteUiState.PermissionDenied.toCardModel())
    }

    @Test
    fun errorKindsPickTheirCard() {
        assertEquals(VoiceCardModel.RecordError, VoiceNoteUiState.Error(VoiceNoteErrorKind.RECORD_FAILED).toCardModel())
        assertEquals(VoiceCardModel.Unavailable, VoiceNoteUiState.Error(VoiceNoteErrorKind.UNAVAILABLE).toCardModel())
    }

    @Test
    fun recordingShowsElapsedAndMaxHintUntilTheLastTenSeconds() {
        val levels = List(32) { 0.2f }
        val m = VoiceNoteUiState.Recording(7_400, 112_600, isWarning = false, levels = levels).toCardModel() as VoiceCardModel.Recording
        assertEquals("0:07", m.timerText)
        assertEquals(false, m.isWarning)
        assertNull(m.timeLeftText)
        assertEquals(levels, m.levels)
    }

    @Test
    fun recordingWarningShowsRemainingTime() {
        val m = VoiceNoteUiState.Recording(111_000, 9_000, isWarning = true, levels = List(32) { 0f }).toCardModel() as VoiceCardModel.Recording
        assertEquals("1:51", m.timerText)
        assertEquals(true, m.isWarning)
        assertEquals("0:09", m.timeLeftText)
    }

    @Test
    fun recordingWarningRoundsRemainingTimeUp() {
        val m = VoiceNoteUiState.Recording(111_100, 8_900, isWarning = true, levels = List(32) { 0f }).toCardModel() as VoiceCardModel.Recording
        assertEquals("0:09", m.timeLeftText)
    }

    @Test
    fun readyMapsTimeSpeedAndSpokenDuration() {
        val m = VoiceNoteUiState.Ready(note, positionMs = 12_300, isPlaying = true, speed = 1.5f, progress = 0.29f).toCardModel() as VoiceCardModel.Player
        assertEquals("0:12", m.positionText)
        assertEquals("0:42", m.durationText)
        assertEquals("1.5×", m.speedLabel)
        assertEquals(true, m.isPlaying)
        assertEquals(SpokenDuration(0, 42), m.spokenDuration)
        assertEquals(waveform, m.waveform)
        assertEquals(0.29f, m.progress, 0f)
        // 42.9 s in 5 s steps -> 8 positions -> 7 steps between the ends
        assertEquals(7, m.seekSteps)
    }

    @Test
    fun progressIsClamped() {
        val m = VoiceNoteUiState.Ready(note, 0, false, 1f, progress = 1.4f).toCardModel() as VoiceCardModel.Player
        assertEquals(1f, m.progress, 0f)
    }

    @Test
    fun speedLabelsCoverTheThreeSpeeds() {
        assertEquals("1×", speedLabel(1f))
        assertEquals("1.5×", speedLabel(1.5f))
        assertEquals("2×", speedLabel(2f))
    }

    @Test
    fun spokenDurationSplitsMinutesAndSeconds() {
        assertEquals(SpokenDuration(0, 42), SpokenDuration.of(42_900))
        assertEquals(SpokenDuration(1, 5), SpokenDuration.of(65_000))
        assertEquals(SpokenDuration(2, 0), SpokenDuration.of(120_000))
        assertEquals(SpokenDuration(0, 0), SpokenDuration.of(-5))
    }

    @Test
    fun eachStateHasItsOwnKindSoTicksDoNotRetriggerTheTransition() {
        val a = VoiceNoteUiState.Recording(1_000, 119_000, false, List(32) { 0f }).toCardModel()
        val b = VoiceNoteUiState.Recording(1_050, 118_950, false, List(32) { 0.4f }).toCardModel()
        assertEquals(a.kind, b.kind)
        assertEquals(VoiceCardKind.Player, VoiceNoteUiState.Ready(note, 0, false, 1f, 0f).toCardModel().kind)
    }
}
