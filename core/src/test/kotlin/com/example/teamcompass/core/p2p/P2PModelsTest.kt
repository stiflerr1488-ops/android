package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class P2PModelsTest {

    @Test
    fun metadata_rejectsInvalidTeamCode() {
        assertFailsWith<IllegalArgumentException> {
            P2PMessageMetadata(
                type = P2PMessageType.POSITION_UPDATE,
                senderId = "uid-1",
                teamCode = "12A456",
                timestampMs = 1_000L,
                sequenceNumber = 1,
            )
        }
    }

    @Test
    fun metadata_defaultsFollowQosPolicy() {
        val metadata = P2PMessageMetadata(
            type = P2PMessageType.SOS_SIGNAL,
            senderId = "uid-1",
            teamCode = "123456",
            timestampMs = 1_000L,
            sequenceNumber = 1,
        )
        assertEquals(P2PPriority.CRITICAL, metadata.priority)
        assertEquals(P2PDeliveryMode.RELIABLE, metadata.deliveryMode)
    }
}
