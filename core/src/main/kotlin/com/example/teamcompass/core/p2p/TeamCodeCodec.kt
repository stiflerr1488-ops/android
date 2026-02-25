package com.example.teamcompass.core.p2p

import com.example.teamcompass.core.TeamCodeValidator

object TeamCodeCodec {
    const val COMPRESSED_SIZE_BYTES: Int = 3

    fun compress(teamCode: String): ByteArray {
        require(TeamCodeValidator.isValid(teamCode)) { "teamCode must be 6 digits" }
        val value = teamCode.toInt()
        return byteArrayOf(
            ((value ushr 16) and 0xFF).toByte(),
            ((value ushr 8) and 0xFF).toByte(),
            (value and 0xFF).toByte(),
        )
    }

    fun decompress(bytes: ByteArray): String {
        require(bytes.size == COMPRESSED_SIZE_BYTES) {
            "compressed team code must be exactly $COMPRESSED_SIZE_BYTES bytes"
        }
        val value = ((bytes[0].toInt() and 0xFF) shl 16) or
            ((bytes[1].toInt() and 0xFF) shl 8) or
            (bytes[2].toInt() and 0xFF)
        require(value in 0..P2PProtocolLimits.TEAM_CODE_INT_MAX) {
            "decoded value out of team code range"
        }
        return value.toString().padStart(6, '0')
    }
}
