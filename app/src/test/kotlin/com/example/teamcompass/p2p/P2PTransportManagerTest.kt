package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PDeliveryMode
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PMessageMetadata
import com.example.teamcompass.core.p2p.P2PMessageType
import com.example.teamcompass.core.p2p.P2PPriority
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.P2PTransportLimits
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class P2PTransportManagerTest {

    @Test
    fun broadcast_reliable_message_skips_transports_without_acks() = runTest {
        val ackTransport = FakeTransport(
            name = "ack",
            limits = P2PTransportLimits(maxPayloadBytes = 256),
            capabilities = setOf(P2PTransportCapability.BROADCAST, P2PTransportCapability.ACKS),
        )
        val nonAckTransport = FakeTransport(
            name = "nonack",
            limits = P2PTransportLimits(maxPayloadBytes = 256),
            capabilities = setOf(P2PTransportCapability.BROADCAST),
        )
        val registry = P2PTransportRegistry(listOf(ackTransport, nonAckTransport))
        val manager = P2PTransportManager(registry)

        val report = manager.broadcast(reliableMessage(sequenceNumber = 1))
        val statuses = report.outcomes.associate { it.transportName to it.status }

        assertEquals(1, ackTransport.broadcastCalls)
        assertEquals(0, nonAckTransport.broadcastCalls)
        assertEquals(TransportDispatchStatus.SENT, statuses.getValue("ack"))
        assertEquals(
            TransportDispatchStatus.SKIPPED_RELIABILITY_NOT_SUPPORTED,
            statuses.getValue("nonack"),
        )
    }

    @Test
    fun broadcast_oversized_payload_is_skipped() = runTest {
        val transport = FakeTransport(
            name = "lora",
            limits = P2PTransportLimits(maxPayloadBytes = 32),
            capabilities = setOf(P2PTransportCapability.BROADCAST, P2PTransportCapability.ACKS),
        )
        val manager = P2PTransportManager(P2PTransportRegistry(listOf(transport)))

        val payload = ByteArray(48) { 1 }
        val report = manager.broadcast(bestEffortMessage(sequenceNumber = 2, payload = payload))

        assertEquals(0, transport.broadcastCalls)
        assertEquals(TransportDispatchStatus.SKIPPED_PAYLOAD_TOO_LARGE, report.outcomes.single().status)
    }

    @Test
    fun receiveAll_deduplicates_messages_from_multiple_transports() = runTest {
        val t1 = FakeTransport(name = "ble")
        val t2 = FakeTransport(name = "wifi")
        val manager = P2PTransportManager(P2PTransportRegistry(listOf(t1, t2)))
        val received = mutableListOf<P2PInboundMessage>()

        val collectJob = launch {
            manager.receiveAll()
                .take(1)
                .toList(received)
        }
        runCurrent()
        val duplicate = bestEffortMessage(sequenceNumber = 77)
        t1.emitIncoming(duplicate)
        t2.emitIncoming(duplicate)

        advanceUntilIdle()

        assertEquals(1, received.size)
        assertEquals("uid-1", received.single().message.metadata.senderId)
        collectJob.cancel()
    }

    @Test
    fun connectedPeers_returns_union_from_all_transports() {
        val t1 = FakeTransport(name = "ble", peers = setOf("p1", "p2"))
        val t2 = FakeTransport(name = "wifi", peers = setOf("p2", "p3"))
        val manager = P2PTransportManager(P2PTransportRegistry(listOf(t1, t2)))

        assertEquals(setOf("p1", "p2", "p3"), manager.connectedPeers())
    }

    @Test
    fun sendToPeer_reports_transport_failures() = runTest {
        val t1 = FakeTransport(name = "ble", failSend = true)
        val t2 = FakeTransport(name = "wifi")
        val manager = P2PTransportManager(P2PTransportRegistry(listOf(t1, t2)))

        val report = manager.sendToPeer(
            peerId = "peer-1",
            message = bestEffortMessage(sequenceNumber = 9),
        )
        val statuses = report.outcomes.associate { it.transportName to it.status }

        assertEquals(TransportDispatchStatus.FAILED, statuses.getValue("ble"))
        assertEquals(TransportDispatchStatus.SENT, statuses.getValue("wifi"))
        assertEquals(1, report.failedCount)
        assertTrue(report.sentCount >= 1)
    }

    private fun reliableMessage(sequenceNumber: Int): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.SOS_SIGNAL,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 1_000L,
                sequenceNumber = sequenceNumber,
                ttl = 3,
                priority = P2PPriority.CRITICAL,
                deliveryMode = P2PDeliveryMode.RELIABLE,
            ),
            payload = byteArrayOf(1, 2, 3),
        )
    }

    private fun bestEffortMessage(
        sequenceNumber: Int,
        payload: ByteArray = byteArrayOf(1, 2, 3),
    ): P2PMessage {
        return P2PMessage(
            metadata = P2PMessageMetadata(
                type = P2PMessageType.POSITION_UPDATE,
                senderId = "uid-1",
                teamCode = "123456",
                timestampMs = 2_000L,
                sequenceNumber = sequenceNumber,
                ttl = 3,
                priority = P2PPriority.LOW,
                deliveryMode = P2PDeliveryMode.BEST_EFFORT,
            ),
            payload = payload,
        )
    }
}

private class FakeTransport(
    override val name: String,
    override val limits: P2PTransportLimits = P2PTransportLimits(maxPayloadBytes = 512),
    override val capabilities: Set<P2PTransportCapability> = setOf(
        P2PTransportCapability.BROADCAST,
        P2PTransportCapability.ACKS,
    ),
    private val peers: Set<String> = emptySet(),
    private val failSend: Boolean = false,
    private val failBroadcast: Boolean = false,
) : P2PTransport {
    private val incoming = MutableSharedFlow<P2PMessage>(extraBufferCapacity = 16)

    var sendCalls: Int = 0
        private set
    var broadcastCalls: Int = 0
        private set

    override suspend fun send(peerId: String, message: P2PMessage): Result<Unit> {
        sendCalls++
        return if (failSend) {
            Result.failure(IllegalStateException("send failed"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun broadcast(message: P2PMessage): Result<Unit> {
        broadcastCalls++
        return if (failBroadcast) {
            Result.failure(IllegalStateException("broadcast failed"))
        } else {
            Result.success(Unit)
        }
    }

    override fun receive(): Flow<P2PMessage> = incoming

    override fun connectedPeers(): Set<String> = peers

    suspend fun emitIncoming(message: P2PMessage) {
        incoming.emit(message)
    }
}
