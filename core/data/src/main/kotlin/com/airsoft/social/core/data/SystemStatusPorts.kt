package com.airsoft.social.core.data

import com.airsoft.social.core.model.DeviceDiagnosticsSnapshot
import com.airsoft.social.core.model.IntegrityVerdict
import com.airsoft.social.core.model.PermissionStatusSnapshot
import com.airsoft.social.core.model.SecurityStatusSnapshot
import com.airsoft.social.core.model.ThermalLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PermissionStatusPort {
    fun observeStatus(): Flow<PermissionStatusSnapshot>
    suspend fun refresh()
}

interface SecurityStatusPort {
    fun observeStatus(): Flow<SecurityStatusSnapshot>
    suspend fun refresh()
}

interface DeviceDiagnosticsPort {
    fun observeStatus(): Flow<DeviceDiagnosticsSnapshot>
    suspend fun refresh()
}

class PreviewPermissionStatusPort : PermissionStatusPort {
    private val state = MutableStateFlow(
        PermissionStatusSnapshot(
            grantedRuntimePermissions = 4,
            requiredRuntimePermissions = 6,
            notificationsEnabled = true,
            backgroundLocationEnabled = false,
        ),
    )

    override fun observeStatus(): Flow<PermissionStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() = Unit
}

class PreviewSecurityStatusPort : SecurityStatusPort {
    private val state = MutableStateFlow(
        SecurityStatusSnapshot(
            activeSessions = 2,
            suspiciousLoginSignals = 1,
            integrityVerdict = IntegrityVerdict.Warning,
            certificatePinningEnabled = false,
        ),
    )

    override fun observeStatus(): Flow<SecurityStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() = Unit
}

class PreviewDeviceDiagnosticsPort : DeviceDiagnosticsPort {
    private val state = MutableStateFlow(
        DeviceDiagnosticsSnapshot(
            batteryOptimizationDisabled = false,
            appStandbyRestricted = false,
            thermalLevel = ThermalLevel.Normal,
            estimatedBatteryDrainPercentPerHour = 4,
        ),
    )

    override fun observeStatus(): Flow<DeviceDiagnosticsSnapshot> = state.asStateFlow()

    override suspend fun refresh() = Unit
}
