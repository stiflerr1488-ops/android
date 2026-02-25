package com.example.teamcompass.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class LocationReadinessUpdate(
    val updatedState: UiState,
    val shouldRefreshPreview: Boolean,
)

internal class LocationReadinessCoordinator(
    private val application: Application,
) {
    private val locationManager = application.getSystemService(LocationManager::class.java)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationServiceEnabled(): Boolean {
        val manager = locationManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            runCatching {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.onFailure { err ->
                Log.w(TAG, "Failed to read legacy location providers state", err)
            }.getOrDefault(true)
        }
    }

    fun applyServiceState(state: UiState): UiState {
        return state.copy(
            tracking = state.tracking.copy(
                isLocationServiceEnabled = isLocationServiceEnabled(),
            ),
        )
    }

    fun refreshReadiness(
        state: UiState,
        permissionError: String,
        servicesDisabledError: String,
        trackingDisabledError: String,
    ): LocationReadinessUpdate {
        val permissionGranted = hasLocationPermission()
        val servicesEnabled = isLocationServiceEnabled()
        val clearLocationError = permissionGranted &&
            servicesEnabled &&
            (
                state.lastError == permissionError ||
                    state.lastError == servicesDisabledError ||
                    state.lastError == trackingDisabledError
                )
        val updatedState = state.copy(
            tracking = state.tracking.copy(
                hasLocationPermission = permissionGranted,
                isLocationServiceEnabled = servicesEnabled,
            ),
            lastError = if (clearLocationError) null else state.lastError,
        )
        return LocationReadinessUpdate(
            updatedState = updatedState,
            shouldRefreshPreview = permissionGranted && servicesEnabled,
        )
    }

    /**
     * Starts periodic location-service readiness monitoring in a scope owned by
     * TeamCompassViewModel (viewModelScope). The owner cancels it in onCleared().
     */
    fun startLocationServiceMonitor(
        scope: CoroutineScope,
        readState: () -> UiState,
        updateState: ((UiState) -> UiState) -> Unit,
        emitError: (String) -> Unit,
        servicesDisabledError: String,
        trackingDisabledError: String,
        pollIntervalMs: (Boolean) -> Long,
    ): Job {
        return scope.launch(Dispatchers.Default) {
            var wasEnabled = readState().isLocationServiceEnabled
            while (true) {
                delay(pollIntervalMs(readState().isTracking))
                val isEnabled = isLocationServiceEnabled()
                if (isEnabled == wasEnabled) continue
                wasEnabled = isEnabled
                updateState { it.copy(tracking = it.tracking.copy(isLocationServiceEnabled = isEnabled)) }
                if (!isEnabled && readState().isTracking) {
                    emitError(trackingDisabledError)
                } else if (isEnabled) {
                    updateState { state ->
                        if (state.lastError == servicesDisabledError || state.lastError == trackingDisabledError) {
                            state.copy(lastError = null)
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    private companion object {
        private const val TAG = "LocationReadiness"
    }
}
