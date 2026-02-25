package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PChunk
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

private const val CHUNK_MAGIC: Int = 0x5443 // "TC"
private const val CHUNK_VERSION: Int = 1

object ChunkEnvelopeCodec {
    fun encode(chunk: P2PChunk): ByteArray {
        val stream = ByteArrayOutputStream()
        DataOutputStream(stream).use { out ->
            out.writeShort(CHUNK_MAGIC)
            out.writeByte(CHUNK_VERSION)
            out.writeLong(chunk.messageId)
            out.writeInt(chunk.index)
            out.writeInt(chunk.total)
            out.writeInt(chunk.payload.size)
            out.write(chunk.payload)
        }
        return stream.toByteArray()
    }

    fun decode(bytes: ByteArray): Result<P2PChunk> {
        return runCatching {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                val magic = input.readUnsignedShort()
                require(magic == CHUNK_MAGIC) { "Invalid chunk magic=$magic" }
                val version = input.readUnsignedByte()
                require(version == CHUNK_VERSION) { "Unsupported chunk version=$version" }
                val messageId = input.readLong()
                val index = input.readInt()
                val total = input.readInt()
                val payloadSize = input.readInt()
                require(payloadSize >= 0) { "payloadSize must be >= 0" }
                val payload = ByteArray(payloadSize)
                input.readFully(payload)
                P2PChunk(
                    messageId = messageId,
                    index = index,
                    total = total,
                    payload = payload,
                )
            }
        }
    }
}
