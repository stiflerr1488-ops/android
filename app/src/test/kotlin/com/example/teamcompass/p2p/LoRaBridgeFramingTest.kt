package com.example.teamcompass.p2p

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoRaBridgeFramingTest {

    @Test
    fun encodeDecode_roundTrip() {
        val frame = LoRaBridgeFrame(
            type = LoRaFrameType.DATA,
            sequenceNumber = 123,
            payload = byteArrayOf(10, 11, 12),
        )
        val encoded = LoRaBridgeFramer.encode(frame)
        val decoded = LoRaBridgeFramer.decode(encoded).getOrThrow()
        assertEquals(frame.type, decoded.type)
        assertEquals(frame.sequenceNumber, decoded.sequenceNumber)
        assertArrayEquals(frame.payload, decoded.payload)
    }

    @Test
    fun decode_crcMismatch_returns_failure() {
        val frame = LoRaBridgeFrame(
            type = LoRaFrameType.DATA,
            sequenceNumber = 123,
            payload = byteArrayOf(1, 2, 3, 4),
        )
        val encoded = LoRaBridgeFramer.encode(frame)
        encoded[encoded.lastIndex] = (encoded.last().toInt() xor 0x01).toByte()
        val result = LoRaBridgeFramer.decode(encoded)
        assertTrue(result.isFailure)
    }
}
