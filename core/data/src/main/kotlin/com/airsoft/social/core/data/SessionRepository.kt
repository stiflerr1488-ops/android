package com.airsoft.social.core.data

import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.datastore.SessionLocalDataSource
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSummary
import com.airsoft.social.core.telemetry.TelemetryReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

interface SessionRepository {
    val authState: Flow<AuthState>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult

    suspend fun registerWithEmail(
        email: String,
        password: String,
        callsign: String? = null,
    ): AuthResult

    suspend fun signInGuest(
        callsign: String? = null,
    ): AuthResult

    suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        callsign: String? = null,
    ): AuthResult

    suspend fun signOut()
    suspend fun currentAuthState(): AuthState
}

class DefaultSessionRepository(
    private val authGateway: AuthGateway,
    private val localDataSource: SessionLocalDataSource,
    private val telemetryReporter: TelemetryReporter,
    scope: CoroutineScope,
) : SessionRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val authState: StateFlow<AuthState> = authGateway.authState
        .transformLatest { state ->
            when (state) {
                is AuthState.SignedIn -> {
                    localDataSource.setLastKnownUser(
                        UserSummary(
                            id = state.session.userId,
                            callsign = state.session.displayName,
                        ),
                    )
                    telemetryReporter.setUser(state.session.userId)
                    emit(state)
                }

                AuthState.SignedOut -> {
                    telemetryReporter.setUser(null)
                    emit(state)
                }

                AuthState.Unknown -> {
                    emit(state)
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Unknown,
        )

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult {
        val result = authGateway.signInWithEmail(email = email, password = password)
        if (result is AuthResult.Success) {
            telemetryReporter.logEvent("new_shell_sign_in", mapOf("provider" to "email"))
        }
        return result
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        callsign: String?,
    ): AuthResult {
        val result = authGateway.registerWithEmail(
            email = email,
            password = password,
            displayName = callsign,
        )
        if (result is AuthResult.Success) {
            telemetryReporter.logEvent("new_shell_register", mapOf("provider" to "email"))
        }
        return result
    }

    override suspend fun signInGuest(
        callsign: String?,
    ): AuthResult {
        val result = authGateway.signInGuest(displayName = callsign)
        if (result is AuthResult.Success) {
            telemetryReporter.logEvent("new_shell_sign_in", mapOf("provider" to "guest"))
        }
        return result
    }

    override suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        callsign: String?,
    ): AuthResult {
        val result = authGateway.upgradeGuestToEmail(
            email = email,
            password = password,
            displayName = callsign,
        )
        if (result is AuthResult.Success) {
            telemetryReporter.logEvent("new_shell_guest_upgrade", mapOf("provider" to "email"))
        }
        return result
    }

    override suspend fun signOut() {
        authGateway.signOut()
        telemetryReporter.logEvent("new_shell_sign_out")
    }

    override suspend fun currentAuthState(): AuthState =
        authGateway.currentSession()?.let(AuthState::SignedIn) ?: AuthState.SignedOut
}
