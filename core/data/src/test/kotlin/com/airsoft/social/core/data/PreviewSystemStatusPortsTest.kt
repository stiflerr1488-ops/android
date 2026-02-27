package com.airsoft.social.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewSystemStatusPortsTest {
    @Test
    fun `permission status port exposes preview snapshot`() = runTest {
        val port = PreviewPermissionStatusPort()

        val status = port.observeStatus().first()

        assertEquals(4, status.grantedRuntimePermissions)
        assertEquals(6, status.requiredRuntimePermissions)
        assertTrue(status.notificationsEnabled)
        assertFalse(status.backgroundLocationEnabled)
    }

    @Test
    fun `security status port exposes preview snapshot`() = runTest {
        val port = PreviewSecurityStatusPort()

        val status = port.observeStatus().first()

        assertEquals(2, status.activeSessions)
        assertEquals(1, status.suspiciousLoginSignals)
    }

    @Test
    fun `device diagnostics port exposes preview snapshot`() = runTest {
        val port = PreviewDeviceDiagnosticsPort()

        val status = port.observeStatus().first()

        assertEquals(4, status.estimatedBatteryDrainPercentPerHour)
        assertFalse(status.batteryOptimizationDisabled)
        assertFalse(status.appStandbyRestricted)
    }
}
