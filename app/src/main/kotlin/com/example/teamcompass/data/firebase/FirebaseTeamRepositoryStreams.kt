package com.example.teamcompass.data.firebase

import com.example.teamcompass.domain.TeamEnemyPing
import com.google.firebase.database.DatabaseError
import java.util.concurrent.atomic.AtomicBoolean

internal enum class EnemyCleanupEmitStrategy {
    EMIT_NOW,
    SCHEDULE_DEBOUNCED,
}

internal data class EnemyCleanupPlan(
    val idsToDelete: List<String>,
    val emitStrategy: EnemyCleanupEmitStrategy,
)

internal data class StateCellEntry(
    val cellId: String,
    val lastUpdatedMs: Long,
)

internal fun shouldFallbackToLegacyStateListener(
    listenerKey: String,
    errorCode: Int,
    stateCellsReadEnabled: Boolean,
): Boolean {
    return stateCellsReadEnabled && isStateCellPermissionDenied(
        listenerKey = listenerKey,
        errorCode = errorCode,
    )
}

internal fun isStateCellPermissionDenied(
    listenerKey: String,
    errorCode: Int,
): Boolean {
    if (!listenerKey.startsWith("stateCell:")) return false
    return errorCode == DatabaseError.PERMISSION_DENIED
}

internal fun shouldEnableStateCellsRead(
    stateCellsEnabled: Boolean,
    stateCellsReadDeniedInSession: Boolean,
): Boolean = stateCellsEnabled && !stateCellsReadDeniedInSession

internal fun isPermissionDeniedMessage(message: String?): Boolean {
    val normalized = message
        ?.trim()
        ?.lowercase()
        .orEmpty()
    return normalized.contains("permission denied")
}

internal fun isStateCellsProbePermissionDenied(error: Throwable): Boolean {
    return isPermissionDeniedMessage(error.message)
}

internal fun shouldFallbackAfterStateCellsPreflight(
    timedOut: Boolean,
    failure: Throwable?,
): Boolean {
    return timedOut || failure != null
}

internal fun computeBackendReachabilitySample(
    connected: Boolean,
    consecutiveProbeFailures: Int,
    failureThreshold: Int = BACKEND_HEALTH_PROBE_FAILURE_THRESHOLD,
): Boolean {
    if (!connected) return false
    val safeThreshold = failureThreshold.coerceAtLeast(1)
    return consecutiveProbeFailures < safeThreshold
}

internal fun shouldCountBackendProbeFailure(failure: Throwable?): Boolean {
    if (failure == null) return false
    return !isPermissionDeniedMessage(failure.message)
}

internal fun selectExpiredEnemyPingIdsForCleanup(
    enemyPings: Collection<TeamEnemyPing>,
    nowMs: Long,
    maxDeletes: Int,
): List<String> {
    val limit = maxDeletes.coerceAtLeast(1)
    return enemyPings
        .asSequence()
        .filter { it.expiresAtMs in 1..nowMs }
        .map { it.id }
        .take(limit)
        .toList()
}

internal fun planEnemyPingCleanupAndEmit(
    enemyPings: Collection<TeamEnemyPing>,
    nowMs: Long,
    maxDeletes: Int,
): EnemyCleanupPlan {
    val ids = selectExpiredEnemyPingIdsForCleanup(
        enemyPings = enemyPings,
        nowMs = nowMs,
        maxDeletes = maxDeletes,
    )
    val emitStrategy = if (ids.isEmpty()) {
        EnemyCleanupEmitStrategy.EMIT_NOW
    } else {
        EnemyCleanupEmitStrategy.SCHEDULE_DEBOUNCED
    }
    return EnemyCleanupPlan(
        idsToDelete = ids,
        emitStrategy = emitStrategy,
    )
}

internal fun pruneStateCellCacheEntries(
    cache: MutableMap<String, StateCellEntry>,
    maxEntries: Int = STATE_CELL_CACHE_MAX_ENTRIES,
    pruneToEntries: Int = STATE_CELL_CACHE_PRUNE_TO_ENTRIES,
): Int {
    val safeMax = maxEntries.coerceAtLeast(1)
    val safePruneTo = pruneToEntries.coerceIn(1, safeMax)
    if (cache.size <= safeMax) return 0

    val removeCount = (cache.size - safePruneTo).coerceAtLeast(0)
    if (removeCount == 0) return 0

    val keysToRemove = cache.entries
        .asSequence()
        .sortedBy { it.value.lastUpdatedMs }
        .take(removeCount)
        .map { it.key }
        .toList()
    keysToRemove.forEach(cache::remove)
    return keysToRemove.size
}

internal fun pruneStateCellCacheIfNeeded(
    cache: MutableMap<String, StateCellEntry>,
    pruneInProgress: AtomicBoolean,
    maxEntries: Int = STATE_CELL_CACHE_MAX_ENTRIES,
    pruneToEntries: Int = STATE_CELL_CACHE_PRUNE_TO_ENTRIES,
) {
    if (cache.size <= maxEntries) return
    if (!pruneInProgress.compareAndSet(false, true)) return
    try {
        synchronized(cache) {
            pruneStateCellCacheEntries(
                cache = cache,
                maxEntries = maxEntries,
                pruneToEntries = pruneToEntries,
            )
        }
    } finally {
        pruneInProgress.set(false)
    }
}

internal fun <T> upsertSortedByCreatedAtDescending(
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    item: T,
    idOf: (T) -> String,
    createdAtOf: (T) -> Long,
    maxItems: Int = Int.MAX_VALUE,
) {
    val id = idOf(item)
    map[id] = item
    removeFromSortedById(
        sorted = sorted,
        id = id,
        idOf = idOf,
    )
    val createdAt = createdAtOf(item)
    val insertAt = sorted.indexOfFirst { existing -> createdAt > createdAtOf(existing) }
    if (insertAt >= 0) {
        sorted.add(insertAt, item)
    } else {
        sorted.add(item)
    }
    trimCacheToMax(
        map = map,
        sorted = sorted,
        idOf = idOf,
        maxItems = maxItems,
    )
}

internal fun <T> removeById(
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    id: String,
    idOf: (T) -> String,
) {
    map.remove(id)
    removeFromSortedById(
        sorted = sorted,
        id = id,
        idOf = idOf,
    )
}

internal fun <T> removeFromSortedById(
    sorted: MutableList<T>,
    id: String,
    idOf: (T) -> String,
) {
    val index = sorted.indexOfFirst { idOf(it) == id }
    if (index >= 0) {
        sorted.removeAt(index)
    }
}

internal fun <T> trimCacheToMax(
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    idOf: (T) -> String,
    maxItems: Int,
) {
    val limit = maxItems.coerceAtLeast(1)
    while (sorted.size > limit) {
        val removed = sorted.removeAt(sorted.lastIndex)
        map.remove(idOf(removed))
    }
}
