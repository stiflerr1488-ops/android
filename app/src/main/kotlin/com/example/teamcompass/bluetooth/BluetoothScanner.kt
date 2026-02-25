package com.example.teamcompass.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Bluetooth Low Energy scanner.
 */
class BluetoothScanner(
    private val context: Context,
    private val adapterProvider: () -> BluetoothAdapter? = {
        context.getSystemService(BluetoothManager::class.java)?.adapter
    },
    private val handlerFactory: (android.os.Looper) -> android.os.Handler = { looper -> android.os.Handler(looper) },
) {

    private var isScanning = false
    private var scanJob: android.os.Handler? = null
    private var currentScanCallback: ScanCallback? = null

    private fun resolveScanner(): BluetoothLeScanner? {
        val adapter = adapterProvider()
        if (adapter == null || !adapter.isEnabled) return null
        return adapter.bluetoothLeScanner
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(Manifest.permission.BLUETOOTH) &&
                hasPermission(Manifest.permission.BLUETOOTH_ADMIN) &&
                (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.BLUETOOTH)
        }
    }

    /**
     * Check whether Bluetooth adapter is available and enabled.
     */
    fun isBluetoothAvailable(): Boolean {
        val bluetoothAdapter = adapterProvider()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Start BLE scanning for a fixed duration.
     *
     * @param duration scan duration in milliseconds
     * @param allyDeviceNames known ally device names to be filtered out
     * @param onDeviceFound callback for each matched device
     * @param onComplete callback with full collected list when scan ends
     */
    fun startScan(
        duration: Long = DEFAULT_SCAN_DURATION,
        allyDeviceNames: Set<String> = emptySet(),
        onDeviceFound: (BluetoothDevice) -> Unit = {},
        onComplete: (List<BluetoothDevice>) -> Unit,
    ) {
        if (!isBluetoothAvailable()) {
            Log.w(TAG, "Bluetooth not available")
            onComplete(emptyList())
            return
        }

        val canScan = hasScanPermission()
        val canConnect = hasConnectPermission()
        if (!canScan || !canConnect) {
            Log.w(TAG, "Bluetooth permissions missing: scan=$canScan, connect=$canConnect")
            onComplete(emptyList())
            return
        }

        val scanner = resolveScanner()
        if (scanner == null) {
            Log.w(TAG, "Bluetooth LE Scanner is null")
            onComplete(emptyList())
            return
        }

        // Stop previous scan if still running.
        stopScan()

        isScanning = true
        val devices = mutableMapOf<String, BluetoothDevice>()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                processScanResult(result, devices, allyDeviceNames, onDeviceFound)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                super.onBatchScanResults(results)
                results.forEach { result ->
                    processScanResult(result, devices, allyDeviceNames, onDeviceFound)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "Scan failed with error: $errorCode")
                clearScheduledStop()
                currentScanCallback = null
                isScanning = false
                onComplete(devices.values.toList())
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0L)
            .build()

        val scanFilters = listOf<ScanFilter>()

        try {
            scanner.startScan(scanFilters, scanSettings, scanCallback)
            currentScanCallback = scanCallback
            Log.d(TAG, "Bluetooth scan started")

            scanJob = handlerFactory(context.mainLooper)
            scanJob?.postDelayed({
                stopScan()
                onComplete(devices.values.toList())
            }, duration)
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth scan permission denied", e)
            clearScheduledStop()
            currentScanCallback = null
            isScanning = false
            onComplete(emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Bluetooth scan", e)
            clearScheduledStop()
            currentScanCallback = null
            isScanning = false
            onComplete(emptyList())
        }
    }

    /**
     * Process a single scan result.
     */
    // BLUETOOTH_CONNECT precondition is checked above via hasConnectPermission().
    @SuppressLint("MissingPermission")
    private fun processScanResult(
        result: ScanResult,
        devices: MutableMap<String, BluetoothDevice>,
        allyDeviceNames: Set<String>,
        onDeviceFound: (BluetoothDevice) -> Unit,
    ) {
        val device = result.device
        val rssi = result.rssi

        // Skip weak signal (roughly farther than 50m).
        if (rssi <= -90) return
        if (!hasConnectPermission()) {
            Log.w(TAG, "Skipping scan result: BLUETOOTH_CONNECT permission missing")
            return
        }

        val name = runCatching { device.name }.getOrNull().orEmpty().ifBlank { "Unknown" }
        val address = runCatching { device.address }.getOrNull()
        if (address.isNullOrBlank()) {
            Log.w(TAG, "Skipping scan result: device address unavailable")
            return
        }

        if (allyDeviceNames.any { allyName -> name.equals(allyName.trim(), ignoreCase = true) }) {
            Log.d(TAG, "Filtered ally device: $name")
            return
        }

        val type = classifyDevice(result)
        val serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid.toString() } ?: emptyList()

        val bluetoothDevice = BluetoothDevice(
            address = address,
            name = name,
            rssi = rssi,
            timestamp = System.currentTimeMillis(),
            type = type,
            serviceUuids = serviceUuids,
        )

        devices[address] = bluetoothDevice
        onDeviceFound(bluetoothDevice)

        Log.d(TAG, "Found: $name ($type) RSSI: $rssi dBm")
    }

    /**
     * Classify device type by advertised services and name.
     */
    // Called only from processScanResult(), where BLUETOOTH_CONNECT precondition is enforced.
    @SuppressLint("MissingPermission")
    private fun classifyDevice(result: ScanResult): DeviceType {
        val device = result.device
        val name = runCatching { device.name }.getOrNull().orEmpty()
        val serviceUuids = result.scanRecord?.serviceUuids ?: emptyList()

        val lowerName = name.lowercase()
        return when {
            lowerName.contains("airpods") ||
                lowerName.contains("buds") ||
                lowerName.contains("headphone") ||
                lowerName.contains("earbud") -> DeviceType.HEADPHONES

            lowerName.contains("watch") ||
                lowerName.contains("garmin") ||
                lowerName.contains("fitbit") ||
                lowerName.contains("amazfit") -> DeviceType.WATCH

            lowerName.contains("tablet") ||
                lowerName.contains("ipad") -> DeviceType.TABLET

            lowerName.contains("macbook") ||
                lowerName.contains("laptop") ||
                lowerName.contains("windows") -> DeviceType.LAPTOP

            serviceUuids.any { uuid -> isPhoneService(uuid.uuid) } ||
                lowerName.contains("phone") ||
                lowerName.contains("samsung") ||
                lowerName.contains("iphone") ||
                lowerName.contains("pixel") ||
                lowerName.contains("xiaomi") ||
                lowerName.contains("huawei") -> DeviceType.PHONE

            else -> DeviceType.UNKNOWN
        }
    }

    /**
     * Check if service UUID is commonly present on phones.
     */
    private fun isPhoneService(uuid: java.util.UUID): Boolean {
        val phoneServices = listOf(
            "0000110a-0000-1000-8000-00805f9b34fb", // OBEX Object Push
            "00001112-0000-1000-8000-00805f9b34fb", // Headset AG
            "0000111f-0000-1000-8000-00805f9b34fb", // Handsfree Audio Gateway
        )
        return phoneServices.any { it.equals(uuid.toString(), ignoreCase = true) }
    }

    /**
     * Stop current BLE scan.
     */
    // BLUETOOTH_SCAN precondition is checked in this method before stopScan().
    @SuppressLint("MissingPermission")
    fun stopScan() {
        clearScheduledStop()
        val callback = currentScanCallback
        currentScanCallback = null
        if (!isScanning) return

        try {
            if (!hasScanPermission()) {
                Log.w(TAG, "Missing scan permission while stopping scan, resetting local state only")
                isScanning = false
                return
            }

            if (callback != null) {
                resolveScanner()?.stopScan(callback)
            }
            isScanning = false
            Log.d(TAG, "Bluetooth scan stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan", e)
            isScanning = false
        }
    }

    /**
     * Whether scanner is currently active.
     */
    fun isScanning(): Boolean = isScanning

    private fun clearScheduledStop() {
        scanJob?.removeCallbacksAndMessages(null)
        scanJob = null
    }

    private companion object {
        private const val TAG = "BluetoothScanner"
        private const val DEFAULT_SCAN_DURATION = 10_000L
    }
}
