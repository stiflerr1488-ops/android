package com.example.teamcompass.ui

import com.example.teamcompass.core.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertsCoordinatorTest {

    @Test
    fun consumeNewCloseEnemyPings_dedupes_within_suppression_window() {
        val coordinator = AlertsCoordinator(processedTtlMs = 10_000L, closePingDistanceMeters = 30.0)
        val now = 1_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, null, now)
        val pings = listOf(
            EnemyPing(
                id = "e1",
                lat = 55.00005,
                lon = 37.00005,
                createdAtMs = now - 1000,
                createdBy = "u-1",
                expiresAtMs = now + 120_000L,
            )
        )

        assertEquals(1, coordinator.consumeNewCloseEnemyPings(pings, me, now))
        assertEquals(0, coordinator.consumeNewCloseEnemyPings(pings, me, now + 2_000L))
    }

    @Test
    fun consumeNewCloseEnemyPings_allows_repeat_after_ttl_window() {
        val coordinator = AlertsCoordinator(processedTtlMs = 5_000L, closePingDistanceMeters = 30.0)
        val now = 2_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, null, now)
        val pings = listOf(
            EnemyPing(
                id = "e1",
                lat = 55.00005,
                lon = 37.00005,
                createdAtMs = now - 1000,
                createdBy = "u-1",
                expiresAtMs = now + 120_000L,
            )
        )

        assertEquals(1, coordinator.consumeNewCloseEnemyPings(pings, me, now))
        assertEquals(1, coordinator.consumeNewCloseEnemyPings(pings, me, now + 6_000L))
    }

    @Test
    fun consumeNewCloseEnemyPings_returns_zero_without_my_location() {
        val coordinator = AlertsCoordinator()
        val now = 3_000_000L
        val pings = listOf(
            EnemyPing(
                id = "e1",
                lat = 55.0,
                lon = 37.0,
                createdAtMs = now - 1000,
                createdBy = "u-1",
                expiresAtMs = now + 120_000L,
            )
        )

        assertEquals(0, coordinator.consumeNewCloseEnemyPings(pings, me = null, nowMs = now))
    }
}
