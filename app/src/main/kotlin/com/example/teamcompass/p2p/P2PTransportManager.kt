package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PDeliveryMode
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.ReplayProtector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class PayloadTooLargeException(
    val transportName: String,
    val payloadSizeBytes: Int,
    val maxPayloadBytes: Int,
) : IllegalArgumentException(
    "message payload ($payloadSizeBytes bytes) exceeds $transportName limit ($maxPayloadBytes bytes)",
)

class ReliabilityNotSupportedException(
    val transportName: String,
) : IllegalStateException(
    "transport $transportName does not support reliable delivery",
)

enum class TransportDispatchStatus {
    SENT,
    FAILED,
    SKIPPED_PAYLOAD_TOO_LARGE,
    SKIPPED_RELIABILITY_NOT_SUPPORTED,
}

data class TransportDispatchOutcome(
    val transportName: String,
    val status: TransportDispatchStatus,
    val error: Throwable? = null,
)

data class P2PDispatchReport(
    val outcomes: List<TransportDispatchOutcome>,
) {
    val sentCount: Int
        get() = outcomes.count { it.status == TransportDispatchStatus.SENT }

    val failedCount: Int
        get() = outcomes.count { it.status == TransportDispatchStatus.FAILED }

    val isSuccessful: Boolean
        get() = sentCount > 0 && failedCount == 0
}

data class P2PInboundMessage(
    val transportName: String,
    val message: P2PMessage,
)

class P2PTransportManager(
    private val transportRegistry: P2PTransportRegistry,
    private val replayProtector: ReplayProtector = ReplayProtector(),
) {

    suspend fun sendToPeer(peerId: String, message: P2PMessage): P2PDispatchReport {
        require(peerId.isNotBlank()) { "peerId must not be blank" }
        val outcomes = transportRegistry.snapshot().map { transport ->
            dispatchToTransport(
                transport = transport,
                message = message,
                action = { send(peerId = peerId, message = message) },
            )
        }
        return P2PDispatchReport(outcomes)
    }

    suspend fun broadcast(message: P2PMessage): P2PDispatchReport {
        val outcomes = transportRegistry.snapshot().map { transport ->
            dispatchToTransport(
                transport = transport,
                message = message,
                action = { broadcast(message) },
            )
        }
        return P2PDispatchReport(outcomes)
    }

    fun receiveAll(): Flow<P2PInboundMessage> {
        val transports = transportRegistry.snapshot()
        if (transports.isEmpty()) return emptyFlow()

        val flows = transports.map { transport ->
            transport.receive()
                .filter { message ->
                    replayProtector.shouldAccept(
                        senderId = message.metadata.senderId,
                        sequenceNumber = message.metadata.sequenceNumber,
                    )
                }
                .map { message ->
                    P2PInboundMessage(
                        transportName = transport.name,
                        message = message,
                    )
                }
        }
        return merge(*flows.toTypedArray())
    }

    fun connectedPeers(): Set<String> {
        return transportRegistry.snapshot()
            .flatMapTo(mutableSetOf()) { transport -> transport.connectedPeers().toList() }
    }

    private suspend fun dispatchToTransport(
        transport: P2PTransport,
        message: P2PMessage,
        action: suspend P2PTransport.() -> Result<Unit>,
    ): TransportDispatchOutcome {
        if (message.payloadSizeBytes > transport.limits.maxPayloadBytes) {
            return TransportDispatchOutcome(
                transportName = transport.name,
                status = TransportDispatchStatus.SKIPPED_PAYLOAD_TOO_LARGE,
                error = PayloadTooLargeException(
                    transportName = transport.name,
                    payloadSizeBytes = message.payloadSizeBytes,
                    maxPayloadBytes = transport.limits.maxPayloadBytes,
                ),
            )
        }

        if (
            message.metadata.deliveryMode == P2PDeliveryMode.RELIABLE &&
            !transport.capabilities.contains(P2PTransportCapability.ACKS)
        ) {
            return TransportDispatchOutcome(
                transportName = transport.name,
                status = TransportDispatchStatus.SKIPPED_RELIABILITY_NOT_SUPPORTED,
                error = ReliabilityNotSupportedException(transportName = transport.name),
            )
        }

        return action(transport).fold(
            onSuccess = {
                TransportDispatchOutcome(
                    transportName = transport.name,
                    status = TransportDispatchStatus.SENT,
                )
            },
            onFailure = { error ->
                TransportDispatchOutcome(
                    transportName = transport.name,
                    status = TransportDispatchStatus.FAILED,
                    error = error,
                )
            },
        )
    }
}
