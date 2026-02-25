package com.example.teamcompass.data.firebase

import android.util.Log
import com.example.teamcompass.core.TeamCodeSecurity

internal suspend fun migrateLegacyJoinSecrets(
    backendClient: RealtimeBackendClient,
    teamPath: String,
    code: String,
    logTag: String,
) {
    val meta = runCatching { backendClient.get(path = "$teamPath/meta") }
        .getOrNull()
        ?: return
    val hasSecrets = !meta.child("joinSalt").stringOrNull().isNullOrBlank() &&
        !meta.child("joinHash").stringOrNull().isNullOrBlank()
    if (hasSecrets) return

    val joinSalt = TeamCodeSecurity.generateSaltHex()
    val joinHash = TeamCodeSecurity.hashJoinCode(code, joinSalt)
    runCatching {
        backendClient.updateChildren(
            path = "$teamPath/meta",
            updates = mapOf(
                "joinSalt" to joinSalt,
                "joinHash" to joinHash,
            ),
        )
    }.onFailure { err ->
        Log.w(logTag, "Failed to migrate legacy join secrets for team=$code", err)
    }
}

internal fun acquireClientRatePermit(
    lastWriteAtByUid: MutableMap<String, Long>,
    uid: String,
    nowMs: Long,
    minIntervalMs: Long,
): Boolean {
    val safeUid = uid.trim()
    if (safeUid.isBlank()) return true
    val safeMinInterval = minIntervalMs.coerceAtLeast(1L)
    synchronized(lastWriteAtByUid) {
        val previousAt = lastWriteAtByUid[safeUid] ?: 0L
        if (nowMs - previousAt < safeMinInterval) {
            return false
        }
        lastWriteAtByUid[safeUid] = nowMs
        return true
    }
}

internal suspend fun cleanupExpiredEnemyPings(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    idsToDelete: List<String>,
    logTag: String,
): Int {
    idsToDelete.forEach { id ->
        runCatching {
            backendClient.removeValue(path = "teams/$teamCode/enemyPings/$id")
        }.onFailure { err ->
            Log.w(logTag, "Failed to schedule enemy ping cleanup for id=$id", err)
        }
    }
    return idsToDelete.size
}
