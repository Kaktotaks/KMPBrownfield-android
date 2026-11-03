package com.example.tasks.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** SortPill (§2): h 40, 1.5 `textPrimary` border; label = current sort; opens a menu with a check on the current one. */
@Composable
fun <T> SortPill(
    label: String,
    options: List<SelectableOption<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TasksTheme.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier
                .heightIn(min = 40.dp)
                .clip(TasksShapes.pill)
                .border(1.5.dp, colors.textPrimary, TasksShapes.pill)
                .clickable(enabled = enabled, role = Role.Button, onClickLabel = stringResource(R.string.sort_label)) { open = true }
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TasksIcons.Sort, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
            Text(label, style = TasksType.subhead.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.textPrimary)
            Icon(TasksIcons.ChevronDown, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(14.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, style = TasksType.body, color = colors.textPrimary) },
                    onClick = {
                        onSelect(option.value)
                        open = false
                    },
                    trailingIcon = {
                        if (option.isSelected) Icon(TasksIcons.Check, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}
