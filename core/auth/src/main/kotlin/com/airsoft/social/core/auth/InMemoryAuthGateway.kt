package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthGateway(
    initialState: AuthState = AuthState.SignedOut,
) : AuthGateway {
    private val state = MutableStateFlow(initialState)

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun signIn(request: SignInRequest): AuthResult {
        val displayName = request.displayName?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: request.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
            ?: "Operator"
        val session = UserSession(
            userId = UUID.randomUUID().toString(),
            displayName = displayName,
            email = request.email,
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentSession(): UserSession? = (state.value as? AuthState.SignedIn)?.session
}

