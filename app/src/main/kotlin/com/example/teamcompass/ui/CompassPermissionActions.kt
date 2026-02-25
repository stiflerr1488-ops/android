package com.example.teamcompass.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private const val BLUETOOTH_AUTO_REQUEST_DELAY_MS = 1_200L

internal data class CompassPermissionActions(
    val requestBluetoothScan: () -> Unit,
    val openLocationSettings: () -> Unit,
)

@Composable
internal fun rememberCompassPermissionActions(
    state: UiState,
    context: Context,
    onRequestPermission: (Boolean) -> Unit,
    onStartTracking: () -> Unit,
    onStartBluetoothScan: () -> Unit,
): CompassPermissionActions {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onRequestPermission(granted)
    }

    fun hasBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val hasScan = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        val hasConnect = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
        return hasScan && hasConnect
    }

    var pendingBluetoothScanAfterPermission by rememberSaveable { mutableStateOf(false) }
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val shouldStartScan = pendingBluetoothScanAfterPermission
        pendingBluetoothScanAfterPermission = false
        if (shouldStartScan && hasBluetoothPermissions()) {
            onStartBluetoothScan()
        }
    }

    var geoPromptRequested by rememberSaveable { mutableStateOf(false) }
    var btPromptRequested by rememberSaveable { mutableStateOf(false) }

    fun openLocationSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }.onFailure {
            Log.w("TeamCompassApp", "Failed to open location settings", it)
        }
    }

    fun requestBluetoothPermissionsIfNeeded(triggerScan: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (triggerScan) onStartBluetoothScan()
            return
        }
        if (hasBluetoothPermissions()) {
            if (triggerScan) onStartBluetoothScan()
            return
        }
        pendingBluetoothScanAfterPermission = triggerScan
        bluetoothLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        )
    }

    LaunchedEffect(state.teamCode, state.uid) {
        if (!geoPromptRequested) {
            geoPromptRequested = true
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        if (!btPromptRequested) {
            btPromptRequested = true
            delay(BLUETOOTH_AUTO_REQUEST_DELAY_MS)
            requestBluetoothPermissionsIfNeeded(triggerScan = false)
        }
    }

    LaunchedEffect(
        state.teamCode,
        state.hasLocationPermission,
        state.isLocationServiceEnabled,
        state.isTracking,
    ) {
        if (
            state.teamCode != null &&
            state.hasLocationPermission &&
            state.isLocationServiceEnabled &&
            !state.isTracking
        ) {
            onStartTracking()
        }
    }

    return CompassPermissionActions(
        requestBluetoothScan = { requestBluetoothPermissionsIfNeeded(triggerScan = true) },
        openLocationSettings = { openLocationSettings() },
    )
}
