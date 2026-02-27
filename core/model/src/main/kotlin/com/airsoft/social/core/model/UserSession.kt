package com.airsoft.social.core.model

data class UserSession(
    val userId: String,
    val displayName: String,
    val email: String? = null,
    val isGuest: Boolean = false,
    val accountRoles: Set<AccountRole> = emptySet(),
)

