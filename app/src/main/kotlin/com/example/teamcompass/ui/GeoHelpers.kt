package com.example.teamcompass.ui

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Returns a destination point from [latDeg],[lonDeg] following [bearingDeg] for [distanceMeters].
 * Uses a simple spherical Earth model (good enough for <=1km tactical range).
 */
fun destinationPoint(
    latDeg: Double,
    lonDeg: Double,
    bearingDeg: Double,
    distanceMeters: Double,
): Pair<Double, Double> {
    val R = 6_371_000.0
    val br = Math.toRadians(bearingDeg)
    val lat1 = Math.toRadians(latDeg)
    val lon1 = Math.toRadians(lonDeg)
    val dr = distanceMeters / R

    val sinLat1 = sin(lat1)
    val cosLat1 = cos(lat1)
    val sinDr = sin(dr)
    val cosDr = cos(dr)

    val lat2 = asin(sinLat1 * cosDr + cosLat1 * sinDr * cos(br))
    val lon2 = lon1 + atan2(
        sin(br) * sinDr * cosLat1,
        cosDr - sinLat1 * sin(lat2)
    )

    return Pair(Math.toDegrees(lat2), Math.toDegrees(lon2))
}
