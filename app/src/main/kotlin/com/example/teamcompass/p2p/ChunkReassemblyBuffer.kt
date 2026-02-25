package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PChunk
import com.example.teamcompass.core.p2p.P2PChunker
import java.util.concurrent.ConcurrentHashMap

class ChunkReassemblyBuffer(
    private val maxMessages: Int = 256,
    private val maxAgeMs: Long = 30_000L,
    private val nowMsProvider: () -> Long = System::currentTimeMillis,
) {
    private data class PartialMessage(
        val createdAtMs: Long,
        val chunksByIndex: MutableMap<Int, P2PChunk> = LinkedHashMap(),
        var totalChunks: Int = -1,
    )

    private val partialByMessageId = ConcurrentHashMap<Long, PartialMessage>()

    fun onChunk(chunk: P2PChunk): Result<ByteArray?> {
        val nowMs = nowMsProvider()
        prune(nowMs)
        val partial = partialByMessageId.computeIfAbsent(chunk.messageId) {
            PartialMessage(createdAtMs = nowMs)
        }
        synchronized(partial) {
            if (partial.totalChunks < 0) {
                partial.totalChunks = chunk.total
            } else if (partial.totalChunks != chunk.total) {
                partialByMessageId.remove(chunk.messageId)
                return Result.failure(
                    IllegalStateException("Inconsistent chunk.total for messageId=${chunk.messageId}"),
                )
            }

            partial.chunksByIndex[chunk.index] = chunk
            if (partial.chunksByIndex.size < partial.totalChunks) {
                return Result.success(null)
            }

            val assembled = P2PChunker.assemble(partial.chunksByIndex.values)
            partialByMessageId.remove(chunk.messageId)
            return assembled.map { it }
        }
    }

    private fun prune(nowMs: Long) {
        val iterator = partialByMessageId.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (nowMs - entry.value.createdAtMs > maxAgeMs) {
                iterator.remove()
            }
        }

        if (partialByMessageId.size <= maxMessages) return
        val extra = partialByMessageId.size - maxMessages
        partialByMessageId.entries
            .sortedBy { it.value.createdAtMs }
            .take(extra)
            .forEach { oldest ->
                partialByMessageId.remove(oldest.key)
            }
    }
}
