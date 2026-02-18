package com.example.teamcompass.core

import java.security.MessageDigest
import java.security.SecureRandom

object TeamCodeSecurity {
    private val random = SecureRandom()

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
        return hashJoinCode(joinCode, saltHex).equals(expectedHashHex, ignoreCase = true)
    }
}
