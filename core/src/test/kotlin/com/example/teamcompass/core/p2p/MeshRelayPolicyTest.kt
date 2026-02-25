package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MeshRelayPolicyTest {

    @Test
    fun decide_dropsExpiredTtl() {
        val policy = MeshRelayPolicy(replayProtector = ReplayProtector())
        val decision = policy.decide(frame(ttl = 0, sequence = 1))
        assertFalse(decision.shouldForward)
        assertEquals(MeshDropReason.EXPIRED_TTL, decision.dropReason)
    }

    @Test
    fun decide_dropsDuplicate() {
        val protector = ReplayProtector()
        val policy = MeshRelayPolicy(
            replayProtector = protector,
            minRelayIntervalMs = 0L,
            jitterMaxMs = 0L,
        )
        val first = policy.decide(frame(ttl = 3, sequence = 10))
        val second = policy.decide(frame(ttl = 3, sequence = 10))
        assertTrue(first.shouldForward)
        assertFalse(second.shouldForward)
        assertEquals(MeshDropReason.DUPLICATE, second.dropReason)
    }

    @Test
    fun decide_appliesRateLimit() {
        var nowMs = 1_000L
        val policy = MeshRelayPolicy(
            replayProtector = ReplayProtector(nowMsProvider = { nowMs }),
            minRelayIntervalMs = 500L,
            jitterMaxMs = 0L,
            nowMsProvider = { nowMs },
        )
        val first = policy.decide(frame(ttl = 3, sequence = 1))
        val second = policy.decide(frame(ttl = 3, sequence = 2))
        nowMs += 600L
        val third = policy.decide(frame(ttl = 3, sequence = 3))

        assertTrue(first.shouldForward)
        assertFalse(second.shouldForward)
        assertEquals(MeshDropReason.RATE_LIMIT, second.dropReason)
        assertTrue(third.shouldForward)
    }

    @Test
    fun decide_returnsTtlMinusOneAndJitter() {
        val policy = MeshRelayPolicy(
            replayProtector = ReplayProtector(),
            minRelayIntervalMs = 0L,
            jitterMaxMs = 99L,
            jitterProvider = { 33L },
        )
        val decision = policy.decide(frame(ttl = 4, sequence = 77))
        assertTrue(decision.shouldForward)
        assertEquals(3, decision.nextTtl)
        assertEquals(33L, decision.jitterDelayMs)
    }

    private fun frame(ttl: Int, sequence: Int): MeshFrame {
        val metadata = P2PMessageMetadata(
            type = P2PMessageType.POSITION_UPDATE,
            senderId = "user-a",
            teamCode = "123456",
            timestampMs = 10_000L,
            sequenceNumber = sequence,
            ttl = ttl.coerceIn(0, P2PProtocolLimits.MAX_MESH_TTL),
        )
        val message = P2PMessage(
            metadata = metadata,
            payload = byteArrayOf(0x01),
        )
        return MeshFrame(
            originalSenderId = "user-a",
            lastHopId = "user-b",
            sequenceNumber = sequence,
            ttl = ttl,
            message = message,
        )
    }
}
