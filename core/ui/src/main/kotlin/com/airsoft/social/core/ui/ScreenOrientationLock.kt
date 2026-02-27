package com.airsoft.social.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun ForceLandscapeOrientation() {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
}

@Composable
fun ForcePortraitOrientation() {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity, orientation) {
        if (activity == null) {
            onDispose {}
        } else {
            val previousOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = previousOrientation
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
