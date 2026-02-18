package com.example.teamcompass.ui

import kotlin.math.cos

private const val EARTH_R = 6_371_000.0

/**
 * Very small-area projection (<= a few km): converts (lat,lon) to local meters (east,north)
 * relative to origin.
 */
fun toLocalMetersEastNorth(
    lat: Double,
    lon: Double,
    originLat: Double,
    originLon: Double,
): Pair<Double, Double> {
    val dLat = Math.toRadians(lat - originLat)
    val dLon = Math.toRadians(lon - originLon)
    val north = dLat * EARTH_R
    val east = dLon * EARTH_R * cos(Math.toRadians(originLat))
    return Pair(east, north)
}

/** Returns (lat,lon) center of a LatLonBox. */
fun latLonBoxCenter(north: Double, south: Double, east: Double, west: Double): Pair<Double, Double> {
    return Pair((north + south) / 2.0, (east + west) / 2.0)
}

/** Approximates LatLonBox width/height in meters (east-west, north-south). */
fun latLonBoxSizeMeters(north: Double, south: Double, east: Double, west: Double): Pair<Double, Double> {
    val midLat = (north + south) / 2.0
    val height = Math.toRadians(north - south) * EARTH_R
    val width = Math.toRadians(east - west) * EARTH_R * cos(Math.toRadians(midLat))
    return Pair(kotlin.math.abs(width), kotlin.math.abs(height))
}
