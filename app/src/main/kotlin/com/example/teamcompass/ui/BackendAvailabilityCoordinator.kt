package com.example.teamcompass.ui

import kotlinx.coroutines.CoroutineScope

/**
 * Drives backend availability/staleness flags in UI state.
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class BackendAvailabilityCoordinator(
    private val backendHealthDelegate: BackendHealthDelegate,
    private val scope: CoroutineScope,
    private val readState: () -> UiState,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val emitError: (String) -> Unit,
) {
    private var lastBackendHealthAvailableSample: Boolean = true

    fun start(backendDownMessage: String) {
        backendHealthDelegate.startHealthMonitor(scope = scope) { available, _, nowMs ->
            lastBackendHealthAvailableSample = available
            var becameUnavailable = false
            var becameRecovered = false
            updateState { state ->
                val telemetry = state.tracking.telemetry
                val stale = computeBackendStale(
                    lastSnapshotAtMs = telemetry.lastSnapshotAtMs,
                    nowMs = nowMs,
                )
                val hasFreshSnapshot = telemetry.lastSnapshotAtMs > 0L && !stale
                val effectiveAvailable = available || hasFreshSnapshot
                becameUnavailable = telemetry.backendAvailable && !effectiveAvailable
                becameRecovered = !telemetry.backendAvailable && effectiveAvailable
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = telemetry.copy(
                            backendAvailable = effectiveAvailable,
                            backendUnavailableSinceMs = if (effectiveAvailable) {
                                0L
                            } else {
                                telemetry.backendUnavailableSinceMs.takeIf { it > 0L } ?: nowMs
                            },
                            isBackendStale = stale,
                        )
                    )
                )
            }
            scheduleStaleRefresh(nowMs = nowMs, backendDownMessage = backendDownMessage)
            if (becameUnavailable) {
                emitError(backendDownMessage)
            } else if (becameRecovered) {
                updateState { state ->
                    if (state.lastError == backendDownMessage) {
                        state.copy(lastError = null)
                    } else {
                        state
                    }
                }
            }
        }
        scheduleStaleRefresh(backendDownMessage = backendDownMessage)
    }

    fun scheduleStaleRefresh(
        nowMs: Long = System.currentTimeMillis(),
        backendDownMessage: String,
    ) {
        backendHealthDelegate.scheduleStaleRefresh(
            scope = scope,
            nowMs = nowMs,
            readLastSnapshotAtMs = { readState().tracking.telemetry.lastSnapshotAtMs },
            onRefresh = { refreshedAtMs ->
                refreshStaleFlag(nowMs = refreshedAtMs, backendDownMessage = backendDownMessage)
            },
        )
    }

    fun refreshStaleFlag(
        nowMs: Long = System.currentTimeMillis(),
        backendDownMessage: String,
    ) {
        var becameUnavailable = false
        var becameRecovered = false
        updateState { state ->
            val telemetry = state.tracking.telemetry
            val stale = computeBackendStale(
                lastSnapshotAtMs = telemetry.lastSnapshotAtMs,
                nowMs = nowMs,
            )
            val hasFreshSnapshot = telemetry.lastSnapshotAtMs > 0L && !stale
            val effectiveAvailable = lastBackendHealthAvailableSample || hasFreshSnapshot
            val unavailableSinceMs = if (effectiveAvailable) {
                0L
            } else {
                telemetry.backendUnavailableSinceMs.takeIf { it > 0L } ?: nowMs
            }
            val changed =
                stale != telemetry.isBackendStale ||
                    effectiveAvailable != telemetry.backendAvailable ||
                    unavailableSinceMs != telemetry.backendUnavailableSinceMs
            if (!changed) {
                state
            } else {
                becameUnavailable = telemetry.backendAvailable && !effectiveAvailable
                becameRecovered = !telemetry.backendAvailable && effectiveAvailable
                state.copy(
                    tracking = state.tracking.copy(
                        telemetry = telemetry.copy(
                            backendAvailable = effectiveAvailable,
                            backendUnavailableSinceMs = unavailableSinceMs,
                            isBackendStale = stale,
                        ),
                    )
                )
            }
        }
        if (becameUnavailable) {
            emitError(backendDownMessage)
        } else if (becameRecovered) {
            updateState { state ->
                if (state.lastError == backendDownMessage) {
                    state.copy(lastError = null)
                } else {
                    state
                }
            }
        }
    }

    fun computeBackendStale(lastSnapshotAtMs: Long, nowMs: Long): Boolean {
        return backendHealthDelegate.computeBackendStale(
            lastSnapshotAtMs = lastSnapshotAtMs,
            nowMs = nowMs,
        )
    }

    fun stop() {
        backendHealthDelegate.stop()
        lastBackendHealthAvailableSample = true
        updateState {
            it.copy(
                tracking = it.tracking.copy(
                    telemetry = it.tracking.telemetry.copy(
                        backendAvailable = true,
                        backendUnavailableSinceMs = 0L,
                        lastSnapshotAtMs = 0L,
                        isBackendStale = false,
                    ),
                )
            )
        }
    }
}
