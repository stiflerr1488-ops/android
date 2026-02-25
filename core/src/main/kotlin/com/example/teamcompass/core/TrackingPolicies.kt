package com.example.teamcompass.core

object TrackingPolicies {
    // GAME: активный игрок (бегает, стреляет)
    // 7 секунд — компромисс между точностью (~10-30м задержка) и батареей
    val game = TrackingPolicy(minIntervalMs = 3_000, minDistanceMeters = 10.0)

    // SILENT: «мёртвый» игрок (идёт в мертвяк / ждёт)
    // 25 секунд — достаточно, погрешность ~20-40м при ходьбе
    val silent = TrackingPolicy(minIntervalMs = 10_000, minDistanceMeters = 30.0)

    fun forMode(mode: TrackingMode): TrackingPolicy =
        when (mode) {
            TrackingMode.GAME -> game
            TrackingMode.SILENT -> silent
        }
    
    /**
     * Адаптивная политика на основе режима движения.
     * Базовая политика умножается на коэффициент из MovementState.
     */
    fun adaptiveForMovement(
        basePolicy: TrackingPolicy,
        movementState: MovementState
    ): TrackingPolicy {
        val intervalFactor = when (movementState) {
            MovementState.STATIONARY -> 4.0    // 4x реже когда стоим
            MovementState.WALKING_SLOW -> 1.5  // 1.5x базового
            MovementState.WALKING_FAST -> 0.7  // 0.7x базового (чаще)
            MovementState.VEHICLE -> 0.3       // 0.3x базового (максимум часто)
        }
        
        val distanceFactor = when (movementState) {
            MovementState.STATIONARY -> 5.0    // 5x больше дистанция
            MovementState.WALKING_SLOW -> 2.0
            MovementState.WALKING_FAST -> 1.0
            MovementState.VEHICLE -> 0.5       // Меньше дистанция для транспорта
        }
        
        return TrackingPolicy(
            minIntervalMs = (basePolicy.minIntervalMs * intervalFactor).toLong().coerceAtLeast(1_000L),
            minDistanceMeters = basePolicy.minDistanceMeters * distanceFactor
        )
    }
}

object RetryBackoff {
    fun delayForAttemptSec(attempt: Int): Long {
        if (attempt <= 0) return 1
        val candidate = 1L shl (attempt - 1).coerceAtMost(20)
        return candidate.coerceAtMost(60)
    }
}
