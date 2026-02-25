package com.airsoft.social.core.telemetry

import org.junit.Test

class NoopTelemetryTest {
    @Test
    fun `noop telemetry methods do not throw`() {
        val telemetry = NoopTelemetryReporter()
        val crashReporter = NoopCrashReporter()

        telemetry.setEnabled(true)
        telemetry.setUser("user-1")
        telemetry.logEvent("shell_opened", mapOf("source" to "test"))
        crashReporter.record(IllegalStateException("boom"), mapOf("screen" to "test"))
    }
}

