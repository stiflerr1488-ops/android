package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthGateway {
    val authState: Flow<AuthState>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String? = null,
    ): AuthResult

    suspend fun signInGuest(
        displayName: String? = null,
    ): AuthResult

    suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        displayName: String? = null,
    ): AuthResult

    suspend fun signOut()
    suspend fun currentSession(): UserSession?
}

