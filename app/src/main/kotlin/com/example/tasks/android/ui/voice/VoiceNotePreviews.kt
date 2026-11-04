package com.example.tasks.android.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.shared.presentation.VoiceNoteConfirmation

// Previews: every card state, light and dark (Detail card), plus the Editor variants and the confirmation dialogs.

@Composable
private fun Frame(dark: Boolean, variant: VoiceCardVariant = VoiceCardVariant.Detail, vararg models: VoiceCardModel) {
    TasksTheme(darkTheme = dark) {
        Column(
            Modifier.background(TasksTheme.colors.bg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            models.forEach { VoiceNoteCard(it, VoiceCardActions.None, variant = variant) }
        }
    }
}

private val detailStatesA = arrayOf(VoiceCardModel.Empty, VoicePreviewData.recording, VoicePreviewData.recordingWarning)
private val detailStatesB = arrayOf(VoicePreviewData.player, VoicePreviewData.playerPlaying, VoiceCardModel.PermissionExplainer)
private val detailStatesC = arrayOf(VoiceCardModel.PermissionDenied, VoiceCardModel.RecordError, VoiceCardModel.Unavailable)

@Preview(name = "Card empty / recording / last 10 s - light", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsALight() = Frame(false, models = detailStatesA)

@Preview(name = "Card empty / recording / last 10 s - dark", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsADark() = Frame(true, models = detailStatesA)

@Preview(name = "Card player / playing 1.5x / ask - light", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsBLight() = Frame(false, models = detailStatesB)

@Preview(name = "Card player / playing 1.5x / ask - dark", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsBDark() = Frame(true, models = detailStatesB)

@Preview(name = "Card denied / record error / unavailable - light", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsCLight() = Frame(false, models = detailStatesC)

@Preview(name = "Card denied / record error / unavailable - dark", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun CardsCDark() = Frame(true, models = detailStatesC)

private val editorStates = arrayOf(VoiceCardModel.Empty, VoicePreviewData.player, VoicePreviewData.recording, VoiceCardModel.PermissionExplainer)

@Preview(name = "Editor variant states - light", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun EditorVariantLight() = Frame(false, VoiceCardVariant.Editor, *editorStates)

@Preview(name = "Editor variant states - dark", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun EditorVariantDark() = Frame(true, VoiceCardVariant.Editor, *editorStates)

@Preview(name = "Indicator light", showBackground = true)
@Composable
private fun IndicatorLight() = TasksTheme(darkTheme = false) { VoiceNoteIndicator("0:42") }

@Preview(name = "Indicator dark", showBackground = true)
@Composable
private fun IndicatorDark() = TasksTheme(darkTheme = true) {
    Column(Modifier.background(TasksTheme.colors.surface).padding(8.dp)) { VoiceNoteIndicator("0:42") }
}

@Preview(name = "Replace dialog light", showBackground = true, heightDp = 400)
@Composable
private fun ReplaceDialogLight() = TasksTheme(darkTheme = false) { VoiceConfirmDialog(VoiceNoteConfirmation.REPLACE, {}, {}) }

@Preview(name = "Delete dialog dark", showBackground = true, heightDp = 400)
@Composable
private fun DeleteDialogDark() = TasksTheme(darkTheme = true) { VoiceConfirmDialog(VoiceNoteConfirmation.DELETE, {}, {}) }
