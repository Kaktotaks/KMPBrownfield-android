package com.example.tasks.android.voice

import com.example.tasks.shared.domain.VoiceNote
import com.example.tasks.shared.presentation.VoiceNoteTarget

/** Which shared target a VoiceNoteViewModel gets: Detail edits a saved task, the editor works on a draft. */
object VoiceTargets {
    fun forTask(taskId: Int): VoiceNoteTarget = VoiceNoteTarget.ExistingTask(taskId)
    fun forEditor(initial: VoiceNote?): VoiceNoteTarget = VoiceNoteTarget.Draft(initial)
}
