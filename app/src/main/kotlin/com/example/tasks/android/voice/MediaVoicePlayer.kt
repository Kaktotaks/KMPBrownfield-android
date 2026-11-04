package com.example.tasks.android.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.example.tasks.shared.domain.AudioPlayer

/**
 * Thin adapter over [MediaPlayer] with transient audio focus. Position/isPlaying are polled by shared every 50 ms;
 * when focus is lost (call, assistant, other app) or headphones are unplugged we pause, so `isPlaying()`
 * turns false and shared flips the card to paused.
 */
class MediaVoicePlayer(private val context: Context) : AudioPlayer {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change -> if (FocusPolicy.shouldPause(change)) pause() }
        .build()
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = pause()
    }

    private var player: MediaPlayer? = null
    private var speed = 1f
    private var completed = false
    private var noisyRegistered = false

    override var onCompletion: (() -> Unit)? = null

    override fun prepare(path: String) {
        dispose()
        val p = MediaPlayer()
        try {
            p.setAudioAttributes(attributes)
            p.setDataSource(path)
            p.prepare()
            p.setOnCompletionListener {
                completed = true
                stopListeningForFocusLoss()
                onCompletion?.invoke()
            }
            player = p
            completed = false
        } catch (e: Exception) {
            runCatching { p.release() }
            throw e
        }
    }

    override fun durationMs(): Long = player?.let { runCatching { it.duration.toLong() }.getOrDefault(0L) } ?: 0L

    override fun play() {
        val p = player ?: return
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        if (completed) {
            p.seekTo(0)
            completed = false
        }
        // The speed is applied only here and while playing: setPlaybackParams() on a paused player would start it.
        applySpeed(p)
        if (!p.isPlaying) p.start()
        if (!noisyRegistered) {
            context.registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            noisyRegistered = true
        }
    }

    override fun pause() {
        val p = player
        if (p != null && runCatching { p.isPlaying }.getOrDefault(false)) runCatching { p.pause() }
        stopListeningForFocusLoss()
    }

    override fun seekTo(positionMs: Long) {
        val p = player ?: return
        completed = false
        p.seekTo(positionMs, MediaPlayer.SEEK_CLOSEST)
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed
        val p = player ?: return
        if (runCatching { p.isPlaying }.getOrDefault(false)) applySpeed(p)
    }

    override fun positionMs(): Long = player?.let { runCatching { it.currentPosition.toLong() }.getOrDefault(0L) } ?: 0L

    override fun isPlaying(): Boolean = player?.let { runCatching { it.isPlaying }.getOrDefault(false) } ?: false

    override fun dispose() {
        stopListeningForFocusLoss()
        player?.let { runCatching { it.release() } }
        player = null
        completed = false
    }

    private fun applySpeed(p: MediaPlayer) {
        p.playbackParams = PlaybackParams().setSpeed(speed)
    }

    private fun stopListeningForFocusLoss() {
        audioManager.abandonAudioFocusRequest(focusRequest)
        if (noisyRegistered) {
            runCatching { context.unregisterReceiver(noisyReceiver) }
            noisyRegistered = false
        }
    }

    /** Which audio-focus changes pause playback: every loss, including a duck (a voice note is speech). */
    object FocusPolicy {
        fun shouldPause(change: Int): Boolean = when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> true
            else -> false
        }
    }
}
