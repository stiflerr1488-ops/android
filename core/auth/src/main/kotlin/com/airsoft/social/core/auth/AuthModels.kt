package com.airsoft.social.core.auth

import com.airsoft.social.core.model.UserSession

sealed interface AuthResult {
    data class Success(val session: UserSession) : AuthResult
    data class Failure(val reason: String, val recoverable: Boolean = true) : AuthResult
}

