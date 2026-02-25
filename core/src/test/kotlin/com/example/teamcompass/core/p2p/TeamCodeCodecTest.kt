package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TeamCodeCodecTest {

    @Test
    fun compressAndDecompress_roundTrip() {
        val compressed = TeamCodeCodec.compress("123456")
        assertEquals(3, compressed.size)
        assertEquals("123456", TeamCodeCodec.decompress(compressed))
    }

    @Test
    fun compress_rejectsInvalidTeamCode() {
        assertFailsWith<IllegalArgumentException> {
            TeamCodeCodec.compress("12A456")
        }
    }

    @Test
    fun decompress_rejectsOutOfRangeValue() {
        val outOfRange = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        assertFailsWith<IllegalArgumentException> {
            TeamCodeCodec.decompress(outOfRange)
        }
    }

    @Test
    fun compress_stableBinaryRepresentation() {
        assertContentEquals(
            expected = byteArrayOf(0x01, 0xE2.toByte(), 0x40),
            actual = TeamCodeCodec.compress("123456"),
        )
    }
}
