package com.example.teamcompass.core

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {
    private const val EarthRadiusMeters = 6_371_000.0

    fun distanceMeters(from: LocationPoint, to: LocationPoint): Double {
        validatePoint(from)
        validatePoint(to)

        val dLat = Math.toRadians(to.lat - from.lat)
        val dLon = Math.toRadians(to.lon - from.lon)
        val lat1 = Math.toRadians(from.lat)
        val lat2 = Math.toRadians(to.lat)

        val a = sin(dLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)

        val c = 2 * asin(min(1.0, sqrt(a)))
        return EarthRadiusMeters * c
    }

    fun bearingDegrees(from: LocationPoint, to: LocationPoint): Double {
        validatePoint(from)
        validatePoint(to)

        val lat1 = Math.toRadians(from.lat)
        val lat2 = Math.toRadians(to.lat)
        val dLon = Math.toRadians(to.lon - from.lon)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        val brng = Math.toDegrees(atan2(y, x))
        return normalizeDegrees0to360(brng)
    }

    fun normalizeRelativeDegrees(angle: Double): Double {
        val shifted = ((angle + 180.0) % 360.0 + 360.0) % 360.0
        return shifted - 180.0
    }

    fun normalizeDegrees0to360(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    private fun validatePoint(point: LocationPoint) {
        require(point.lat.isFinite() && point.lon.isFinite()) {
            "Latitude/longitude must be finite numbers"
        }
        require(point.lat in -90.0..90.0) { "Latitude must be within [-90, 90]" }
        require(point.lon in -180.0..180.0) { "Longitude must be within [-180, 180]" }
    }
}
