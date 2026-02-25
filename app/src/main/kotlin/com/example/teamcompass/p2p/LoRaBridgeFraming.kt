package com.example.teamcompass.p2p

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.CRC32

private const val LORA_MAGIC: Int = 0xAA55
private const val LORA_VERSION: Int = 1

enum class LoRaFrameType(val wireCode: Int) {
    DATA(1),
    ACK(2),
    HEARTBEAT(3),
    UNKNOWN(255),
    ;

    companion object {
        fun fromWireCode(code: Int): LoRaFrameType {
            return entries.firstOrNull { it.wireCode == code } ?: UNKNOWN
        }
    }
}

data class LoRaBridgeFrame(
    val type: LoRaFrameType,
    val sequenceNumber: Int,
    val payload: ByteArray,
)

object LoRaBridgeFramer {
    fun encode(frame: LoRaBridgeFrame): ByteArray {
        val payload = frame.payload
        val payloadLength = payload.size
        require(payloadLength >= 0) { "payloadLength must be >= 0" }

        val bodyStream = ByteArrayOutputStream()
        DataOutputStream(bodyStream).use { out ->
            out.writeShort(LORA_MAGIC)
            out.writeByte(LORA_VERSION)
            out.writeByte(frame.type.wireCode)
            out.writeInt(frame.sequenceNumber)
            out.writeShort(payloadLength)
            out.write(payload)
        }
        val bodyBytes = bodyStream.toByteArray()

        val crc32 = CRC32().apply { update(bodyBytes) }.value.toInt()
        val frameStream = ByteArrayOutputStream()
        DataOutputStream(frameStream).use { out ->
            out.write(bodyBytes)
            out.writeInt(crc32)
        }
        return frameStream.toByteArray()
    }

    fun decode(frameBytes: ByteArray): Result<LoRaBridgeFrame> {
        return runCatching {
            require(frameBytes.size >= 2 + 1 + 1 + 4 + 2 + 4) { "frame is too short" }

            val payloadAndHeaderSize = frameBytes.size - 4
            val payloadAndHeader = frameBytes.copyOfRange(0, payloadAndHeaderSize)
            val expectedCrc = CRC32().apply { update(payloadAndHeader) }.value.toInt()

            DataInputStream(ByteArrayInputStream(frameBytes)).use { input ->
                val magic = input.readUnsignedShort()
                require(magic == LORA_MAGIC) { "invalid LoRa frame magic=$magic" }
                val version = input.readUnsignedByte()
                require(version == LORA_VERSION) { "unsupported LoRa frame version=$version" }

                val typeWire = input.readUnsignedByte()
                val type = LoRaFrameType.fromWireCode(typeWire)
                val sequenceNumber = input.readInt()
                val payloadLength = input.readUnsignedShort()
                val payload = ByteArray(payloadLength)
                input.readFully(payload)

                val actualCrc = input.readInt()
                require(actualCrc == expectedCrc) { "LoRa frame CRC mismatch" }

                LoRaBridgeFrame(
                    type = type,
                    sequenceNumber = sequenceNumber,
                    payload = payload,
                )
            }
        }
    }
}
