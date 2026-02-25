package com.example.teamcompass.core

object TeamCodeValidator {
    private val TEAM_CODE_REGEX = Regex("^\\d{6}$")

    fun isValid(code: String?): Boolean = code?.matches(TEAM_CODE_REGEX) == true

    fun normalize(code: String?): String? = code?.takeIf(::isValid)
}
