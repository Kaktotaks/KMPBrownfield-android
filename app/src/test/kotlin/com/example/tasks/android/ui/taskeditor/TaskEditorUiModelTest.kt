package com.example.tasks.android.ui.taskeditor

import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.ValidationError
import com.example.tasks.shared.presentation.TaskEditorUiState
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskEditorUiModelTest {

    private fun state(isNew: Boolean = true, errors: List<ValidationError> = emptyList()) = TaskEditorUiState(
        isNew = isNew,
        title = "Hi",
        notes = "n",
        priority = Priority.HIGH,
        dueDate = LocalDate(2026, 10, 29),
        errors = errors,
    )

    @Test
    fun utcMillisRoundTrip() {
        val dates = listOf(LocalDate(1970, 1, 1), LocalDate(2026, 10, 29), LocalDate(2028, 2, 29), LocalDate(1969, 12, 31))
        dates.forEach { assertEquals(it, utcMillisToLocalDate(it.toUtcMillis())) }
    }

    @Test
    fun utcMillisKnownValues() {
        assertEquals(0L, LocalDate(1970, 1, 1).toUtcMillis())
        assertEquals(86_400_000L, LocalDate(1970, 1, 2).toUtcMillis())
        // Any time inside a UTC day maps to that day, also before the epoch (floor, not truncation).
        assertEquals(LocalDate(1970, 1, 2), utcMillisToLocalDate(86_400_000L + 23 * 3_600_000L))
        assertEquals(LocalDate(1969, 12, 31), utcMillisToLocalDate(-1L))
    }

    @Test
    fun errorsMapToTheirFieldAndBlockSave() {
        val model = state(errors = listOf(ValidationError.TITLE_TOO_SHORT, ValidationError.DUE_DATE_IN_PAST)).toUiModel()

        assertEquals("Title must be at least 3 characters.", model.titleError)
        assertEquals("Due date can't be in the past.", model.dueDateError)
        assertNull(model.notesError)
        assertEquals(2, model.errorFieldCount)
        assertFalse(model.canSave)
    }

    @Test
    fun notesErrorBelongsToTheNotesField() {
        val model = state(errors = listOf(ValidationError.NOTES_TOO_LONG)).toUiModel()

        assertEquals("Notes must be at most 500 characters.", model.notesError)
        assertNull(model.titleError)
        assertEquals(1, model.errorFieldCount)
    }

    @Test
    fun fieldsAndLabels() {
        val model = state(isNew = false).toUiModel()

        assertEquals("Edit task", model.screenTitle)
        assertEquals("New task", state(isNew = true).toUiModel().screenTitle)
        assertEquals("Hi", model.title)
        assertEquals("n", model.notes)
        assertEquals("29 Oct 2026", model.dueDateText)
        assertEquals(LocalDate(2026, 10, 29), utcMillisToLocalDate(model.dueDateMillis))
        assertEquals(listOf("Low", "Medium", "High"), model.priorityOptions.map { it.label })
        assertEquals(listOf(Priority.HIGH), model.priorityOptions.filter { it.isSelected }.map { it.value })
        assertTrue(model.canSave)
        assertEquals(0, model.errorFieldCount)
    }

    @Test
    fun timeAndPlaceMapToTheirRows() {
        val withBoth = state().copy(
            time = kotlinx.datetime.LocalTime(14, 5),
            place = com.example.tasks.shared.domain.Place(com.example.tasks.shared.domain.GeoPoint(48.46, 35.04), "Shevchenka St 9"),
        ).toUiModel()
        assertEquals("14:05", withBoth.timeText)
        assertEquals(14, withBoth.timeHour)
        assertEquals(5, withBoth.timeMinute)
        assertEquals("Shevchenka St 9", withBoth.placeAddress)

        val without = state().toUiModel()
        assertNull(without.timeText)
        assertNull(without.placeAddress)
    }
}
