package com.example.tasks.android.ui.splash

import android.content.Intent

/**
 * DEBUG variant (release has a same-named object that always returns null).
 *
 *   adb shell am start -n com.example.tasks.android/.MainActivity --el splashFrozenAtMs 820
 */
object SplashDebug {
    const val EXTRA_FROZEN_AT_MS = "splashFrozenAtMs"

    /** Fixed elapsed time to render the overlay at (no clock, no auto-removal), or null for the normal splash. */
    fun frozenAtMs(intent: Intent?): Long? =
        intent?.takeIf { it.hasExtra(EXTRA_FROZEN_AT_MS) }
            ?.getLongExtra(EXTRA_FROZEN_AT_MS, -1L)
            ?.takeIf { it >= 0L }
}
