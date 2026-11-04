package com.example.tasks.android.voice

import android.os.SystemClock
import com.example.tasks.shared.domain.AudioPlayer
import com.example.tasks.shared.domain.AudioRecorder
import com.example.tasks.shared.domain.MicPermission
import com.example.tasks.shared.domain.MicPermissionProvider
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

/** Debug fake: no microphone. Writes the recorded duration (ms, text) into the file so [FakeVoicePlayer] can read it back. */
class FakeVoiceRecorder(private val failOnStart: Boolean) : AudioRecorder {
    private var path: String? = null
    private var startedAt = 0L

    override fun start(path: String) {
        if (failOnStart) error("Fake recorder: forced RecordFailed")
        this.path = path
        startedAt = SystemClock.elapsedRealtime()
        File(path).writeText("0")
    }

    /** A moving, deterministic input level so the live bars have shape. */
    override fun level(): Float {
        if (path == null) return 0f
        val t = (SystemClock.elapsedRealtime() - startedAt) / 1000f
        return (0.15f + 0.85f * abs(sin(t * 3.1f) * sin(t * 1.3f + 0.5f))).coerceIn(0f, 1f)
    }

    override fun stop() {
        val p = path ?: return
        path = null
        File(p).writeText((SystemClock.elapsedRealtime() - startedAt).toString())
    }

    override fun cancel() {
        path = null
    }
}

/** Debug fake: a wall-clock "player" over the duration stored in the (fake) file; 42 s when unreadable. */
class FakeVoicePlayer(private val failOnPrepare: Boolean) : AudioPlayer {
    override var onCompletion: (() -> Unit)? = null
    private var durationMs = 0L
    private var position = 0L
    private var playing = false
    private var lastTick = 0L
    private var speed = 1f

    override fun prepare(path: String): Long {
        if (failOnPrepare) error("Fake player: forced Unavailable")
        durationMs = File(path).takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull()?.takeIf { it > 0 } ?: 42_000L
        position = 0
        playing = false
        return durationMs
    }

    private fun advance() {
        if (!playing) return
        val now = SystemClock.elapsedRealtime()
        position += ((now - lastTick) * speed).toLong()
        lastTick = now
        if (position >= durationMs) {
            position = durationMs
            playing = false
            onCompletion?.invoke()
        }
    }

    override fun play() {
        if (position >= durationMs) position = 0
        lastTick = SystemClock.elapsedRealtime()
        playing = true
    }

    override fun pause() {
        advance()
        playing = false
    }

    override fun seekTo(positionMs: Long) {
        advance()
        position = positionMs.coerceIn(0, durationMs)
        lastTick = SystemClock.elapsedRealtime()
    }

    override fun setSpeed(speed: Float) {
        advance()
        this.speed = speed
    }

    override fun positionMs(): Long {
        advance()
        return position
    }

    override fun isPlaying(): Boolean {
        advance()
        return playing
    }

    override fun release() {
        playing = false
    }
}

/** Debug fake permission: a fixed status; [request] grants (or denies when [denyOnRequest]) without a system dialog. */
class FakeMicPermission(private var current: MicPermission, private val denyOnRequest: Boolean) : MicPermissionProvider {
    override fun status(): MicPermission = current

    override fun request(onResult: (Boolean) -> Unit) {
        val granted = !denyOnRequest
        current = if (granted) MicPermission.GRANTED else MicPermission.DENIED
        onResult(granted)
    }
}
