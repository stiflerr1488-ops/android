package com.example.teamcompass.ui

import android.app.Application
import android.util.Log
import android.view.Window
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class TeamCompassDeviceUiCoordinator(
    private val scope: CoroutineScope,
    private val application: Application,
    private val headingSensorCoordinator: HeadingSensorCoordinator,
    private val locationReadinessCoordinator: LocationReadinessCoordinator,
    private val autoBrightnessBinding: AutoBrightnessBinding,
    private val bluetoothScanCoordinatorProvider: () -> BluetoothScanCoordinator?,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (String) -> Unit,
    private val persistAutoBrightnessEnabled: (Boolean) -> Unit,
    private val persistScreenBrightness: (Float) -> Unit,
    private val persistHasStartedOnce: (Boolean) -> Unit,
    private val startTracking: (TrackingMode, Boolean) -> Unit,
    private val servicesDisabledError: () -> String,
    private val trackingDisabledError: () -> String,
    private val bluetoothUnavailableError: () -> String,
    private val pollIntervalMs: (Boolean) -> Long,
    private val logTag: String,
) {
    private var locationServiceMonitorJob: Job? = null

    fun startHeading() {
        headingSensorCoordinator.start()
    }

    fun stopHeading() {
        headingSensorCoordinator.stop()
    }

    fun startLocationServiceMonitor() {
        locationServiceMonitorJob?.cancel()
        locationServiceMonitorJob = locationReadinessCoordinator.startLocationServiceMonitor(
            scope = scope,
            readState = readState,
            updateState = updateState,
            emitError = emitError,
            servicesDisabledError = servicesDisabledError(),
            trackingDisabledError = trackingDisabledError(),
            pollIntervalMs = pollIntervalMs,
        )
    }

    fun stopLocationServiceMonitor() {
        locationServiceMonitorJob?.cancel()
        locationServiceMonitorJob = null
    }

    fun bindAutoBrightnessWindow(window: Window?) {
        val state = readState()
        autoBrightnessBinding.bindWindow(
            window = window,
            normalBrightness = state.screenBrightness,
            enabled = state.autoBrightnessEnabled,
        )
    }

    fun setAutoBrightnessEnabled(enabled: Boolean) {
        autoBrightnessBinding.setEnabled(enabled)
        persistAutoBrightnessEnabled(enabled)
    }

    fun setScreenBrightness(brightness: Float) {
        autoBrightnessBinding.setBrightness(brightness)
        persistScreenBrightness(brightness)
    }

    fun setHasStartedOnce(value: Boolean) {
        updateState { it.copy(tracking = it.tracking.copy(hasStartedOnce = value)) }
        persistHasStartedOnce(value)
    }

    fun autoStartTrackingIfNeeded() {
        val state = readState()
        if (!state.hasStartedOnce && !state.isTracking) {
            setHasStartedOnce(true)
            startTracking(state.defaultMode, true)
        }
    }

    fun hasBluetoothPermission(): Boolean = bluetoothScanCoordinatorProvider()?.hasBluetoothPermission() ?: false

    fun startBluetoothScan() {
        val coordinator = bluetoothScanCoordinatorProvider()
        if (coordinator == null) {
            emitError(bluetoothUnavailableError())
            return
        }
        coordinator.startScan()
    }

    fun cancelBluetoothScan() {
        bluetoothScanCoordinatorProvider()?.cancelScan()
    }

    fun clearAutoBrightnessBinding() {
        autoBrightnessBinding.clear()
    }

    fun shutdownBluetoothCoordinatorSafely() {
        val coordinator = bluetoothScanCoordinatorProvider() ?: return
        runCatching { coordinator.shutdown() }
            .onFailure { err ->
                Log.w(logTag, "Failed to shutdown Bluetooth coordinator", err)
            }
    }

    fun onCleared() {
        stopHeading()
        stopLocationServiceMonitor()
        clearAutoBrightnessBinding()
        shutdownBluetoothCoordinatorSafely()
    }
}
