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
    private data class Account(
        val uid: String,
        val email: String,
        val password: String,
        val displayName: String,
    )

    private val state = MutableStateFlow(initialState)
    private val accountsByEmail = mutableMapOf<String, Account>()

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult {
        val normalized = email.trim().lowercase()
        val account = accountsByEmail[normalized]
            ?: return AuthResult.Failure("Account not found")
        if (account.password != password) {
            return AuthResult.Failure("Invalid email or password")
        }
        val session = UserSession(
            userId = account.uid,
            displayName = account.displayName,
            email = account.email,
            isGuest = false,
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || password.isBlank()) {
            return AuthResult.Failure("Email and password are required", recoverable = true)
        }
        if (accountsByEmail.containsKey(normalized)) {
            return AuthResult.Failure("Account already exists", recoverable = true)
        }
        val resolvedName = resolvedDisplayName(displayName, normalized)
        val account = Account(
            uid = UUID.randomUUID().toString(),
            email = normalized,
            password = password,
            displayName = resolvedName,
        )
        accountsByEmail[normalized] = account
        val session = UserSession(
            userId = account.uid,
            displayName = account.displayName,
            email = account.email,
            isGuest = false,
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun signInGuest(
        displayName: String?,
    ): AuthResult {
        val session = UserSession(
            userId = UUID.randomUUID().toString(),
            displayName = resolvedDisplayName(displayName, null),
            email = null,
            isGuest = true,
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult {
        val current = (state.value as? AuthState.SignedIn)?.session
            ?: return AuthResult.Failure("No signed-in session")
        if (!current.isGuest) {
            return AuthResult.Failure("Current session is not a guest session")
        }
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || password.isBlank()) {
            return AuthResult.Failure("Email and password are required", recoverable = true)
        }
        if (accountsByEmail.containsKey(normalized)) {
            return AuthResult.Failure("Account already exists", recoverable = true)
        }
        val upgraded = Account(
            uid = current.userId,
            email = normalized,
            password = password,
            displayName = resolvedDisplayName(displayName, normalized, fallback = current.displayName),
        )
        accountsByEmail[normalized] = upgraded
        val session = UserSession(
            userId = upgraded.uid,
            displayName = upgraded.displayName,
            email = upgraded.email,
            isGuest = false,
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentSession(): UserSession? = (state.value as? AuthState.SignedIn)?.session

    private fun resolvedDisplayName(
        displayName: String?,
        email: String?,
        fallback: String = "Operator",
    ): String {
        val trimmed = displayName?.trim().orEmpty()
        if (trimmed.isNotEmpty()) return trimmed
        val emailPrefix = email?.substringBefore('@').orEmpty().trim()
        if (emailPrefix.isNotEmpty()) return emailPrefix
        return fallback
    }
}

