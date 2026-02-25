package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PDeliveryMode
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PMessageMetadata
import com.example.teamcompass.core.p2p.P2PMessageType
import com.example.teamcompass.core.p2p.P2PPriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoRaBridgeTransportTest {

    @Test
    fun send_wraps_chunked_message_into_frames() = runTest {
        val bridge = FakeLoRaBridgeClient()
        val transport = LoRaBridgeTransport(
            bridgeClient = bridge,
            limits = com.example.teamcompass.core.p2p.P2PTransportLimits(
                maxPayloadBytes = 64,
                recommendedPayloadBytes = 48,
            ),
        )
        val result = transport.broadcast(message(payloadSize = 180))
        assertTrue(result.isSuccess)
        assertTrue(bridge.sentFrames.size > 1)
    }

    @Test
    fun receive_decodes_frame_and_reassembles_message() = runTest {
        val bridge = FakeLoRaBridgeClient()
        val transport = LoRaBridgeTransport(
            bridgeClient = bridge,
            limits = com.example.teamcompass.core.p2p.P2PTransportLimits(
                maxPayloadBytes = 64,
                recommendedPayloadBytes = 48,
            ),
        )
        val original = message(payloadSize = 170)
        transport.broadcast(original)

        val collectJob = launch {
            val inbound = transport.receive().first()
            assertEquals(original.metadata.sequenceNumber, inbound.metadata.sequenceNumber)
            assertEquals(original.payload.size, inbound.payload.size)
        }
        runCurrent()
        bridge.sentFrames.forEach { bytes ->
            bridge.emitIncoming(bytes)
        }
        advanceUntilIdle()
        collectJob.cancel()
    }

    private fun message(payloadSize: Int): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.ENEMY_PING,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 2_000L,
                sequenceNumber = 21,
                ttl = 3,
                priority = P2PPriority.HIGH,
                deliveryMode = P2PDeliveryMode.RELIABLE,
            ),
            payload = ByteArray(payloadSize) { 0x3F },
        )
    }
}

private class FakeLoRaBridgeClient : LoRaBridgeClient {
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val sentFrames = mutableListOf<ByteArray>()

    override suspend fun sendFrame(frameBytes: ByteArray): Result<Unit> {
        sentFrames += frameBytes
        return Result.success(Unit)
    }

    override fun incomingFrames(): Flow<ByteArray> = incoming

    override fun connectedPeers(): Set<String> = setOf("lora-bridge")

    suspend fun emitIncoming(frameBytes: ByteArray) {
        incoming.emit(frameBytes)
    }
}
