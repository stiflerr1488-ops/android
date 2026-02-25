package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset

data class PointDialogState(
    val id: String?,
    val isTeam: Boolean,
    val createdBy: String?,
    val lat: Double,
    val lon: Double,
    val initialLabel: String,
    val initialIconRaw: String,
)

data class PointActionState(
    val marker: PointMarkerUi,
    val pressLat: Double,
    val pressLon: Double,
)

data class PointMarkerUi(
    val id: String,
    val isTeam: Boolean,
    val createdBy: String?,
    val createdAtMs: Long,
    val lat: Double,
    val lon: Double,
    val label: String,
    val iconRaw: String,
    val posPx: Offset,
    val inRange: Boolean,
)

data class WaypointRouteUi(
    val isTeam: Boolean,
    val points: List<Offset>,
)

data class LocalEnemyPingUi(
    val id: String,
    val lat: Double,
    val lon: Double,
    val type: QuickCommandType,
    val createdAtMs: Long,
    val expiresAtMs: Long,
)
