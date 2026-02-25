package com.airsoft.social.core.telemetry

class NoopTelemetryReporter : TelemetryReporter {
    override fun setUser(id: String?) = Unit
    override fun logEvent(name: String, params: Map<String, Any?>) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
}

class NoopCrashReporter : CrashReporter {
    override fun record(throwable: Throwable, context: Map<String, String>) = Unit
}

