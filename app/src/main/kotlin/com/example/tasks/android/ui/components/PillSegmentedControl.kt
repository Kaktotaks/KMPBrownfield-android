package com.example.tasks.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/**
 * PillSegmentedControl (§2): `surface2` track, padding 3, selected = `inverse` (light) / `accent` (dark).
 * The thumb slides (M3: 250 ms emphasized). [segmentHeight] 34 (filter) / 36 (editor priority);
 * [dotColor] adds the 8 dp priority dot before each label.
 */
@Composable
fun <T> PillSegmentedControl(
    options: List<SelectableOption<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    segmentHeight: androidx.compose.ui.unit.Dp = 34.dp,
    enabled: Boolean = true,
    dotColor: ((T) -> Color)? = null,
) {
    val colors = TasksTheme.colors
    val thumbColor = if (colors.isDark) colors.accent else colors.inverse
    val onThumb = if (colors.isDark) colors.onAccent else colors.onInverse
    val selectedIndex = options.indexOfFirst { it.isSelected }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(TasksShapes.pill)
            .background(colors.surface2)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / options.size.coerceAtLeast(1)
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = emphasizedSpec(Motion.medium),
            label = "thumbOffset",
        )
        Box(
            Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
                .height(segmentHeight)
                .clip(TasksShapes.pill)
                .background(thumbColor),
        )
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.Start) {
            options.forEach { option ->
                val textColor by animateColorAsState(
                    if (option.isSelected) onThumb else colors.textPrimary,
                    emphasizedSpec(Motion.medium), label = "segmentText",
                )
                Row(
                    Modifier
                        .width(segmentWidth)
                        .height(segmentHeight)
                        .selectable(
                            selected = option.isSelected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(option.value) },
                        ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dotColor != null) {
                        Box(Modifier.size(8.dp).background(dotColor(option.value), CircleShape))
                    }
                    Text(
                        option.label,
                        style = if (option.isSelected) TasksType.bodyStrong else TasksType.bodyMedium,
                        color = textColor,
                    )
                }
            }
        }
    }
}
