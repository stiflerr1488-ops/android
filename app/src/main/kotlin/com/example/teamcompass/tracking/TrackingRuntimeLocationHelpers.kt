package com.example.teamcompass.tracking

import android.location.Location
import com.example.teamcompass.core.MovementState
import com.example.teamcompass.core.PlayerMode
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

internal fun trackingRuntimeLocationPriorityFor(movementState: MovementState): Int {
    return when (movementState) {
        MovementState.STATIONARY -> Priority.PRIORITY_LOW_POWER
        MovementState.WALKING_SLOW -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        MovementState.WALKING_FAST, MovementState.VEHICLE -> Priority.PRIORITY_HIGH_ACCURACY
    }
}

internal fun trackingRuntimeBuildLocationRequest(
    priority: Int,
    minIntervalMs: Long,
    mode: PlayerMode,
    movementState: MovementState,
): LocationRequest {
    return LocationRequest.Builder(priority, minIntervalMs)
        .setMinUpdateIntervalMillis((minIntervalMs / 2L).coerceAtLeast(2_000L))
        .setWaitForAccurateLocation(mode == PlayerMode.GAME && movementState != MovementState.STATIONARY)
        .build()
}

internal fun trackingRuntimeLocationCallback(
    onLocation: (Location) -> Unit,
): LocationCallback {
    return object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            onLocation(loc)
        }
    }
}
