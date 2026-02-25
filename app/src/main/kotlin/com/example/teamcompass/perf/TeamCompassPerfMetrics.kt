package com.example.teamcompass.perf

import java.util.concurrent.atomic.AtomicLong

internal data class TeamCompassPerfSnapshot(
    val rtdbSnapshotEmits: Long,
    val rtdbCleanupSweeps: Long,
    val rtdbCleanupWrites: Long,
    val mapBitmapLoadRequests: Long,
    val mapBitmapCacheHits: Long,
    val mapBitmapDecodes: Long,
    val mapBitmapDecodeTotalMs: Long,
    val mapBitmapDecodedBytes: Long,
    val fullscreenMapFirstRenderSamples: Long,
    val fullscreenMapFirstRenderTotalMs: Long,
    val peakAppUsedMemoryBytes: Long,
) {
    val mapBitmapCacheHitRate: Double
        get() = if (mapBitmapLoadRequests <= 0L) 0.0 else mapBitmapCacheHits.toDouble() / mapBitmapLoadRequests

    val averageMapBitmapDecodeMs: Double
        get() = if (mapBitmapDecodes <= 0L) 0.0 else mapBitmapDecodeTotalMs.toDouble() / mapBitmapDecodes

    val averageFullscreenMapFirstRenderMs: Double
        get() = if (fullscreenMapFirstRenderSamples <= 0L) 0.0 else fullscreenMapFirstRenderTotalMs.toDouble() / fullscreenMapFirstRenderSamples
}

internal object TeamCompassPerfMetrics {
    private val rtdbSnapshotEmits = AtomicLong(0L)
    private val rtdbCleanupSweeps = AtomicLong(0L)
    private val rtdbCleanupWrites = AtomicLong(0L)
    private val mapBitmapLoadRequests = AtomicLong(0L)
    private val mapBitmapCacheHits = AtomicLong(0L)
    private val mapBitmapDecodes = AtomicLong(0L)
    private val mapBitmapDecodeTotalMs = AtomicLong(0L)
    private val mapBitmapDecodedBytes = AtomicLong(0L)
    private val fullscreenMapFirstRenderSamples = AtomicLong(0L)
    private val fullscreenMapFirstRenderTotalMs = AtomicLong(0L)
    private val peakAppUsedMemoryBytes = AtomicLong(0L)

    fun recordRtdbSnapshotEmit() {
        rtdbSnapshotEmits.incrementAndGet()
    }

    fun recordRtdbCleanupSweep(deleteWrites: Int) {
        rtdbCleanupSweeps.incrementAndGet()
        if (deleteWrites > 0) {
            rtdbCleanupWrites.addAndGet(deleteWrites.toLong())
        }
    }

    fun recordMapBitmapLoadRequest(cacheHit: Boolean) {
        mapBitmapLoadRequests.incrementAndGet()
        if (cacheHit) {
            mapBitmapCacheHits.incrementAndGet()
        }
        recordMemorySample()
    }

    fun recordMapBitmapDecode(durationMs: Long, decodedBytes: Int) {
        mapBitmapDecodes.incrementAndGet()
        mapBitmapDecodeTotalMs.addAndGet(durationMs.coerceAtLeast(0L))
        mapBitmapDecodedBytes.addAndGet(decodedBytes.coerceAtLeast(0).toLong())
        recordMemorySample()
    }

    fun recordFullscreenMapFirstRender(durationMs: Long) {
        fullscreenMapFirstRenderSamples.incrementAndGet()
        fullscreenMapFirstRenderTotalMs.addAndGet(durationMs.coerceAtLeast(0L))
        recordMemorySample()
    }

    fun snapshot(): TeamCompassPerfSnapshot {
        return TeamCompassPerfSnapshot(
            rtdbSnapshotEmits = rtdbSnapshotEmits.get(),
            rtdbCleanupSweeps = rtdbCleanupSweeps.get(),
            rtdbCleanupWrites = rtdbCleanupWrites.get(),
            mapBitmapLoadRequests = mapBitmapLoadRequests.get(),
            mapBitmapCacheHits = mapBitmapCacheHits.get(),
            mapBitmapDecodes = mapBitmapDecodes.get(),
            mapBitmapDecodeTotalMs = mapBitmapDecodeTotalMs.get(),
            mapBitmapDecodedBytes = mapBitmapDecodedBytes.get(),
            fullscreenMapFirstRenderSamples = fullscreenMapFirstRenderSamples.get(),
            fullscreenMapFirstRenderTotalMs = fullscreenMapFirstRenderTotalMs.get(),
            peakAppUsedMemoryBytes = peakAppUsedMemoryBytes.get(),
        )
    }

    fun resetForTest() {
        rtdbSnapshotEmits.set(0L)
        rtdbCleanupSweeps.set(0L)
        rtdbCleanupWrites.set(0L)
        mapBitmapLoadRequests.set(0L)
        mapBitmapCacheHits.set(0L)
        mapBitmapDecodes.set(0L)
        mapBitmapDecodeTotalMs.set(0L)
        mapBitmapDecodedBytes.set(0L)
        fullscreenMapFirstRenderSamples.set(0L)
        fullscreenMapFirstRenderTotalMs.set(0L)
        peakAppUsedMemoryBytes.set(0L)
    }

    private fun recordMemorySample() {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        while (true) {
            val current = peakAppUsedMemoryBytes.get()
            if (used <= current) return
            if (peakAppUsedMemoryBytes.compareAndSet(current, used)) return
        }
    }
}
