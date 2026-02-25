package com.example.teamcompass.ui

import com.example.teamcompass.core.GeoMath
import com.example.teamcompass.core.LocationPoint

internal fun pruneLocalEnemyPingPreviews(
    state: UiState,
    localEnemyPings: List<LocalEnemyPingUi>,
    now: Long,
): List<LocalEnemyPingUi> {
    return localEnemyPings.filter { local ->
        if (now >= local.expiresAtMs) return@filter false
        val localPoint = LocationPoint(local.lat, local.lon, 0.0, 0.0, null, local.createdAtMs)
        val acknowledged = state.enemyPings.any { remote ->
            remote.createdBy == state.uid &&
                remote.type == local.type &&
                kotlin.math.abs(remote.createdAtMs - local.createdAtMs) <= 20_000L &&
                GeoMath.distanceMeters(
                    localPoint,
                    LocationPoint(remote.lat, remote.lon, 0.0, 0.0, null, remote.createdAtMs),
                ) <= 18.0
        }
        !acknowledged
    }
}

internal fun buildCompassEnemyOverlays(
    remoteEnemyPings: List<EnemyPing>,
    localEnemyPings: List<LocalEnemyPingUi>,
    me: LocationPoint?,
    headingDeg: Double?,
    now: Long,
): List<RadarOverlay> {
    val overlays = mutableListOf<RadarOverlay>()
    val self = me ?: return overlays
    val heading = headingDeg ?: 0.0

    remoteEnemyPings.forEach { ping ->
        val expiresAt = if (ping.expiresAtMs > 0L) ping.expiresAtMs else ping.createdAtMs + 120_000L
        if (now >= expiresAt) return@forEach
        val point = LocationPoint(ping.lat, ping.lon, 0.0, 0.0, null, now)
        val dist = GeoMath.distanceMeters(self, point)
        val bearing = GeoMath.bearingDegrees(self, point)
        val rel = GeoMath.normalizeRelativeDegrees(bearing - heading)
        overlays.add(
            RadarOverlay(
                id = ping.id,
                label = if (ping.isBluetooth) "BT" else "",
                icon = if (ping.isBluetooth) "BT" else ping.type.name,
                relativeBearingDeg = rel,
                distanceMeters = dist,
                kind = if (ping.isBluetooth) RadarOverlayKind.BLUETOOTH_DEVICE else RadarOverlayKind.ENEMY_PING,
            )
        )
    }

    localEnemyPings.forEach { ping ->
        if (now >= ping.expiresAtMs) return@forEach
        val point = LocationPoint(ping.lat, ping.lon, 0.0, 0.0, null, now)
        val dist = GeoMath.distanceMeters(self, point)
        val bearing = GeoMath.bearingDegrees(self, point)
        val rel = GeoMath.normalizeRelativeDegrees(bearing - heading)
        overlays.add(
            RadarOverlay(
                id = ping.id,
                label = "",
                icon = ping.type.name,
                relativeBearingDeg = rel,
                distanceMeters = dist,
                kind = RadarOverlayKind.ENEMY_PING,
            )
        )
    }

    return overlays
}
