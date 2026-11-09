package com.example.tasks.android.ui.taskeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.DestructiveButton
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.components.TasksTextField
import com.example.tasks.android.ui.map.MapIcons
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.Place
import com.example.tasks.shared.presentation.PlaceSearchViewModel

/**
 * Address search of the Editor's Place row (shared [PlaceSearchViewModel]: Nominatim, 500 ms debounce, 3+ characters).
 * Picking a result sets the place; "Clear place" removes it. This is also the non-gesture alternative to long-press / drag.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSearchSheet(
    viewModel: PlaceSearchViewModel,
    currentPlace: Place?,
    onPick: (Place) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TasksTheme.colors
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = TasksShapes.sheetTop,
        containerColor = colors.bg,
        scrimColor = colors.scrim,
    ) {
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.editor_search_address),
                style = TasksType.sheetTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 4.dp).semantics { heading() },
            )
            TasksTextField(
                label = stringResource(R.string.editor_place),
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.editor_search_hint),
            )
            Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                val status = when {
                    state.failed -> stringResource(R.string.editor_search_failed)
                    state.noResults -> stringResource(R.string.editor_search_none)
                    state.query.trim().length < PlaceSearchViewModel.MIN_QUERY_LENGTH -> stringResource(R.string.editor_search_min)
                    state.isSearching -> stringResource(R.string.map_finding_address)
                    else -> null
                }
                if (status != null) Text(status, style = TasksType.subhead, color = if (state.failed) colors.danger else colors.textSecondary)
            }
            LazyColumn(Modifier.heightIn(max = 320.dp)) {
                itemsIndexed(state.results) { index, place ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Button) { onPick(place) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(MapIcons.Pin, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        Text(place.address, style = TasksType.body, color = colors.textPrimary)
                    }
                    if (index < state.results.lastIndex) HorizontalDivider(color = colors.border)
                }
            }
            if (currentPlace != null) {
                DestructiveButton(
                    text = stringResource(R.string.editor_clear_place),
                    onClick = onClear,
                    icon = TasksIcons.Trash,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(Modifier.padding(bottom = 16.dp))
        }
    }
}
