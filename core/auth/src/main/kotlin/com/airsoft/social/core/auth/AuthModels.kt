package com.airsoft.social.core.auth

import com.airsoft.social.core.model.UserSession

data class SignInRequest(
    val email: String? = null,
    val password: String? = null,
    val displayName: String? = null,
    val providerHint: String? = null,
)

sealed interface AuthResult {
    data class Success(val session: UserSession) : AuthResult
    data class Failure(val reason: String, val recoverable: Boolean = true) : AuthResult
}

