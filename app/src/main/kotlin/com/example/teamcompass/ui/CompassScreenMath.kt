package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.teamcompass.core.GeoMath
import com.example.teamcompass.core.LocationPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

internal data class CompassStickyZoomResult(
    val rangeMeters: Float,
    val stickyStop: Float?,
)

internal fun applyCompassStickyZoom(
    raw: Float,
    zoomStops: List<Float>,
    currentStickyStop: Float?,
): CompassStickyZoomResult {
    val clamped = raw.coerceIn(zoomStops.first(), zoomStops.last())
    val held = currentStickyStop
    if (held != null) {
        val releaseBand = max(8f, held * 0.12f)
        if (abs(clamped - held) <= releaseBand) {
            return CompassStickyZoomResult(rangeMeters = held, stickyStop = held)
        }
    }
    val nearest = zoomStops.minByOrNull { abs(clamped - it) } ?: clamped
    val captureBand = max(6f, nearest * 0.07f)
    return if (abs(clamped - nearest) <= captureBand) {
        CompassStickyZoomResult(rangeMeters = nearest, stickyStop = nearest)
    } else {
        CompassStickyZoomResult(rangeMeters = clamped, stickyStop = null)
    }
}

internal fun buildCompassRadarPointMarkers(
    teamPoints: List<MapPoint>,
    privatePoints: List<MapPoint>,
    me: LocationPoint?,
    headingDeg: Double?,
    rangeMeters: Float,
    radarSize: IntSize,
    defaultPointLabel: String,
): List<PointMarkerUi> {
    val mePoint = me ?: return emptyList()
    val heading = headingDeg ?: 0.0
    val w = radarSize.width.toFloat()
    val h = radarSize.height.toFloat()
    if (w <= 0f || h <= 0f) return emptyList()

    val r = min(w, h) * 0.49f
    val cx = w / 2f
    val cy = h / 2f

    fun toScreen(lat: Double, lon: Double): Pair<Offset, Boolean> {
        val lp = LocationPoint(lat, lon, 0.0, 0.0, null, 0L)
        val dist = GeoMath.distanceMeters(mePoint, lp).toFloat()
        val bearing = GeoMath.bearingDegrees(mePoint, lp)
        val rel = GeoMath.normalizeRelativeDegrees(bearing - heading).toFloat()
        val distN = (dist / rangeMeters).coerceIn(0f, 1f)
        val rad = (rel * PI.toFloat() / 180f)
        val dx = sin(rad)
        val dy = -cos(rad)
        return Pair(Offset(cx + dx * (r * distN), cy + dy * (r * distN)), dist <= rangeMeters)
    }

    val out = mutableListOf<PointMarkerUi>()
    teamPoints.forEach { mp ->
        val (pos, inRange) = toScreen(mp.lat, mp.lon)
        out += PointMarkerUi(
            id = mp.id,
            isTeam = true,
            createdBy = mp.createdBy,
            createdAtMs = mp.createdAtMs,
            lat = mp.lat,
            lon = mp.lon,
            label = mp.label.ifBlank { defaultPointLabel },
            iconRaw = mp.icon,
            posPx = pos,
            inRange = inRange,
        )
    }
    privatePoints.forEach { mp ->
        val (pos, inRange) = toScreen(mp.lat, mp.lon)
        out += PointMarkerUi(
            id = mp.id,
            isTeam = false,
            createdBy = mp.createdBy,
            createdAtMs = mp.createdAtMs,
            lat = mp.lat,
            lon = mp.lon,
            label = mp.label.ifBlank { defaultPointLabel },
            iconRaw = mp.icon,
            posPx = pos,
            inRange = inRange,
        )
    }
    return out
}

internal fun radarScreenToLatLon(
    offset: Offset,
    me: LocationPoint?,
    headingDeg: Double?,
    radarSize: IntSize,
    rangeMeters: Float,
): Pair<Double, Double>? {
    val mePoint = me ?: return null
    val heading = headingDeg ?: 0.0
    val w = radarSize.width.toFloat()
    val h = radarSize.height.toFloat()
    if (w <= 0f || h <= 0f) return null

    val r = min(w, h) * 0.49f
    val cx = w / 2f
    val cy = h / 2f
    val vx = offset.x - cx
    val vy = offset.y - cy
    val len = sqrt(vx * vx + vy * vy)
    if (len > r) return null

    val relRad = kotlin.math.atan2(vx.toDouble(), (-vy).toDouble())
    val relDeg = Math.toDegrees(relRad)
    val dist = (len / r) * rangeMeters
    val absBearing = GeoMath.normalizeDegrees0to360(relDeg + heading)
    val dest = destinationPoint(mePoint.lat, mePoint.lon, absBearing, dist.toDouble())
    return Pair(dest.first, dest.second)
}
