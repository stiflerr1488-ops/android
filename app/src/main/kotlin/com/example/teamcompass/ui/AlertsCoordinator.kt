package com.example.teamcompass.ui

import com.example.teamcompass.core.GeoMath
import com.example.teamcompass.core.LocationPoint

internal class AlertsCoordinator(
    private val processedTtlMs: Long = 2 * 60_000L,
    private val closePingDistanceMeters: Double = 30.0,
) {
    private val processedEnemyPingIds = LinkedHashMap<String, Long>()

    fun consumeNewCloseEnemyPings(
        enemyPings: List<EnemyPing>,
        me: LocationPoint?,
        nowMs: Long,
    ): Int {
        if (me == null) return 0

        val processedIterator = processedEnemyPingIds.entries.iterator()
        while (processedIterator.hasNext()) {
            val entry = processedIterator.next()
            if (nowMs - entry.value > processedTtlMs) {
                processedIterator.remove()
            }
        }

        var closeAlerts = 0
        enemyPings.forEach { ping ->
            val expiresAt = ping.expiresAtMs.takeIf { it > 0L } ?: (ping.createdAtMs + 120_000L)
            if (ping.createdAtMs <= 0L || nowMs >= expiresAt) return@forEach
            if (processedEnemyPingIds.containsKey(ping.id)) return@forEach

            val distance = GeoMath.distanceMeters(
                me,
                LocationPoint(
                    lat = ping.lat,
                    lon = ping.lon,
                    accMeters = 0.0,
                    speedMps = 0.0,
                    headingDeg = null,
                    timestampMs = ping.createdAtMs,
                )
            )
            if (distance <= closePingDistanceMeters) {
                closeAlerts += 1
            }
            processedEnemyPingIds[ping.id] = nowMs
        }
        return closeAlerts
    }
}
