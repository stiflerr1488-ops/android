package com.airsoft.social.feature.settings.impl

import com.airsoft.social.core.data.DeviceDiagnosticsPort
import com.airsoft.social.core.data.PermissionStatusPort
import com.airsoft.social.core.data.SecurityStatusPort
import com.airsoft.social.core.model.DeviceDiagnosticsSnapshot
import com.airsoft.social.core.model.IntegrityVerdict
import com.airsoft.social.core.model.PermissionStatusSnapshot
import com.airsoft.social.core.model.SecurityStatusSnapshot
import com.airsoft.social.core.model.ThermalLevel
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsSystemStatusViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `viewmodel exposes snapshots from all status ports`() = runTest {
        val permissions = FakePermissionStatusPort()
        val security = FakeSecurityStatusPort()
        val diagnostics = FakeDeviceDiagnosticsPort()
        val viewModel = SettingsSystemStatusViewModel(
            permissionStatusPort = permissions,
            securityStatusPort = security,
            deviceDiagnosticsPort = diagnostics,
        )

        permissions.emit(
            PermissionStatusSnapshot(
                grantedRuntimePermissions = 5,
                requiredRuntimePermissions = 6,
                notificationsEnabled = true,
                backgroundLocationEnabled = true,
            ),
        )
        security.emit(
            SecurityStatusSnapshot(
                activeSessions = 3,
                suspiciousLoginSignals = 0,
                integrityVerdict = IntegrityVerdict.Ok,
                certificatePinningEnabled = true,
            ),
        )
        diagnostics.emit(
            DeviceDiagnosticsSnapshot(
                batteryOptimizationDisabled = true,
                appStandbyRestricted = false,
                thermalLevel = ThermalLevel.Warm,
                estimatedBatteryDrainPercentPerHour = 7,
            ),
        )
        advanceUntilIdle()

        assertEquals(5, viewModel.permissionStatus.value.grantedRuntimePermissions)
        assertEquals(3, viewModel.securityStatus.value.activeSessions)
        assertEquals(ThermalLevel.Warm, viewModel.deviceDiagnostics.value.thermalLevel)
    }

    @Test
    fun `refresh delegates to all ports`() = runTest {
        val permissions = FakePermissionStatusPort()
        val security = FakeSecurityStatusPort()
        val diagnostics = FakeDeviceDiagnosticsPort()
        val viewModel = SettingsSystemStatusViewModel(
            permissionStatusPort = permissions,
            securityStatusPort = security,
            deviceDiagnosticsPort = diagnostics,
        )

        viewModel.refreshPermissions()
        viewModel.refreshSecurity()
        viewModel.refreshDiagnostics()
        advanceUntilIdle()

        assertTrue(permissions.refreshCalled)
        assertTrue(security.refreshCalled)
        assertTrue(diagnostics.refreshCalled)
    }
}

private class FakePermissionStatusPort : PermissionStatusPort {
    private val state = MutableStateFlow(PermissionStatusSnapshot())
    var refreshCalled: Boolean = false
        private set

    override fun observeStatus(): Flow<PermissionStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        refreshCalled = true
    }

    fun emit(value: PermissionStatusSnapshot) {
        state.value = value
    }
}

private class FakeSecurityStatusPort : SecurityStatusPort {
    private val state = MutableStateFlow(SecurityStatusSnapshot())
    var refreshCalled: Boolean = false
        private set

    override fun observeStatus(): Flow<SecurityStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        refreshCalled = true
    }

    fun emit(value: SecurityStatusSnapshot) {
        state.value = value
    }
}

private class FakeDeviceDiagnosticsPort : DeviceDiagnosticsPort {
    private val state = MutableStateFlow(DeviceDiagnosticsSnapshot())
    var refreshCalled: Boolean = false
        private set

    override fun observeStatus(): Flow<DeviceDiagnosticsSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        refreshCalled = true
    }

    fun emit(value: DeviceDiagnosticsSnapshot) {
        state.value = value
    }
}
