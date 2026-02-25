package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PChunker
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.P2PTransportLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull

interface BleBridgeClient {
    suspend fun sendPacket(peerId: String?, packet: ByteArray): Result<Unit>
    fun incomingPackets(): Flow<ByteArray>
    fun connectedPeers(): Set<String>
}

object NoOpBleBridgeClient : BleBridgeClient {
    override suspend fun sendPacket(peerId: String?, packet: ByteArray): Result<Unit> {
        return Result.failure(IllegalStateException("BLE bridge is not configured"))
    }

    override fun incomingPackets(): Flow<ByteArray> = emptyFlow()

    override fun connectedPeers(): Set<String> = emptySet()
}

class BleP2PTransport(
    private val bridgeClient: BleBridgeClient,
    private val messageCodec: P2PMessageCodec = BinaryP2PMessageCodec(),
    private val reassemblyBuffer: ChunkReassemblyBuffer = ChunkReassemblyBuffer(),
    override val name: String = "ble",
    override val limits: P2PTransportLimits = P2PTransportLimits(
        maxPayloadBytes = 256,
        recommendedPayloadBytes = 160,
    ),
    override val capabilities: Set<P2PTransportCapability> = setOf(
        P2PTransportCapability.BROADCAST,
        P2PTransportCapability.ACKS,
    ),
) : P2PTransport {

    override suspend fun send(peerId: String, message: P2PMessage): Result<Unit> {
        return sendInternal(peerId = peerId, message = message)
    }

    override suspend fun broadcast(message: P2PMessage): Result<Unit> {
        return sendInternal(peerId = null, message = message)
    }

    override fun receive(): Flow<P2PMessage> {
        return bridgeClient.incomingPackets().mapNotNull { packet ->
            val chunk = ChunkEnvelopeCodec.decode(packet).getOrNull() ?: return@mapNotNull null
            val assembled = reassemblyBuffer.onChunk(chunk).getOrNull() ?: return@mapNotNull null
            val encodedMessage = assembled ?: return@mapNotNull null
            messageCodec.decode(encodedMessage).getOrNull()
        }
    }

    override fun connectedPeers(): Set<String> = bridgeClient.connectedPeers()

    private suspend fun sendInternal(peerId: String?, message: P2PMessage): Result<Unit> {
        val encoded = messageCodec.encode(message)
        val chunkPayloadBytes = (limits.maxPayloadBytes - CHUNK_ENVELOPE_OVERHEAD_BYTES).coerceAtLeast(8)
        val messageId = deriveMessageId(message)
        val chunks = P2PChunker.chunk(
            payload = encoded,
            maxChunkPayloadBytes = chunkPayloadBytes,
            messageId = messageId,
        )

        chunks.forEach { chunk ->
            val packet = ChunkEnvelopeCodec.encode(chunk)
            val writeResult = bridgeClient.sendPacket(peerId = peerId, packet = packet)
            if (writeResult.isFailure) {
                return writeResult
            }
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
