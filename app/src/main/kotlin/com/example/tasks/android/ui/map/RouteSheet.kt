package com.example.tasks.android.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.Inter
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal val COLLAPSED_SHEET_HEIGHT = 176.dp
internal val EXPANDED_SHEET_HEIGHT = 640.dp

/** A bottom sheet surface: top radius 28, `bg`, 1 dp edge, soft shadow, Android drag handle (32 x 4, #8C8C85). */
@Composable
internal fun SheetSurface(
    modifier: Modifier = Modifier,
    handle: @Composable () -> Unit = { SheetHandle() },
    content: @Composable () -> Unit,
) {
    val colors = TasksTheme.colors
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        modifier
            .shadow(12.dp, shape, clip = false, ambientColor = Color.Black.copy(MapColors.shadowAlpha(colors, 0.12f)), spotColor = Color.Black.copy(MapColors.shadowAlpha(colors, 0.12f)))
            .clip(shape)
            .background(MapColors.sheetEdge(colors))
            .padding(top = 1.dp)
            .clip(shape)
            .background(colors.bg),
    ) {
        handle()
        content()
    }
}

@Composable
internal fun SheetHandle(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(16.dp).padding(top = 12.dp), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.width(32.dp).height(4.dp).background(MapColors.sheetGrabber(TasksTheme.colors), CircleShape))
    }
}

/**
 * The route sheet (components/RouteSheet.dc.html): a custom draggable sheet above the tab bar with two detents,
 * 176 dp (summary + next stop) and 640 dp (the full list), spring snap with velocity. Dragging the header or tapping
 * the handle switches the detent (TalkBack: the handle is a button with a state description).
 */
@Composable
internal fun RouteSheet(
    model: MapScreenModel,
    expanded: Boolean,
    maxExpandedHeight: Dp,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Int) -> Unit,
    onRecenter: () -> Unit,
    onHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TasksTheme.colors
    val density = LocalDensity.current
    val collapsedPx = with(density) { COLLAPSED_SHEET_HEIGHT.toPx() }
    val expandedPx = with(density) { minOf(EXPANDED_SHEET_HEIGHT, maxExpandedHeight).toPx() }.coerceAtLeast(collapsedPx)
    val height = remember { Animatable(if (expanded) expandedPx else collapsedPx) }
    val scope = rememberCoroutineScope()
    val latestExpanded by rememberUpdatedState(expanded)
    val reduce = com.example.tasks.android.ui.theme.LocalReduceMotion.current

    LaunchedEffect(expanded, expandedPx) {
        val target = if (expanded) expandedPx else collapsedPx
        if (reduce) height.snapTo(target) else height.animateTo(target, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
    }
    // The screen only needs the RESTING height (attribution margins), so it is reported when no animation is running
    // (initially, and after every settle), never per drag frame: a per-frame state write recomposed the whole Map
    // screen and re-poked the MapLibre view on every pixel of the drag.
    LaunchedEffect(Unit) {
        snapshotFlow { height.isRunning }.filter { !it }.collectLatest { onHeightChange(with(density) { height.value.toDp() }) }
    }

    val dragState = rememberDraggableState { delta ->
        // UNDISPATCHED: the sheet follows the finger in the same frame instead of one dispatch later.
        scope.launch(start = CoroutineStart.UNDISPATCHED) { height.snapTo((height.value - delta).coerceIn(collapsedPx, expandedPx)) }
    }
    // Velocity-aware snap to the nearest detent (a fling past 700 px/s goes that way), then a spring.
    val settle: suspend CoroutineScope.(Float) -> Unit = { velocity ->
        val mid = (collapsedPx + expandedPx) / 2
        val goExpanded = if (velocity < -700f) true else if (velocity > 700f) false else height.value > mid
        onExpandedChange(goExpanded)
        // The effect above animates on a state change; settle explicitly when we were already at that state.
        val target = if (goExpanded) expandedPx else collapsedPx
        if (reduce) height.snapTo(target) else height.animateTo(target, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
    }
    val stateText = stringResource(if (expanded) R.string.map_a11y_sheet_collapse else R.string.map_a11y_sheet_expand)
    val toggle = { onExpandedChange(!latestExpanded) }
    val routeSheetLabel = stringResource(R.string.map_a11y_route_sheet)

    SheetSurface(
        modifier
            .fillMaxWidth()
            .animatedHeight { height.value }
            .semantics { isTraversalGroup = true; contentDescription = routeSheetLabel },
        handle = {
            Box(
                Modifier
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = settle,
                    )
                    .clickable(role = Role.Button, onClick = toggle)
                    .semantics { stateDescription = stateText; contentDescription = stateText },
            ) { SheetHandle() }
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = settle,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        sheetTitle(model.dayLabel, model.day),
                        style = TasksType.title,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        summaryText(model),
                        style = TasksType.subhead.copy(fontFeatureSettings = "tnum"),
                        color = colors.textSecondary,
                    )
                }
                if (model.summary != SummaryKind.Empty) RecenterButton(onRecenter)
            }

            when {
                model.summary == SummaryKind.Empty -> EmptyCard()
                expanded -> RouteList(model, onSelect, Modifier.weight(1f, fill = false))
                model.next != null -> NextCard(model.next, model.rows.firstOrNull { it.taskId == model.next.taskId }?.past == true, onSelect)
            }
        }
    }
}

