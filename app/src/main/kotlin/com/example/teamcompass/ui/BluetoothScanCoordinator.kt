package com.example.teamcompass.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.teamcompass.R
import com.example.teamcompass.bluetooth.BluetoothDevice
import com.example.teamcompass.bluetooth.BluetoothScanResult
import com.example.teamcompass.bluetooth.BluetoothScanner
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coordinates BLE scan lifecycle and publication of nearby contacts into team enemy pings.
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class BluetoothScanCoordinator(
    private val application: Application,
    private val teamRepository: TeamRepository,
    private val scope: CoroutineScope,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (String) -> Unit,
    private val bluetoothScanner: BluetoothScanner = BluetoothScanner(application),
) {
    private val bluetoothPublishCooldownByAddress = mutableMapOf<String, Long>()
    private var scanResultClearJob: Job? = null

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scan = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH_SCAN,
            ) == PackageManager.PERMISSION_GRANTED
            val connect = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED
            scan && connect
        } else {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.BLUETOOTH,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun startScan() {
        if (!bluetoothScanner.isBluetoothAvailable()) {
            emitError(application.getString(R.string.vm_error_bluetooth_unavailable))
            return
        }

        if (!hasBluetoothPermission()) {
            emitError(application.getString(R.string.vm_error_bluetooth_permission_required))
            return
        }

        updateState { it.copy(bluetooth = it.bluetooth.copy(isScanning = true)) }

        val allyNames = readState().players
            .map { it.nick }
            .filter { it.isNotBlank() }
            .toSet()

        Log.d(TAG, "Starting Bluetooth scan, filtering ${allyNames.size} ally devices")

        scope.launch {
            bluetoothScanner.startScan(
                duration = SCAN_DURATION_MS,
                allyDeviceNames = allyNames,
                onDeviceFound = { device ->
                    Log.d(
                        TAG,
                        "BT Found: ${device.name} (${device.type}) RSSI: ${device.rssi} dBm, distance=${device.calculateDistanceMeters().toInt()}m",
                    )
                },
                onComplete = { devices ->
                    Log.d(TAG, "Bluetooth scan complete: found ${devices.size} devices")
                    val now = System.currentTimeMillis()
                    val expiresAt = now + SCAN_RESULT_TTL_MS
                    updateState {
                        it.copy(
                            bluetooth = it.bluetooth.copy(
                                isScanning = false,
                                scanResult = BluetoothScanResult(
                                    devices = devices,
                                    scannedAt = now,
                                    expiresAt = expiresAt,
                                ),
                            ),
                        )
                    }
                    publishBluetoothContactsToTeam(devices)

                    scanResultClearJob?.cancel()
                    scanResultClearJob = scope.launch {
                        delay(SCAN_RESULT_TTL_MS)
                        updateState {
                            val current = it.bluetooth.scanResult
                            if (current != null && current.expiresAt == expiresAt) {
                                it.copy(bluetooth = it.bluetooth.copy(scanResult = null))
                            } else {
                                it
                            }
                        }
                    }
                },
            )
        }
    }

    fun cancelScan() {
        bluetoothScanner.stopScan()
        scanResultClearJob?.cancel()
        scanResultClearJob = null
        updateState { it.copy(bluetooth = it.bluetooth.copy(isScanning = false)) }
    }

    fun shutdown() {
        scanResultClearJob?.cancel()
        scanResultClearJob = null
        cancelScan()
        bluetoothPublishCooldownByAddress.clear()
    }

    private fun publishBluetoothContactsToTeam(devices: List<BluetoothDevice>) {
        if (devices.isEmpty()) return
        val state = readState()
        val me = state.me ?: return
        val teamCode = state.teamCode ?: return
        val uid = state.uid ?: return

        val now = System.currentTimeMillis()
        bluetoothPublishCooldownByAddress.entries.removeIf { now - it.value > BT_PUBLISH_COOLDOWN_MS * 2 }

        val toPublish = devices
            .sortedByDescending { it.rssi }
            .filter { device ->
                val last = bluetoothPublishCooldownByAddress[device.address] ?: 0L
                (now - last) >= BT_PUBLISH_COOLDOWN_MS
            }
            .take(MAX_BT_PINGS_PER_SCAN)

        if (toPublish.isEmpty()) return

        scope.launch {
            toPublish.forEach { device ->
                val rawDistance = device.calculateDistanceMeters()
                val distanceM = when {
                    rawDistance.isNaN() || rawDistance.isInfinite() || rawDistance <= 0.0 -> 12.0
                    else -> rawDistance.coerceIn(2.0, 250.0)
                }
                val pseudoBearingAbs = ((device.address.hashCode().toLong() and 0x7fffffffL) % 360L).toDouble()
                val point = destinationPoint(
                    latDeg = me.lat,
                    lonDeg = me.lon,
                    bearingDeg = pseudoBearingAbs,
                    distanceMeters = distanceM,
                )
                when (
                    teamRepository.addEnemyPing(
                        teamCode = teamCode,
                        uid = uid,
                        lat = point.first,
                        lon = point.second,
                        type = "BLUETOOTH",
                        ttlMs = BLUETOOTH_PING_TTL_MS,
                    )
                ) {
                    is TeamActionResult.Success -> {
                        bluetoothPublishCooldownByAddress[device.address] = now
                    }

                    is TeamActionResult.Failure -> {
                        Log.w(TAG, "Failed to publish BT contact for team=$teamCode")
                    }
                }
            }
        }
    }

    private companion object {
        private const val TAG = "BluetoothScanCoord"
        private const val SCAN_DURATION_MS = 10_000L
        private const val SCAN_RESULT_TTL_MS = 30_000L
        private const val BLUETOOTH_PING_TTL_MS = 35_000L
        private const val BT_PUBLISH_COOLDOWN_MS = 15_000L
        private const val MAX_BT_PINGS_PER_SCAN = 8
    }
}
