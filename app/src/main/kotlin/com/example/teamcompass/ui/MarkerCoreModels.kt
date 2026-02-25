package com.example.teamcompass.ui

enum class MarkerKind {
    POINT,
    ENEMY_PING,
}

enum class MarkerScope {
    TEAM,
    PRIVATE,
    TEAM_EVENT,
}

enum class MarkerState {
    ACTIVE,
    EXPIRED,
    DISABLED,
}

enum class MarkerSource {
    TEAM_POINT,
    PRIVATE_POINT,
    ENEMY_PING,
}

data class UnifiedMarker(
    val id: String,
    val kind: MarkerKind,
    val scope: MarkerScope,
    val state: MarkerState,
    val ownerUid: String?,
    val lat: Double,
    val lon: Double,
    val label: String,
    val iconRaw: String,
    val colorArgb: Long? = null,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val expiresAtMs: Long = 0L,
    val source: MarkerSource,
)
