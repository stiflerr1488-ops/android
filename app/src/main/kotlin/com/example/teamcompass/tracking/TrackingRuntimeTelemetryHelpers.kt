package com.example.teamcompass.tracking

import com.example.teamcompass.domain.TrackingTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal fun MutableStateFlow<TrackingTelemetry>.trackingRuntimeRecordErrorReason(
    reason: String,
) {
    update {
        it.copy(
            rtdbWriteErrors = it.rtdbWriteErrors + 1,
            lastTrackingRestartReason = reason,
        )
    }
}

internal fun MutableStateFlow<TrackingTelemetry>.trackingRuntimeRecordRestart(reason: String) {
    update {
        it.copy(
            trackingRestarts = it.trackingRestarts + 1,
            lastTrackingRestartReason = reason,
        )
    }
}

internal fun MutableStateFlow<TrackingTelemetry>.trackingRuntimeRecordWatchdogThrottled() {
    update { it.copy(lastTrackingRestartReason = "Watchdog restart throttled") }
}

internal fun MutableStateFlow<TrackingTelemetry>.trackingRuntimeRecordLastLocation(nowMs: Long) {
    update { it.copy(lastLocationAtMs = nowMs) }
}

internal fun MutableStateFlow<TrackingTelemetry>.trackingRuntimeIncrementAdaptiveSends() {
    update { it.copy(adaptiveLocationUpdatesCount = it.adaptiveLocationUpdatesCount + 1) }
}
