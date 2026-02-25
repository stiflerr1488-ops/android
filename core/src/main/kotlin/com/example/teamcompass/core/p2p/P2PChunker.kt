package com.example.teamcompass.core.p2p

import java.io.ByteArrayOutputStream

data class P2PChunk(
    val messageId: Long,
    val index: Int,
    val total: Int,
    val payload: ByteArray,
) {
    init {
        require(index >= 0) { "index must be >= 0" }
        require(total > 0) { "total must be > 0" }
        require(index < total) { "index must be < total" }
    }
}

object P2PChunker {
    fun chunk(
        payload: ByteArray,
        maxChunkPayloadBytes: Int,
        messageId: Long,
    ): List<P2PChunk> {
        require(payload.isNotEmpty()) { "payload must not be empty" }
        require(maxChunkPayloadBytes > 0) { "maxChunkPayloadBytes must be > 0" }

        val total = (payload.size + maxChunkPayloadBytes - 1) / maxChunkPayloadBytes
        return List(total) { chunkIndex ->
            val from = chunkIndex * maxChunkPayloadBytes
            val to = minOf(from + maxChunkPayloadBytes, payload.size)
            P2PChunk(
                messageId = messageId,
                index = chunkIndex,
                total = total,
                payload = payload.copyOfRange(from, to),
            )
        }
    }

    fun assemble(chunks: Collection<P2PChunk>): Result<ByteArray> {
        if (chunks.isEmpty()) return Result.failure(IllegalArgumentException("chunks must not be empty"))
        val first = chunks.first()
        val expectedMessageId = first.messageId
        val expectedTotal = first.total

        if (chunks.any { it.messageId != expectedMessageId }) {
            return Result.failure(IllegalArgumentException("chunks contain different messageId values"))
        }
        if (chunks.any { it.total != expectedTotal }) {
            return Result.failure(IllegalArgumentException("chunks contain different total values"))
        }
        if (chunks.size != expectedTotal) {
            return Result.failure(IllegalStateException("missing chunks: expected=$expectedTotal actual=${chunks.size}"))
        }

        val ordered = chunks.sortedBy { it.index }
        for (index in 0 until expectedTotal) {
            if (ordered[index].index != index) {
                return Result.failure(IllegalStateException("missing chunk index=$index"))
            }
        }

        val output = ByteArrayOutputStream()
        ordered.forEach { chunk -> output.write(chunk.payload) }
        return Result.success(output.toByteArray())
    }
}
