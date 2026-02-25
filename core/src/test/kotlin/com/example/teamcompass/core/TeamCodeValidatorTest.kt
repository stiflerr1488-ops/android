package com.example.teamcompass.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TeamCodeValidatorTest {

    @Test
    fun isValid_accepts_exact_six_digits() {
        assertTrue(TeamCodeValidator.isValid("123456"))
        assertTrue(TeamCodeValidator.isValid("000001"))
    }

    @Test
    fun isValid_rejects_invalid_values() {
        assertFalse(TeamCodeValidator.isValid(null))
        assertFalse(TeamCodeValidator.isValid(""))
        assertFalse(TeamCodeValidator.isValid("12345"))
        assertFalse(TeamCodeValidator.isValid("1234567"))
        assertFalse(TeamCodeValidator.isValid("12A456"))
        assertFalse(TeamCodeValidator.isValid(" 123456 "))
    }

    @Test
    fun normalize_returns_code_only_when_valid() {
        assertEquals("654321", TeamCodeValidator.normalize("654321"))
        assertNull(TeamCodeValidator.normalize("65432"))
        assertNull(TeamCodeValidator.normalize("65A321"))
    }
}
