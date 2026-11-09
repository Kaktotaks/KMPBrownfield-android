package com.example.tasks.android.ui.map

import com.example.tasks.shared.domain.GeoPoint
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.DayLabel
import com.example.tasks.shared.presentation.MapStop
import com.example.tasks.shared.presentation.MapUiState
import com.example.tasks.shared.presentation.RouteState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// Plain Kotlin (no Compose / Android): the shared MapUiState -> what the Map screen draws. Unit-tested on the JVM.
// There is no routing, geocoding, past / overdue or next-stop logic here: all of it is computed in `shared`
// (MapViewModel); this file only picks which of those values each widget shows.

enum class PinStatus { Default, Done, Overdue }

/** Which status chip floats under the day selector (at most one; offline beats failed beats loading). */
enum class ChipKind { None, Loading, Failed, Offline }

/** Which summary line the route sheet shows. */
enum class SummaryKind { Ready, Loading, Fallback, Idle, Empty }

data class LegModel(val minutes: Int, val meters: Double)

data class PinModel(
    val taskId: Int,
    /** 1-based route order; null = a place without a time (pin without a number, not routed). */
    val order: Int?,
    val point: GeoPoint,
    val priority: Priority,
    val status: PinStatus,
    val past: Boolean,
    val timeText: String?,
    val title: String,
    val selected: Boolean,
    val lifted: Boolean,
) {
    val done: Boolean get() = status == PinStatus.Done
    val overdue: Boolean get() = status == PinStatus.Overdue

    /** Pin body + order badge opacity (§5): 45 % when past, 70 % while a past pin is selected. */
    val bodyOpacity: Float get() = if (past) (if (selected) 0.7f else 0.45f) else 1f

    /** Time label opacity (§5): faded when past, except overdue (full, danger text + outline). */
    val timeOpacity: Float get() = if (past && !overdue) 0.45f else 1f
}

data class StopRowModel(
    val taskId: Int,
    val order: Int?,
    val timeText: String?,
    val title: String,
    val address: String,
    val priority: Priority,
    val status: PinStatus,
    val past: Boolean,
    val isNext: Boolean,
    /** The leg to the following routed stop (the connector under this row), when it has a duration. */
    val legToNext: LegModel?,
)

data class StartRowModel(
    /** "09:18" (Leave by / Left); null when there is no routed stop or no duration. */
    val leaveByText: String?,
    val startIsPast: Boolean,
    val legToNext: LegModel?,
)

data class NextStopModel(
    val taskId: Int,
    val order: Int,
    val timeText: String,
    val title: String,
    val priority: Priority,
    val minutesAway: Int?,
)

data class MapScreenModel(
    val day: LocalDate,
    val dayLabel: DayLabel,
    val summary: SummaryKind,
    val stopsCount: Int,
    val durationMinutes: Int?,
    val distanceMeters: Double?,
    val chip: ChipKind,
    val start: StartRowModel,
    val rows: List<StopRowModel>,
    val pins: List<PinModel>,
    val next: NextStopModel?,
    val hasStops: Boolean,
    val isLoading: Boolean,
)

fun formatTime(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)

private fun legOf(stop: MapStop?): LegModel? {
    val leg = stop?.legFromPrevious ?: return null
    val seconds = leg.durationSeconds ?: return null
    return LegModel(minutes = ((seconds + 30) / 60).toInt().coerceAtLeast(1), meters = leg.distanceMeters)
}

private fun MapStop.status(): PinStatus = when {
    isDone -> PinStatus.Done
    isOverdue -> PinStatus.Overdue
    else -> PinStatus.Default
}

fun chipFor(state: MapUiState): ChipKind = when {
    state.tilesOffline -> ChipKind.Offline
    state.routeFailed -> ChipKind.Failed
    state.route is RouteState.Loading -> ChipKind.Loading
    else -> ChipKind.None
}

