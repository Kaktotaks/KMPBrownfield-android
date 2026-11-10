package com.example.tasks.android.ui.map

import com.example.tasks.shared.data.DemoRoute
import com.example.tasks.shared.domain.GeoPoint
import com.example.tasks.shared.domain.Place
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.domain.Task
import com.example.tasks.shared.presentation.DayLabel
import com.example.tasks.shared.presentation.MapStop
import com.example.tasks.shared.presentation.MapUiState
import com.example.tasks.shared.presentation.MoveState
import com.example.tasks.shared.presentation.RouteState
import com.example.tasks.shared.routing.Leg
import com.example.tasks.shared.routing.Route
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Map UI model: shared MapUiState -> what the screen draws. No Android, no Compose. */
class MapModelsTest {
    private val day = LocalDate(2026, 11, 4)
    private val start = DemoRoute.START

    private fun place(i: Int) = Place(GeoPoint(48.46 + i * 0.001, 35.04 + i * 0.001), "Street $i")

    private fun leg(minutes: Int, km: Double, from: Int, to: Int) =
        Leg(minutes * 60.0, km * 1000, listOf(place(from).point, place(to).point))

    private fun stop(
        id: Int, order: Int?, time: LocalTime?, priority: Priority = Priority.MEDIUM,
        done: Boolean = false, past: Boolean = false, overdue: Boolean = false, next: Boolean = false, leg: Leg? = null,
    ) = MapStop(
        task = Task(id, "Task $id", "", priority, day, done, null, time, place(id)),
        place = place(id),
        order = order,
        isPast = past,
        isOverdue = overdue,
        isDone = done,
        isNext = next,
        legFromPrevious = leg,
        legIsPast = past && leg != null,
    )

    private val s1 = stop(1, 1, LocalTime(9, 30), Priority.LOW, done = true, past = true, leg = leg(12, 3.1, 0, 1))
    private val s2 = stop(2, 2, LocalTime(11, 0), Priority.HIGH, past = true, overdue = true, leg = leg(9, 2.2, 1, 2))
    private val s3 = stop(3, 3, LocalTime(13, 15), Priority.MEDIUM, next = true, leg = leg(14, 3.4, 2, 3))
    private val loose = stop(4, null, null, Priority.LOW)

    private fun route(vararg legs: Leg, fallback: Boolean = false) = Route(
        geometry = legs.flatMap { it.geometry },
        legs = legs.toList(),
        totalDurationSeconds = if (fallback) null else legs.sumOf { it.durationSeconds ?: 0.0 },
        totalDistanceMeters = legs.sumOf { it.distanceMeters },
        isFallback = fallback,
    )

    private fun state(
        stops: List<MapStop> = listOf(s1, s2, s3, loose),
        route: RouteState = RouteState.Ready(route(s1.legFromPrevious!!, s2.legFromPrevious!!, s3.legFromPrevious!!)),
        selected: Int? = null,
        move: MoveState? = null,
        offline: Boolean = false,
        failed: Boolean = false,
    ) = MapUiState(
        day = day, dayLabel = DayLabel.TODAY, start = start, stops = stops, isTasksLoading = false,
        route = route, stopsCount = stops.size, routedStopsCount = stops.count { it.order != null },
        totalDurationMinutes = 35, totalDistanceMeters = 8700.0,
        nextStop = stops.firstOrNull { it.isNext }, nextStopMinutesAway = 35,
        leaveBy = LocalTime(9, 18), startIsPast = true,
        selectedTaskId = selected, sheetExpanded = false, move = move, newTaskDraft = null,
        tilesOffline = offline, routeFailed = failed,
    )

    // ---- rows

    @Test
    fun rowsFollowTheStopsAndShowTheLegToTheNextRoutedStop() {
        val m = state().toScreenModel()
        assertEquals(listOf(1, 2, 3, 4), m.rows.map { it.taskId })
        // The connector under a row is the leg INTO the following stop.
        assertEquals(LegModel(9, 2200.0), m.rows[0].legToNext)
        assertEquals(LegModel(14, 3400.0), m.rows[1].legToNext)
        assertNull(m.rows[2].legToNext) // next row is an unscheduled pin: no leg
        assertNull(m.rows[3].legToNext)
        // The Start row's leg is the leg into the first routed stop.
        assertEquals(LegModel(12, 3100.0), m.start.legToNext)
        assertEquals("09:18", m.start.leaveByText)
        assertTrue(m.start.startIsPast)
    }

    @Test
    fun rowStatusPastAndNextComeFromShared() {
        val rows = state().toScreenModel().rows
        assertEquals(PinStatus.Done, rows[0].status)
        assertEquals(PinStatus.Overdue, rows[1].status)
        assertEquals(PinStatus.Default, rows[2].status)
        assertTrue(rows[0].past && rows[1].past)
        assertFalse(rows[2].past)
        assertTrue(rows[2].isNext)
        assertEquals("09:30", rows[0].timeText)
        assertNull(rows[3].timeText)
        assertNull(rows[3].order)
    }

    @Test
    fun fallbackLegsHaveNoDurationSoNoLegLine() {
        val fb = leg(0, 1.0, 0, 1).copy(durationSeconds = null)
        val stops = listOf(s1.copy(legFromPrevious = fb), s2.copy(legFromPrevious = fb))
        val m = state(stops = stops, route = RouteState.Fallback(route(fb, fb, fallback = true))).toScreenModel()
        assertNull(m.start.legToNext)
        assertNull(m.rows[0].legToNext)
    }

    // ---- pins

