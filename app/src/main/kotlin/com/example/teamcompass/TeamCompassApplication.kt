package com.example.teamcompass

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import com.example.teamcompass.ui.MapBitmapCache
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that initializes Firebase telemetry when available.
 */
@HiltAndroidApp
class TeamCompassApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val runtimeFlags = TestRuntimeFlagsReader.current()
        Log.i(
            TAG,
            "Runtime flags: hermetic=${runtimeFlags.hermetic}, disableTelemetry=${runtimeFlags.disableTelemetry}",
        )

        // Keep local/unit test processes without configured Firebase from crashing.
        val firebaseApp = FirebaseApp.getApps(this).firstOrNull() ?: FirebaseApp.initializeApp(this)
        if (firebaseApp == null) {
            Log.w(TAG, "FirebaseApp not initialized; skipping Analytics/Crashlytics setup")
            return
        }

        val telemetryEnabled = !runtimeFlags.disableTelemetry
        runCatching {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(telemetryEnabled)
        }.onFailure { err ->
            Log.w(TAG, "Crashlytics setup skipped", err)
        }

        runCatching {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(telemetryEnabled)
        }.onFailure { err ->
            Log.w(TAG, "Analytics setup skipped", err)
        }

        if (telemetryEnabled) {
            Log.i(TAG, "TeamCompassApplication initialized with Firebase telemetry enabled")
        } else {
            Log.i(TAG, "TeamCompassApplication initialized with telemetry disabled by runtime flags")
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        MapBitmapCache.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            Log.w(TAG, "Memory trim signal received: level=$level")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        MapBitmapCache.onLowMemory()
        Log.w(TAG, "Low memory signal received, map cache evicted")
    }

    companion object {
        private const val TAG = "TeamCompassApp"
    }
}