fun MapUiState.toScreenModel(): MapScreenModel {
    val routed = stops.filter { it.order != null }
    val summary = when (route) {
        is RouteState.Empty -> SummaryKind.Empty
        is RouteState.Ready -> SummaryKind.Ready
        is RouteState.Loading -> SummaryKind.Loading
        is RouteState.Fallback -> SummaryKind.Fallback
        is RouteState.Idle -> SummaryKind.Idle
    }
    val rows = stops.mapIndexed { i, stop ->
        val following = stops.getOrNull(i + 1)?.takeIf { stop.order != null && it.order != null }
        StopRowModel(
            taskId = stop.task.id,
            order = stop.order,
            timeText = stop.task.time?.let(::formatTime),
            title = stop.task.title,
            address = stop.place.address,
            priority = stop.task.priority,
            status = stop.status(),
            past = stop.isPast,
            isNext = stop.isNext,
            legToNext = legOf(following),
        )
    }
    val pins = stops.map { stop ->
        PinModel(
            taskId = stop.task.id,
            order = stop.order,
            point = stop.place.point,
            priority = stop.task.priority,
            status = stop.status(),
            past = stop.isPast,
            timeText = stop.task.time?.let(::formatTime),
            title = stop.task.title,
            selected = stop.task.id == selectedTaskId,
            lifted = move?.taskId == stop.task.id,
        )
    }
    val nextModel = nextStop?.takeIf { it.order != null }?.let {
        NextStopModel(
            taskId = it.task.id,
            order = it.order!!,
            timeText = it.task.time?.let(::formatTime).orEmpty(),
            title = it.task.title,
            priority = it.task.priority,
            minutesAway = nextStopMinutesAway,
        )
    }
    return MapScreenModel(
        day = day,
        dayLabel = dayLabel,
        summary = summary,
        stopsCount = stopsCount,
        durationMinutes = totalDurationMinutes,
        distanceMeters = totalDistanceMeters,
        chip = chipFor(this),
        start = StartRowModel(
            leaveByText = leaveBy?.let(::formatTime),
            startIsPast = startIsPast,
            legToNext = legOf(routed.firstOrNull()),
        ),
        rows = rows,
        pins = pins,
        next = nextModel,
        hasStops = stops.isNotEmpty(),
        isLoading = isTasksLoading,
    )
}

// region Route lines (§3.2)

/**
 * What the GeoJSON route layers show. [past] = legs into past stops (grey, faded), [upcoming] = the rest (lime),
 * [pending] = the dashed straight "building route" line. [dashed] = a straight-line fallback (dash 10 8).
 * [pastShare] = the part of the route (by distance) that is past: the draw animation runs past first, then upcoming.
 */
data class RouteLines(
    val past: List<GeoPoint>,
    val upcoming: List<GeoPoint>,
    val pending: List<GeoPoint>,
    val dashed: Boolean,
    val pastShare: Float,
) {
    val isEmpty: Boolean get() = past.size < 2 && upcoming.size < 2 && pending.size < 2

    companion object {
        val None = RouteLines(emptyList(), emptyList(), emptyList(), dashed = false, pastShare = 0f)
    }
}

fun MapUiState.toRouteLines(): RouteLines {
    val routed = stops.filter { it.order != null }
    return when (val r = route) {
        is RouteState.Loading ->
            RouteLines(emptyList(), emptyList(), listOf(start.point) + routed.map { it.place.point }, dashed = false, pastShare = 0f)
        is RouteState.Ready -> linesOf(r.route.legs, routed, dashed = false)
        is RouteState.Fallback -> linesOf(r.route.legs, routed, dashed = true)
        else -> RouteLines.None
    }
}

private fun linesOf(legs: List<com.example.tasks.shared.routing.Leg>, routed: List<MapStop>, dashed: Boolean): RouteLines {
    val past = ArrayList<GeoPoint>()
    val upcoming = ArrayList<GeoPoint>()
    var pastMeters = 0.0
    var totalMeters = 0.0
    legs.forEachIndexed { i, leg ->
        totalMeters += leg.distanceMeters
        if (routed.getOrNull(i)?.isPast == true) {
            past += leg.geometry
            pastMeters += leg.distanceMeters
        } else {
            upcoming += leg.geometry
        }
    }
    val share = if (totalMeters > 0.0) (pastMeters / totalMeters).toFloat() else 0f
    return RouteLines(past, upcoming, emptyList(), dashed, share)
}

// endregion

// region Pin label collisions (§3.3)

data class PinAnchor(val id: Int, val x: Float, val y: Float)

/**
 * Ids whose time label is hidden: when two anchors are closer than [minDistancePx] the label of the lower one
 * (larger y; the later one on a tie) goes. Order badges always stay.
 */
fun hiddenTimeLabels(anchors: List<PinAnchor>, minDistancePx: Float): Set<Int> {
    val hidden = HashSet<Int>()
    for (i in anchors.indices) {
        for (j in i + 1 until anchors.size) {
            val a = anchors[i]
            val b = anchors[j]
            val dx = a.x - b.x
            val dy = a.y - b.y
            if (dx * dx + dy * dy < minDistancePx * minDistancePx) {
                hidden += if (b.y >= a.y) b.id else a.id
            }
        }
    }
    return hidden
}

// endregion

// region Camera (§6)

data class CameraPaddingPx(val left: Int, val top: Int, val right: Int, val bottom: Int)

/** Fit padding: top = selector bottom + 24, bottom = sheet height + 24, sides 48 (all dp, converted with [density]). */
fun fitPadding(selectorBottomDp: Float, sheetHeightDp: Float, density: Float): CameraPaddingPx = CameraPaddingPx(
    left = (48f * density).toInt(),
    top = ((selectorBottomDp + 24f) * density).toInt(),
    right = (48f * density).toInt(),
    bottom = ((sheetHeightDp + 24f) * density).toInt(),
)

/** Start + every stop of the day (what "fit to route" frames); only the start when the day has no stop. */
fun MapUiState.fitPoints(): List<GeoPoint> = listOf(start.point) + stops.map { it.place.point }

// endregion
