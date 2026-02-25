package com.example.teamcompass.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.ComponentCallbacks2
import android.util.LruCache
import com.example.teamcompass.perf.TeamCompassPerfMetrics
import kotlin.math.max

internal object MapBitmapCache {
    private const val MIN_REQUEST_SIDE_PX = 512
    private const val DEFAULT_REQUEST_SIDE_PX = 1920
    private const val MAX_CACHE_BYTES = 24 * 1024 * 1024

    // Byte-sized cache for already decoded map overlays.
    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount.coerceAtLeast(1)
        }
    }

    fun load(path: String, requestSidePx: Int = DEFAULT_REQUEST_SIDE_PX): Bitmap? {
        val requested = requestSidePx.coerceAtLeast(MIN_REQUEST_SIDE_PX)
        val key = "$path@$requested"
        cache.get(key)?.let { cached ->
            TeamCompassPerfMetrics.recordMapBitmapLoadRequest(cacheHit = true)
            return cached
        }
        TeamCompassPerfMetrics.recordMapBitmapLoadRequest(cacheHit = false)

        val decodeStartedAt = System.currentTimeMillis()
        val decoded = decodeDownsampled(path, requested) ?: return null
        val decodeDurationMs = System.currentTimeMillis() - decodeStartedAt
        TeamCompassPerfMetrics.recordMapBitmapDecode(
            durationMs = decodeDurationMs,
            decodedBytes = decoded.byteCount,
        )
        cache.put(key, decoded)
        return decoded
    }

    private fun decodeDownsampled(path: String, requestSidePx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val srcMaxSide = max(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while ((srcMaxSide / sample) > requestSidePx * 2) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeFile(path, options)
    }

    internal fun onTrimMemory(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
                level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                cache.evictAll()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW ||
                level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                cache.trimToSize((cache.maxSize() / 2).coerceAtLeast(1))
            }
        }
    }

    internal fun onLowMemory() {
        cache.evictAll()
    }

    internal fun clearForTest() {
        cache.evictAll()
    }
}
