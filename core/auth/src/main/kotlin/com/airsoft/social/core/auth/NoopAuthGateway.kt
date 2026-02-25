package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object NoopAuthGateway : AuthGateway {
    override val authState: Flow<AuthState> = flowOf(AuthState.SignedOut)

    override suspend fun signIn(request: SignInRequest): AuthResult =
        AuthResult.Failure(reason = "Auth provider is not configured")

    override suspend fun signOut() = Unit

    override suspend fun currentSession(): UserSession? = null
}