    @Test
    fun pinsCarryStatusSelectionAndLift() {
        val st = state(selected = 3, move = MoveState(2, place(2), place(2).point, null, false))
        val pins = st.toScreenModel().pins
        assertEquals(listOf(1, 2, 3, 4), pins.map { it.taskId })
        assertTrue(pins[2].selected)
        assertFalse(pins[0].selected)
        assertTrue(pins[1].lifted)
        assertTrue(pins[0].done && pins[1].overdue)
        assertNull(pins[3].order)
    }

    @Test
    fun pinOpacityFollowsSection5() {
        val pins = state().toScreenModel().pins
        // past body 45 %, 70 % while selected; time label faded unless overdue (then full)
        assertEquals(0.45f, pins[0].bodyOpacity, 0f)
        assertEquals(0.45f, pins[1].bodyOpacity, 0f)
        assertEquals(1f, pins[2].bodyOpacity, 0f)
        assertEquals(0.45f, pins[0].timeOpacity, 0f)
        assertEquals(1f, pins[1].timeOpacity, 0f)
        assertEquals(0.7f, pins[0].copy(selected = true).bodyOpacity, 0f)
    }

    // ---- chips / summary

    @Test
    fun chipPrecedence() {
        assertEquals(ChipKind.None, state().toScreenModel().chip)
        assertEquals(ChipKind.Loading, state(route = RouteState.Loading).toScreenModel().chip)
        assertEquals(ChipKind.Failed, state(failed = true, route = RouteState.Fallback(route(fallback = true))).toScreenModel().chip)
        assertEquals(ChipKind.Offline, state(offline = true, failed = true).toScreenModel().chip)
    }

    @Test
    fun summaryKinds() {
        assertEquals(SummaryKind.Ready, state().toScreenModel().summary)
        assertEquals(SummaryKind.Loading, state(route = RouteState.Loading).toScreenModel().summary)
        assertEquals(SummaryKind.Fallback, state(route = RouteState.Fallback(route(fallback = true))).toScreenModel().summary)
        assertEquals(SummaryKind.Empty, state(stops = emptyList(), route = RouteState.Empty).toScreenModel().summary)
        assertEquals(SummaryKind.Idle, state(route = RouteState.Idle).toScreenModel().summary)
    }

    @Test
    fun nextStopModel() {
        val next = state().toScreenModel().next!!
        assertEquals(3, next.taskId)
        assertEquals(3, next.order)
        assertEquals("13:15", next.timeText)
        assertEquals(35, next.minutesAway)
        assertNull(state(stops = listOf(s1, s2, loose)).toScreenModel().next)
    }

    // ---- route lines

    @Test
    fun readyRouteSplitsPastAndUpcomingLegs() {
        val lines = state().toRouteLines()
        // legs 0 and 1 end at past stops (1, 2); leg 2 ends at the upcoming stop 3
        assertEquals(s1.legFromPrevious!!.geometry + s2.legFromPrevious!!.geometry, lines.past)
        assertEquals(s3.legFromPrevious!!.geometry, lines.upcoming)
        assertTrue(lines.pending.isEmpty())
        assertFalse(lines.dashed)
        assertEquals(5300.0 / 8700.0, lines.pastShare.toDouble(), 1e-4)
    }

    @Test
    fun fallbackRouteIsDashed() {
        val fb = route(s1.legFromPrevious!!, s3.legFromPrevious!!, fallback = true)
        val lines = state(stops = listOf(s1, s3), route = RouteState.Fallback(fb)).toRouteLines()
        assertTrue(lines.dashed)
        assertEquals(s1.legFromPrevious!!.geometry, lines.past)
        assertEquals(s3.legFromPrevious!!.geometry, lines.upcoming)
    }

    @Test
    fun loadingDrawsAStraightPendingLineThroughStartAndRoutedStops() {
        val lines = state(route = RouteState.Loading).toRouteLines()
        assertEquals(listOf(start.point, place(1).point, place(2).point, place(3).point), lines.pending)
        assertTrue(lines.past.isEmpty() && lines.upcoming.isEmpty())
    }

    @Test
    fun noRouteWhenEmptyOrIdle() {
        assertTrue(state(stops = emptyList(), route = RouteState.Empty).toRouteLines().isEmpty)
        assertTrue(state(route = RouteState.Idle).toRouteLines().isEmpty)
    }

    // ---- pin label collisions

    @Test
    fun lowerLabelOfTwoCloseAnchorsIsHidden() {
        val hidden = hiddenTimeLabels(listOf(PinAnchor(1, 100f, 100f), PinAnchor(2, 110f, 118f), PinAnchor(3, 300f, 300f)), 24f)
        assertEquals(setOf(2), hidden)
        // order does not matter: the one with the larger y loses its label
        assertEquals(setOf(1), hiddenTimeLabels(listOf(PinAnchor(2, 110f, 118f), PinAnchor(1, 100f, 130f)), 24f))
    }

    @Test
    fun farAnchorsKeepTheirLabels() {
        assertTrue(hiddenTimeLabels(listOf(PinAnchor(1, 0f, 0f), PinAnchor(2, 24f, 0f)), 24f).isEmpty())
    }

    // ---- camera

    @Test
    fun fitPaddingFollowsSection6() {
        // selector bottom 68 dp -> top 92; sheet 176 + bar 80 -> bottom 280; sides 48; at 2x density
        val p = fitPadding(selectorBottomDp = 68f, sheetHeightDp = 256f, density = 2f)
        assertEquals(CameraPaddingPx(left = 96, top = 184, right = 96, bottom = 560), p)
    }

    @Test
    fun fitPointsAreStartPlusEveryStop() {
        assertEquals(listOf(start.point, place(1).point, place(2).point, place(3).point, place(4).point), state().fitPoints())
        assertEquals(listOf(start.point), state(stops = emptyList(), route = RouteState.Empty).fitPoints())
    }
}
