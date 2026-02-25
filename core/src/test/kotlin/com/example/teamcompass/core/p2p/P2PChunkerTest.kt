package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class P2PChunkerTest {

    @Test
    fun chunk_singleChunkWhenPayloadFits() {
        val payload = ByteArray(10) { it.toByte() }
        val chunks = P2PChunker.chunk(payload, maxChunkPayloadBytes = 16, messageId = 1L)
        assertEquals(1, chunks.size)
        assertContentEquals(payload, chunks.single().payload)
    }

    @Test
    fun chunkAndAssemble_roundTrip() {
        val payload = ByteArray(57) { (it + 1).toByte() }
        val chunks = P2PChunker.chunk(payload, maxChunkPayloadBytes = 16, messageId = 55L)
        assertEquals(4, chunks.size)

        val assembled = P2PChunker.assemble(chunks)
        assertTrue(assembled.isSuccess)
        assertContentEquals(payload, assembled.getOrThrow())
    }

    @Test
    fun assemble_failsWhenChunkMissing() {
        val payload = ByteArray(40) { it.toByte() }
        val chunks = P2PChunker.chunk(payload, maxChunkPayloadBytes = 10, messageId = 9L)
        val incomplete = chunks.filterNot { it.index == 2 }
        val assembled = P2PChunker.assemble(incomplete)
        assertFalse(assembled.isSuccess)
    }
}
