package com.example.tasks.android.ui.voice

import com.example.tasks.shared.domain.VoiceNote
import com.example.tasks.shared.domain.VoiceNoteFormat
import com.example.tasks.shared.domain.VoiceNoteLimits
import com.example.tasks.shared.presentation.VoiceNoteErrorKind
import com.example.tasks.shared.presentation.VoiceNoteUiState

/** Whole minutes + remaining seconds of a duration, for spoken text ("1 minute 5 seconds"). Floored, like the m:ss text. */
data class SpokenDuration(val minutes: Int, val seconds: Int) {
    companion object {
        fun of(ms: Long): SpokenDuration {
            val total = (ms.coerceAtLeast(0) / 1000).toInt()
            return SpokenDuration(total / 60, total % 60)
        }
    }
}

/** Which card is showing; the content cross-fades only when this changes (not on every 50 ms recorder tick). */
enum class VoiceCardKind { Empty, PermissionExplainer, PermissionDenied, Recording, Player, RecordError, Unavailable }

/**
 * Everything the voice-note card draws, already mapped from the shared [VoiceNoteUiState]. Plain Kotlin: no Compose,
 * unit-tested on the JVM. Times are formatted by shared's `VoiceNoteFormat` so both apps print identical text.
 */
sealed interface VoiceCardModel {
    val kind: VoiceCardKind

    data object Empty : VoiceCardModel {
        override val kind = VoiceCardKind.Empty
    }

    data object PermissionExplainer : VoiceCardModel {
        override val kind = VoiceCardKind.PermissionExplainer
    }

    data object PermissionDenied : VoiceCardModel {
        override val kind = VoiceCardKind.PermissionDenied
    }

    data class Recording(
        /** Elapsed, "0:07". */
        val timerText: String,
        /** Last 10 s: timer and hint switch to `danger`. */
        val isWarning: Boolean,
        /** "0:09" while [isWarning] (the hint reads "0:09 left"), else null ("max 2:00" is shown). */
        val timeLeftText: String?,
        /** Exactly 32 values, oldest first. */
        val levels: List<Float>,
    ) : VoiceCardModel {
        override val kind = VoiceCardKind.Recording
    }

    data class Player(
        /** Identifies the clip: a Recording -> Player change with a new name means a clip was saved (not a cancel). */
        val fileName: String,
        /** Needed to tell the 2:00 limit apart ("Recording stopped, at 2 minutes"). */
        val durationMs: Long,
        /** 40 bars, 0..1. */
        val waveform: List<Float>,
        val progress: Float,
        val isPlaying: Boolean,
        /** Played part of "0:12 / 0:42". */
        val positionText: String,
        /** "0:42" (compact player label and the denominator). */
        val durationText: String,
        /** "1×" / "1.5×" / "2×". */
        val speedLabel: String,
        val spokenDuration: SpokenDuration,
        /** Steps of 5 s for the adjustable seek (TalkBack slider). */
        val seekSteps: Int,
    ) : VoiceCardModel {
        override val kind = VoiceCardKind.Player
    }

    data object RecordError : VoiceCardModel {
        override val kind = VoiceCardKind.RecordError
    }

    data object Unavailable : VoiceCardModel {
        override val kind = VoiceCardKind.Unavailable
    }
}

fun speedLabel(speed: Float): String = when {
    speed >= 2f -> "2×"
    speed >= 1.5f -> "1.5×"
    else -> "1×"
}

/** Shared VM state -> card model. Pure function (called from [VoiceNoteSection] inside `remember(state)`). */
fun VoiceNoteUiState.toCardModel(): VoiceCardModel = when (this) {
    VoiceNoteUiState.Empty -> VoiceCardModel.Empty
    VoiceNoteUiState.PermissionExplainer -> VoiceCardModel.PermissionExplainer
    VoiceNoteUiState.PermissionDenied -> VoiceCardModel.PermissionDenied
    is VoiceNoteUiState.Recording -> VoiceCardModel.Recording(
        timerText = VoiceNoteFormat.duration(elapsedMs),
        isWarning = isWarning,
        timeLeftText = if (isWarning) VoiceNoteFormat.duration(remainingMs) else null,
        levels = levels,
    )
    is VoiceNoteUiState.Ready -> note.toPlayerModel(progress, isPlaying, positionMs, speed)
    is VoiceNoteUiState.Error -> when (kind) {
        VoiceNoteErrorKind.RECORD_FAILED -> VoiceCardModel.RecordError
        VoiceNoteErrorKind.UNAVAILABLE -> VoiceCardModel.Unavailable
    }
}

fun VoiceNote.toPlayerModel(progress: Float, isPlaying: Boolean, positionMs: Long, speed: Float) = VoiceCardModel.Player(
    fileName = fileName,
    durationMs = durationMs,
    waveform = waveform,
    progress = progress.coerceIn(0f, 1f),
    isPlaying = isPlaying,
    positionText = VoiceNoteFormat.duration(positionMs),
    durationText = durationText,
    speedLabel = speedLabel(speed),
    spokenDuration = SpokenDuration.of(durationMs),
    seekSteps = ((durationMs / SEEK_STEP_MS).toInt() - 1).coerceAtLeast(0),
)

const val SEEK_STEP_MS = 5_000L

/** Number of bars in the live recorder. */
const val LIVE_BARS = VoiceNoteLimits.LIVE_BARS
