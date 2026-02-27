package com.airsoft.social.infra.device

import android.Manifest
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.airsoft.social.core.data.DeviceDiagnosticsPort
import com.airsoft.social.core.data.PermissionStatusPort
import com.airsoft.social.core.data.SecurityStatusPort
import com.airsoft.social.core.model.DeviceDiagnosticsSnapshot
import com.airsoft.social.core.model.IntegrityVerdict
import com.airsoft.social.core.model.PermissionStatusSnapshot
import com.airsoft.social.core.model.SecurityStatusSnapshot
import com.airsoft.social.core.model.ThermalLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPermissionStatusPort(
    private val context: Context,
) : PermissionStatusPort {

    private val state = MutableStateFlow(computeSnapshot())

    override fun observeStatus(): Flow<PermissionStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        state.value = computeSnapshot()
    }

    private fun computeSnapshot(): PermissionStatusSnapshot {
        val runtimePermissions = buildRuntimePermissionsList()
        val granted = runtimePermissions.count { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val backgroundLocationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        return PermissionStatusSnapshot(
            grantedRuntimePermissions = granted,
            requiredRuntimePermissions = runtimePermissions.size,
            notificationsEnabled = notificationsEnabled,
            backgroundLocationEnabled = backgroundLocationEnabled,
        )
    }

    private fun buildRuntimePermissionsList(): List<String> = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            @Suppress("DEPRECATION")
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.distinct()
}

class AndroidSecurityStatusPort(
    private val context: Context,
) : SecurityStatusPort {

    private val state = MutableStateFlow(computeSnapshot())

    override fun observeStatus(): Flow<SecurityStatusSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        state.value = computeSnapshot()
    }

    private fun computeSnapshot(): SecurityStatusSnapshot {
        val appFlags = context.applicationInfo.flags
        val isDebuggable = (appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val hasTestKeys = Build.TAGS?.contains("test-keys", ignoreCase = true) == true
        val nonUserBuild = Build.TYPE != "user"
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isDeviceSecure = when {
            keyguard == null -> null
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> keyguard.isDeviceSecure
            else -> keyguard.isKeyguardSecure
        }

        val suspiciousSignals = listOf(isDebuggable, hasTestKeys, nonUserBuild).count { it }
        val verdict = when {
            isDeviceSecure == null -> IntegrityVerdict.Unknown
            suspiciousSignals == 0 && isDeviceSecure -> IntegrityVerdict.Ok
            else -> IntegrityVerdict.Warning
        }

        return SecurityStatusSnapshot(
            activeSessions = 1,
            suspiciousLoginSignals = suspiciousSignals,
            integrityVerdict = verdict,
            certificatePinningEnabled = false,
        )
    }
}

class AndroidDeviceDiagnosticsPort(
    private val context: Context,
) : DeviceDiagnosticsPort {

    private val state = MutableStateFlow(computeSnapshot())

    override fun observeStatus(): Flow<DeviceDiagnosticsSnapshot> = state.asStateFlow()

    override suspend fun refresh() {
        state.value = computeSnapshot()
    }

    private fun computeSnapshot(): DeviceDiagnosticsSnapshot {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val optimizationDisabled = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        val backgroundRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activityManager?.isBackgroundRestricted ?: false
        } else {
            false
        }

        val thermalLevel = mapThermalLevel(
            thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager?.currentThermalStatus
            } else {
                null
            },
        )

        val rawCurrentNowMicroAmp = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val estimatedDrainPercentPerHour = estimateDrainPerHour(
            thermalLevel = thermalLevel,
            rawCurrentNowMicroAmp = rawCurrentNowMicroAmp,
        )

        return DeviceDiagnosticsSnapshot(
            batteryOptimizationDisabled = optimizationDisabled,
            appStandbyRestricted = backgroundRestricted,
            thermalLevel = thermalLevel,
            estimatedBatteryDrainPercentPerHour = estimatedDrainPercentPerHour,
        )
    }

    private fun estimateDrainPerHour(
        thermalLevel: ThermalLevel,
        rawCurrentNowMicroAmp: Int?,
    ): Int {
        val baseline = when (thermalLevel) {
            ThermalLevel.Normal -> 4
            ThermalLevel.Warm -> 6
            ThermalLevel.Hot -> 9
        }
        if (rawCurrentNowMicroAmp == null || rawCurrentNowMicroAmp == Int.MIN_VALUE || rawCurrentNowMicroAmp == 0) {
            return baseline
        }
        val dischargeMicroAmp = kotlin.math.abs(rawCurrentNowMicroAmp)
        val extra = when {
            dischargeMicroAmp >= 600_000 -> 2
            dischargeMicroAmp >= 300_000 -> 1
            else -> 0
        }
        return (baseline + extra).coerceAtMost(20)
    }

    private fun mapThermalLevel(thermalStatus: Int?): ThermalLevel = when {
        thermalStatus == null -> ThermalLevel.Normal
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> ThermalLevel.Normal
        thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.Hot
        thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.Warm
        else -> ThermalLevel.Normal
    }
}
