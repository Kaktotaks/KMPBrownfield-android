package com.example.tasks.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** HeroCard (§2): `heroBg`, r `xl`, padding 22 (detail) / 24 (stats). */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 22.dp,
    containerColor: Color = TasksTheme.colors.heroBg,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(TasksShapes.xl)
            .background(containerColor)
            .padding(padding),
        content = content,
    )
}

data class InfoRow(val icon: ImageVector, val label: String, val value: String)

/** InfoRowsCard (§2): `surface`, r `l`, 18 h padding, rows 52 dp with 1 dp `border` dividers. */
@Composable
fun InfoRowsCard(rows: List<InfoRow>, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(TasksShapes.l)
            .background(colors.surface)
            .padding(horizontal = 18.dp),
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(row.icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                Text(row.label, style = TasksType.body, color = colors.textSecondary, modifier = Modifier.weight(1f))
                Text(row.value, style = TasksType.bodyStrong, color = colors.textPrimary)
            }
            if (index < rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.border)
        }
    }
}

enum class StatTileVariant { Default, Done, Overdue }

/** StatTile (§2): r `l`, padding 16 v / 18 h, label `subhead` + number `statNumber` (tabular). */
@Composable
fun StatTile(label: String, number: Int, variant: StatTileVariant, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val (container, labelColor, numberColor) = when (variant) {
        StatTileVariant.Default -> Triple(colors.surface, colors.textSecondary, colors.textPrimary)
        StatTileVariant.Done -> Triple(colors.accent, colors.onAccentMuted, colors.onAccent)
        StatTileVariant.Overdue -> Triple(colors.dangerContainer, colors.onDangerContainer, colors.onDangerContainer)
    }
    Column(
        modifier
            .clip(TasksShapes.l)
            .background(container)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(label, style = TasksType.subhead, color = labelColor)
        Text(number.toString(), style = TasksType.statNumber, color = numberColor)
    }
}

/** CompletionRing (§2): 104 x 104, radius 42, stroke 14, round caps, starts at 12 o'clock, clockwise. */
@Composable
fun CompletionRing(progress: Float, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Canvas(modifier.size(104.dp)) {
        val stroke = 14.dp.toPx()
        val radius = 42.dp.toPx()
        val topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius)
        val arcSize = Size(radius * 2, radius * 2)
        drawArc(colors.ringTrack, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
        val sweep = progress.coerceIn(0f, 1f) * 360f
        if (sweep > 0f) {
            drawArc(
                colors.accent, -90f, sweep, useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}

/** PriorityBar (§2): label + "N tasks"; bar h 10, `surface2` track; [fraction] = count / total, already animated. */
@Composable
fun PriorityBar(label: String, countText: String, fraction: Float, fill: Color, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = TasksType.label15, color = colors.textPrimary)
            Text(countText, style = TasksType.subhead15, color = colors.textSecondary)
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(TasksShapes.pill).background(colors.surface2)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .clip(TasksShapes.pill)
                    .background(fill),
            )
        }
    }
}
