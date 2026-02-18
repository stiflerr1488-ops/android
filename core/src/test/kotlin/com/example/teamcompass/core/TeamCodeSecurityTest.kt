package com.example.teamcompass.core

import kotlin.test.Test
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
}
