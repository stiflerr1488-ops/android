package com.example.teamcompass.tracking

import com.example.teamcompass.core.PlayerMode
import kotlin.math.min

private const val WATCHDOG_STALE_THRESHOLD_MS = 45_000L
private const val WATCHDOG_RESTART_SUPPRESSION_MS = 90_000L

internal fun trackingIsWatchdogRestartAllowed(lastRestartAtMs: Long, nowMs: Long): Boolean {
    if (lastRestartAtMs <= 0L) return true
    return nowMs - lastRestartAtMs >= WATCHDOG_RESTART_SUPPRESSION_MS
}

internal fun trackingDecideWatchdogAction(
    lastLocationAtMs: Long,
    lastRestartAtMs: Long,
    nowMs: Long,
): TrackingRuntime.WatchdogAction {
    if (lastLocationAtMs <= 0L) return TrackingRuntime.WatchdogAction.NO_ACTION
    val staleMs = nowMs - lastLocationAtMs
    if (staleMs <= WATCHDOG_STALE_THRESHOLD_MS) return TrackingRuntime.WatchdogAction.NO_ACTION
    return if (trackingIsWatchdogRestartAllowed(lastRestartAtMs, nowMs)) {
        TrackingRuntime.WatchdogAction.RESTART
    } else {
        TrackingRuntime.WatchdogAction.THROTTLED
    }
}

internal fun trackingResolveSendIntervalMs(
    mode: PlayerMode,
    anchored: Boolean,
    adaptiveIntervalMs: Long,
): Long {
    val safeAdaptiveInterval = adaptiveIntervalMs.coerceAtLeast(1_000L)
    return when {
        mode == PlayerMode.DEAD -> 60_000L
        anchored -> min(safeAdaptiveInterval * 3L, 60_000L)
        else -> safeAdaptiveInterval
    }
}

internal fun trackingApplyIntervalJitter(
    intervalMs: Long,
    uid: String,
    nowMs: Long,
    jitterRatio: Double = 0.10,
): Long {
    val safeInterval = intervalMs.coerceAtLeast(1_000L)
    val safeRatio = jitterRatio.coerceIn(0.0, 0.40)
    if (safeRatio <= 0.0) return safeInterval
    val bucket = (nowMs / safeInterval).coerceAtLeast(1L)
    val seed = uid.hashCode().toLong() * 1_103_515_245L + bucket * 12_345L
    val mixed = seed xor (seed ushr 16)
    val normalized = (mixed and 0x7fffffff).toDouble() / 0x7fffffff
    val factor = (1.0 - safeRatio) + normalized * (safeRatio * 2.0)
    return (safeInterval * factor).toLong().coerceAtLeast(1_000L)
}

internal fun trackingResolveMinDistanceMeters(
    anchored: Boolean,
    adaptiveMinDistanceMeters: Double,
): Double {
    val safeDistance = adaptiveMinDistanceMeters.coerceAtLeast(1.0)
    return if (anchored) safeDistance * 3.0 else safeDistance
}

internal fun trackingShouldSendState(
    shouldForce: Boolean,
    lastSentMs: Long,
    nowMs: Long,
    intervalMs: Long,
    movedDistanceMeters: Double,
    minDistanceMeters: Double,
): Boolean {
    if (shouldForce) return true
    if (lastSentMs <= 0L) return true
    val safeIntervalMs = intervalMs.coerceAtLeast(1_000L)
    if (nowMs - lastSentMs >= safeIntervalMs) return true
    return movedDistanceMeters >= minDistanceMeters.coerceAtLeast(1.0)
}

internal fun trackingShouldResetLastSentAfterMove(
    moved: Boolean,
    wasAnchored: Boolean,
    mode: PlayerMode,
): Boolean = moved && wasAnchored && mode == PlayerMode.GAME
