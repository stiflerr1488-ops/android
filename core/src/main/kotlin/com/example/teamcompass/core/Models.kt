package com.example.teamcompass.core

data class LocationPoint(
    val lat: Double,
    val lon: Double,
    val accMeters: Double,
    val speedMps: Double,
    val headingDeg: Double?,
    val timestampMs: Long,
)

data class PlayerState(
    val uid: String,
    val nick: String,
    val point: LocationPoint,
)

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
)
