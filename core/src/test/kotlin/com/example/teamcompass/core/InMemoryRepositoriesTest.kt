package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InMemoryRepositoriesTest {
    @Test
    fun `join assigns suffix for duplicate nickname`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val now = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Компас", nowMs = now)

        val member = repo.joinMatch(meta.matchId, uid = "u1", nick = "Компас", nowMs = now + 1)
        assertEquals("Компас#2", member.nick)
    }

    @Test
    fun `lock blocks new joins`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val now = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Лидер", nowMs = now)
        repo.lockMatch(meta.matchId)

        val thrown = runCatching {
            repo.joinMatch(meta.matchId, uid = "u1", nick = "Скаут", nowMs = now + 1)
        }.exceptionOrNull()

        assertTrue(thrown is IllegalArgumentException)
    }

    @Test
    fun `join is idempotent for same uid`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val now = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Лидер", nowMs = now)

        val first = repo.joinMatch(meta.matchId, uid = "u1", nick = "Скаут", nowMs = now + 1)
        val second = repo.joinMatch(meta.matchId, uid = "u1", nick = "Другой", nowMs = now + 2)
        val snapshot = repo.getSnapshot(meta.matchId)

        assertEquals(first, second)
        assertEquals(2, snapshot?.members?.size)
        assertEquals("Скаут", second.nick)
    }

    @Test
    fun `rejoin by existing member still succeeds when match becomes locked`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val now = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Лидер", nowMs = now)
        val member = repo.joinMatch(meta.matchId, uid = "u1", nick = "Скаут", nowMs = now + 1)

        repo.lockMatch(meta.matchId)

        val sameMember = repo.joinMatch(meta.matchId, uid = "u1", nick = "НовыйНик", nowMs = now + 2)
        assertEquals(member, sameMember)
    }

    @Test
    fun `expired match rejects joins`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val createdAt = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Лидер", nowMs = createdAt)
        val expiredAt = createdAt + 12 * 60 * 60 * 1000

        assertFailsWith<IllegalArgumentException> {
            repo.joinMatch(meta.matchId, uid = "u1", nick = "Скаут", nowMs = expiredAt + 1)
        }
    }

    @Test
    fun `rejoin by existing member still succeeds when match is expired`() {
        val states = InMemoryStateRepository()
        val repo = InMemoryMatchRepository(states)
        val createdAt = 1_000L
        val meta = repo.createMatch(ownerUid = "u0", ownerNick = "Лидер", nowMs = createdAt)
        val member = repo.joinMatch(meta.matchId, uid = "u1", nick = "Скаут", nowMs = createdAt + 1)
        val expiredAt = createdAt + 12 * 60 * 60 * 1000

        val sameMember = repo.joinMatch(meta.matchId, uid = "u1", nick = "НовыйНик", nowMs = expiredAt + 10)
        assertEquals(member, sameMember)
    }
}
