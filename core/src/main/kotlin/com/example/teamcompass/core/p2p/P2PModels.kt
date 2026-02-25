package com.example.teamcompass.core.p2p

import com.example.teamcompass.core.TeamCodeValidator

object P2PProtocolLimits {
    const val PROTOCOL_VERSION_V1: Int = 1
    const val TEAM_CODE_INT_MAX: Int = 999_999
    const val BLE_MAX_PAYLOAD_BYTES: Int = 20
    const val WIFI_MAX_PAYLOAD_BYTES: Int = 1_400
    const val LORA_MAX_PAYLOAD_BYTES: Int = 243
    const val MAX_SENDER_ID_LENGTH: Int = 64
    const val DEFAULT_MESH_TTL: Int = 3
    const val MAX_MESH_TTL: Int = 7
}

enum class P2PMessageType {
    POSITION_UPDATE,
    TEAM_STATE_SYNC,
    ENEMY_PING,
    SOS_SIGNAL,
    ROUTING_REQUEST,
    ROUTING_RESPONSE,
    HEARTBEAT,
    ACK,
}

enum class P2PPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW,
}

enum class P2PDeliveryMode {
    RELIABLE,
    BEST_EFFORT,
}

data class P2PTransmissionClass(
    val priority: P2PPriority,
    val deliveryMode: P2PDeliveryMode,
    val maxRetries: Int,
    val relayEnabled: Boolean,
)

object P2PQosPolicy {
    fun forType(type: P2PMessageType): P2PTransmissionClass {
        return when (type) {
            P2PMessageType.SOS_SIGNAL -> P2PTransmissionClass(
                priority = P2PPriority.CRITICAL,
                deliveryMode = P2PDeliveryMode.RELIABLE,
                maxRetries = 5,
                relayEnabled = true,
            )

            P2PMessageType.ROUTING_REQUEST,
            P2PMessageType.ROUTING_RESPONSE -> P2PTransmissionClass(
                priority = P2PPriority.HIGH,
                deliveryMode = P2PDeliveryMode.RELIABLE,
                maxRetries = 3,
                relayEnabled = true,
            )

            P2PMessageType.TEAM_STATE_SYNC,
            P2PMessageType.ENEMY_PING -> P2PTransmissionClass(
                priority = P2PPriority.HIGH,
                deliveryMode = P2PDeliveryMode.RELIABLE,
                maxRetries = 2,
                relayEnabled = true,
            )

            P2PMessageType.ACK -> P2PTransmissionClass(
                priority = P2PPriority.NORMAL,
                deliveryMode = P2PDeliveryMode.BEST_EFFORT,
                maxRetries = 0,
                relayEnabled = false,
            )

            P2PMessageType.POSITION_UPDATE,
            P2PMessageType.HEARTBEAT -> P2PTransmissionClass(
                priority = P2PPriority.LOW,
                deliveryMode = P2PDeliveryMode.BEST_EFFORT,
                maxRetries = 0,
                relayEnabled = true,
            )
        }
    }
}

data class P2PMessageMetadata(
    val version: Int = P2PProtocolLimits.PROTOCOL_VERSION_V1,
    val type: P2PMessageType,
    val senderId: String,
    val teamCode: String,
    val timestampMs: Long,
    val sequenceNumber: Int,
    val ttl: Int = P2PProtocolLimits.DEFAULT_MESH_TTL,
    val priority: P2PPriority = P2PQosPolicy.forType(type).priority,
    val deliveryMode: P2PDeliveryMode = P2PQosPolicy.forType(type).deliveryMode,
) {
    init {
        require(version > 0) { "version must be positive" }
        require(senderId.isNotBlank()) { "senderId must not be blank" }
        require(senderId.length <= P2PProtocolLimits.MAX_SENDER_ID_LENGTH) {
            "senderId must be <= ${P2PProtocolLimits.MAX_SENDER_ID_LENGTH} chars"
        }
        require(TeamCodeValidator.isValid(teamCode)) { "teamCode must be 6 digits" }
        require(timestampMs > 0L) { "timestampMs must be positive" }
        require(sequenceNumber >= 0) { "sequenceNumber must be >= 0" }
        require(ttl in 0..P2PProtocolLimits.MAX_MESH_TTL) {
            "ttl must be in 0..${P2PProtocolLimits.MAX_MESH_TTL}"
        }
    }
}

data class P2PMessage(
    val metadata: P2PMessageMetadata,
    val payload: ByteArray,
    /**
     * Security material attached to payload (signature or authentication tag).
     * The interpretation depends on configured security profile.
     */
    val signature: ByteArray = ByteArray(0),
) {
    init {
        require(payload.isNotEmpty()) { "payload must not be empty" }
    }

    val payloadSizeBytes: Int
        get() = payload.size
}

data class MeshFrame(
    val originalSenderId: String,
    val lastHopId: String,
    val sequenceNumber: Int,
    val ttl: Int,
    val message: P2PMessage,
) {
    init {
        require(originalSenderId.isNotBlank()) { "originalSenderId must not be blank" }
        require(lastHopId.isNotBlank()) { "lastHopId must not be blank" }
        require(sequenceNumber >= 0) { "sequenceNumber must be >= 0" }
        require(ttl >= 0) { "ttl must be >= 0" }
    }
}
