package com.example.tasks.android.ui.map

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tasks.android.R
import com.example.tasks.android.TasksApplication
import com.example.tasks.android.ui.components.MapViewModelFactory
import com.example.tasks.android.ui.components.createVoiceNoteViewModel
import com.example.tasks.android.ui.taskeditor.TaskEditorSheet
import com.example.tasks.android.ui.taskeditor.toUtcMillis
import com.example.tasks.android.ui.taskeditor.utcMillisToLocalDate
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.android.ui.voice.VoiceCardVariant
import com.example.tasks.android.ui.voice.VoiceNoteSection
import com.example.tasks.android.voice.VoiceTargets
import com.example.tasks.shared.presentation.MapViewModel
import com.example.tasks.shared.presentation.VoiceNoteViewModel
import kotlinx.coroutines.delay

/**
 * Stateful entry point of the Map tab: collects the shared [MapViewModel], maps its state to [MapScreenModel] /
 * [RouteLines], forwards gestures, and hosts the dialogs (date picker, delete confirm) and the Editor sheet.
 * Camera moves (fit / focus / fly) are triggered here from state changes; the drawing itself is in [MapLayer].
 *
 * @param bottomInset height of the tab bar this screen is drawn over (the map fills the whole area).
 * @param onChromeHidden true while the task / new-task sheet covers the tab bar (TasksRoot then hides the bar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenRoute(
    bottomInset: Dp,
    onChromeHidden: (Boolean) -> Unit,
    onSettled: () -> Unit = {},
    viewModel: MapViewModel = viewModel(factory = MapViewModelFactory),
) {
    val colors = TasksTheme.colors
    val density = LocalDensity.current
    val reduce = LocalReduceMotion.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val model = remember(state) { state.toScreenModel() }
    val lines = remember(state) { state.toRouteLines() }
    val controller = remember { MapController() }
    val view = LocalView.current

    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Int?>(null) }
    var editTaskId by remember { mutableStateOf<Int?>(null) }
    var routeSheetHeight by remember { mutableStateOf(176.dp) }
    var taskSheetHeight by remember { mutableStateOf(0.dp) }
    var newTaskSheetHeight by remember { mutableStateOf(0.dp) }

    // The splash waits for the first screen to settle; starting on the Map (debug hook) must release it too.
    LaunchedEffect(state.isTasksLoading) { if (!state.isTasksLoading) onSettled() }
    // Debug screenshot script (no-op in release).
    LaunchedEffect(Unit) { MapDebug.apply(viewModel) }

    val taskSheetOpen = state.selectedStop != null && state.move == null && state.newTaskDraft == null
    val sheetCoversBar = taskSheetOpen || state.newTaskDraft != null
    LaunchedEffect(sheetCoversBar) { onChromeHidden(sheetCoversBar) }
    DisposableEffect(Unit) { onDispose { onChromeHidden(false) } }

    // Attribution (OpenStreetMap / OpenFreeMap) stays visible above the route sheet.
    LaunchedEffect(routeSheetHeight, bottomInset, controller.styleReady, sheetCoversBar) {
        if (!controller.styleReady) return@LaunchedEffect
        val bottom = with(density) { ((if (sheetCoversBar) 0.dp else routeSheetHeight) + bottomInset + 8.dp).roundToPx() }
        controller.setAttributionMargins(with(density) { 8.dp.roundToPx() }, bottom)
    }

    // Camera: fit to start + stops on first load, on every day change and on Recenter (§6).
    val fitRequest = remember { mutableStateOf(0) }
    LaunchedEffect(state.day, controller.styleReady, state.isTasksLoading, fitRequest.value) {
        if (!controller.styleReady || state.isTasksLoading) return@LaunchedEffect
        awaitMapSize(controller)
        val selectorBottom = (SELECTOR_TOP_GAP + SELECTOR_HEIGHT).value + statusBarHeightDp(view)
        val padding = fitPadding(selectorBottom, COLLAPSED_SHEET_HEIGHT.value + bottomInset.value, density.density)
        controller.fit(state.fitPoints(), padding, animate = fitRequest.value > 0 || controller.hasFitted)
        controller.hasFitted = true
    }

    // Tap a pin / row: the camera centres the pin above the task sheet.
    val selectedId = state.selectedTaskId
    LaunchedEffect(selectedId, taskSheetOpen, taskSheetHeight, controller.styleReady) {
        val stop = state.selectedStop ?: return@LaunchedEffect
        if (!taskSheetOpen || !controller.styleReady || taskSheetHeight == 0.dp) return@LaunchedEffect
        delay(if (reduce) 0 else 60)
        val top = with(density) { (SELECTOR_TOP_GAP + SELECTOR_HEIGHT + 24.dp).toPx() + statusBarHeightPx(view) }.toInt()
        controller.focus(stop.place.point, ViewportInsets(top, with(density) { taskSheetHeight.toPx() }.toInt()), animate = true)
    }

    // Long-press: the dropped pin is centred above the new-task sheet.
    val draft = state.newTaskDraft
    LaunchedEffect(draft?.point, newTaskSheetHeight, controller.styleReady) {
        val d = draft ?: return@LaunchedEffect
        if (!controller.styleReady || newTaskSheetHeight == 0.dp) return@LaunchedEffect
        val top = with(density) { (SELECTOR_TOP_GAP + 24.dp).toPx() + statusBarHeightPx(view) }.toInt()
        controller.focus(d.point, ViewportInsets(top, with(density) { minOf(newTaskSheetHeight, 420.dp).toPx() }.toInt()), animate = true)
    }

    // Move pin: the map flies to the pin's place, which then sits under the fixed centre pin.
    val moveTask = state.move?.taskId
    LaunchedEffect(moveTask, controller.styleReady) {
        val m = state.move ?: return@LaunchedEffect
        if (!controller.styleReady) return@LaunchedEffect
        controller.centerOn(m.originalPlace.point, animate = true)
    }

    MapScreen(
        state = state,
        model = model,
        lines = lines,
        controller = controller,
        bottomInset = bottomInset,
        onPrevious = viewModel::previousDay,
        onNext = viewModel::nextDay,
        onChooseDay = { showDatePicker = true },
        onSelect = viewModel::selectTask,
        onClearSelection = viewModel::clearSelection,
        onSheetExpanded = viewModel::setSheetExpanded,
        onRecenter = { fitRequest.value += 1 },
        onRetry = {
            viewModel.retry()
            viewModel.reportTilesOffline(false)
            controller.reload(colors.isDark, reduce)
        },
        onLongPress = viewModel::longPress,
        onAddHere = { controller.target()?.let(viewModel::longPress) },
        onMoveCandidate = viewModel::updateMoveCandidate,
        onMoveCancel = viewModel::cancelMove,
        onMoveDone = {
            viewModel.confirmMove()
            view.confirmHaptic()
        },
        onTilesOffline = viewModel::reportTilesOffline,
        onRouteSheetHeight = { routeSheetHeight = it },
        onTaskSheetHeight = { taskSheetHeight = it },
        onNewTaskSheetHeight = { newTaskSheetHeight = it },
        taskSheet = { stop, maxHeight ->
            TaskMapSheet(
                stop = stop,
                dayLabel = state.dayLabel,
                day = state.day,
                routedCount = state.routedStopsCount,
                maxHeight = maxHeight,
                voiceSection = { key(stop.task.id) { TaskVoiceSection(stop.task.id) } },
                onClose = viewModel::clearSelection,
                onMarkDone = { viewModel.markDone(stop.task.id) },
                onEdit = { editTaskId = stop.task.id },
                onMove = { viewModel.startMove(stop.task.id) },
                onDelete = { confirmDelete = stop.task.id },
            )
        },
        newTaskSheet = { d, maxHeight ->
            NewTaskAtLocationSheet(
                draft = d,
                day = state.day,
                dayLabel = state.dayLabel,
                mapViewModel = viewModel,
                maxHeight = maxHeight,
                onCancel = viewModel::cancelNewTask,
                onSaved = { view.confirmHaptic() },
            )
        },
        modifier = Modifier.fillMaxSize(),
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.day.toUtcMillis())
        // Lime is fill-only (§1.1): the selected day sits on the accent container.
        val pickerColors = DatePickerDefaults.colors(
            containerColor = colors.surface,
            selectedDayContainerColor = colors.accent,
            selectedDayContentColor = colors.onAccent,
            todayContentColor = colors.textPrimary,
            todayDateBorderColor = colors.textPrimary,
            selectedYearContainerColor = colors.accent,
            selectedYearContentColor = colors.onAccent,
            currentYearContentColor = colors.textPrimary,
            navigationContentColor = colors.textPrimary,
            dividerColor = colors.border,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = pickerColors,
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.selectDay(utcMillisToLocalDate(it)) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.map_save), style = TasksType.button, color = colors.textPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.map_cancel), style = TasksType.button, color = colors.textPrimary)
                }
            },
        ) { DatePicker(state = pickerState, colors = pickerColors) }
    }

    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = colors.surface,
            title = { Text(stringResource(R.string.map_delete_title), style = TasksType.sheetTitle, color = colors.textPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = null
                    viewModel.delete(id)
                }) { Text(stringResource(R.string.map_delete), style = TasksType.button, color = colors.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(stringResource(R.string.map_cancel), style = TasksType.button, color = colors.textPrimary)
                }
            },
        )
    }

    editTaskId?.let { id -> TaskEditorSheet(taskId = id, onDismiss = { editTaskId = null }) }
}

/** The voice note of the selected task: the unchanged VoiceNoteCard, driven by an ExistingTask VM owned by the sheet. */
@Composable
private fun TaskVoiceSection(taskId: Int) {
    val container = (LocalContext.current.applicationContext as TasksApplication).container
    val owner = remember(taskId) {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val voiceViewModel: VoiceNoteViewModel = viewModel(
        viewModelStoreOwner = owner,
        key = "voice-$taskId",
        factory = remember(taskId) {
            viewModelFactory { initializer { container.createVoiceNoteViewModel(VoiceTargets.forTask(taskId)) } }
        },
    )
    VoiceNoteSection(voiceViewModel, VoiceCardVariant.Detail)
}

private suspend fun awaitMapSize(controller: MapController) {
    var frames = 0
    while ((controller.mapView?.width ?: 0) == 0 && frames < 60) {
        delay(16)
        frames++
    }
}

private fun statusBarHeightPx(view: android.view.View): Int =
    androidx.core.view.ViewCompat.getRootWindowInsets(view)
        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())?.top ?: 0

private fun statusBarHeightDp(view: android.view.View): Float = statusBarHeightPx(view) / view.resources.displayMetrics.density
