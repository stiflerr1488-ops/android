package com.airsoft.social.feature.auth.impl

import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.data.SESSION_ERROR_INVALID_EMAIL
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `prefills callsign and enables upgrade flow for guest session`() = runTest {
        val repository = FakeSessionRepository(
            initialState = AuthState.SignedIn(
                UserSession(
                    userId = "guest-id",
                    displayName = "Teiwaz_",
                ),
            ),
        )
        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Teiwaz_", state.callsign)
        assertTrue(state.isUpgradeFlow)
    }

    @Test
    fun `upgrade flow is false for signed out and true only for guest signed in`() = runTest {
        val repository = FakeSessionRepository(initialState = AuthState.SignedOut)
        val viewModel = AuthViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUpgradeFlow)
        assertFalse(viewModel.uiState.value.isRegisteredAccount)

        repository.emitState(
            AuthState.SignedIn(
                UserSession(
                    userId = "guest-id",
                    displayName = "Ghost",
                    email = null,
                ),
            ),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isUpgradeFlow)
        assertFalse(viewModel.uiState.value.isRegisteredAccount)

        repository.emitState(
            AuthState.SignedIn(
                UserSession(
                    userId = "reg-id",
                    displayName = "Ghost",
                    email = "ghost@example.com",
                ),
            ),
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isUpgradeFlow)
        assertTrue(viewModel.uiState.value.isRegisteredAccount)
    }

    @Test
    fun `continue as guest validates callsign length`() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.CallsignChanged("ab"))
        viewModel.onAction(AuthAction.ContinueAsGuestClicked)
        advanceUntilIdle()

        assertEquals("auth_error_invalid_callsign", viewModel.uiState.value.errorMessage)
        assertEquals(0, repository.continueAsGuestCalls)
    }

    @Test
    fun `register validates email format`() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.CallsignChanged("Ghost"))
        viewModel.onAction(AuthAction.EmailChanged("invalid-email"))
        viewModel.onAction(AuthAction.RegisterByEmailClicked)
        advanceUntilIdle()

        assertEquals("auth_error_invalid_email", viewModel.uiState.value.errorMessage)
        assertEquals(0, repository.registerByEmailCalls)
    }

    @Test
    fun `continue as guest submits to repository`() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.CallsignChanged("Ghost"))
        viewModel.onAction(AuthAction.ContinueAsGuestClicked)
        advanceUntilIdle()

        assertEquals(1, repository.continueAsGuestCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `register failure is shown in ui state`() = runTest {
        val repository = FakeSessionRepository().apply {
            registerResult = AuthResult.Failure("email уже используется")
        }
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.CallsignChanged("Ghost"))
        viewModel.onAction(AuthAction.EmailChanged("ghost@example.com"))
        viewModel.onAction(AuthAction.RegisterByEmailClicked)
        advanceUntilIdle()

        assertEquals(1, repository.registerByEmailCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("email уже используется", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `session invalid email reason maps to auth email error key`() = runTest {
        val repository = FakeSessionRepository().apply {
            registerResult = AuthResult.Failure(SESSION_ERROR_INVALID_EMAIL)
        }
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.CallsignChanged("Ghost"))
        viewModel.onAction(AuthAction.EmailChanged("ghost@example.com"))
        viewModel.onAction(AuthAction.RegisterByEmailClicked)
        advanceUntilIdle()

        assertEquals("auth_error_invalid_email", viewModel.uiState.value.errorMessage)
    }
}

private class FakeSessionRepository(
    initialState: AuthState = AuthState.SignedOut,
) : SessionRepository {
    private val state = MutableStateFlow(initialState)

    var continueAsGuestCalls: Int = 0
    var registerByEmailCalls: Int = 0

    var continueResult: AuthResult = AuthResult.Success(
        UserSession(
            userId = "guest",
            displayName = "Ghost",
        ),
    )
    var registerResult: AuthResult = AuthResult.Success(
        UserSession(
            userId = "registered",
            displayName = "Ghost",
            email = "ghost@example.com",
            accountRoles = setOf(AccountRole.USER),
        ),
    )

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun continueAsGuest(callsign: String): AuthResult {
        continueAsGuestCalls += 1
        if (continueResult is AuthResult.Success) {
            state.value = AuthState.SignedIn(
                (continueResult as AuthResult.Success).session.copy(displayName = callsign),
            )
        }
        return continueResult
    }

    override suspend fun registerWithEmail(
        callsign: String,
        email: String,
        password: String?,
    ): AuthResult {
        registerByEmailCalls += 1
        if (registerResult is AuthResult.Success) {
            state.value = AuthState.SignedIn(
                (registerResult as AuthResult.Success).session.copy(
                    displayName = callsign,
                    email = email,
                ),
            )
        }
        return registerResult
    }

    override suspend fun signInMock(callsign: String): AuthResult = continueAsGuest(callsign)

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentAuthState(): AuthState = state.value

    fun emitState(newState: AuthState) {
        state.value = newState
    }
}
