package com.example.teamcompass.tracking

import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.MovementState
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamStatePayload
import com.example.teamcompass.domain.TrackingSessionConfig
import com.example.teamcompass.domain.TrackingTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class TrackingRuntimeMovementTelemetrySnapshot(
    val state: MovementState,
    val stationaryMs: Long,
    val walkingMs: Long,
    val vehicleMs: Long,
)

internal fun trackingRuntimeMovementTelemetrySnapshot(
    currentState: MovementState,
    nowMs: Long,
    stateEnterTimeMs: Long,
    stationaryStartTimeMs: Long,
    walkingStartTimeMs: Long,
    vehicleStartTimeMs: Long,
): TrackingRuntimeMovementTelemetrySnapshot {
    val stationaryMs = stationaryStartTimeMs +
        if (currentState == MovementState.STATIONARY) nowMs - stateEnterTimeMs else 0L
    val walkingMs = walkingStartTimeMs +
        if (currentState == MovementState.WALKING_SLOW || currentState == MovementState.WALKING_FAST) {
            nowMs - stateEnterTimeMs
        } else {
            0L
        }
    val vehicleMs = vehicleStartTimeMs +
        if (currentState == MovementState.VEHICLE) nowMs - stateEnterTimeMs else 0L
    return TrackingRuntimeMovementTelemetrySnapshot(
        state = currentState,
        stationaryMs = stationaryMs,
        walkingMs = walkingMs,
        vehicleMs = vehicleMs,
    )
}

internal fun trackingRuntimeSendStateAsync(
    cfg: TrackingSessionConfig,
    point: LocationPoint,
    anchored: Boolean,
    modeNow: PlayerMode,
    sosNow: Long,
    headingNow: Double?,
    scope: CoroutineScope,
    sendMutex: Mutex,
    repository: TeamRepository,
    telemetry: MutableStateFlow<TrackingTelemetry>,
) {
    val payload = TeamStatePayload(
        callsign = cfg.callsign,
        lat = point.lat,
        lon = point.lon,
        acc = point.accMeters,
        speed = point.speedMps,
        heading = headingNow,
        ts = point.timestampMs,
        mode = modeNow.name,
        anchored = anchored,
        sosUntilMs = sosNow,
    )
    scope.launch {
        sendMutex.withLock {
            when (val result = repository.upsertState(cfg.teamCode, cfg.uid, payload)) {
                is TeamActionResult.Success -> Unit
                is TeamActionResult.Failure -> {
                    telemetry.update {
                        it.copy(
                            rtdbWriteErrors = it.rtdbWriteErrors + 1,
                            lastTrackingRestartReason = result.details.message ?: result.details.error.name,
                        )
                    }
                }
            }
        }
    }
}
