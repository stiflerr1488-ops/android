package com.airsoft.social.core.model

sealed interface AuthState {
    data object Unknown : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val session: UserSession) : AuthState
}

