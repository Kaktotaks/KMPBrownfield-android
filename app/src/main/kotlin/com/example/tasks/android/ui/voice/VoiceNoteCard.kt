package com.example.tasks.android.ui.voice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** Everything the card can ask for; the stateful [VoiceNoteSection] wires these to the shared VM. */
data class VoiceCardActions(
    val onRecord: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onSeek: (Float) -> Unit = {},
    val onCycleSpeed: () -> Unit = {},
    val onReRecord: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onPermissionContinue: () -> Unit = {},
    val onPermissionNotNow: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
) {
    companion object {
        val None = VoiceCardActions()
    }
}

/** Where the card lives. */
enum class VoiceCardVariant {
    /** Task detail: a `surface` card (r20, padding 18) with the "VOICE NOTE" overline inside. */
    Detail,

    /** Editor sheet: the overline is a field label above; no own card; empty = dashed row, recorded = compact player. */
    Editor,
}

/**
 * VoiceNoteCard: one card for every state. The content changes with the one `emphasized` easing: record expands the
 * recorder (height + fade, 250 ms) and Stop collapses it to the player (250 ms); under reduced motion both become a
 * 150 ms cross-fade without size animation.
 */
@Composable
fun VoiceNoteCard(
    model: VoiceCardModel,
    actions: VoiceCardActions,
    modifier: Modifier = Modifier,
    variant: VoiceCardVariant = VoiceCardVariant.Detail,
) {
    val colors = TasksTheme.colors
    val reduceMotion = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    // Light tick on Start and Stop (notes board: HapticFeedbackConstants.CONFIRM).
    val tickedActions = actions.copy(
        onRecord = { haptic.performHapticFeedback(HapticFeedbackType.Confirm); actions.onRecord() },
        onStop = { haptic.performHapticFeedback(HapticFeedbackType.Confirm); actions.onStop() },
    )
    val content: @Composable () -> Unit = {
        AnimatedContent(
            targetState = model,
            contentKey = { it.kind },
            transitionSpec = {
                val d = if (reduceMotion) Motion.short else Motion.medium
                fadeIn(tween(d, easing = Motion.emphasized)) togetherWith fadeOut(tween(d, easing = Motion.emphasized)) using
                    SizeTransform(clip = false) { _, _ -> if (reduceMotion) snap() else tween(Motion.medium, easing = Motion.emphasized) }
            },
            label = "voiceCard",
        ) { m ->
            when (variant) {
                VoiceCardVariant.Detail -> DetailContent(m, tickedActions)
                VoiceCardVariant.Editor -> EditorContent(m, tickedActions)
            }
        }
    }
    when (variant) {
        VoiceCardVariant.Detail -> Column(
            modifier.fillMaxWidth().clip(TasksShapes.l).background(colors.surface).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionLabel()
            content()
        }
        VoiceCardVariant.Editor -> Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionLabel()
            content()
        }
    }
}

@Composable
private fun SectionLabel() {
    Text(stringResource(R.string.voice_section).uppercase(), style = TasksType.overline, color = TasksTheme.colors.textSecondary)
}

@Composable
private fun DetailContent(model: VoiceCardModel, actions: VoiceCardActions) {
    val colors = TasksTheme.colors
    when (model) {
        VoiceCardModel.Empty -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    VoiceIcons.Mic, contentDescription = null, tint = colors.textSecondary,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.surface2).padding(10.dp),
                )
                Text(
                    stringResource(R.string.voice_empty_body),
                    style = TasksType.subhead15.copy(lineHeight = 22.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
            RecordButton(actions.onRecord)
        }
        VoiceCardModel.PermissionExplainer -> MicPermissionCard(denied = false, actions = actions)
        VoiceCardModel.PermissionDenied -> MicPermissionCard(denied = true, actions = actions)
        is VoiceCardModel.Recording -> VoiceRecorderView(model, actions)
        is VoiceCardModel.Player -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            VoicePlayerView(model, actions)
            ReRecordDeleteRow(actions)
        }
        VoiceCardModel.RecordError -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(TasksShapes.m).background(colors.dangerContainer).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(TasksIcons.Alert, contentDescription = null, tint = colors.onDangerContainer, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(R.string.voice_err_record),
                    style = TasksType.subhead.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = colors.onDangerContainer,
                )
            }
            RecordButton(actions.onRecord)
        }
        VoiceCardModel.Unavailable -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    VoiceIcons.AlertTriangle, contentDescription = null, tint = colors.onDangerContainer,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.dangerContainer).padding(13.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.voice_err_unavailable), style = TasksType.headline, color = colors.textPrimary)
                    Text(stringResource(R.string.voice_err_unavailable_body), style = TasksType.subhead, color = colors.textSecondary)
                }
            }
            VoiceButton(
                stringResource(R.string.voice_delete_note), actions.onDelete, colors.dangerContainer, colors.onDangerContainer,
                Modifier.fillMaxWidth(), icon = TasksIcons.Trash,
            )
        }
    }
}

/** Editor: no own card. Empty = dashed field-style row (does not compete with the lime Save); recorded = compact player. */
@Composable
private fun EditorContent(model: VoiceCardModel, actions: VoiceCardActions) {
    val colors = TasksTheme.colors
    when (model) {
        VoiceCardModel.Empty -> {
            val dash = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(TasksShapes.m)
                    .clickable(role = Role.Button, onClick = actions.onRecord)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.outline,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dash),
                        )
                    }
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RecordDot(10.dp)
                Text(stringResource(R.string.voice_record), style = TasksType.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.voice_editor_hint), style = TasksType.subhead, color = colors.textSecondary)
            }
        }
        is VoiceCardModel.Player -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VoicePlayerView(model, actions, compact = true)
            ReRecordDeleteRow(actions)
        }
        else -> Column(
            Modifier.fillMaxWidth().clip(TasksShapes.l).background(colors.surface).padding(16.dp),
        ) { DetailContent(model, actions) }
    }
}

@Composable
private fun RecordButton(onClick: () -> Unit) {
    val colors = TasksTheme.colors
    VoiceButton(
        stringResource(R.string.voice_record), onClick, colors.inverse, colors.onInverse,
        Modifier.fillMaxWidth(), leading = { RecordDot(10.dp) },
    )
}

/** Re-record and Delete stay two separate buttons, side by side: Secondary + mic, Destructive + trash. */
@Composable
private fun ReRecordDeleteRow(actions: VoiceCardActions) {
    val colors = TasksTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        VoiceButton(
            stringResource(R.string.voice_rerecord), actions.onReRecord, colors.inverse, colors.onInverse,
            Modifier.weight(1f), icon = VoiceIcons.Mic, description = stringResource(R.string.voice_a11y_rerecord),
        )
        VoiceButton(
            stringResource(R.string.voice_delete), actions.onDelete, colors.dangerContainer, colors.onDangerContainer,
            Modifier.weight(1f), icon = TasksIcons.Trash, description = stringResource(R.string.voice_delete_note),
        )
    }
}
