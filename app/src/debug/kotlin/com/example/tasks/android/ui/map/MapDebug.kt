package com.example.tasks.android.ui.map

import android.content.Intent
import com.example.tasks.android.AppContainer
import com.example.tasks.shared.presentation.MapViewModel
import kotlinx.coroutines.flow.first

/**
 * DEBUG variant (release has a same-named no-op object). Applies [MapDebugConfig] from the launch intent: freezes the
 * app clock BEFORE the first load (MainActivity calls [configure] before `startLoading()`, so a fresh database is seeded
 * and the demo route is dated from the frozen "now"), points OSRM at another host, and scripts the Map screen once.
 */
object MapDebug {
    @Volatile
    private var config = MapDebugConfig()

    @Volatile
    private var applied = false

    val forceOffline: Boolean get() = config.offline
    val startOnMap: Boolean get() = config.opensMap

    fun configure(intent: Intent?, container: AppContainer) {
        config = if (intent == null) {
            MapDebugConfig()
        } else {
            MapDebugConfig.parse(
                now = intent.getStringExtra(MapDebugConfig.EXTRA_NOW),
                day = intent.getStringExtra(MapDebugConfig.EXTRA_DAY),
                select = intent.takeIf { it.hasExtra(MapDebugConfig.EXTRA_SELECT) }?.getIntExtra(MapDebugConfig.EXTRA_SELECT, 0),
                expanded = intent.getBooleanExtra(MapDebugConfig.EXTRA_EXPANDED, false),
                move = intent.getBooleanExtra(MapDebugConfig.EXTRA_MOVE, false),
                newHere = intent.getStringExtra(MapDebugConfig.EXTRA_NEW_HERE),
                offline = intent.getBooleanExtra(MapDebugConfig.EXTRA_OFFLINE, false),
                osrmHost = intent.getStringExtra(MapDebugConfig.EXTRA_OSRM_HOST),
            )
        }
        applied = false
        // Only touch the clock when asked, so a normal debug launch follows the real time.
        container.clock.frozen = config.frozenNow
        container.osrmBaseUrl = config.osrmBaseUrl ?: com.example.tasks.shared.routing.OsrmRoutingApi.DEFAULT_BASE_URL
    }

    /** Runs the scripted actions once per launch, after the tasks are loaded. */
    suspend fun apply(viewModel: MapViewModel) {
        val c = config
        if (applied || !c.opensMap) return
        applied = true
        viewModel.uiState.first { !it.isTasksLoading }
        c.day?.let(viewModel::selectDay)
        if (c.expanded) viewModel.setSheetExpanded(true)
        if (c.offline) viewModel.reportTilesOffline(true)
        c.selectOrder?.let { order ->
            val stop = viewModel.uiState.first { s -> s.day == (c.day ?: s.day) && s.stops.any { it.order == order } }.stops.first { it.order == order }
            viewModel.selectTask(stop.task.id)
            if (c.move) viewModel.startMove(stop.task.id)
        }
        c.newHere?.let(viewModel::longPress)
    }
}
