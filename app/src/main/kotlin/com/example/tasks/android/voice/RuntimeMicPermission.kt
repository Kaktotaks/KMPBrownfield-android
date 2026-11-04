package com.example.tasks.android.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.tasks.shared.domain.MicPermission
import com.example.tasks.shared.domain.MicPermissionProvider

/** Pure decision table behind [RuntimeMicPermission.status]; JVM-tested. */
object MicPermissionPolicy {
    /**
     * - granted -> GRANTED
     * - never asked -> NOT_DETERMINED (explainer, then the system dialog)
     * - asked before and the system would still show its dialog (rationale == true, the first denial) ->
     *   NOT_DETERMINED, so the explainer appears again (Android may ask twice, notes board)
     * - asked before and no rationale (denied twice, "Don't ask again", or revoked in Settings) -> DENIED: Open Settings
     */
    fun resolve(granted: Boolean, askedBefore: Boolean, shouldShowRationale: Boolean): MicPermission = when {
        granted -> MicPermission.GRANTED
        !askedBefore -> MicPermission.NOT_DETERMINED
        shouldShowRationale -> MicPermission.NOT_DETERMINED
        else -> MicPermission.DENIED
    }
}

/**
 * RECORD_AUDIO adapter. The system dialog needs an Activity result launcher, which only exists in composition:
 * `MicPermissionHost` (ui/voice) registers it with `rememberLauncherForActivityResult(RequestPermission())` and
 * attaches itself as [host]; results are bridged back to the callback given to [request].
 */
class RuntimeMicPermission(context: Context) : MicPermissionProvider {
    /** What the composable bridge offers: launch the system dialog, ask the Activity about the rationale. */
    interface Host {
        fun launch()
        fun shouldShowRationale(): Boolean
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private var pending: ((Boolean) -> Unit)? = null

    @Volatile
    var host: Host? = null

    override fun status(): MicPermission = MicPermissionPolicy.resolve(
        granted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED,
        askedBefore = prefs.getBoolean(KEY_ASKED, false),
        shouldShowRationale = host?.shouldShowRationale() ?: false,
    )

    override fun request(onResult: (Boolean) -> Unit) {
        val h = host
        if (h == null) {
            onResult(false) // no Activity in composition: cannot show a dialog
            return
        }
        pending?.invoke(false)
        pending = onResult
        prefs.edit().putBoolean(KEY_ASKED, true).apply()
        h.launch()
    }

    /** Called by the composable bridge with the system dialog's result. */
    fun onSystemResult(granted: Boolean) {
        val callback = pending
        pending = null
        callback?.invoke(granted)
    }

    private companion object {
        const val PREFS = "voice_notes_permission"
        const val KEY_ASKED = "record_audio_asked"
    }
}
