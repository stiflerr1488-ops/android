package com.example.teamcompass.core.p2p

import kotlinx.coroutines.flow.Flow

enum class P2PTransportCapability {
    BROADCAST,
    MESH_RELAY,
    ACKS,
}

data class P2PTransportLimits(
    val maxPayloadBytes: Int,
    val recommendedPayloadBytes: Int = maxPayloadBytes,
) {
    init {
        require(maxPayloadBytes > 0) { "maxPayloadBytes must be > 0" }
        require(recommendedPayloadBytes > 0) { "recommendedPayloadBytes must be > 0" }
        require(recommendedPayloadBytes <= maxPayloadBytes) {
            "recommendedPayloadBytes must be <= maxPayloadBytes"
        }
    }
}

interface P2PTransport {
    val name: String
    val limits: P2PTransportLimits
    val capabilities: Set<P2PTransportCapability>

    suspend fun send(peerId: String, message: P2PMessage): Result<Unit>
    suspend fun broadcast(message: P2PMessage): Result<Unit>
    fun receive(): Flow<P2PMessage>
    fun connectedPeers(): Set<String>
}

fun P2PTransport.supports(capability: P2PTransportCapability): Boolean {
    return capabilities.contains(capability)
}
