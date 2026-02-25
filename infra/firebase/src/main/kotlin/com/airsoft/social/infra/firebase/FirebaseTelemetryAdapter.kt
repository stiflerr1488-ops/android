package com.airsoft.social.infra.firebase

import android.app.Application
import android.os.Bundle
import com.airsoft.social.core.telemetry.CrashReporter
import com.airsoft.social.core.telemetry.TelemetryReporter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseTelemetryAdapter(
    application: Application,
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application),
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
) : TelemetryReporter, CrashReporter {
    override fun setUser(id: String?) {
        analytics.setUserId(id)
        crashlytics.setUserId(id ?: "")
    }

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics.logEvent(name, bundle)
    }

    override fun setEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun record(throwable: Throwable, context: Map<String, String>) {
        context.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(throwable)
    }
}

