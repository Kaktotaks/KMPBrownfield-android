package com.example.tasks.android.ui.voice

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.presentation.VoiceNoteConfirmation
import com.example.tasks.shared.presentation.VoiceNoteViewModel

/**
 * Stateful voice-note card: collects the shared [VoiceNoteViewModel], maps to [VoiceCardModel], wires every intent,
 * hosts the Replace / Delete dialogs, opens app Settings, forwards the lifecycle (ON_STOP -> onAppBackgrounded,
 * ON_RESUME -> onAppForegrounded) and posts the TalkBack announcements.
 */
@Composable
fun VoiceNoteSection(
    viewModel: VoiceNoteViewModel,
    variant: VoiceCardVariant,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmation by viewModel.confirmation.collectAsStateWithLifecycle()
    val model = androidx.compose.runtime.remember(state) { state.toCardModel() }
    val context = LocalContext.current

    // Leaving the app: recording stops and is kept, playback pauses. Coming back: "granted in Settings" is picked up.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onAppForegrounded() }

    VoiceAnnouncements(model)

    val actions = remember(viewModel, context) {
        VoiceCardActions(
            onRecord = viewModel::onRecordTapped,
            onStop = viewModel::onStop,
            onCancel = viewModel::onCancelRecording,
            onPlayPause = viewModel::onPlayPause,
            onSeek = viewModel::onSeek,
            onCycleSpeed = viewModel::onCycleSpeed,
            onReRecord = viewModel::onReRecordTapped,
            onDelete = viewModel::onDeleteTapped,
            onPermissionContinue = viewModel::onExplainerContinue,
            onPermissionNotNow = viewModel::onPermissionDismissed,
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                runCatching { context.startActivity(intent) }
                viewModel.onOpenSettingsHandled()
            },
        )
    }
    VoiceNoteCard(model, actions, modifier, variant)

    confirmation?.let { VoiceConfirmDialog(it, onConfirm = viewModel::onConfirm, onDismiss = viewModel::onDismissConfirmation) }
}

/** M3 AlertDialog on `surface`, r `xl`: "Replace voice note?" / "Delete voice note?" with a danger confirm action. */
@Composable
fun VoiceConfirmDialog(confirmation: VoiceNoteConfirmation, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = TasksTheme.colors
    val replace = confirmation == VoiceNoteConfirmation.REPLACE
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = TasksShapes.xl,
        title = {
            Text(
                stringResource(if (replace) R.string.voice_replace_title else R.string.voice_delete_title),
                style = TasksType.headline.copy(fontSize = 24.sp, lineHeight = 30.sp), color = colors.textPrimary,
            )
        },
        text = {
            Text(
                stringResource(if (replace) R.string.voice_replace_body else R.string.voice_delete_body),
                style = TasksType.subhead, color = colors.textSecondary,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.voice_cancel), style = TasksType.button, color = colors.textPrimary) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(if (replace) R.string.voice_replace_confirm else R.string.voice_delete),
                    style = TasksType.button, color = colors.danger,
                )
            }
        },
    )
}

/** TalkBack: "Recording started", "10 seconds left", "Recording stopped, 7 seconds" (the timer itself is never read). */
@Composable
private fun VoiceAnnouncements(model: VoiceCardModel) {
    val view = LocalView.current
    val started = stringResource(R.string.voice_a11y_started)
    val tenLeft = stringResource(R.string.voice_a11y_ten_left)
    var previous by remember { mutableStateOf<VoiceCardKind?>(null) }
    val stoppedPrefix = (model as? VoiceCardModel.Player)?.spokenDuration?.asText()
    val stopped = stoppedPrefix?.let { stringResource(R.string.voice_a11y_stopped, it) }

    LaunchedEffect(model.kind) {
        val was = previous
        previous = model.kind
        when {
            model.kind == VoiceCardKind.Recording && was != VoiceCardKind.Recording -> view.announce(started)
            was == VoiceCardKind.Recording && model.kind == VoiceCardKind.Player && stopped != null -> view.announce(stopped)
        }
    }
    val warning = (model as? VoiceCardModel.Recording)?.isWarning == true
    LaunchedEffect(warning) { if (warning) view.announce(tenLeft) }
}

@Suppress("DEPRECATION")
private fun View.announce(text: String) = announceForAccessibility(text)
