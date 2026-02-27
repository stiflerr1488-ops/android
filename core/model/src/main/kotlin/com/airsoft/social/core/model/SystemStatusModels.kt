package com.airsoft.social.core.model

enum class IntegrityVerdict {
    Unknown,
    Ok,
    Warning,
}

enum class ThermalLevel {
    Normal,
    Warm,
    Hot,
}

data class PermissionStatusSnapshot(
    val grantedRuntimePermissions: Int = 0,
    val requiredRuntimePermissions: Int = 0,
    val notificationsEnabled: Boolean = false,
    val backgroundLocationEnabled: Boolean = false,
)

data class SecurityStatusSnapshot(
    val activeSessions: Int = 0,
    val suspiciousLoginSignals: Int = 0,
    val integrityVerdict: IntegrityVerdict = IntegrityVerdict.Unknown,
    val certificatePinningEnabled: Boolean = false,
)

data class DeviceDiagnosticsSnapshot(
    val batteryOptimizationDisabled: Boolean = false,
    val appStandbyRestricted: Boolean = false,
    val thermalLevel: ThermalLevel = ThermalLevel.Normal,
    val estimatedBatteryDrainPercentPerHour: Int = 0,
)
