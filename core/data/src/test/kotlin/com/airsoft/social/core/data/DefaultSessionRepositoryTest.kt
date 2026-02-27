package com.airsoft.social.core.data

import com.airsoft.social.core.auth.InMemoryAuthGateway
import com.airsoft.social.core.datastore.SessionLocalDataSource
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSummary
import com.airsoft.social.core.telemetry.NoopTelemetryReporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSessionLocalDataSource : SessionLocalDataSource {
    private val state = MutableStateFlow<UserSummary?>(null)

    override fun observeLastKnownUser(): Flow<UserSummary?> = state

    override suspend fun setLastKnownUser(user: UserSummary?) {
        state.value = user
    }
}

class DefaultSessionRepositoryTest {
    @Test
    fun `signInGuest emits signed in state`() = runTest {
        val auth = InMemoryAuthGateway()
        val local = FakeSessionLocalDataSource()
        val repository = DefaultSessionRepository(
            authGateway = auth,
            localDataSource = local,
            telemetryReporter = NoopTelemetryReporter(),
            scope = backgroundScope,
        )

        repository.signInGuest("Nomad")
        val state = repository.authState.first { it is AuthState.SignedIn }

        assertTrue(state is AuthState.SignedIn)
        assertEquals("Nomad", (state as AuthState.SignedIn).session.displayName)
    }
}
