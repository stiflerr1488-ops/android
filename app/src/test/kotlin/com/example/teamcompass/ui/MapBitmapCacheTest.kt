package com.example.teamcompass.ui

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.teamcompass.perf.TeamCompassPerfMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class MapBitmapCacheTest {

    @Test
    fun load_returnsCachedBitmap_forSamePathAndRequestSide() {
        TeamCompassPerfMetrics.resetForTest()
        MapBitmapCache.clearForTest()
        val file = createBitmapFile(width = 1024, height = 1024)

        val first = MapBitmapCache.load(file.absolutePath, requestSidePx = 1024)
        val second = MapBitmapCache.load(file.absolutePath, requestSidePx = 1024)

        assertNotNull(first)
        assertSame(first, second)
        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.mapBitmapLoadRequests)
        assertEquals(1L, snapshot.mapBitmapCacheHits)
        assertEquals(1L, snapshot.mapBitmapDecodes)
    }

    @Test
    fun load_usesDifferentCacheKeys_forDifferentRequestSide() {
        TeamCompassPerfMetrics.resetForTest()
        MapBitmapCache.clearForTest()
        val file = createBitmapFile(width = 2048, height = 1024)

        val large = MapBitmapCache.load(file.absolutePath, requestSidePx = 2048)
        val small = MapBitmapCache.load(file.absolutePath, requestSidePx = 512)

        assertNotNull(large)
        assertNotNull(small)
        assertNotSame(large, small)
        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.mapBitmapLoadRequests)
        assertEquals(0L, snapshot.mapBitmapCacheHits)
        assertEquals(2L, snapshot.mapBitmapDecodes)
    }

    @Test
    fun load_clampsSmallRequestSideToMinimum_andReusesSameCacheEntry() {
        TeamCompassPerfMetrics.resetForTest()
        MapBitmapCache.clearForTest()
        val file = createBitmapFile(width = 1024, height = 1024)

        val clamped = MapBitmapCache.load(file.absolutePath, requestSidePx = 100)
        val minimum = MapBitmapCache.load(file.absolutePath, requestSidePx = 512)

        assertNotNull(clamped)
        assertSame(clamped, minimum)
        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.mapBitmapLoadRequests)
        assertEquals(1L, snapshot.mapBitmapCacheHits)
        assertEquals(1L, snapshot.mapBitmapDecodes)
    }

    @Test
    fun onLowMemory_evictsCache_andForcesDecodeAgain() {
        TeamCompassPerfMetrics.resetForTest()
        MapBitmapCache.clearForTest()
        val file = createBitmapFile(width = 1024, height = 1024)

        val first = MapBitmapCache.load(file.absolutePath, requestSidePx = 1024)
        MapBitmapCache.onLowMemory()
        val second = MapBitmapCache.load(file.absolutePath, requestSidePx = 1024)

        assertNotNull(first)
        assertNotNull(second)
        assertNotSame(first, second)
        val snapshot = TeamCompassPerfMetrics.snapshot()
        assertEquals(2L, snapshot.mapBitmapLoadRequests)
        assertEquals(0L, snapshot.mapBitmapCacheHits)
        assertEquals(2L, snapshot.mapBitmapDecodes)
    }

    private fun createBitmapFile(width: Int, height: Int): File {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val file = File(app.cacheDir, "map_cache_${UUID.randomUUID()}.png")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        bitmap.recycle()
        return file
    }
}
