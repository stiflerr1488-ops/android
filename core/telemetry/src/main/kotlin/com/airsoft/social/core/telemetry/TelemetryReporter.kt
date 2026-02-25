package com.airsoft.social.core.telemetry

interface TelemetryReporter {
    fun setUser(id: String?)
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setEnabled(enabled: Boolean)
}

interface CrashReporter {
    fun record(throwable: Throwable, context: Map<String, String> = emptyMap())
}

