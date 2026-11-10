package com.example.tasks.android.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.theme.InterTight
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.DniproBounds
import com.example.tasks.shared.presentation.DayLabel
import com.example.tasks.shared.presentation.MapStop
import kotlinx.datetime.LocalDate

/** "Today, 13:15" / "Tomorrow, 13:15" / "Mon 9 Nov, 13:15". */
@Composable
private fun dayAndTime(label: DayLabel, day: LocalDate, time: String): String = when (label) {
    DayLabel.TODAY -> stringResource(R.string.map_when_today, time)
    DayLabel.TOMORROW -> stringResource(R.string.map_when_day_time, stringResource(R.string.map_day_tomorrow), time)
    DayLabel.YESTERDAY -> stringResource(R.string.map_when_day_time, stringResource(R.string.map_day_yesterday), time)
    DayLabel.NONE -> stringResource(R.string.map_when_day_time, dateText(day), time)
}

/** Second line of the task sheet header: "Today, 13:15 · Stop 3 of 5", "11:00 · Overdue · Stop 2 of 5", … */
@Composable
internal fun taskWhenText(stop: MapStop, label: DayLabel, day: LocalDate, routedCount: Int): String {
    val time = stop.task.time
    val order = stop.order
    if (time == null || order == null) {
        val dayWord = when (label) {
            DayLabel.TODAY -> stringResource(R.string.map_day_today)
            DayLabel.TOMORROW -> stringResource(R.string.map_day_tomorrow)
            DayLabel.YESTERDAY -> stringResource(R.string.map_day_yesterday)
            DayLabel.NONE -> dateText(day)
        }
        return stringResource(R.string.map_when_untimed, dayWord)
    }
    val timeText = formatTime(time)
    val base = when {
        stop.isOverdue -> stringResource(R.string.map_stop_overdue, timeText)
        stop.isDone -> stringResource(R.string.map_stop_done, timeText)
        else -> dayAndTime(label, day, timeText)
    }
    return stringResource(R.string.map_when_stop, base, order, routedCount)
}

/**
 * TaskMapSheet (components/TaskMapSheet.dc.html): a non-modal bottom sheet (no scrim) over the map for the selected pin:
 * priority badge + time, title, address, notes, the unchanged VoiceNoteCard ([voiceSection]) and four actions.
 * Past stops use the tinted badge and `textTertiary` title (never opacity on text).
 */
@Composable
internal fun TaskMapSheet(
    stop: MapStop,
    dayLabel: DayLabel,
    day: LocalDate,
    routedCount: Int,
    maxHeight: Dp,
    voiceSection: @Composable () -> Unit,
    onClose: () -> Unit,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TasksTheme.colors
    val past = stop.isPast
    val whenText = taskWhenText(stop, dayLabel, day, routedCount)
    val address = if (DniproBounds.contains(stop.place.point)) stringResource(R.string.map_address_city, stop.place.address) else stop.place.address
    SheetSurface(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .heightIn(max = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 6.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MapPriorityBadge(stop.task.priority, tinted = past, large = true)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(MapIcons.ClockSmall, null, tint = if (stop.isOverdue) colors.danger else colors.textSecondary, modifier = Modifier.size(15.dp))
                    Text(
                        whenText,
                        style = TasksType.subhead.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                        color = if (stop.isOverdue) colors.danger else colors.textSecondary,
                        maxLines = 2,
                    )
                }
                val closeLabel = stringResource(R.string.map_close)
                Box(
                    Modifier
                        .requiredSize(48.dp)
                        .clickable(role = Role.Button, onClick = onClose)
                        .semantics { contentDescription = closeLabel },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(36.dp).background(colors.surface2, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(TasksIcons.Close, null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(
                stop.task.title,
                style = TextStyle(fontFamily = InterTight, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 31.sp, letterSpacing = (-0.03).sp),
                color = if (past) colors.textTertiary else colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(MapIcons.Pin, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                Text(address, style = TasksType.subhead.copy(fontSize = 15.sp), color = colors.textSecondary)
            }
            if (stop.task.notes.isNotBlank()) {
                Text(stop.task.notes, style = TasksType.body.copy(fontSize = 15.sp, lineHeight = 22.sp), color = colors.textPrimary)
            }
            voiceSection()
            Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(MapIcons.Check, stringResource(R.string.map_mark_done), colors.accent, colors.onAccent, onMarkDone, Modifier.weight(1f), enabled = !stop.isDone)
                ActionTile(TasksIcons.Pencil, stringResource(R.string.map_edit), colors.inverse, colors.onInverse, onEdit, Modifier.weight(1f))
                ActionTile(MapIcons.Move, stringResource(R.string.map_move_pin), colors.surface2, colors.textPrimary, onMove, Modifier.weight(1f))
                ActionTile(TasksIcons.Trash, stringResource(R.string.map_delete), colors.dangerContainer, colors.onDangerContainer, onDelete, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TasksTheme.colors
    val bg = if (enabled) container else colors.surface2
    val fg = if (enabled) content else colors.textTertiary
    Column(
        modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Text(label, style = TasksType.caption.copy(fontWeight = FontWeight.SemiBold), color = fg, maxLines = 1)
    }
}
