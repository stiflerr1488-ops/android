package com.example.teamcompass.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompassCalculatorTest {
    private val calculator = CompassCalculator()

    @Test
    fun hiddenTarget_hasZeroRelativeBearing() {
        val now = 1_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, 0.0, now)
        val hiddenPlayer = PlayerState(
            uid = "p1",
            nick = "Hidden",
            point = LocationPoint(55.0001, 37.0001, 5.0, 0.0, 0.0, now - 121_000L),
        )

        val targets = calculator.buildTargets(me, myHeadingDeg = 180.0, others = listOf(hiddenPlayer), nowMs = now)

        assertEquals(1, targets.size)
        assertEquals(Staleness.HIDDEN, targets.single().staleness)
        assertEquals(0.0, targets.single().relativeBearingDeg)
    }

    @Test
    fun relativeBearing_handles359To0Boundary() {
        val now = 2_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, 0.0, now)
        val northPlayer = PlayerState(
            uid = "p2",
            nick = "North",
            point = LocationPoint(55.0009, 37.0, 5.0, 0.0, 0.0, now - 2_000L),
        )

        val target = calculator.buildTargets(me, myHeadingDeg = 359.0, others = listOf(northPlayer), nowMs = now).single()

        assertTrue(abs(target.relativeBearingDeg - 1.0) < 5.0, "relative=${target.relativeBearingDeg}")
    }

    @Test
    fun stalenessBoundary_120secStillStale() {
        val now = 3_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, 0.0, now)
        val player = PlayerState(
            uid = "p3",
            nick = "Stale",
            point = LocationPoint(55.0002, 37.0, 5.0, 0.0, 0.0, now - 120_000L),
        )

        val target = calculator.buildTargets(me, myHeadingDeg = 0.0, others = listOf(player), nowMs = now).single()

        assertEquals(Staleness.STALE, target.staleness)
    }

    @Test
    fun sosUntilInFuture_setsSosActive() {
        val now = 4_000_000L
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, 0.0, now)
        val player = PlayerState(
            uid = "p4",
            nick = "SOS",
            point = LocationPoint(55.0003, 37.0002, 5.0, 0.0, 0.0, now - 5_000L),
            mode = PlayerMode.DEAD,
            sosUntilMs = now + 10_000L,
        )

        val target = calculator.buildTargets(me, myHeadingDeg = 45.0, others = listOf(player), nowMs = now).single()

        assertTrue(target.sosActive)
    }
}
