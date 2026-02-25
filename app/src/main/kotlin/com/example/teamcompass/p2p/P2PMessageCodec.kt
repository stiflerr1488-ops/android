package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PDeliveryMode
import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PMessageMetadata
import com.example.teamcompass.core.p2p.P2PMessageType
import com.example.teamcompass.core.p2p.P2PPriority
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

interface P2PMessageCodec {
    fun encode(message: P2PMessage): ByteArray
    fun decode(bytes: ByteArray): Result<P2PMessage>
}

class BinaryP2PMessageCodec : P2PMessageCodec {

    override fun encode(message: P2PMessage): ByteArray {
        val stream = ByteArrayOutputStream()
        DataOutputStream(stream).use { out ->
            val meta = message.metadata
            out.writeInt(meta.version)
            out.writeInt(meta.type.ordinal)
            out.writeUTF(meta.senderId)
            out.writeUTF(meta.teamCode)
            out.writeLong(meta.timestampMs)
            out.writeInt(meta.sequenceNumber)
            out.writeInt(meta.ttl)
            out.writeInt(meta.priority.ordinal)
            out.writeInt(meta.deliveryMode.ordinal)
            out.writeInt(message.payload.size)
            out.write(message.payload)
            out.writeInt(message.signature.size)
            out.write(message.signature)
        }
        return stream.toByteArray()
    }

    override fun decode(bytes: ByteArray): Result<P2PMessage> {
        return runCatching {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                val version = input.readInt()
                val typeOrdinal = input.readInt()
                val senderId = input.readUTF()
                val teamCode = input.readUTF()
                val timestampMs = input.readLong()
                val sequenceNumber = input.readInt()
                val ttl = input.readInt()
                val priorityOrdinal = input.readInt()
                val deliveryOrdinal = input.readInt()

                val payloadSize = input.readInt()
                require(payloadSize > 0) { "payloadSize must be > 0" }
                val payload = ByteArray(payloadSize)
                input.readFully(payload)

                val signatureSize = input.readInt()
                require(signatureSize >= 0) { "signatureSize must be >= 0" }
                val signature = ByteArray(signatureSize)
                input.readFully(signature)

                val type = P2PMessageType.entries.getOrNull(typeOrdinal)
                    ?: throw IllegalArgumentException("Unknown message type ordinal=$typeOrdinal")
                val priority = P2PPriority.entries.getOrNull(priorityOrdinal)
                    ?: throw IllegalArgumentException("Unknown priority ordinal=$priorityOrdinal")
                val deliveryMode = P2PDeliveryMode.entries.getOrNull(deliveryOrdinal)
                    ?: throw IllegalArgumentException("Unknown delivery ordinal=$deliveryOrdinal")

                val metadata = P2PMessageMetadata(
                    version = version,
                    type = type,
                    senderId = senderId,
                    teamCode = teamCode,
                    timestampMs = timestampMs,
                    sequenceNumber = sequenceNumber,
                    ttl = ttl,
                    priority = priority,
                    deliveryMode = deliveryMode,
                )
                P2PMessage(
                    metadata = metadata,
                    payload = payload,
                    signature = signature,
                )
            }
        }
    }
}
