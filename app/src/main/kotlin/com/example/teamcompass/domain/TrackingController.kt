package com.example.teamcompass.domain

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.core.TrackingPolicy
import kotlinx.coroutines.flow.StateFlow

data class TrackingSessionConfig(
    val teamCode: String,
    val uid: String,
    val callsign: String,
    val mode: TrackingMode,
    val gamePolicy: TrackingPolicy,
    val silentPolicy: TrackingPolicy,
    val playerMode: PlayerMode,
    val sosUntilMs: Long,
)

data class TrackingTelemetry(
    val rtdbWriteErrors: Int = 0,
    val trackingRestarts: Int = 0,
    val lastLocationAtMs: Long = 0L,
    val lastTrackingRestartReason: String? = null,
    // Телеметрия адаптивного трекинга
    val currentMovementState: String = "STATIONARY",
    val adaptiveLocationUpdatesCount: Int = 0,
    val stationaryTimeMs: Long = 0L,
    val walkingTimeMs: Long = 0L,
    val vehicleTimeMs: Long = 0L,
)

interface TrackingController {
    val isTracking: StateFlow<Boolean>
    val location: StateFlow<LocationPoint?>
    val isAnchored: StateFlow<Boolean>
    val telemetry: StateFlow<TrackingTelemetry>

    fun start(config: TrackingSessionConfig)
    fun stop()
    fun updateHeading(headingDeg: Double?)
    fun updateStatus(playerMode: PlayerMode, sosUntilMs: Long, forceSend: Boolean = false)
}
