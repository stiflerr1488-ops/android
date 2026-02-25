package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PChunker
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.P2PTransportLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import java.util.concurrent.atomic.AtomicInteger

interface LoRaBridgeClient {
    suspend fun sendFrame(frameBytes: ByteArray): Result<Unit>
    fun incomingFrames(): Flow<ByteArray>
    fun connectedPeers(): Set<String>
}

object NoOpLoRaBridgeClient : LoRaBridgeClient {
    override suspend fun sendFrame(frameBytes: ByteArray): Result<Unit> {
        return Result.failure(IllegalStateException("LoRa bridge is not configured"))
    }

    override fun incomingFrames(): Flow<ByteArray> = emptyFlow()

    override fun connectedPeers(): Set<String> = emptySet()
}

class LoRaBridgeTransport(
    private val bridgeClient: LoRaBridgeClient,
    private val messageCodec: P2PMessageCodec = BinaryP2PMessageCodec(),
    private val reassemblyBuffer: ChunkReassemblyBuffer = ChunkReassemblyBuffer(
        maxMessages = 512,
        maxAgeMs = 120_000L,
    ),
    override val name: String = "lora",
    override val limits: P2PTransportLimits = P2PTransportLimits(
        maxPayloadBytes = 220,
        recommendedPayloadBytes = 96,
    ),
    override val capabilities: Set<P2PTransportCapability> = setOf(
        P2PTransportCapability.BROADCAST,
        P2PTransportCapability.MESH_RELAY,
        P2PTransportCapability.ACKS,
    ),
) : P2PTransport {

    private val frameSequence = AtomicInteger(0)

    override suspend fun send(peerId: String, message: P2PMessage): Result<Unit> {
        return sendInternal(message = message)
    }

    override suspend fun broadcast(message: P2PMessage): Result<Unit> {
        return sendInternal(message = message)
    }

    override fun receive(): Flow<P2PMessage> {
        return bridgeClient.incomingFrames().mapNotNull { frameBytes ->
            val frame = LoRaBridgeFramer.decode(frameBytes).getOrNull() ?: return@mapNotNull null
            if (frame.type != LoRaFrameType.DATA) return@mapNotNull null

            val chunk = ChunkEnvelopeCodec.decode(frame.payload).getOrNull() ?: return@mapNotNull null
            val assembled = reassemblyBuffer.onChunk(chunk).getOrNull() ?: return@mapNotNull null
            val encodedMessage = assembled ?: return@mapNotNull null
            messageCodec.decode(encodedMessage).getOrNull()
        }
    }

    override fun connectedPeers(): Set<String> = bridgeClient.connectedPeers()

    private suspend fun sendInternal(message: P2PMessage): Result<Unit> {
        val encoded = messageCodec.encode(message)
        val chunkPayloadBytes = (limits.maxPayloadBytes - CHUNK_ENVELOPE_OVERHEAD_BYTES).coerceAtLeast(8)
        val messageId = deriveMessageId(message)
        val chunks = P2PChunker.chunk(
            payload = encoded,
            maxChunkPayloadBytes = chunkPayloadBytes,
            messageId = messageId,
        )

        chunks.forEach { chunk ->
            val chunkBytes = ChunkEnvelopeCodec.encode(chunk)
            val frame = LoRaBridgeFrame(
                type = LoRaFrameType.DATA,
                sequenceNumber = frameSequence.getAndUpdate { current ->
                    if (current == Int.MAX_VALUE) 0 else current + 1
                },
                payload = chunkBytes,
            )
            val frameBytes = LoRaBridgeFramer.encode(frame)
            val writeResult = bridgeClient.sendFrame(frameBytes)
            if (writeResult.isFailure) return writeResult
        }
        return Result.success(Unit)
    }

    private fun deriveMessageId(message: P2PMessage): Long {
        val timestampPart = message.metadata.timestampMs and 0x0000_FFFF_FFFF_0000L
        val sequencePart = message.metadata.sequenceNumber.toLong() and 0x0000_0000_0000_FFFFL
        return timestampPart xor sequencePart
    }

    private companion object {
        private const val CHUNK_ENVELOPE_OVERHEAD_BYTES = 23
    }
}
