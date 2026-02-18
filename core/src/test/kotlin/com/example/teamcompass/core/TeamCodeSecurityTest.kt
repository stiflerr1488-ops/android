package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TeamCodeSecurityTest {
    @Test
    fun `hash verify succeeds with same code and salt`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", salt)
        assertTrue(TeamCodeSecurity.verifyJoinCode("123456", salt, hash))
    }

    @Test
    fun `hash verify fails with different code`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", salt)
        assertFalse(TeamCodeSecurity.verifyJoinCode("654321", salt, hash))
    }

    @Test
    fun `hash verify fails with malformed expected hash`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        assertFalse(TeamCodeSecurity.verifyJoinCode("123456", salt, "invalid-hex"))
    }

    @Test
    fun `hash verify fails with wrong hash length`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", salt)
        assertFalse(TeamCodeSecurity.verifyJoinCode("123456", salt, hash.dropLast(2)))
    }

    @Test
    fun `hash verify accepts upper-case expected hash`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", salt).uppercase()
        assertTrue(TeamCodeSecurity.verifyJoinCode("123456", salt, hash))
    }

    @Test
    fun `hash verify trims join code input`() {
        val salt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", salt)
        assertTrue(TeamCodeSecurity.verifyJoinCode(" 123456  ", salt, hash))
    }

    @Test
    fun `hash generation rejects malformed salt`() {
        assertFailsWith<IllegalArgumentException> {
            TeamCodeSecurity.hashJoinCode("123456", "xyz")
        }
    }

    @Test
    fun `hash generation rejects too short but hex salt`() {
        assertFailsWith<IllegalArgumentException> {
            TeamCodeSecurity.hashJoinCode("123456", "a1b2c3d4")
        }
    }

    @Test
    fun `hash verify fails with malformed salt`() {
        val validSalt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", validSalt)
        assertFalse(TeamCodeSecurity.verifyJoinCode("123456", "bad-salt", hash))
    }

    @Test
    fun `hash verify fails with too short but hex salt`() {
        val validSalt = TeamCodeSecurity.generateSaltHex()
        val hash = TeamCodeSecurity.hashJoinCode("123456", validSalt)
        assertFalse(TeamCodeSecurity.verifyJoinCode("123456", "a1b2c3d4", hash))
    }

    @Test
    fun `generate salt enforces minimum length of 8 bytes`() {
        val salt = TeamCodeSecurity.generateSaltHex(bytes = 1)
        assertEquals(16, salt.length)
    }
}
