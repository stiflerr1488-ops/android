package com.example.teamcompass.ui

/**
 * Overlay marker rendered on top of the radar:
 * - Team points (visible to everyone)
 * - Private points (only on this device)
 * - Enemy pings (2 minutes "red halo")
 */
enum class RadarOverlayKind {
    TEAM_POINT,
    PRIVATE_POINT,
    ENEMY_PING,
    BLUETOOTH_DEVICE,
}

data class RadarOverlay(
    val id: String,
    val label: String,
    /** Emoji or short glyph to show inside the marker. */
    val icon: String,
    /** Relative bearing to the screen forward (0 = up). */
    val relativeBearingDeg: Double,
    val distanceMeters: Double,
    /** Optional tactical radius around the marker (meters). */
    val radiusMeters: Double = 0.0,
    val kind: RadarOverlayKind,
)
