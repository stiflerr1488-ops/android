package com.example.teamcompass.core.p2p

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object P2PSecurityLimits {
    const val MIN_TAG_SIZE_BYTES: Int = 8
    const val DEFAULT_TAG_SIZE_BYTES: Int = 16
    const val MAX_TAG_SIZE_BYTES: Int = 32
}

interface MessageSigner {
    fun sign(data: ByteArray): ByteArray
    fun verify(data: ByteArray, signature: ByteArray): Boolean
}

interface MessageAuthenticator {
    val algorithm: String
    fun computeTag(data: ByteArray, key: ByteArray, tagSizeBytes: Int = P2PSecurityLimits.DEFAULT_TAG_SIZE_BYTES): ByteArray
    fun verifyTag(data: ByteArray, key: ByteArray, expectedTag: ByteArray): Boolean
}

class HmacSha256MessageAuthenticator : MessageAuthenticator {
    override val algorithm: String = "HmacSHA256"

    override fun computeTag(data: ByteArray, key: ByteArray, tagSizeBytes: Int): ByteArray {
        require(key.isNotEmpty()) { "key must not be empty" }
        require(tagSizeBytes in P2PSecurityLimits.MIN_TAG_SIZE_BYTES..P2PSecurityLimits.MAX_TAG_SIZE_BYTES) {
            "tagSizeBytes must be in ${P2PSecurityLimits.MIN_TAG_SIZE_BYTES}.." +
                P2PSecurityLimits.MAX_TAG_SIZE_BYTES
        }
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        val fullTag = mac.doFinal(data)
        return fullTag.copyOf(tagSizeBytes)
    }

    override fun verifyTag(data: ByteArray, key: ByteArray, expectedTag: ByteArray): Boolean {
        if (expectedTag.size !in P2PSecurityLimits.MIN_TAG_SIZE_BYTES..P2PSecurityLimits.MAX_TAG_SIZE_BYTES) {
            return false
        }
        val actualTag = computeTag(data = data, key = key, tagSizeBytes = expectedTag.size)
        return MessageDigest.isEqual(actualTag, expectedTag)
    }
}

object P2PMessageCanonicalizer {
    fun canonicalBytes(message: P2PMessage): ByteArray {
        val stream = ByteArrayOutputStream()
        DataOutputStream(stream).use { output ->
            val metadata = message.metadata
            output.writeInt(metadata.version)
            output.writeInt(metadata.type.ordinal)
            output.writeUTF(metadata.senderId)
            output.writeUTF(metadata.teamCode)
            output.writeLong(metadata.timestampMs)
            output.writeInt(metadata.sequenceNumber)
            output.writeInt(metadata.ttl)
            output.writeInt(metadata.priority.ordinal)
            output.writeInt(metadata.deliveryMode.ordinal)
            output.writeInt(message.payload.size)
            output.write(message.payload)
        }
        return stream.toByteArray()
    }
}

object P2PMessageAuthenticator {
    fun attachTag(
        message: P2PMessage,
        key: ByteArray,
        authenticator: MessageAuthenticator = HmacSha256MessageAuthenticator(),
        tagSizeBytes: Int = P2PSecurityLimits.DEFAULT_TAG_SIZE_BYTES,
    ): P2PMessage {
        val canonical = P2PMessageCanonicalizer.canonicalBytes(message)
        val tag = authenticator.computeTag(
            data = canonical,
            key = key,
            tagSizeBytes = tagSizeBytes,
        )
        return message.copy(signature = tag)
    }

    fun verifyTag(
        message: P2PMessage,
        key: ByteArray,
        authenticator: MessageAuthenticator = HmacSha256MessageAuthenticator(),
    ): Boolean {
        if (message.signature.isEmpty()) return false
        val canonical = P2PMessageCanonicalizer.canonicalBytes(message.copy(signature = ByteArray(0)))
        return authenticator.verifyTag(
            data = canonical,
            key = key,
            expectedTag = message.signature,
        )
    }
}
