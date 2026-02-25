package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoCellTest {

    @Test
    fun encode_and_decodeBoundingBox_are_consistent() {
        val cell = GeoCell.encode(lat = 59.81041, lon = 30.36703, precision = 6)
        val box = GeoCell.decodeBoundingBox(cell)
        requireNotNull(box)
        assertTrue(59.81041 in box.minLat..box.maxLat)
        assertTrue(30.36703 in box.minLon..box.maxLon)
    }

    @Test
    fun neighbors3x3_returns_nine_cells_including_center() {
        val center = GeoCell.encode(lat = 59.81041, lon = 30.36703, precision = 6)
        val cells = GeoCell.neighbors3x3(center)
        assertEquals(9, cells.size)
        assertTrue(cells.contains(center))
    }
}
