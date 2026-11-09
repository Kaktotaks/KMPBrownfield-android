package com.example.tasks.android.ui.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.pressScale
import com.example.tasks.android.ui.theme.Inter
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.DayLabel
import kotlinx.datetime.LocalDate

private val badgeText = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.04.sp)

/** Priority badge of the route rows / next card (11 sp) and the task sheet (12 sp): solid, or tinted when the stop is past. */
@Composable
internal fun MapPriorityBadge(priority: Priority, tinted: Boolean, modifier: Modifier = Modifier, large: Boolean = false) {
    val colors = TasksTheme.colors
    val faded = fadedBadge(priority, colors.isDark)
    val container = if (tinted) faded.container else when (priority) {
        Priority.HIGH -> colors.priorityHigh
        Priority.MEDIUM -> colors.priorityMedium
        Priority.LOW -> colors.priorityLow
    }
    val content = if (tinted) faded.content else when (priority) {
        Priority.HIGH -> colors.onPriorityHigh
        Priority.MEDIUM -> colors.onPriorityMedium
        Priority.LOW -> colors.onPriorityLow
    }
    val pad = if (tinted) (if (large) 9.dp to 3.dp else 7.dp to 2.dp) else (if (large) 10.dp to 4.dp else 8.dp to 3.dp)
    Box(
        modifier
            .clip(TasksShapes.xs)
            .then(if (tinted) Modifier.border(1.dp, faded.border, TasksShapes.xs) else Modifier)
            .background(container)
            .padding(horizontal = pad.first, vertical = pad.second),
    ) {
        Text(
            priorityLabel(priority).uppercase(),
            style = badgeText.copy(fontSize = if (large) 12.sp else 11.sp, lineHeight = if (large) 16.sp else 14.sp),
            color = content,
            maxLines = 1,
        )
    }
}

/** A 48 dp touch target that looks like a [visual]-sized circle button (no ripple, a press scale instead). */
@Composable
private fun CircleIconButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier
            .requiredSize(48.dp)
            .pressScale(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClickLabel = null, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { content() }
}

/**
 * DaySelector (components/DaySelector.dc.html): one pill "‹ Today · Wed 4 Nov ›". Tapping the label opens the date picker.
 * Chevrons are drawn 44 dp as designed with a 48 dp touch target.
 */
