package com.example.teamcompass.core

import java.security.MessageDigest
import java.security.SecureRandom

object TeamCodeSecurity {
    private val random = SecureRandom()
    private const val SHA256_HEX_LENGTH = 64

    fun generateSaltHex(bytes: Int = 16): String {
        val data = ByteArray(bytes.coerceAtLeast(8))
        random.nextBytes(data)
        return data.joinToString("") { "%02x".format(it) }
    }

    fun hashJoinCode(joinCode: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = "${joinCode.trim()}:$saltHex".toByteArray(Charsets.UTF_8)
        val digest = md.digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyJoinCode(joinCode: String, saltHex: String, expectedHashHex: String): Boolean {
        val expectedDigest = expectedHashHex.hexToByteArrayOrNull() ?: return false
        if (expectedHashHex.length != SHA256_HEX_LENGTH) return false

        val actualDigest = MessageDigest
            .getInstance("SHA-256")
            .digest("${joinCode.trim()}:$saltHex".toByteArray(Charsets.UTF_8))

        return MessageDigest.isEqual(actualDigest, expectedDigest)
    }

    private fun String.hexToByteArrayOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } catch (_: NumberFormatException) {
            null
        }
    }
}
