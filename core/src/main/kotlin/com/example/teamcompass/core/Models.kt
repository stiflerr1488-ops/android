package com.example.teamcompass.core

data class LocationPoint(
    val lat: Double,
    val lon: Double,
    val accMeters: Double,
    val speedMps: Double,
    val headingDeg: Double?,
    val timestampMs: Long,
)

/**
 * Роль участника в команде.
 * Определяет права доступа к управлению матчем и участниками.
 */
enum class Role(val priority: Int) {
    COMMANDER(3),  // Командир — полные права
    DEPUTY(2),     // Заместитель командира — ограниченные права
    FIGHTER(1),    // Боец — базовые права
}

/**
 * Проверка, может ли роль выполнять действия командира.
 */
fun Role.canCommand(): Boolean = this == Role.COMMANDER

/**
 * Проверка, может ли роль выполнять действия заместителя (включая командира).
 */
fun Role.canDeputy(): Boolean = this == Role.COMMANDER || this == Role.DEPUTY

data class PlayerState(
    val uid: String,
    val nick: String,
    val point: LocationPoint,
    val mode: PlayerMode = PlayerMode.GAME,
    val role: Role = Role.FIGHTER,
    val anchored: Boolean = false,
    /** Epoch millis. 0 means SOS is inactive. */
    val sosUntilMs: Long = 0L,
)

enum class PlayerMode {
    GAME,
    DEAD,
}

enum class Staleness {
    FRESH,
    SUSPECT,
    STALE,
    HIDDEN,
}

enum class TrackingMode {
    GAME,
    SILENT,
}

data class TrackingPolicy(
    val minIntervalMs: Long,
    val minDistanceMeters: Double,
)

data class CompassTarget(
    val uid: String,
    val nick: String,
    val distanceMeters: Double,
    val relativeBearingDeg: Double,
    val staleness: Staleness,
    val lowAccuracy: Boolean,
    val lastSeenSec: Long,
    val mode: PlayerMode = PlayerMode.GAME,
    val anchored: Boolean = false,
    val sosActive: Boolean = false,
)