@Composable
internal fun DaySelector(
    day: LocalDate,
    label: DayLabel,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TasksTheme.colors
    val date = dateText(day)
    val word = when (label) {
        DayLabel.TODAY -> stringResource(R.string.map_day_today)
        DayLabel.TOMORROW -> stringResource(R.string.map_day_tomorrow)
        DayLabel.YESTERDAY -> stringResource(R.string.map_day_yesterday)
        DayLabel.NONE -> null
    }
    val a11yDay = if (word != null) "$word, $date" else date
    val chooseDescription = stringResource(R.string.map_a11y_choose_day, a11yDay)
    Row(
        modifier
            .shadow(8.dp, CircleShape, clip = false, ambientColor = Color.Black.copy(0.12f), spotColor = Color.Black.copy(0.12f))
            .height(48.dp)
            .background(colors.border, CircleShape)
            .padding(1.dp)
            .background(colors.surface, CircleShape)
            .padding(1.dp)
            .semantics { role = Role.Button },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            CircleIconButton(stringResource(R.string.map_a11y_prev_day), onPrevious) {
                Icon(MapIcons.ChevronLeft, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Row(
            Modifier
                .height(44.dp)
                .widthIn(min = 168.dp)
                .clip(CircleShape)
                .background(colors.surface2)
                .clickable(role = Role.Button, onClick = onChoose)
                .semantics(mergeDescendants = true) { contentDescription = chooseDescription }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val strong = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp)
            Text(word ?: date, style = strong, color = colors.textPrimary, maxLines = 1)
            if (word != null) {
                Text("· $date", style = strong.copy(fontWeight = FontWeight.Medium), color = colors.textSecondary, maxLines = 1)
            }
        }
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            CircleIconButton(stringResource(R.string.map_a11y_next_day), onNext) {
                Icon(MapIcons.ChevronRight, null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** 16 dp spinner of the loading chip: a track ring and a 90° arc; static under reduced motion. */
@Composable
private fun Spinner(modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val reduce = LocalReduceMotion.current
    val angle = if (reduce) 0f else rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerAngle",
    ).value
    Canvas(modifier.size(16.dp).graphicsLayer { rotationZ = angle }) {
        val stroke = 3.dp.toPx() * (16f / 24f) * 1.5f
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(colors.border, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(colors.textPrimary, -90f, 90f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

/**
 * Status chip under the day selector (loading / failed / offline), announced politely (a polite live region).
 * Offline carries a Retry button.
 */
@Composable
internal fun StatusChip(kind: ChipKind, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    if (kind == ChipKind.None) return
    val colors = TasksTheme.colors
    val text = stringResource(
        when (kind) {
            ChipKind.Loading -> R.string.map_chip_loading
            ChipKind.Failed -> R.string.map_chip_failed
            else -> R.string.map_chip_offline
        },
    )
    Row(
        modifier
            .shadow(6.dp, CircleShape, clip = false, ambientColor = Color.Black.copy(0.12f), spotColor = Color.Black.copy(0.12f))
            .heightIn(min = 36.dp)
            .background(colors.border, CircleShape)
            .padding(1.dp)
            .background(colors.surface, CircleShape)
            .padding(start = 12.dp, end = if (kind == ChipKind.Offline) 0.dp else 12.dp, top = 5.dp, bottom = 5.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (kind) {
            ChipKind.Loading -> Spinner()
            ChipKind.Failed -> Icon(MapIcons.Info, null, tint = colors.danger, modifier = Modifier.size(16.dp))
            else -> Icon(MapIcons.WifiOff, null, tint = colors.danger, modifier = Modifier.size(16.dp))
        }
        Text(
            text,
            style = TasksType.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (kind == ChipKind.Offline) {
            Box(
                Modifier
                    .padding(end = 4.dp)
                    .height(28.dp)
                    .clip(CircleShape)
                    .background(colors.inverse)
                    .clickable(role = Role.Button, onClick = onRetry)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.map_retry), style = TasksType.caption.copy(fontWeight = FontWeight.Bold), color = colors.onInverse)
            }
        }
    }
}

/** MovePinBanner (components/MovePinBanner.dc.html): always-dark pill with Cancel and Done. */
@Composable
internal fun MovePinBanner(onCancel: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Row(
        modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(0.25f), spotColor = Color.Black.copy(0.25f))
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MapColors.bannerBorder(colors))
            .padding(1.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(MapColors.bannerBg)
            .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(MapIcons.Move, null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(
            stringResource(R.string.map_move_banner),
            style = TasksType.label15,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onCancel)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.height(40.dp).clip(CircleShape).background(MapColors.bannerButton).padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.map_move_cancel), style = TasksType.subhead.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }
        }
        Box(
            Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onDone)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.height(40.dp).clip(CircleShape).background(colors.accent).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.map_move_done), style = TasksType.subhead.copy(fontWeight = FontWeight.Bold), color = colors.onAccent)
            }
        }
    }
}

/** "New place" card above the tab bar while moving a pin. */
@Composable
internal fun MoveCard(address: String, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(0.12f), spotColor = Color.Black.copy(0.12f))
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stringResource(R.string.map_move_card_title).uppercase(), style = TasksType.overline, color = colors.textSecondary)
        Text(address, style = TasksType.headline, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(stringResource(R.string.map_move_card_body), style = TasksType.caption, color = colors.textSecondary)
    }
}

/** Decorative: hides a subtree from accessibility. */
internal fun Modifier.decorative(): Modifier = clearAndSetSemantics { }
