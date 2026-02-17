package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompassCalculatorTest {
    private val calculator = CompassCalculator()

    @Test
    fun `hidden target still preserves computed relative bearing`() {
        val me = LocationPoint(
            lat = 55.0,
            lon = 37.0,
            accMeters = 5.0,
            speedMps = 0.0,
            headingDeg = null,
            timestampMs = 10_000,
        )
        val otherPoint = LocationPoint(
            lat = 55.0005,
            lon = 37.0005,
            accMeters = 5.0,
            speedMps = 0.0,
            headingDeg = null,
            timestampMs = 0,
        )
        val other = PlayerState(uid = "u1", nick = "Scout", point = otherPoint)

        val actual = calculator.buildTargets(
            me = me,
            myHeadingDeg = 30.0,
            others = listOf(other),
            nowMs = 200_000,
        ).single()

        val expectedRelative = GeoMath.normalizeRelativeDegrees(
            GeoMath.bearingDegrees(me, otherPoint) - 30.0,
        )

        assertEquals(Staleness.HIDDEN, actual.staleness)
        assertEquals(expectedRelative, actual.relativeBearingDeg)
    }

    @Test
    fun `future timestamps are clamped to zero staleness age`() {
        val me = LocationPoint(55.0, 37.0, 5.0, 0.0, null, 10_000)
        val other = PlayerState(
            uid = "u1",
            nick = "Scout",
            point = LocationPoint(55.0001, 37.0001, 5.0, 0.0, null, 12_000),
        )

        val actual = calculator.buildTargets(
            me = me,
            myHeadingDeg = 0.0,
            others = listOf(other),
            nowMs = 11_000,
        ).single()

        assertEquals(0, actual.lastSeenSec)
        assertEquals(Staleness.FRESH, actual.staleness)
        assertTrue(actual.distanceMeters > 0)
    }
}
