package com.example.tasks.android.ui.voice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.tasks.android.voice.RuntimeMicPermission

/**
 * Composition-side half of [RuntimeMicPermission]: registers the RECORD_AUDIO launcher
 * (`rememberLauncherForActivityResult(RequestPermission())`) and attaches it as the provider's host,
 * so the shared VM's `permission.request { granted -> ... }` callback fires with the system dialog's result.
 */
@Composable
fun MicPermissionHost(permission: RuntimeMicPermission) {
    val context = LocalContext.current
    val currentPermission by rememberUpdatedState(permission)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        currentPermission.onSystemResult(granted)
    }
    DisposableEffect(permission, launcher) {
        val host = object : RuntimeMicPermission.Host {
            override fun launch() = launcher.launch(Manifest.permission.RECORD_AUDIO)
            override fun shouldShowRationale(): Boolean {
                val activity = context.findActivity() ?: return false
                return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
            }
        }
        permission.host = host
        onDispose { if (permission.host === host) permission.host = null }
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