/**
 * A short tap on empty map steps the expanded route sheet down to the collapsed state. Not while a pin is selected (that
 * tap deselects), nor during the Move flow or a new-task draft, which keep their own behaviour.
 */
internal fun collapsesOnMapTap(expanded: Boolean, hasSelection: Boolean, moving: Boolean, hasDraft: Boolean): Boolean =
    expanded && !hasSelection && !moving && !hasDraft

/**
 * Fixed height read in the LAYOUT phase: the animated/dragged value changes every frame, and reading it in
 * composition (`Modifier.height(value.toDp())`) recomposed the sheet and its content on every drag pixel.
 */
internal fun Modifier.animatedHeight(heightPx: () -> Float): Modifier = layout { measurable, constraints ->
    val h = heightPx().roundToInt().coerceIn(constraints.minHeight, constraints.maxHeight)
    val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
    layout(placeable.width, h) { placeable.place(0, 0) }
}

@Composable
private fun RecenterButton(onClick: () -> Unit) {
    val colors = TasksTheme.colors
    Row(
        Modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .height(40.dp)
                .border(1.5.dp, colors.textPrimary, CircleShape)
                .padding(start = 10.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(MapIcons.Recenter, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.map_recenter), style = TasksType.subhead.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun EmptyCard() {
    val colors = TasksTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).background(colors.surface2, CircleShape), contentAlignment = Alignment.Center) {
            Icon(MapIcons.PinPlus, null, tint = colors.textSecondary, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.map_empty_title), style = TasksType.button.copy(lineHeight = 21.sp), color = colors.textPrimary)
            Text(stringResource(R.string.map_empty_body), style = TasksType.subhead, color = colors.textSecondary)
        }
    }
}

@Composable
private fun NextCard(next: NextStopModel, past: Boolean, onSelect: (Int) -> Unit) {
    val colors = TasksTheme.colors
    val priority = priorityLabel(next.priority)
    val description = stringResource(R.string.map_a11y_next, next.timeText, next.title, priority, next.order)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(role = Role.Button) { onSelect(next.taskId) }
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).background(colors.inverse, CircleShape), contentAlignment = Alignment.Center) {
            Text(next.order.toString(), style = TasksType.caption.copy(fontWeight = FontWeight.Bold), color = colors.onInverse)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            val away = next.minutesAway
            Text(
                if (away != null) stringResource(R.string.map_next, next.timeText, durationText(away)) else next.timeText,
                style = TasksType.caption.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = colors.textSecondary,
            )
            Text(next.title, style = TasksType.button.copy(lineHeight = 21.sp), color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        MapPriorityBadge(next.priority, tinted = past)
    }
}

@Composable
private fun RouteList(model: MapScreenModel, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val stopsInOrder = stringResource(R.string.map_a11y_stops_in_order)
    LazyColumn(
        modifier
            .fillMaxWidth()
            .semantics { contentDescription = stopsInOrder },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 24.dp),
    ) {
        item(key = "start") {
            RouteRow(
                kind = RowKind.Start,
                row = null,
                start = model.start,
                onClick = {},
            )
        }
        itemsIndexed(model.rows, key = { _, r -> r.taskId }) { _, row ->
            RouteRow(kind = RowKind.Stop, row = row, start = null, onClick = { onSelect(row.taskId) })
        }
    }
}

private enum class RowKind { Start, Stop }

