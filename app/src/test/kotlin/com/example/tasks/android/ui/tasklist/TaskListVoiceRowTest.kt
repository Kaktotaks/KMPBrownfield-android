package com.example.tasks.android.ui.tasklist

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.domain.VoiceNote
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskListVoiceRowTest {
    private val today = LocalDate(2026, 11, 4)

    @Test
    fun rowWithVoiceNoteCarriesTheFormattedDuration() {
        val task = Task(1, "Call", "", Priority.LOW, LocalDate(2026, 11, 6), false, VoiceNote("n.m4a", 42_900, List(40) { 0.5f }))
        val row = task.toRowUi(today)
        assertEquals("0:42", row.voiceDurationText)
        assertEquals(42_900L, row.voiceDurationMs)
    }

    @Test
    fun rowWithoutVoiceNoteHasNoIndicator() {
        val row = Task(1, "Call", "", Priority.LOW, LocalDate(2026, 11, 6), false).toRowUi(today)
        assertNull(row.voiceDurationText)
        assertNull(row.voiceDurationMs)
    }
}
