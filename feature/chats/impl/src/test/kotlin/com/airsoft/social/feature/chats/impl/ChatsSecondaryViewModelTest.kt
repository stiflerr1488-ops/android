package com.airsoft.social.feature.chats.impl

import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatsSecondaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `chat room viewmodel loads seeded chat`() = runTest {
        val viewModel = ChatRoomViewModel(
            chatsRepository = FakeChatsRepository(),
            sessionRepository = FakeSessionRepository(),
        )

        viewModel.load("team-ew-general")
        advanceUntilIdle()

        assertEquals("team-ew-general", viewModel.uiState.value.chatId)
        assertTrue(viewModel.uiState.value.title.contains("EW"))
        assertTrue(viewModel.uiState.value.messages.isNotEmpty())
    }

    @Test
    fun `chat room toggles sound label`() = runTest {
        val viewModel = ChatRoomViewModel(
            chatsRepository = FakeChatsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        viewModel.load("team-ew-general")
        advanceUntilIdle()

        val before = viewModel.uiState.value.soundLabel
        viewModel.onAction(ChatRoomAction.ToggleSound)

        assertTrue(before != viewModel.uiState.value.soundLabel)
    }

    @Test
    fun `chat room ignores empty submit`() = runTest {
        val viewModel = ChatRoomViewModel(
            chatsRepository = FakeChatsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        viewModel.load("team-ew-general")
        advanceUntilIdle()
        val beforeCount = viewModel.uiState.value.messages.size

        viewModel.onAction(ChatRoomAction.SubmitText("   "))
        advanceUntilIdle()

        assertEquals(beforeCount, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `chat room sends demo message`() = runTest {
        val viewModel = ChatRoomViewModel(
            chatsRepository = FakeChatsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        viewModel.load("team-ew-general")
        advanceUntilIdle()

        viewModel.onAction(ChatRoomAction.SendDemoMessage)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.last().content.contains("shell"))
    }

    @Test
    fun `chat room blocks send for guest account`() = runTest {
        val viewModel = ChatRoomViewModel(
            chatsRepository = FakeChatsRepository(),
            sessionRepository = FakeSessionRepository(registered = false),
        )
        viewModel.load("team-ew-general")
        advanceUntilIdle()

        val beforeCount = viewModel.uiState.value.messages.size
        viewModel.onAction(ChatRoomAction.SendDemoMessage)
        advanceUntilIdle()

        assertEquals(beforeCount, viewModel.uiState.value.messages.size)
        assertTrue(viewModel.uiState.value.sendGuardMessage?.contains("регистрация") == true)
    }

    @Test
    fun `player card viewmodel loads seeded player`() = runTest {
        val viewModel = PlayerCardViewModel(FakeChatsRepository())

        viewModel.load("ghost")
        advanceUntilIdle()

        assertEquals("ghost", viewModel.uiState.value.playerId)
        assertEquals("Ghost", viewModel.uiState.value.callsign)
        assertTrue(viewModel.uiState.value.tags.isNotEmpty())
    }

    @Test
    fun `player card viewmodel handles unknown user`() = runTest {
        val viewModel = PlayerCardViewModel(FakeChatsRepository())

        viewModel.load("missing-user")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.callsign.contains("не найден"))
    }
}

private class FakeSessionRepository(
    registered: Boolean = true,
) : SessionRepository {
    private val state = MutableStateFlow<AuthState>(
        if (registered) {
            AuthState.SignedIn(
                UserSession(
                    userId = "u-registered",
                    displayName = "Teiwaz_",
                    email = "teiwaz@example.com",
                ),
            )
        } else {
            AuthState.SignedIn(
                UserSession(
                    userId = "u-guest",
                    displayName = "Teiwaz_",
                    email = null,
                ),
            )
        },
    )

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun continueAsGuest(callsign: String): AuthResult {
        state.value = AuthState.SignedIn(UserSession(userId = "u-guest", displayName = callsign))
        return AuthResult.Success((state.value as AuthState.SignedIn).session)
    }

    override suspend fun registerWithEmail(
        callsign: String,
        email: String,
        password: String?,
    ): AuthResult {
        state.value = AuthState.SignedIn(
            UserSession(
                userId = "u-registered",
                displayName = callsign,
                email = email,
            ),
        )
        return AuthResult.Success((state.value as AuthState.SignedIn).session)
    }

    override suspend fun signInMock(callsign: String): AuthResult = continueAsGuest(callsign)

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentAuthState(): AuthState = state.value
}
