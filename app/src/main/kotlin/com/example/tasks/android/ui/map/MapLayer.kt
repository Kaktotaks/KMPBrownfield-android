package com.example.tasks.android.ui.map

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.domain.GeoPoint
import com.example.tasks.shared.presentation.MapUiState
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.roundToInt

/** `true` while the device has an internet-capable default network (airplane mode / no connection = false). */
@Composable
private fun rememberOnline(): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(isOnline(context)) }
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { state.value = true }
            override fun onLost(network: Network) { state.value = isOnline(context) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                state.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }
    return state
}

private fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * The map layer (§3.4): ONE clipped box with its own stacking context holding the MapLibre `MapView` and the Compose pin
 * overlay (start marker, task pins, dropped pin, move ghost). Everything that must stay above (selector, chips, sheets,
 * tab bar) is a sibling drawn after this box by [MapScreen], so no annotation can ever draw over them, also while the
 * camera moves. Pins are positioned with `projection.toScreenLocation` in layout lambdas that read `controller.tick`
 * (bumped by every camera move), so they follow the map without recomposing.
 */
@Composable
internal fun MapLayer(
    state: MapUiState,
    model: MapScreenModel,
    lines: RouteLines,
    controller: MapController,
    onPinClick: (Int) -> Unit,
    onMapClick: () -> Unit,
    onLongPress: (GeoPoint) -> Unit,
    onMoveCandidate: (GeoPoint) -> Unit,
    onTilesOffline: (Boolean) -> Unit,
    onAddHere: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dark = TasksTheme.colors.isDark
    val reduce = LocalReduceMotion.current
    val online by rememberOnline()

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }
    val latestLongPress by rememberUpdatedState(onLongPress)
    val latestClick by rememberUpdatedState(onMapClick)
    val latestMove by rememberUpdatedState(onMoveCandidate)
    val latestOffline by rememberUpdatedState(onTilesOffline)
    val moving by rememberUpdatedState(state.move != null)
    val onlineNow by rememberUpdatedState(online)
    var mapReady by remember { mutableStateOf(false) }
    // A failed tile / TileJSON request since the last (re)load; a rendered frame only clears "offline" without one.
    var sawFailure by remember { mutableStateOf(false) }

    // Lifecycle forwarding: the observer replays CREATE/START/RESUME to the already-resumed owner.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Leaving the Map tab disposes the composition: release the GL resources now.
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
            controller.detach()
        }
    }

    DisposableEffect(mapView) {
        controller.mapView = mapView
        var userGesture = false
        mapView.addOnDidFailLoadingMapListener {
            sawFailure = true
            latestOffline(true)
        }
        mapView.addOnDidFinishRenderingMapListener { fully ->
            if (fully && !sawFailure && onlineNow && !MapDebug.forceOffline) latestOffline(false)
        }
        mapView.getMapAsync { map: MapLibreMap ->
            controller.attach(map)
            map.addOnMapLongClickListener { ll ->
                mapView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                latestLongPress(GeoPoint(ll.latitude, ll.longitude))
                true
            }
            map.addOnMapClickListener {
                latestClick()
                false
            }
            map.addOnCameraMoveStartedListener { reason ->
                userGesture = reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE
            }
            map.addOnCameraMoveListener {
                if (userGesture && moving) controller.fromScreen(mapView.width / 2f, mapView.height / 2f)?.let(latestMove)
            }
            map.addOnCameraIdleListener {
                if (userGesture && moving) controller.fromScreen(mapView.width / 2f, mapView.height / 2f)?.let(latestMove)
                userGesture = false
            }
            sawFailure = false
            controller.loadStyle(dark, reduce)
            mapReady = true
        }
        onDispose { }
    }

    // Light / dark: switch the shared style with the system appearance.
    var loadedDark by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(mapReady, dark) {
        if (mapReady) {
            if (loadedDark != null && loadedDark != dark) {
                sawFailure = false
                controller.loadStyle(dark, reduce)
            }
            loadedDark = dark
        }
    }
    // Network came back: reload the style so tiles are requested again.
    var wasOnline by remember { mutableStateOf(online) }
    LaunchedEffect(online, mapReady) {
        if (!mapReady) return@LaunchedEffect
        if (!online) {
            latestOffline(true)
        } else if (!wasOnline) {
            sawFailure = false
            controller.loadStyle(dark, reduce)
        }
        wasOnline = online
    }
    // Route layers follow the shared state; the route dims while a pin is moved.
    LaunchedEffect(lines, controller.styleReady, state.move != null) {
        if (controller.styleReady) controller.setRoute(lines, dimRoute = state.move != null, animate = true)
    }

    val mapLabel = stringResource(R.string.map_a11y_map)
    val addHere = stringResource(R.string.map_a11y_add_here)

    // Day change: pins fade / scale in (200 ms), a 150 ms cross-fade under reduced motion.
    var shownDay by remember { mutableStateOf(state.day) }
    val dayAlpha = remember { Animatable(1f) }
    LaunchedEffect(state.day) {
        if (state.day != shownDay) {
            shownDay = state.day
            dayAlpha.snapTo(0f)
            dayAlpha.animateTo(1f, tween(if (reduce) MapMotion.DAY_FADE_REDUCED_MS else MapMotion.DAY_FADE_MS, easing = MapMotion.emphasized))
        }
    }

    Box(modifier.clipToBounds()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = mapLabel
                    traversalIndex = 100f
                    customActions = listOf(CustomAccessibilityAction(addHere) { onAddHere(); true })
                },
        )
        PinsOverlay(
            state = state,
            model = model,
            controller = controller,
            dayAlpha = { dayAlpha.value },
            reduce = reduce,
            onPinClick = onPinClick,
        )
    }
}

