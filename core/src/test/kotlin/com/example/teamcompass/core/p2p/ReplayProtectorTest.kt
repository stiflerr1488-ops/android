package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReplayProtectorTest {

    @Test
    fun shouldAccept_rejectsDuplicateFromSameSender() {
        val protector = ReplayProtector(ttlMs = 60_000L, maxEntries = 128)
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 7))
        assertFalse(protector.shouldAccept(senderId = "u1", sequenceNumber = 7))
    }

    @Test
    fun shouldAccept_acceptsSameSequenceForDifferentSenders() {
        val protector = ReplayProtector(ttlMs = 60_000L, maxEntries = 128)
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 3))
        assertTrue(protector.shouldAccept(senderId = "u2", sequenceNumber = 3))
    }

    @Test
    fun expiredEntries_arePrunedAndAcceptedAgain() {
        var nowMs = 1_000L
        val protector = ReplayProtector(
            ttlMs = 100L,
            maxEntries = 128,
            nowMsProvider = { nowMs },
        )

        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 11))
        nowMs = 1_500L
        protector.pruneExpired(nowMs)
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 11))
    }

    @Test
    fun maxEntries_evictsOldest() {
        var nowMs = 1_000L
        val protector = ReplayProtector(
            ttlMs = 60_000L,
            maxEntries = 2,
            nowMsProvider = { nowMs },
        )
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 1))
        nowMs++
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 2))
        nowMs++
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 3))

        assertEquals(2, protector.size())
        assertTrue(protector.shouldAccept(senderId = "u1", sequenceNumber = 1))
    }
}
