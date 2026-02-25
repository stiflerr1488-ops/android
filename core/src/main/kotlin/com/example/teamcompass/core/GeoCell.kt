package com.example.teamcompass.core

import kotlin.math.max

/**
 * Lightweight geohash helper for realtime spatial sharding.
 */
object GeoCell {
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLon: Double,
        val maxLon: Double,
    ) {
        val centerLat: Double get() = (minLat + maxLat) / 2.0
        val centerLon: Double get() = (minLon + maxLon) / 2.0
        val latSpan: Double get() = max(1e-9, maxLat - minLat)
        val lonSpan: Double get() = max(1e-9, maxLon - minLon)
    }

    fun encode(lat: Double, lon: Double, precision: Int = 6): String {
        val safePrecision = precision.coerceIn(1, 12)
        var minLat = -90.0
        var maxLat = 90.0
        var minLon = -180.0
        var maxLon = 180.0
        val normalizedLat = lat.coerceIn(-89.999999, 89.999999)
        val normalizedLon = normalizeLon(lon)

        val out = StringBuilder(safePrecision)
        var isLon = true
        var bit = 0
        var ch = 0
        while (out.length < safePrecision) {
            if (isLon) {
                val mid = (minLon + maxLon) / 2.0
                if (normalizedLon >= mid) {
                    ch = (ch shl 1) or 1
                    minLon = mid
                } else {
                    ch = ch shl 1
                    maxLon = mid
                }
            } else {
                val mid = (minLat + maxLat) / 2.0
                if (normalizedLat >= mid) {
                    ch = (ch shl 1) or 1
                    minLat = mid
                } else {
                    ch = ch shl 1
                    maxLat = mid
                }
            }
            isLon = !isLon
            bit += 1
            if (bit == 5) {
                out.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }
        return out.toString()
    }

    fun decodeBoundingBox(hash: String): BoundingBox? {
        val normalized = hash.trim().lowercase()
        if (normalized.isEmpty()) return null

        var minLat = -90.0
        var maxLat = 90.0
        var minLon = -180.0
        var maxLon = 180.0
        var isLon = true

        for (char in normalized) {
            val idx = BASE32.indexOf(char)
            if (idx < 0) return null
            for (mask in intArrayOf(16, 8, 4, 2, 1)) {
                val bit = (idx and mask) != 0
                if (isLon) {
                    val mid = (minLon + maxLon) / 2.0
                    if (bit) minLon = mid else maxLon = mid
                } else {
                    val mid = (minLat + maxLat) / 2.0
                    if (bit) minLat = mid else maxLat = mid
                }
                isLon = !isLon
            }
        }
        return BoundingBox(
            minLat = minLat,
            maxLat = maxLat,
            minLon = minLon,
            maxLon = maxLon,
        )
    }

    fun neighbors3x3(cellId: String): Set<String> {
        val normalized = cellId.trim().lowercase()
        val bbox = decodeBoundingBox(normalized) ?: return setOf(normalized)
        val latStep = bbox.latSpan
        val lonStep = bbox.lonSpan

        val out = linkedSetOf<String>()
        for (dy in -1..1) {
            for (dx in -1..1) {
                val lat = (bbox.centerLat + dy * latStep).coerceIn(-89.999999, 89.999999)
                val lon = normalizeLon(bbox.centerLon + dx * lonStep)
                out += encode(lat = lat, lon = lon, precision = normalized.length)
            }
        }
        return out
    }

    private fun normalizeLon(lon: Double): Double {
        var normalized = lon
        while (normalized < -180.0) normalized += 360.0
        while (normalized >= 180.0) normalized -= 360.0
        return normalized
    }
}

