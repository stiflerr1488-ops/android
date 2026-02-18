package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoMathTest {
    @Test
    fun `distance is near zero for same point`() {
        val p = LocationPoint(55.0, 37.0, 5.0, 0.0, null, 0)
        assertTrue(GeoMath.distanceMeters(p, p) < 0.01)
    }

    @Test
    fun `bearing to north is near zero`() {
        val from = LocationPoint(55.0, 37.0, 5.0, 0.0, null, 0)
        val to = LocationPoint(55.001, 37.0, 5.0, 0.0, null, 0)
        val bearing = GeoMath.bearingDegrees(from, to)
        assertTrue(bearing < 2.0 || bearing > 358.0)
    }

    @Test
    fun `relative angle normalized to minus 180 plus 180`() {
        assertEquals(-170.0, GeoMath.normalizeRelativeDegrees(190.0))
        assertEquals(170.0, GeoMath.normalizeRelativeDegrees(-190.0))
    }
}
