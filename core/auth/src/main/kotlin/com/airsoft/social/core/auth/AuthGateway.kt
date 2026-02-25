package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthGateway {
    val authState: Flow<AuthState>
    suspend fun signIn(request: SignInRequest): AuthResult
    suspend fun signOut()
    suspend fun currentSession(): UserSession?
}

