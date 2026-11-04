package com.example.tasks.android.voice

import android.media.AudioManager
import com.example.tasks.shared.domain.MicPermission
import com.example.tasks.shared.domain.VoiceNote
import com.example.tasks.shared.presentation.VoiceNoteTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioAdapterLogicTest {

    @Test
    fun levelIsSqrtOfNormalizedAmplitude() {
        assertEquals(0f, AudioLevel.fromMaxAmplitude(0), 0f)
        assertEquals(1f, AudioLevel.fromMaxAmplitude(32767), 1e-6f)
        // 25% of full scale -> 50% on the bars
        assertEquals(0.5f, AudioLevel.fromMaxAmplitude(8192), 1e-3f)
    }

    @Test
    fun levelIsClampedToZeroOne() {
        assertEquals(0f, AudioLevel.fromMaxAmplitude(-5), 0f)
        assertEquals(1f, AudioLevel.fromMaxAmplitude(40_000), 0f)
    }

    @Test
    fun levelRisesMonotonically() {
        val levels = listOf(0, 100, 1000, 8000, 20000, 32767).map(AudioLevel::fromMaxAmplitude)
        assertEquals(levels.sorted(), levels)
    }

    @Test
    fun permissionStatusDecisionTable() {
        val r = MicPermissionPolicy::resolve
        assertEquals(MicPermission.GRANTED, r(true, false, false))
        assertEquals(MicPermission.GRANTED, r(true, true, true))
        assertEquals(MicPermission.NOT_DETERMINED, r(false, false, false)) // first run
        assertEquals(MicPermission.NOT_DETERMINED, r(false, true, true)) // denied once: the system will ask again
        assertEquals(MicPermission.DENIED, r(false, true, false)) // permanent: Open Settings
    }

    @Test
    fun everyAudioFocusLossPausesPlayback() {
        val p = MediaVoicePlayer.FocusPolicy
        assertTrue(p.shouldPause(AudioManager.AUDIOFOCUS_LOSS))
        assertTrue(p.shouldPause(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT))
        assertTrue(p.shouldPause(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK))
        assertFalse(p.shouldPause(AudioManager.AUDIOFOCUS_GAIN))
    }

    @Test
    fun detailEditsTheSavedTaskAndEditorEditsADraft() {
        assertEquals(VoiceNoteTarget.ExistingTask(7), VoiceTargets.forTask(7))
        assertEquals(VoiceNoteTarget.Draft(null), VoiceTargets.forEditor(null))
        val note = VoiceNote("note-1.m4a", 42_000, List(40) { 0.5f })
        assertEquals(VoiceNoteTarget.Draft(note), VoiceTargets.forEditor(note))
    }
}
