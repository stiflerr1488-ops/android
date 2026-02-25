package com.example.teamcompass.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import java.util.Locale

internal fun animatableFor(
    store: MutableMap<String, Animatable<Float, AnimationVector1D>>,
    key: String,
    init: Float,
): Animatable<Float, AnimationVector1D> = store.getOrPut(key) { Animatable(init) }

internal fun unwrapAngle(prev: Float, target: Float): Float {
    var t = target
    while (t - prev > 180f) t -= 360f
    while (t - prev < -180f) t += 360f
    return t
}

internal fun alphaForAgeSec(ageSec: Long): Float {
    return when {
        ageSec <= 20 -> 1.0f
        ageSec <= 60 -> {
            val k = (ageSec - 20).toFloat() / 40f
            1.0f + (0.65f - 1.0f) * k
        }
        ageSec <= 120 -> {
            val k = (ageSec - 60).toFloat() / 60f
            0.65f + (0.15f - 0.65f) * k
        }
        else -> 0.0f
    }
}

internal fun formatRangeLabel(meters: Int): String {
    return if (meters >= 1000) {
        val km = meters / 1000.0
        if (km == km.roundToInt().toDouble()) {
            "${km.roundToInt()}РєРј"
        } else {
            String.format(Locale.US, "%.1fРєРј", km)
        }
    } else {
        "${meters}Рј"
    }
}

internal fun roundRadarRingMeters(rawMeters: Float): Int {
    val step = when {
        rawMeters <= 120f -> 5f
        rawMeters <= 300f -> 10f
        else -> 25f
    }
    return ((rawMeters / step).roundToInt() * step).toInt().coerceAtLeast(1)
}

internal fun geoToRadarScreen(
    lat: Double,
    lon: Double,
    myLat: Double,
    myLon: Double,
    headingDeg: Double,
    center: Offset,
    pixelsPerMeter: Double,
): Offset {
    val (east, north) = toLocalMetersEastNorth(lat, lon, myLat, myLon)
    val dist = kotlin.math.sqrt(east * east + north * north)
    if (dist <= 0.01) return center
    val bearing = Math.toDegrees(kotlin.math.atan2(east, north))
    val relDeg = bearing - headingDeg
    val rad = Math.toRadians(relDeg)
    val dx = kotlin.math.sin(rad) * dist * pixelsPerMeter
    val dy = -kotlin.math.cos(rad) * dist * pixelsPerMeter
    return Offset((center.x + dx).toFloat(), (center.y + dy).toFloat())
}

internal fun relToScreenDeg(absDeg: Float, headingDeg: Float): Float = absDeg - headingDeg

internal fun polarToPos(
    center: Offset,
    radarRadiusPx: Float,
    rangeMeters: Float,
    relDeg: Float,
    distMeters: Float,
): Offset {
    val distN = (distMeters / rangeMeters).coerceIn(0f, 1f)
    val rad = (relDeg * PI / 180.0).toFloat()
    val dx = sin(rad)
    val dy = -cos(rad)
    return Offset(center.x + dx * (radarRadiusPx * distN), center.y + dy * (radarRadiusPx * distN))
}

internal fun normalizeEnemyPingType(raw: String): String {
    return when (raw.trim().uppercase(Locale.US)) {
        "ENEMY", "ENEMY_CONTACT", "CONTACT" -> "ENEMY"
        "ATTACK" -> "ATTACK"
        "DEFENSE", "DEFEND", "RETREAT", "RALLY" -> "DEFENSE"
        else -> "DANGER"
    }
}
