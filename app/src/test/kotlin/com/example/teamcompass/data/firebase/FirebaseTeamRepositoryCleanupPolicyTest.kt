package com.example.teamcompass.data.firebase

import com.example.teamcompass.domain.TeamEnemyPing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTeamRepositoryCleanupPolicyTest {

    @Test
    fun selectExpiredEnemyPingIdsForCleanup_returnsOnlyExpired_withMaxLimit() {
        val now = 10_000L
        val enemyPings = listOf(
            ping(id = "active-1", createdAt = 1_000L, expiresAt = now + 5_000L),
            ping(id = "expired-1", createdAt = 2_000L, expiresAt = now - 1L),
            ping(id = "expired-2", createdAt = 3_000L, expiresAt = now),
            ping(id = "active-2", createdAt = 4_000L, expiresAt = now + 20_000L),
            ping(id = "expired-3", createdAt = 5_000L, expiresAt = now - 10L),
        )

        val selected = selectExpiredEnemyPingIdsForCleanup(
            enemyPings = enemyPings,
            nowMs = now,
            maxDeletes = 2,
        )

        assertEquals(listOf("expired-1", "expired-2"), selected)
    }

    @Test
    fun selectExpiredEnemyPingIdsForCleanup_treatsNonPositiveLimitAsOne() {
        val now = 20_000L
        val enemyPings = listOf(
            ping(id = "expired-1", createdAt = 1_000L, expiresAt = now - 1L),
            ping(id = "expired-2", createdAt = 2_000L, expiresAt = now - 2L),
        )

        val selected = selectExpiredEnemyPingIdsForCleanup(
            enemyPings = enemyPings,
            nowMs = now,
            maxDeletes = 0,
        )

        assertEquals(listOf("expired-1"), selected)
    }

    @Test
    fun selectExpiredEnemyPingIdsForCleanup_ignoresNeverExpiringAndFutureEntries() {
        val now = 30_000L
        val enemyPings = listOf(
            ping(id = "never-expire", createdAt = 1_000L, expiresAt = 0L),
            ping(id = "future", createdAt = 2_000L, expiresAt = now + 1_000L),
        )

        val selected = selectExpiredEnemyPingIdsForCleanup(
            enemyPings = enemyPings,
            nowMs = now,
            maxDeletes = 10,
        )

        assertEquals(emptyList<String>(), selected)
    }

    @Test
    fun planEnemyPingCleanupAndEmit_noExpired_returnsEmitNow_withoutDeletes() {
        val now = 40_000L
        val enemyPings = listOf(
            ping(id = "future-1", createdAt = 1_000L, expiresAt = now + 1_000L),
            ping(id = "future-2", createdAt = 2_000L, expiresAt = now + 2_000L),
        )

        val plan = planEnemyPingCleanupAndEmit(
            enemyPings = enemyPings,
            nowMs = now,
            maxDeletes = 12,
        )

        assertTrue(plan.idsToDelete.isEmpty())
        assertEquals(EnemyCleanupEmitStrategy.EMIT_NOW, plan.emitStrategy)
    }

    @Test
    fun planEnemyPingCleanupAndEmit_expired_returnsDebounced_withLimitedDeletes() {
        val now = 50_000L
        val enemyPings = listOf(
            ping(id = "expired-1", createdAt = 1_000L, expiresAt = now - 1L),
            ping(id = "expired-2", createdAt = 2_000L, expiresAt = now - 2L),
            ping(id = "active", createdAt = 3_000L, expiresAt = now + 5_000L),
        )

        val plan = planEnemyPingCleanupAndEmit(
            enemyPings = enemyPings,
            nowMs = now,
            maxDeletes = 1,
        )

        assertEquals(listOf("expired-1"), plan.idsToDelete)
        assertEquals(EnemyCleanupEmitStrategy.SCHEDULE_DEBOUNCED, plan.emitStrategy)
    }

    @Test
    fun upsertSortedByCreatedAtDescending_insertsInDescendingOrder() {
        val map = linkedMapOf<String, TeamEnemyPing>()
        val sorted = mutableListOf<TeamEnemyPing>()

        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "mid", createdAt = 2_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "new", createdAt = 3_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "old", createdAt = 1_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )

        assertEquals(listOf("new", "mid", "old"), sorted.map { it.id })
    }

    @Test
    fun upsertSortedByCreatedAtDescending_replacesExistingAndMovesItem() {
        val map = linkedMapOf<String, TeamEnemyPing>()
        val sorted = mutableListOf<TeamEnemyPing>()

        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "a", createdAt = 1_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "b", createdAt = 2_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "a", createdAt = 3_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )

        assertEquals(listOf("a", "b"), sorted.map { it.id })
        assertEquals(3_000L, map.getValue("a").createdAtMs)
    }

    @Test
    fun upsertSortedByCreatedAtDescending_enforces_max_items() {
        val map = linkedMapOf<String, TeamEnemyPing>()
        val sorted = mutableListOf<TeamEnemyPing>()

        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "old", createdAt = 1_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
            maxItems = 2,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "mid", createdAt = 2_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
            maxItems = 2,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "new", createdAt = 3_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
            maxItems = 2,
        )

        assertEquals(listOf("new", "mid"), sorted.map { it.id })
        assertEquals(setOf("new", "mid"), map.keys)
    }

    @Test
    fun removeById_removesFromMapAndSortedList() {
        val map = linkedMapOf<String, TeamEnemyPing>()
        val sorted = mutableListOf<TeamEnemyPing>()
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "a", createdAt = 2_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = ping(id = "b", createdAt = 1_000L, expiresAt = 9_000L),
            idOf = TeamEnemyPing::id,
            createdAtOf = TeamEnemyPing::createdAtMs,
        )

        removeById(
            map = map,
            sorted = sorted,
            id = "a",
            idOf = TeamEnemyPing::id,
        )

        assertFalse(map.containsKey("a"))
        assertEquals(listOf("b"), sorted.map { it.id })
    }

    private fun ping(id: String, createdAt: Long, expiresAt: Long): TeamEnemyPing {
        return TeamEnemyPing(
            id = id,
            lat = 55.0,
            lon = 37.0,
            createdAtMs = createdAt,
            createdBy = "u-1",
            expiresAtMs = expiresAt,
            type = "DANGER",
        )
    }
}
