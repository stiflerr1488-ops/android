package com.example.teamcompass.perf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamCompassPerfMetricsTest {

    @Test
    fun snapshot_aggregatesRtdbCounters() {
        TeamCompassPerfMetrics.resetForTest()

        TeamCompassPerfMetrics.recordRtdbSnapshotEmit()
        TeamCompassPerfMetrics.recordRtdbSnapshotEmit()
        TeamCompassPerfMetrics.recordRtdbCleanupSweep(deleteWrites = 0)
        TeamCompassPerfMetrics.recordRtdbCleanupSweep(deleteWrites = 3)

        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.rtdbSnapshotEmits)
        assertEquals(2L, snapshot.rtdbCleanupSweeps)
        assertEquals(3L, snapshot.rtdbCleanupWrites)
    }

    @Test
    fun snapshot_computesMapAveragesAndHitRate() {
        TeamCompassPerfMetrics.resetForTest()

        TeamCompassPerfMetrics.recordMapBitmapLoadRequest(cacheHit = false)
        TeamCompassPerfMetrics.recordMapBitmapDecode(durationMs = 20L, decodedBytes = 1000)
        TeamCompassPerfMetrics.recordMapBitmapLoadRequest(cacheHit = true)
        TeamCompassPerfMetrics.recordMapBitmapLoadRequest(cacheHit = false)
        TeamCompassPerfMetrics.recordMapBitmapDecode(durationMs = 40L, decodedBytes = 2000)

        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(3L, snapshot.mapBitmapLoadRequests)
        assertEquals(1L, snapshot.mapBitmapCacheHits)
        assertEquals(2L, snapshot.mapBitmapDecodes)
        assertEquals(60L, snapshot.mapBitmapDecodeTotalMs)
        assertEquals(3000L, snapshot.mapBitmapDecodedBytes)
        assertEquals(1.0 / 3.0, snapshot.mapBitmapCacheHitRate, 0.000001)
        assertEquals(30.0, snapshot.averageMapBitmapDecodeMs, 0.000001)
    }

    @Test
    fun snapshot_computesFullscreenFirstRenderAverage() {
        TeamCompassPerfMetrics.resetForTest()

        TeamCompassPerfMetrics.recordFullscreenMapFirstRender(120L)
        TeamCompassPerfMetrics.recordFullscreenMapFirstRender(180L)

        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.fullscreenMapFirstRenderSamples)
        assertEquals(300L, snapshot.fullscreenMapFirstRenderTotalMs)
        assertEquals(150.0, snapshot.averageFullscreenMapFirstRenderMs, 0.000001)
        assertTrue(snapshot.peakAppUsedMemoryBytes >= 0L)
    }
}
