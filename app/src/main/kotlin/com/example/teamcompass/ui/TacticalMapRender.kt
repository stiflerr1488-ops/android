package com.example.teamcompass.ui

import android.graphics.Bitmap

/** Prepared map data for rendering on the radar. */
data class TacticalMapRender(
    val overlay: GroundOverlay,
    val bitmap: Bitmap,
    val opacity: Float,
    val points: List<KmlPoint> = emptyList(),
    val lines: List<KmlLine> = emptyList(),
    val polygons: List<KmlPolygon> = emptyList(),
)
