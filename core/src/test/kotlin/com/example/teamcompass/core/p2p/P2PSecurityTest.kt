package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class P2PSecurityTest {

    @Test
    fun attachAndVerifyTag_success() {
        val key = "team-secret-key".encodeToByteArray()
        val baseMessage = testMessage(payload = "hello".encodeToByteArray())
        val signed = P2PMessageAuthenticator.attachTag(message = baseMessage, key = key)

        assertEquals(P2PSecurityLimits.DEFAULT_TAG_SIZE_BYTES, signed.signature.size)
        assertTrue(P2PMessageAuthenticator.verifyTag(message = signed, key = key))
    }

    @Test
    fun verifyTag_failsWhenPayloadTampered() {
        val key = "team-secret-key".encodeToByteArray()
        val signed = P2PMessageAuthenticator.attachTag(
            message = testMessage(payload = "hello".encodeToByteArray()),
            key = key,
        )
        val tampered = signed.copy(payload = "hello!".encodeToByteArray())

        assertFalse(P2PMessageAuthenticator.verifyTag(message = tampered, key = key))
    }

    @Test
    fun hmacAuthenticator_honorsCustomTagSize() {
        val auth = HmacSha256MessageAuthenticator()
        val tag = auth.computeTag(
            data = "payload".encodeToByteArray(),
            key = "k".encodeToByteArray(),
            tagSizeBytes = 12,
        )
        assertEquals(12, tag.size)
    }

    private fun testMessage(payload: ByteArray): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.POSITION_UPDATE,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 1_000L,
                sequenceNumber = 1,
                ttl = 3,
            ),
            payload = payload,
        )
    }
}
