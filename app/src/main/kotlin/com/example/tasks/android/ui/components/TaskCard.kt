package com.example.tasks.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.android.ui.voice.SpokenDuration
import com.example.tasks.android.ui.voice.VoiceNoteIndicator
import com.example.tasks.android.ui.voice.asText
import com.example.tasks.shared.domain.Priority

/**
 * TaskCard (§2): `surface`, r `l`, padding 14 v / 16 h. Whole card opens detail; [CheckCircle] is a separate target.
 * Accessibility: the content is ONE element ("title, High priority, Due …, overdue") with role Button and a
 * custom action that toggles; the checkbox stays individually focusable (§7).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    title: String,
    dueDate: String,
    priority: Priority,
    isDone: Boolean,
    isOverdue: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    /** Voice-note indicator text ("0:42"); null = no voice note. */
    voiceDurationText: String? = null,
    voiceDurationMs: Long? = null,
) {
    val colors = TasksTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = androidx.compose.animation.core.tween(Motion.press, easing = Motion.easeOut),
        label = "cardPress",
    )
    val titleColor by animateColorAsState(
        if (isDone) colors.textTertiary else colors.textPrimary,
        emphasizedSpec(Motion.short), label = "titleColor",
    )
    val dueText = stringResource(R.string.due_format, dueDate)
    val priorityName = stringResource(R.string.a11y_priority, priority.displayName)
    val overdueWord = stringResource(R.string.a11y_overdue)
    val doneWord = stringResource(R.string.a11y_done)
    val toggleAction = stringResource(if (isDone) R.string.mark_active else R.string.mark_done)
    val voiceLabel = if (voiceDurationText != null) {
        stringResource(R.string.voice_a11y_row, SpokenDuration.of(voiceDurationMs ?: 0L).asText())
    } else {
        null
    }
    val cardLabel = listOfNotNull(
        title, priorityName, dueText,
        if (isDone) doneWord else null,
        if (isOverdue) overdueWord else null,
        voiceLabel,
    ).joinToString(", ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(TasksShapes.l)
            .background(colors.surface)
            // Touch handling for the whole card; semantics live on the content column below.
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() },
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CheckCircle(
            checked = isDone,
            isOverdue = isOverdue,
            onToggle = onToggle,
            label = stringResource(if (isDone) R.string.a11y_mark_not_done else R.string.a11y_mark_done, title),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics {
                    contentDescription = cardLabel
                    role = Role.Button
                    onClick { onClick(); true }
                    customActions = listOf(CustomAccessibilityAction(toggleAction) { onToggle(); true })
                },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = title,
                    style = TasksType.headline,
                    color = titleColor,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
                PriorityBadge(priority)
            }
            // The indicator joins the due line behind a 1 x 14 divider and wraps to its own line when it does not fit.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                DueLine(dueDate = dueDate, isOverdue = isOverdue, isDone = isDone)
                if (voiceDurationText != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(width = 1.dp, height = 14.dp).background(colors.border))
                        VoiceNoteIndicator(voiceDurationText)
                    }
                }
            }
        }
    }
}

/** CheckCircle (§2): 28 dp visible, 48 dp touch target (Compose expands it automatically). M1 animations. */
@Composable
fun CheckCircle(
    checked: Boolean,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = TasksTheme.colors
    val haptic = LocalHapticFeedback.current
    val fill by animateColorAsState(
        if (checked) colors.accent else androidx.compose.ui.graphics.Color.Transparent,
        emphasizedSpec(Motion.short), label = "checkFill",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.6f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "checkScale",
    )
    val ringColor = if (isOverdue && !checked) colors.danger else colors.textPrimary
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .then(if (checked) Modifier else Modifier.border(BorderStroke(2.dp, ringColor), CircleShape))
            .background(fill)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onToggle()
                },
            )
            .clearAndSetSemantics {
                contentDescription = label
                role = Role.Checkbox
                toggleableState = androidx.compose.ui.state.ToggleableState(checked)
                onClick { onToggle(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = TasksIcons.CheckBold,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
            )
        }
    }
}

/** PriorityBadge (§2): r `xs`, padding 3 v / 8 h, always shows the word. */
@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Text(
        text = priority.displayName.uppercase(),
        style = TasksType.badge,
        color = colors.onPriority(priority),
        modifier = modifier
            .background(colors.priorityFill(priority), TasksShapes.xs)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** StatusBadge (§2, detail hero, on `heroBg`): Active = accent outline, Done = accent fill. 12 sp. */
@Composable
fun StatusBadge(isDone: Boolean, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val text = stringResource(if (isDone) R.string.status_done else R.string.status_active).uppercase()
    val shape = TasksShapes.xs
    Text(
        text = text,
        style = TasksType.badge.copy(fontSize = 12.sp),
        color = if (isDone) colors.onAccent else colors.accent,
        modifier = modifier
            .then(if (isDone) Modifier.background(colors.accent, shape) else Modifier.border(1.5.dp, colors.accent, shape))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Due line (§2): "Due 5 Nov 2026"; overdue and not done: clock icon + "… · Overdue" in `danger`, 600. */
@Composable
fun DueLine(dueDate: String, isOverdue: Boolean, isDone: Boolean, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val due = stringResource(R.string.due_format, dueDate)
    if (isOverdue && !isDone) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(TasksIcons.Clock, contentDescription = null, tint = colors.danger, modifier = Modifier.size(14.dp))
            Text(
                text = "$due ${stringResource(R.string.overdue_suffix)}",
                style = TasksType.subhead.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = colors.danger,
            )
        }
    } else {
        val color by animateColorAsState(
            if (isDone) colors.textTertiary else colors.textSecondary,
            emphasizedSpec(Motion.short), label = "dueColor",
        )
        Text(due, style = TasksType.subhead, color = color, modifier = modifier)
    }
}
