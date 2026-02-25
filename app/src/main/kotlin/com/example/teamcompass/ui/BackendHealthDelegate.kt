package com.example.teamcompass.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class BackendHealthDelegate(
    private val backendHealthMonitor: BackendHealthMonitor,
    private val staleWarningMs: Long,
    private val nowMsProvider: () -> Long = System::currentTimeMillis,
    private val delayFn: suspend (Long) -> Unit = { delay(it) },
) {
    private var backendHealthJob: Job? = null
    private var backendStaleMonitorJob: Job? = null

    /**
     * Starts backend health sampling in a scope owned by TeamCompassViewModel (viewModelScope).
     * The owner cancels this work through [stop] from TeamCompassViewModel.onCleared().
     */
    fun startHealthMonitor(
        scope: CoroutineScope,
        onAvailabilitySample: suspend (available: Boolean, previous: Boolean?, nowMs: Long) -> Unit,
    ) {
        backendHealthJob?.cancel()
        backendHealthJob = scope.launch {
            backendHealthMonitor.collect { available, previous ->
                onAvailabilitySample(available, previous, nowMsProvider())
            }
        }
    }

    /**
     * Schedules stale refresh in a scope owned by TeamCompassViewModel (viewModelScope).
     * Any previous stale refresh job is cancelled before scheduling a new one.
     */
    fun scheduleStaleRefresh(
        scope: CoroutineScope,
        nowMs: Long = nowMsProvider(),
        readLastSnapshotAtMs: () -> Long,
        onRefresh: (nowMs: Long) -> Unit,
    ) {
        backendStaleMonitorJob?.cancel()
        val lastSnapshotAtMs = readLastSnapshotAtMs()
        if (lastSnapshotAtMs <= 0L) {
            onRefresh(nowMs)
            return
        }
        val delayMs = (lastSnapshotAtMs + staleWarningMs - nowMs).coerceAtLeast(0L) + 1L
        backendStaleMonitorJob = scope.launch {
            delayFn(delayMs)
            onRefresh(nowMsProvider())
        }
    }

    fun computeBackendStale(lastSnapshotAtMs: Long, nowMs: Long = nowMsProvider()): Boolean {
        if (lastSnapshotAtMs <= 0L) return false
        return nowMs - lastSnapshotAtMs > staleWarningMs
    }

    fun stop() {
        backendHealthJob?.cancel()
        backendHealthJob = null
        backendStaleMonitorJob?.cancel()
        backendStaleMonitorJob = null
    }
}
