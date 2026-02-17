package com.example.teamcompass.core

object TrackingPolicies {
    val game = TrackingPolicy(minIntervalMs = 3_000, minDistanceMeters = 10.0)
    val silent = TrackingPolicy(minIntervalMs = 10_000, minDistanceMeters = 30.0)

    fun forMode(mode: TrackingMode): TrackingPolicy =
        when (mode) {
            TrackingMode.GAME -> game
            TrackingMode.SILENT -> silent
        }
}

object RetryBackoff {
    fun delayForAttemptSec(attempt: Int): Long {
        if (attempt <= 0) return 1
        val candidate = 1L shl (attempt - 1).coerceAtMost(20)
        return candidate.coerceAtMost(60)
    }
}
