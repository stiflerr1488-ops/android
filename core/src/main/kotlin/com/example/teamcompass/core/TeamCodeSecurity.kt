package com.example.teamcompass.core

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

object TeamCodeSecurity {
    private val random = SecureRandom()

    private const val MIN_SALT_BYTES = 8
    private const val SHA256_HEX_LENGTH = 64

    fun generateSaltHex(bytes: Int = 16): String {
        val data = ByteArray(bytes.coerceAtLeast(MIN_SALT_BYTES))
        random.nextBytes(data)
        return data.toHexString()
    }

    fun hashJoinCode(joinCode: String, saltHex: String): String {
        require(saltHex.isHexString()) { "saltHex must be a valid hex string" }
        return hashJoinCodeDigest(joinCode, saltHex).toHexString()
    }

    fun verifyJoinCode(joinCode: String, saltHex: String, expectedHashHex: String): Boolean {
        if (!saltHex.isHexString()) return false
        if (expectedHashHex.length != SHA256_HEX_LENGTH) return false

        val expectedDigest = expectedHashHex.hexToByteArrayOrNull() ?: return false
        val actualDigest = hashJoinCodeDigest(joinCode, saltHex)

        return MessageDigest.isEqual(actualDigest, expectedDigest)
    }

    private fun hashJoinCodeDigest(joinCode: String, saltHex: String): ByteArray {
        val input = "${joinCode.trim()}:$saltHex".toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256").digest(input)
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(Locale.ROOT, byte.toInt() and 0xff) }

    private fun String.isHexString(): Boolean =
        isNotEmpty() && length % 2 == 0 && all { it.isHexDigit() }

    private fun String.hexToByteArrayOrNull(): ByteArray? {
        if (!isHexString()) return null

        val out = ByteArray(length / 2)
        var outIndex = 0
        var i = 0
        while (i < length) {
            val hi = this[i].hexDigitValueOrNegative()
            val lo = this[i + 1].hexDigitValueOrNegative()
            if (hi < 0 || lo < 0) return null
            out[outIndex++] = ((hi shl 4) or lo).toByte()
            i += 2
        }
        return out
    }

    private fun Char.isHexDigit(): Boolean =
        (this in '0'..'9') || (this in 'a'..'f') || (this in 'A'..'F')

    private fun Char.hexDigitValueOrNegative(): Int = when (this) {
        in '0'..'9' -> code - '0'.code
        in 'a'..'f' -> code - 'a'.code + 10
        in 'A'..'F' -> code - 'A'.code + 10
        else -> -1
    }
}
