package com.example.tasks.android.ui.voice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.pressScale
import com.example.tasks.android.ui.theme.LocalReduceMotion
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** Voice-note action buttons are 48 high, r16, Inter 600 15, icon 18 + 8 gap (notes board, "Action row"). */
@Composable
internal fun VoiceButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    description: String? = null,
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(source)
            .heightIn(min = 48.dp)
            .clip(TasksShapes.m)
            .background(container)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(text, style = TasksType.label15, color = content)
        if (trailingIcon != null) Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
    }
}

/** The red "record" dot of the Record buttons. */
@Composable
internal fun RecordDot(size: Dp = 10.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(TasksTheme.colors.danger))
}

/** Recording dot + halo; opacity pulses 1 <-> 0.3, 1000 ms per half-cycle, solid under reduced motion. */
@Composable
internal fun PulsingDot() {
    val danger = TasksTheme.colors.danger
    val reduceMotion = LocalReduceMotion.current
    val alpha = if (reduceMotion) {
        1f
    } else {
        val pulse by rememberInfiniteTransition(label = "recordingDot").animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(Motion.pulseHalfCycle, easing = Motion.easeInOut), RepeatMode.Reverse),
            label = "recordingDotAlpha",
        )
        pulse
    }
    Box(Modifier.size(22.dp).alpha(alpha).clip(CircleShape).background(danger.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(danger))
    }
}

/** VoiceRecorderView: red dot + "Recording" + hint, big timer, live bars, Cancel (1 fr) + Stop (2 fr). */
@Composable
fun VoiceRecorderView(model: VoiceCardModel.Recording, actions: VoiceCardActions, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val timerColor by animateColorAsState(
        if (model.isWarning) colors.danger else colors.textPrimary,
        tween(Motion.short, easing = Motion.emphasized), label = "timerColor",
    )
    val hintColor by animateColorAsState(
        if (model.isWarning) colors.danger else colors.textSecondary,
        tween(Motion.short, easing = Motion.emphasized), label = "hintColor",
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                // Recording is shown by the dot AND the word; the running timer is never read out (notes board).
                modifier = Modifier.semantics(mergeDescendants = true) {},
            ) {
                PulsingDot()
                Text(stringResource(R.string.voice_recording), style = TasksType.label15, color = colors.textPrimary)
            }
            val hint = if (model.timeLeftText != null) {
                stringResource(R.string.voice_seconds_left, model.timeLeftText)
            } else {
                stringResource(R.string.voice_max_hint)
            }
            Text(
                hint,
                style = (if (model.isWarning) TasksType.subhead.copy(fontWeight = FontWeight.SemiBold) else TasksType.subhead).merge(TasksType.tabular),
                color = hintColor,
            )
        }
        Text(
            model.timerText,
            style = TimerStyle,
            color = timerColor,
            modifier = Modifier.clearAndSetSemantics {},
        )
        LiveWaveform(model.levels, Modifier.padding(top = 4.dp, bottom = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VoiceButton(
                stringResource(R.string.voice_cancel), actions.onCancel, colors.surface2, colors.textPrimary,
                Modifier.weight(1f), icon = VoiceIcons.Close,
            )
            VoiceButton(
                stringResource(R.string.voice_stop), actions.onStop, colors.accent, colors.onAccent,
                Modifier.weight(2f), icon = VoiceIcons.Stop,
            )
        }
    }
}

/** Inter Tight 800 48/52, tabular figures. */
private val TimerStyle = TasksType.statNumber.copy(fontSize = 48.sp, lineHeight = 52.sp)

@Composable
fun SpokenDuration.asText(): String {
    val parts = buildList {
        if (minutes > 0) add(pluralStringResource(R.plurals.voice_a11y_minutes, minutes, minutes))
        if (seconds > 0 || minutes == 0) add(pluralStringResource(R.plurals.voice_a11y_seconds, seconds, seconds))
    }
    return parts.joinToString(" ")
}

/** 48 (detail) / 40 (compact) accent circle with a play <-> pause morph (150 ms crossfade + scale). */
@Composable
internal fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit, size: Dp, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val reduceMotion = LocalReduceMotion.current
    Box(
        modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(size).clip(CircleShape).background(colors.accent), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    val spec = tween<Float>(Motion.short, easing = Motion.emphasized)
                    if (reduceMotion) {
                        fadeIn(spec) togetherWith fadeOut(spec)
                    } else {
                        (fadeIn(spec) + scaleIn(spec, initialScale = 0.6f)) togetherWith (fadeOut(spec) + scaleOut(spec, targetScale = 0.6f))
                    }
                },
                label = "playPause",
            ) { playing ->
                Icon(
                    if (playing) VoiceIcons.Pause else VoiceIcons.Play,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(if (size >= 48.dp) 22.dp else 18.dp),
                )
            }
        }
    }
}

