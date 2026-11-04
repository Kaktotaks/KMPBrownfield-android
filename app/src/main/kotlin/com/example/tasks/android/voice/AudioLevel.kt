package com.example.tasks.android.voice

import kotlin.math.sqrt

/** Input-level mapping of the Android recorder (API contract: `sqrt(maxAmplitude / 32767)`). Pure, JVM-tested. */
object AudioLevel {
    const val MAX_AMPLITUDE = 32767f

    /** `MediaRecorder.getMaxAmplitude()` (0..32767) -> 0..1. The sqrt makes quiet speech visible, like iOS' dB mapping. */
    fun fromMaxAmplitude(maxAmplitude: Int): Float =
        sqrt((maxAmplitude / MAX_AMPLITUDE).coerceIn(0f, 1f))
}
