package com.example.teamcompass.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MapProjectionMathTest {

    @Test
    fun worldToScreen_and_screenToWorld_round_trip() {
        val viewport = MapViewportState(scalePxPerMeter = 2.5f, offsetPx = Offset(40f, -30f))
        val canvas = IntSize(1080, 720)
        val east = 123.45
        val north = -67.89

        val screen = worldToScreen(
            eastMeters = east,
            northMeters = north,
            viewport = viewport,
            canvasSize = canvas,
        )
        val world = screenToWorld(
            screen = screen,
            viewport = viewport,
            canvasSize = canvas,
        )

        assertTrue(abs(world.first - east) < 1e-5)
        assertTrue(abs(world.second - north) < 1e-5)
    }

    @Test
    fun fitViewportToPoints_clamps_scale_to_bounds() {
        val originLat = 55.0
        val originLon = 37.0
        val canvas = IntSize(1000, 800)

        val veryClose = listOf(
            Pair(55.0000001, 37.0000001),
            Pair(55.0000002, 37.0000002),
        )
        val veryFar = listOf(
            Pair(56.0, 36.0),
            Pair(54.0, 38.0),
        )

        val closeViewport = fitViewportToPoints(
            points = veryClose,
            originLat = originLat,
            originLon = originLon,
            canvasSize = canvas,
            minScalePxPerMeter = 0.01f,
            maxScalePxPerMeter = 120f,
        )
        val farViewport = fitViewportToPoints(
            points = veryFar,
            originLat = originLat,
            originLon = originLon,
            canvasSize = canvas,
            minScalePxPerMeter = 0.01f,
            maxScalePxPerMeter = 120f,
        )

        assertTrue(closeViewport.scalePxPerMeter in 0.01f..120f)
        assertTrue(farViewport.scalePxPerMeter in 0.01f..120f)
    }

    @Test
    fun fitViewportToPoints_centers_bbox() {
        val points = listOf(
            Pair(55.0, 37.0),
            Pair(55.0002, 37.0002),
        )
        val viewport = fitViewportToPoints(
            points = points,
            originLat = 55.0001,
            originLon = 37.0001,
            canvasSize = IntSize(1200, 800),
            minScalePxPerMeter = 0.01f,
            maxScalePxPerMeter = 120f,
        )

        val centerScreen = worldToScreen(
            eastMeters = 0.0,
            northMeters = 0.0,
            viewport = viewport,
            canvasSize = IntSize(1200, 800),
        )

        assertTrue(abs(centerScreen.x - 600f) < 4f)
        assertTrue(abs(centerScreen.y - 400f) < 4f)
    }
}