/** Speed pill: outline 1.5 `textPrimary`, 32 high, cycles 1x / 1.5x / 2x on tap (48 dp touch target). */
@Composable
internal fun SpeedPill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Box(
        modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .heightIn(min = 32.dp)
                .sizeIn(minWidth = 52.dp)
                .border(1.5.dp, colors.textPrimary, TasksShapes.pill)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = TasksType.subhead.copy(fontWeight = FontWeight.SemiBold).merge(TasksType.tabular), color = colors.textPrimary)
        }
    }
}

/**
 * VoicePlayerView. Full: 48 play/pause + scrubbable 40-bar waveform (56 high), "0:12 / 0:42" + speed pill.
 * Compact (editor): 56 high row on `surface` with a border, 40 play/pause, mini waveform and the duration.
 * Accessibility: ONE element "Voice note, 42 seconds" with actions Play/Pause and Speed, plus an adjustable
 * seek (progressBarRangeInfo + setProgress, steps of 5 s).
 */
@Composable
fun VoicePlayerView(
    model: VoiceCardModel.Player,
    actions: VoiceCardActions,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = TasksTheme.colors
    val description = stringResource(R.string.voice_a11y_player, model.spokenDuration.asText())
    val playLabel = stringResource(if (model.isPlaying) R.string.voice_a11y_pause else R.string.voice_a11y_play)
    val speedAction = stringResource(R.string.voice_a11y_speed, model.speedLabel)
    val position = stringResource(R.string.voice_a11y_progress, model.positionText, model.durationText)
    val semanticsModifier = Modifier.clearAndSetSemantics {
        contentDescription = description
        stateDescription = position
        progressBarRangeInfo = ProgressBarRangeInfo(model.progress, 0f..1f, model.seekSteps)
        setProgress { target ->
            actions.onSeek(target.coerceIn(0f, 1f))
            true
        }
        customActions = buildList {
            add(CustomAccessibilityAction(playLabel) { actions.onPlayPause(); true })
            if (!compact) add(CustomAccessibilityAction(speedAction) { actions.onCycleSpeed(); true })
        }
    }

    if (compact) {
        Row(
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(TasksShapes.m)
                .background(colors.surface)
                .border(1.dp, colors.border, TasksShapes.m)
                .then(semanticsModifier)
                .padding(start = 4.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayPauseButton(model.isPlaying, actions.onPlayPause, size = 40.dp)
            WaveformView(model.waveform, model.progress, actions.onSeek, Modifier.weight(1f), height = 28.dp)
            Text(model.durationText, style = TasksType.savePill.merge(TasksType.tabular), color = colors.textPrimary)
        }
    } else {
        Column(modifier.fillMaxWidth().then(semanticsModifier)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                PlayPauseButton(model.isPlaying, actions.onPlayPause, size = 48.dp)
                WaveformView(model.waveform, model.progress, actions.onSeek, Modifier.weight(1f), height = 56.dp)
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)) { append(model.positionText) }
                        withStyle(SpanStyle(color = colors.textSecondary)) { append(" / ${model.durationText}") }
                    },
                    style = TasksType.subhead.merge(TasksType.tabular),
                )
                SpeedPill(model.speedLabel, actions.onCycleSpeed)
            }
        }
    }
}

/** MicPermissionCard: 48 circle icon + headline + body + actions. `ask` = accent circle, `denied` = dangerContainer circle. */
@Composable
fun MicPermissionCard(denied: Boolean, actions: VoiceCardActions, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(if (denied) colors.dangerContainer else colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (denied) VoiceIcons.MicOff else VoiceIcons.Mic,
                    contentDescription = null,
                    tint = if (denied) colors.onDangerContainer else colors.onAccent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(if (denied) R.string.voice_perm_denied_title else R.string.voice_perm_title),
                    style = TasksType.headline,
                    color = colors.textPrimary,
                )
                Text(
                    stringResource(if (denied) R.string.voice_perm_denied_body else R.string.voice_perm_body),
                    style = TasksType.subhead,
                    color = colors.textSecondary,
                )
            }
        }
        if (denied) {
            VoiceButton(
                stringResource(R.string.voice_open_settings), actions.onOpenSettings, colors.inverse, colors.onInverse,
                Modifier.fillMaxWidth(), trailingIcon = VoiceIcons.ExternalLink,
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceButton(stringResource(R.string.voice_perm_not_now), actions.onPermissionNotNow, colors.surface2, colors.textPrimary, Modifier.weight(1f))
                VoiceButton(stringResource(R.string.voice_perm_continue), actions.onPermissionContinue, colors.accent, colors.onAccent, Modifier.weight(1f))
            }
        }
    }
}

/** VoiceNoteIndicator for list rows: mic 14 + "0:42" (subhead 14/500, `textSecondary`, never recolored by overdue). */
@Composable
fun VoiceNoteIndicator(durationText: String, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(VoiceIcons.Mic, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        Text(durationText, style = TasksType.subhead.merge(TasksType.tabular), color = colors.textSecondary)
    }
}
