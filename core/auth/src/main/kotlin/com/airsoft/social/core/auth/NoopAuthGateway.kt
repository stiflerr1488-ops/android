package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object NoopAuthGateway : AuthGateway {
    override val authState: Flow<AuthState> = flowOf(AuthState.SignedOut)

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult =
        AuthResult.Failure(reason = "Auth provider is not configured")

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult =
        AuthResult.Failure(reason = "Auth provider is not configured")

    override suspend fun signInGuest(
        displayName: String?,
    ): AuthResult =
        AuthResult.Failure(reason = "Auth provider is not configured")

    override suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult =
        AuthResult.Failure(reason = "Auth provider is not configured")

    override suspend fun signOut() = Unit

    override suspend fun currentSession(): UserSession? = null
}

