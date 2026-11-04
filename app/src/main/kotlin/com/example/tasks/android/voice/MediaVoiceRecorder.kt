package com.example.tasks.android.voice

import android.content.Context
import android.media.MediaRecorder
import com.example.tasks.shared.domain.AudioRecorder

/**
 * Thin adapter over [MediaRecorder]: AAC in MPEG-4 (.m4a), mono, 44.1 kHz, 64 kbps. All state-machine logic
 * (timer, limits, files) lives in shared; this class only starts, meters and stops the hardware.
 */
class MediaVoiceRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null

    override fun start(path: String) {
        cancel()
        val r = MediaRecorder(context)
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(SAMPLE_RATE)
            r.setAudioChannels(1)
            r.setAudioEncodingBitRate(BIT_RATE)
            r.setOutputFile(path)
            r.prepare()
            r.start()
            recorder = r
        } catch (e: Exception) {
            runCatching { r.release() }
            throw e
        }
    }

    override fun level(): Float {
        val r = recorder ?: return 0f
        return AudioLevel.fromMaxAmplitude(runCatching { r.maxAmplitude }.getOrDefault(0))
    }

    override fun stop() {
        val r = recorder ?: return
        recorder = null
        try {
            r.stop() // throws if no valid audio data was written: shared turns it into the error card
        } finally {
            r.release()
        }
    }

    /** Never throws; safe when idle. Leaves the file for shared to delete. */
    override fun cancel() {
        val r = recorder ?: return
        recorder = null
        runCatching { r.stop() }
        runCatching { r.release() }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val BIT_RATE = 64_000
    }
}