@Composable
private fun PinsOverlay(
    state: MapUiState,
    model: MapScreenModel,
    controller: MapController,
    dayAlpha: () -> Float,
    reduce: Boolean,
    onPinClick: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val colors = TasksTheme.colors
    val anchorX = with(density) { PinGeometry.ANCHOR_X.dp.toPx() }
    val anchorY = with(density) { PinGeometry.ANCHOR_Y.dp.toPx() }
    val minGap = with(density) { 24.dp.toPx() }
    val move = state.move
    val draft = state.newTaskDraft

    // Time labels of closely stacked pins: hide the lower one's label (order badges always stay).
    val hiddenLabels by remember(model.pins) {
        derivedStateOf {
            hiddenTimeLabels(
                model.pins.filter { !it.lifted }.mapNotNull { p -> controller.project(p.point)?.let { PinAnchor(p.taskId, it.x, it.y) } },
                minGap,
            )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val a = dayAlpha()
                alpha = a
                if (!reduce) {
                    val s = 0.9f + 0.1f * a
                    scaleX = s
                    scaleY = s
                }
            }
            .semantics { isTraversalGroup = true; traversalIndex = 3f },
    ) {
        // Dashed link between the ghost (original place) and the moving pin.
        if (move != null) {
            val ghostColor = colors.textSecondary
            Canvas(Modifier.fillMaxSize()) {
                val g = controller.project(move.originalPlace.point) ?: return@Canvas
                drawLine(
                    ghostColor,
                    g,
                    Offset(size.width / 2f, size.height / 2f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                )
            }
            Box(
                Modifier.offset {
                    val g = controller.project(move.originalPlace.point)
                    if (g == null) IntOffset(-10_000, -10_000) else IntOffset((g.x - 18.dp.toPx()).roundToInt(), (g.y - 18.dp.toPx()).roundToInt())
                },
            ) { MoveGhostView() }
        }

        // Start marker (first in the swipe order).
        Box(
            Modifier
                .offset {
                    val p = controller.project(state.start.point)
                    if (p == null) IntOffset(-10_000, -10_000) else IntOffset((p.x - 20.dp.toPx()).roundToInt(), (p.y - 20.dp.toPx()).roundToInt())
                }
                .semantics { traversalIndex = 0f },
        ) { StartMarkerView() }

        // Pins: route order (the swipe order), the selected one drawn last so it is above its neighbours.
        val ordered = model.pins.filter { !it.lifted }.sortedBy { if (it.selected) 1 else 0 }
        ordered.forEach { pin ->
            key(pin.taskId) {
                val description = pinDescription(pin)
                Box(
                    Modifier
                        .offset {
                            val p = controller.project(pin.point)
                            if (p == null) IntOffset(-10_000, -10_000) else IntOffset((p.x - anchorX).roundToInt(), (p.y - anchorY).roundToInt())
                        }
                        .semantics { traversalIndex = (pin.order ?: (1000 + pin.taskId)).toFloat() },
                ) {
                    TaskPinView(
                        pin = pin,
                        description = description,
                        showTime = pin.taskId !in hiddenLabels,
                        onClick = { onPinClick(pin.taskId) },
                    )
                }
            }
        }

        // Temporary pin of a long-press.
        if (draft != null) {
            val label = stringResource(R.string.map_a11y_dropped_pin, draft.address ?: stringResource(R.string.map_finding_address))
            Box(
                Modifier.offset {
                    val p = controller.project(draft.point)
                    if (p == null) IntOffset(-10_000, -10_000) else IntOffset((p.x - 38.dp.toPx()).roundToInt(), (p.y - 38.dp.toPx()).roundToInt())
                },
            ) { DroppedPinView(label) }
        }

        // The pin being moved: lifted, fixed at the centre of the map while the map pans underneath.
        if (move != null) {
            model.pins.firstOrNull { it.lifted }?.let { pin ->
                Box(Modifier.align(Alignment.Center).offset(y = (38 - PinGeometry.ANCHOR_Y).dp)) {
                    TaskPinView(pin = pin, description = pinDescription(pin), showTime = false)
                }
            }
        }
    }
}

/** Haptic `CONFIRM` (API 30+, else a virtual key) for a saved pin. */
internal fun View.confirmHaptic() {
    performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
}
