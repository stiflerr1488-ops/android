package com.airsoft.social.core.data

import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.MessageDeliveryState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstChatsRepositoryTest {

    private fun repository(
        failSend: Boolean = false,
        sessionRepository: SessionRepository? = null,
    ): OfflineFirstChatsRepository {
        val preview = FakeSocialPreviewRepository()
        return OfflineFirstChatsRepository(
            localDataSource = InMemoryChatsLocalDataSource(),
            chatsRemoteDataSource = PreviewChatsRemoteDataSource(preview),
            messagesRemoteDataSource = PreviewMessagesRemoteDataSource(
                previewRepository = preview,
                shouldFailSend = { _, text -> failSend && text.contains("#fail") },
            ),
            sessionRepository = sessionRepository,
        )
    }

    @Test
    fun `returns seeded threads`() = runTest {
        val repository = repository()

        val threads = repository.observeThreads().first()

        assertTrue(threads.isNotEmpty())
        assertEquals("team-ew-general", threads.first().chatId)
    }

    @Test
    fun `returns messages for valid chat`() = runTest {
        val repository = repository()

        val messages = repository.observeMessages("team-ew-general").first()

        assertTrue(messages.isNotEmpty())
        assertEquals("team-ew-general", messages.first().chatId)
    }

    @Test
    fun `send text appends local message and marks sent`() = runTest {
        val repository = repository()

        val result = repository.sendTextMessage("team-ew-general", "Привет от теста")
        val messages = repository.observeMessages("team-ew-general").first()
        val last = messages.last()

        assertTrue(result is AppResult.Success)
        assertEquals("Привет от теста", last.content)
        assertTrue(last.isMine)
        assertEquals(MessageDeliveryState.SENT, last.deliveryState)
    }

    @Test
    fun `send text failure marks message failed`() = runTest {
        val repository = repository(failSend = true)

        val result = repository.sendTextMessage("team-ew-general", "#fail test")
        val messages = repository.observeMessages("team-ew-general").first()
        val failed = messages.lastOrNull()

        assertTrue(result is AppResult.Failure)
        assertNotNull(failed)
        assertEquals(MessageDeliveryState.FAILED, failed?.deliveryState)
    }

    @Test
    fun `mark chat read resets unread count`() = runTest {
        val repository = repository()

        val before = repository.observeThreads().first().first { it.chatId == "team-ew-general" }
        repository.markChatRead("team-ew-general")
        val after = repository.observeThreads().first().first { it.chatId == "team-ew-general" }

        assertTrue(before.unreadCount > 0)
        assertEquals(0, after.unreadCount)
    }

    @Test
    fun `blank message is rejected`() = runTest {
        val repository = repository()

        val result = repository.sendTextMessage("team-ew-general", "   ")

        assertTrue(result is AppResult.Failure)
        val messages = repository.observeMessages("team-ew-general").first()
        assertFalse(messages.any { it.content.isBlank() })
    }

    @Test
    fun `guest session cannot send messages`() = runTest {
        val repository = repository(
            sessionRepository = FakeSessionRepository(
                AuthState.SignedIn(
                    UserSession(
                        userId = "guest-id",
                        displayName = "Ghost",
                    ),
                ),
            ),
        )

        val result = repository.sendTextMessage("team-ew-general", "test message")

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun `registered session can send messages`() = runTest {
        val repository = repository(
            sessionRepository = FakeSessionRepository(
                AuthState.SignedIn(
                    UserSession(
                        userId = "registered-id",
                        displayName = "Teiwaz_",
                        email = "teiwaz@example.com",
                        accountRoles = setOf(AccountRole.USER),
                    ),
                ),
            ),
        )

        val result = repository.sendTextMessage("team-ew-general", "registered message")

        assertTrue(result is AppResult.Success)
    }
}

private class FakeSessionRepository(
    initialState: AuthState = AuthState.SignedOut,
) : SessionRepository {
    private val state = MutableStateFlow(initialState)

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun continueAsGuest(callsign: String): AuthResult {
        val session = UserSession(userId = "guest", displayName = callsign)
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun registerWithEmail(
        callsign: String,
        email: String,
        password: String?,
    ): AuthResult {
        val session = UserSession(
            userId = "registered",
            displayName = callsign,
            email = email,
            accountRoles = setOf(AccountRole.USER),
        )
        state.value = AuthState.SignedIn(session)
        return AuthResult.Success(session)
    }

    override suspend fun signInMock(callsign: String): AuthResult = continueAsGuest(callsign)

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentAuthState(): AuthState = state.value
}
