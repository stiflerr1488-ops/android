package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
