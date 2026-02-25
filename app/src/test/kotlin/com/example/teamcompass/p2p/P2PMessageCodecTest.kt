package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PDeliveryMode
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PMessageMetadata
import com.example.teamcompass.core.p2p.P2PMessageType
import com.example.teamcompass.core.p2p.P2PPriority
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class P2PMessageCodecTest {

    private val codec = BinaryP2PMessageCodec()

    @Test
    fun roundTrip_preserves_all_fields() {
        val original = P2PMessage(
            metadata = P2PMessageMetadata(
                version = 1,
                type = P2PMessageType.ENEMY_PING,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 1_700_000_000_000L,
                sequenceNumber = 42,
                ttl = 3,
                priority = P2PPriority.HIGH,
                deliveryMode = P2PDeliveryMode.RELIABLE,
            ),
            payload = byteArrayOf(1, 2, 3, 4),
            signature = byteArrayOf(5, 6, 7),
        )

        val decoded = codec.decode(codec.encode(original)).getOrThrow()
        assertEquals(original.metadata, decoded.metadata)
        assertArrayEquals(original.payload, decoded.payload)
        assertArrayEquals(original.signature, decoded.signature)
    }

    @Test
    fun decode_invalid_bytes_returns_failure() {
        val bad = byteArrayOf(0x01, 0x02, 0x03)
        val result = codec.decode(bad)
        assertTrue(result.isFailure)
    }
}