/** RouteStopRow (components/RouteStopRow.dc.html): order badge + connector, then time line, title, address, leg to the next stop. */
@Composable
private fun RouteRow(kind: RowKind, row: StopRowModel?, start: StartRowModel?, onClick: () -> Unit) {
    val colors = TasksTheme.colors
    val isStart = kind == RowKind.Start
    val past = row?.past == true
    val done = row?.status == PinStatus.Done
    val overdue = row?.status == PinStatus.Overdue
    val leg = if (isStart) start?.legToNext else row?.legToNext
    val badgeAlpha = if (past && !overdue) 0.45f else 1f

    val timeLine: String
    val titleText: String
    val addressText: String
    val description: String
    if (isStart) {
        timeLine = stringResource(R.string.map_start)
        titleText = stringResource(R.string.map_start_address)
        addressText = start?.leaveByText?.let {
            stringResource(if (start.startIsPast) R.string.map_left else R.string.map_leave_by, it)
        }.orEmpty()
        description = stringResource(R.string.map_a11y_start, stringResource(R.string.map_start_address_long))
    } else {
        val r = row!!
        val time = r.timeText
        timeLine = when {
            time == null -> stringResource(R.string.map_no_time)
            overdue -> stringResource(R.string.map_stop_overdue, time)
            done -> stringResource(R.string.map_stop_done, time)
            else -> time
        }
        titleText = r.title
        addressText = r.address
        val pin = PinModel(r.taskId, r.order, com.example.tasks.shared.domain.GeoPoint(0.0, 0.0), r.priority, r.status, r.past, r.timeText, r.title, selected = false, lifted = false)
        description = stringResource(R.string.map_a11y_row_hint, pinDescription(pin))
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Badge + connector (decorative; the row text is the accessible element).
        Column(Modifier.width(28.dp).clearAndSetSemantics { }, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(28.dp)
                    .graphicsAlpha(badgeAlpha)
                    .then(
                        when {
                            isStart -> Modifier.background(MapColors.startBg, CircleShape).then(if (colors.isDark) Modifier.border(1.5.dp, Color(0xFF3A3A36), CircleShape) else Modifier)
                            done -> Modifier.background(colors.accent, CircleShape)
                            overdue -> Modifier.background(colors.surface, CircleShape).border(2.dp, colors.danger, CircleShape)
                            row?.order == null -> Modifier.background(colors.surface2, CircleShape)
                            else -> Modifier.background(colors.textPrimary, CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isStart -> Icon(MapIcons.HomeSmall, null, tint = colors.accent, modifier = Modifier.size(15.dp))
                    done -> Icon(MapIcons.PinCheck, null, tint = Color(0xFF0A0A0A), modifier = Modifier.size(15.dp))
                    row?.order == null -> Icon(MapIcons.Pin, null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                    else -> Text(
                        row.order.toString(),
                        style = TasksType.caption.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                        color = if (overdue) colors.danger else colors.bg,
                    )
                }
            }
            if (leg != null) {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .heightIn(min = 12.dp)
                        .background(MapColors.connector(colors), RoundedCornerShape(1.dp)),
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(if (isStart) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick))
                .semantics(mergeDescendants = true) { contentDescription = description; if (!isStart) role = Role.Button }
                .padding(bottom = if (leg != null) 4.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        timeLine,
                        style = TasksType.caption.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                        color = when {
                            overdue -> colors.danger
                            done || past -> colors.textTertiary
                            else -> colors.textSecondary
                        },
                    )
                    Text(
                        titleText,
                        style = TasksType.button.copy(lineHeight = 21.sp),
                        color = if (done || past) colors.textTertiary else colors.textPrimary,
                        textDecoration = if (done) TextDecoration.LineThrough else null,
                    )
                    if (addressText.isNotEmpty()) Text(addressText, style = TasksType.caption, color = colors.textSecondary)
                }
                if (!isStart && row != null) MapPriorityBadge(row.priority, tinted = past)
            }
            if (leg != null) {
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val legColor = if (past) colors.textTertiary else colors.textSecondary
                    Icon(MapIcons.Car, null, tint = legColor, modifier = Modifier.size(14.dp))
                    Text(
                        legText(leg),
                        style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, fontFeatureSettings = "tnum"),
                        color = legColor,
                    )
                }
            }
        }
    }
}

private fun Modifier.graphicsAlpha(alpha: Float): Modifier =
    if (alpha >= 1f) this else this.then(Modifier.graphicsLayer { this.alpha = alpha })
