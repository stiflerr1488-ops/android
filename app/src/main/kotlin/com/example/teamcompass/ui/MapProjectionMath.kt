package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

data class MapViewportState(
    val scalePxPerMeter: Float,
    val offsetPx: Offset,
)

fun worldToScreen(
    eastMeters: Double,
    northMeters: Double,
    viewport: MapViewportState,
    canvasSize: IntSize,
): Offset {
    val center = Offset(
        x = canvasSize.width / 2f + viewport.offsetPx.x,
        y = canvasSize.height / 2f + viewport.offsetPx.y,
    )
    return Offset(
        x = center.x + eastMeters.toFloat() * viewport.scalePxPerMeter,
        y = center.y - northMeters.toFloat() * viewport.scalePxPerMeter,
    )
}

fun screenToWorld(
    screen: Offset,
    viewport: MapViewportState,
    canvasSize: IntSize,
): Pair<Double, Double> {
    val centerX = canvasSize.width / 2f + viewport.offsetPx.x
    val centerY = canvasSize.height / 2f + viewport.offsetPx.y
    val east = (screen.x - centerX) / viewport.scalePxPerMeter
    val north = -(screen.y - centerY) / viewport.scalePxPerMeter
    return Pair(east.toDouble(), north.toDouble())
}

fun fitViewportToPoints(
    points: List<Pair<Double, Double>>, // lat, lon
    originLat: Double,
    originLon: Double,
    canvasSize: IntSize,
    minScalePxPerMeter: Float,
    maxScalePxPerMeter: Float,
): MapViewportState {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) {
        return MapViewportState(scalePxPerMeter = 1f, offsetPx = Offset.Zero)
    }
    if (points.isEmpty()) {
        return MapViewportState(scalePxPerMeter = 0.5f, offsetPx = Offset.Zero)
    }

    val locals = points.map { (lat, lon) ->
        toLocalMetersEastNorth(
            lat = lat,
            lon = lon,
            originLat = originLat,
            originLon = originLon,
        )
    }

    val minEast = locals.minOf { it.first }
    val maxEast = locals.maxOf { it.first }
    val minNorth = locals.minOf { it.second }
    val maxNorth = locals.maxOf { it.second }

    val spanEast = max(1.0, maxEast - minEast)
    val spanNorth = max(1.0, maxNorth - minNorth)
    val fitScaleX = (canvasSize.width * 0.85) / spanEast
    val fitScaleY = (canvasSize.height * 0.85) / spanNorth
    val scale = min(fitScaleX, fitScaleY).toFloat().coerceIn(minScalePxPerMeter, maxScalePxPerMeter)

    val centerEast = (minEast + maxEast) / 2.0
    val centerNorth = (minNorth + maxNorth) / 2.0
    val offset = Offset(
        x = (-centerEast * scale).toFloat(),
        y = (centerNorth * scale).toFloat(),
    )
    return MapViewportState(scalePxPerMeter = scale, offsetPx = offset)
}
