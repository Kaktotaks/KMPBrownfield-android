package com.example.tasks.android.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.shared.domain.GeoPoint
import com.example.tasks.shared.presentation.MapStop
import com.example.tasks.shared.presentation.MapUiState
import com.example.tasks.shared.presentation.NewTaskDraft

/** Selector top offset below the status bar (design: 44 dp on a 24 dp bar) and its height. */
internal val SELECTOR_TOP_GAP = 20.dp
internal val SELECTOR_HEIGHT = 48.dp

/**
 * The Map tab layout (§3.4, same z-order as iOS): the clipped map layer at z 0, then the floating controls (z 10), the
 * route sheet (z 20), the tab bar (z 30, drawn by TasksRoot below this box; the sheet sits above it) and the modal-style
 * sheets (z 40, which cover the tab bar and never dim the map).
 *
 * [bottomInset] is the height of the tab bar this screen sits on (0 while it is hidden for the task / new-task sheet).
 */
@Composable
internal fun MapScreen(
    state: MapUiState,
    model: MapScreenModel,
    lines: RouteLines,
    controller: MapController,
    bottomInset: Dp,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChooseDay: () -> Unit,
    onSelect: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onSheetExpanded: (Boolean) -> Unit,
    onRecenter: () -> Unit,
    onRetry: () -> Unit,
    onLongPress: (GeoPoint) -> Unit,
    onAddHere: () -> Unit,
    onMoveCandidate: (GeoPoint) -> Unit,
    onMoveCancel: () -> Unit,
    onMoveDone: () -> Unit,
    onTilesOffline: (Boolean) -> Unit,
    onRouteSheetHeight: (Dp) -> Unit,
    onTaskSheetHeight: (Dp) -> Unit,
    onNewTaskSheetHeight: (Dp) -> Unit,
    taskSheet: @Composable (MapStop, Dp) -> Unit,
    newTaskSheet: @Composable (NewTaskDraft, Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduce = LocalReduceMotion.current
    val density = LocalDensity.current
    val selected = state.selectedStop
    val moving = state.move != null
    val draft = state.newTaskDraft
    val taskSheetOpen = selected != null && !moving && draft == null
    val routeSheetVisible = !taskSheetOpen && !moving && draft == null
    val selectorVisible = !moving && draft == null
    // Keep the last content while a sheet slides out (the state is already null by then).
    var shownStop by remember { mutableStateOf<MapStop?>(null) }
    if (selected != null) shownStop = selected
    var shownDraft by remember { mutableStateOf<NewTaskDraft?>(null) }
    if (draft != null) shownDraft = draft

    BoxWithConstraints(modifier.fillMaxSize().clipToBounds()) {
        val containerHeight = maxHeight
        val topInsetForSheets = SELECTOR_TOP_GAP + SELECTOR_HEIGHT + 24.dp

        // z 0: the map (own stacking context: tiles, route, start, pins, dropped pin, move ghost).
        Box(Modifier.fillMaxSize().zIndex(0f)) {
            MapLayer(
                state = state,
                model = model,
                lines = lines,
                controller = controller,
                onPinClick = onSelect,
                onMapClick = {
                    if (selected != null && !moving) {
                        onClearSelection()
                    } else if (collapsesOnMapTap(state.sheetExpanded, selected != null, moving, draft != null)) {
                        onSheetExpanded(false)
                    }
                },
                onLongPress = onLongPress,
                onMoveCandidate = onMoveCandidate,
                onTilesOffline = onTilesOffline,
                onAddHere = onAddHere,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // z 10: floating controls. The day selector is the first element for TalkBack, then the route sheet, then the pins.
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(10f)
                .statusBarsPadding()
                .padding(top = SELECTOR_TOP_GAP, start = 16.dp, end = 16.dp)
                .semantics { isTraversalGroup = true; traversalIndex = 0f },
        ) {
            if (selectorVisible) {
                DaySelector(
                    day = model.day,
                    label = model.dayLabel,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onChoose = onChooseDay,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                StatusChip(
                    kind = model.chip,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 58.dp),
                )
            }
            if (moving) {
                MovePinBanner(onCancel = onMoveCancel, onDone = onMoveDone, modifier = Modifier.align(Alignment.TopCenter))
            }
        }
        if (moving) {
            val move = state.move!!
            val address = move.candidateAddress ?: if (move.isResolvingAddress) stringResource(R.string.map_finding_address) else "%.4f, %.4f".format(move.candidate.lat, move.candidate.lng)
            Box(Modifier.fillMaxSize().zIndex(10f), contentAlignment = Alignment.BottomCenter) {
                MoveCard(address, Modifier.padding(start = 16.dp, end = 16.dp, bottom = bottomInset + 16.dp))
            }
        }

        // z 20: the route sheet, above the tab bar.
        if (routeSheetVisible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(20f)
                    .semantics { isTraversalGroup = true; traversalIndex = 1f },
                contentAlignment = Alignment.BottomCenter,
            ) {
                RouteSheet(
                    model = model,
                    expanded = state.sheetExpanded,
                    maxExpandedHeight = containerHeight - bottomInset - topInsetForSheets - 24.dp,
                    onExpandedChange = onSheetExpanded,
                    onSelect = onSelect,
                    onRecenter = onRecenter,
                    onHeightChange = onRouteSheetHeight,
                    modifier = Modifier.padding(bottom = bottomInset),
                )
            }
        }

        // z 40: the task sheet / new-task sheet: non-modal, no scrim, over the tab bar.
        val slide = if (reduce) {
            fadeIn(tween(MapMotion.DAY_FADE_REDUCED_MS)) to fadeOut(tween(MapMotion.DAY_FADE_REDUCED_MS))
        } else {
            (slideInVertically(tween(MapMotion.SHEET_SLIDE_MS, easing = MapMotion.emphasized)) { it } + fadeIn(tween(MapMotion.SHEET_SLIDE_MS))) to
                (slideOutVertically(tween(MapMotion.SHEET_SLIDE_MS, easing = MapMotion.emphasized)) { it } + fadeOut(tween(MapMotion.SHEET_SLIDE_MS)))
        }
        Box(Modifier.fillMaxSize().zIndex(40f), contentAlignment = Alignment.BottomCenter) {
            AnimatedVisibility(visible = taskSheetOpen, enter = slide.first, exit = slide.second) {
                shownStop?.let { stop ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .onSizeChanged { onTaskSheetHeight(with(density) { it.height.toDp() }) },
                    ) { taskSheet(stop, containerHeight - topInsetForSheets) }
                }
            }
            AnimatedVisibility(visible = draft != null, enter = slide.first, exit = slide.second) {
                shownDraft?.let { d ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .onSizeChanged { onNewTaskSheetHeight(with(density) { it.height.toDp() }) },
                    ) { newTaskSheet(d, containerHeight - topInsetForSheets - 56.dp) }
                }
            }
        }
    }
}
