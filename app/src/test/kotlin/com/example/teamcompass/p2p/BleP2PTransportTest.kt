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
class BleP2PTransportTest {

    @Test
    fun send_chunks_payload_and_writes_packets() = runTest {
        val bridge = FakeBleBridgeClient()
        val transport = BleP2PTransport(
            bridgeClient = bridge,
            limits = com.example.teamcompass.core.p2p.P2PTransportLimits(
                maxPayloadBytes = 48,
                recommendedPayloadBytes = 32,
            ),
        )
        val message = message(payloadSize = 140)

        val result = transport.broadcast(message)

        assertTrue(result.isSuccess)
        assertTrue(bridge.sentPackets.size > 1)
    }

    @Test
    fun receive_reassembles_chunks_into_message() = runTest {
        val bridge = FakeBleBridgeClient()
        val transport = BleP2PTransport(
            bridgeClient = bridge,
            limits = com.example.teamcompass.core.p2p.P2PTransportLimits(
                maxPayloadBytes = 48,
                recommendedPayloadBytes = 32,
            ),
        )
        val message = message(payloadSize = 120)

        val packetsResult = transport.broadcast(message)
        assertTrue(packetsResult.isSuccess)

        val collector = launch {
            val inbound = transport.receive().first()
            assertEquals(message.metadata.sequenceNumber, inbound.metadata.sequenceNumber)
            assertEquals(message.payload.size, inbound.payload.size)
        }
        runCurrent()
        bridge.sentPackets.forEach { packet ->
            bridge.emitIncoming(packet)
        }
        advanceUntilIdle()
        collector.cancel()
    }

    private fun message(payloadSize: Int): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.POSITION_UPDATE,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 1_000L,
                sequenceNumber = 10,
                ttl = 3,
                priority = P2PPriority.LOW,
                deliveryMode = P2PDeliveryMode.BEST_EFFORT,
            ),
            payload = ByteArray(payloadSize) { 0x2A },
        )
    }
}

private class FakeBleBridgeClient : BleBridgeClient {
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 32)
    val sentPackets = mutableListOf<ByteArray>()

    override suspend fun sendPacket(peerId: String?, packet: ByteArray): Result<Unit> {
        sentPackets += packet
        return Result.success(Unit)
    }

    override fun incomingPackets(): Flow<ByteArray> = incoming

    override fun connectedPeers(): Set<String> = setOf("ble-1")

    suspend fun emitIncoming(packet: ByteArray) {
        incoming.emit(packet)
    }
}
