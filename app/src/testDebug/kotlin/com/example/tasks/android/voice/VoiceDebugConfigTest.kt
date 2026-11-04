package com.example.tasks.android.voice

import com.example.tasks.shared.domain.MicPermission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceDebugConfigTest {
    @Test
    fun parsesAllExtras() {
        val c = VoiceDebugConfig.parse(true, "notDetermined", true, "RecordFailed", "1:42000, 4:18000,bad,x:1")
        assertEquals(true, c.fakeAudio)
        assertEquals(MicPermission.NOT_DETERMINED, c.micPermission)
        assertEquals(true, c.denyOnRequest)
        assertEquals(VoiceDebugConfig.ForceError.RECORD_FAILED, c.forceError)
        assertEquals(mapOf(1 to 42_000L, 4 to 18_000L), c.seed)
    }

    @Test
    fun defaultsAreRealAdapters() {
        val c = VoiceDebugConfig.parse(false, null, false, null, null)
        assertNull(c.micPermission)
        assertEquals(VoiceDebugConfig.ForceError.NONE, c.forceError)
        assertEquals(emptyMap<Int, Long>(), c.seed)
    }
}
