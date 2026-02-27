package com.airsoft.social.feature.settings.impl

import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.data.SettingsRepository
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShellSettingsScreensViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `settings viewmodel toggles push and cycles theme`() = runTest {
        val viewModel = SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        advanceUntilIdle()

        val initialTheme = viewModel.uiState.value.selectedTheme
        val initialPush = viewModel.uiState.value.pushEnabled

        viewModel.onAction(SettingsAction.TogglePush)
        viewModel.onAction(SettingsAction.CycleTheme)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedTheme != initialTheme)
        assertFalse(viewModel.uiState.value.pushEnabled == initialPush)
    }

    @Test
    fun `settings viewmodel cycles theme in deterministic order`() = runTest {
        val viewModel = SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        advanceUntilIdle()

        assertEquals(SettingsThemeMode.System, viewModel.uiState.value.selectedTheme)

        viewModel.onAction(SettingsAction.CycleTheme)
        advanceUntilIdle()
        assertEquals(SettingsThemeMode.Light, viewModel.uiState.value.selectedTheme)

        viewModel.onAction(SettingsAction.CycleTheme)
        advanceUntilIdle()
        assertEquals(SettingsThemeMode.Dark, viewModel.uiState.value.selectedTheme)

        viewModel.onAction(SettingsAction.CycleTheme)
        advanceUntilIdle()
        assertEquals(SettingsThemeMode.System, viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `settings viewmodel toggles telemetry and tactical quick launch`() = runTest {
        val viewModel = SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        advanceUntilIdle()

        val initialTelemetry = viewModel.uiState.value.telemetryEnabled
        val initialTacticalQuickLaunch = viewModel.uiState.value.tacticalQuickLaunchEnabled

        viewModel.onAction(SettingsAction.ToggleTelemetry)
        viewModel.onAction(SettingsAction.ToggleTacticalQuickLaunch)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.telemetryEnabled == initialTelemetry)
        assertFalse(viewModel.uiState.value.tacticalQuickLaunchEnabled == initialTacticalQuickLaunch)
    }

    @Test
    fun `navigation actions do not mutate settings state`() = runTest {
        val viewModel = SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            sessionRepository = FakeSessionRepository(),
        )
        advanceUntilIdle()
        val initialState = viewModel.uiState.value

        viewModel.onAction(SettingsAction.OpenAccountClicked)
        viewModel.onAction(SettingsAction.OpenPrivacyClicked)
        viewModel.onAction(SettingsAction.OpenPermissionsClicked)
        viewModel.onAction(SettingsAction.OpenSecurityClicked)
        viewModel.onAction(SettingsAction.OpenBatteryClicked)
        viewModel.onAction(SettingsAction.OpenSupportClicked)
        viewModel.onAction(SettingsAction.OpenAboutClicked)
        advanceUntilIdle()

        assertEquals(initialState, viewModel.uiState.value)
    }

    @Test
    fun `ui state exposes signed in roles and write scopes`() = runTest {
        val viewModel = SettingsViewModel(
            settingsRepository = FakeSettingsRepository(),
            sessionRepository = FakeSessionRepository(
                AuthState.SignedIn(
                    UserSession(
                        userId = "u-1",
                        displayName = "Teiwaz_",
                        email = "test+commercial@example.com",
                        accountRoles = setOf(AccountRole.USER, AccountRole.COMMERCIAL),
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals("Teiwaz_", viewModel.uiState.value.accountCallsign)
        assertTrue(viewModel.uiState.value.accountAccess.canCreateGameEvents)
        assertTrue(viewModel.uiState.value.accountAccess.canCreateShopListings)
    }

    @Test
    fun `settings account viewmodel maps guest session as read only`() = runTest {
        val viewModel = SettingsAccountViewModel(
            sessionRepository = FakeSessionRepository(
                AuthState.SignedIn(
                    UserSession(
                        userId = "guest-id",
                        displayName = "Ghost",
                    ),
                ),
            ),
        )
        val collectJob = backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isGuest)
        assertEquals("Ghost", state.callsign)
        assertEquals(null, state.email)
        assertTrue(state.roles.isEmpty())
        assertFalse(state.access.canSendChatMessages)
        assertFalse(state.access.canCreateMarketplaceListings)
        assertFalse(state.access.canCreateRideShareListings)
        assertFalse(state.access.canCreateGameEvents)
        assertFalse(state.access.canCreateShopListings)
        collectJob.cancel()
    }

    @Test
    fun `settings account viewmodel maps registered commercial session`() = runTest {
        val viewModel = SettingsAccountViewModel(
            sessionRepository = FakeSessionRepository(
                AuthState.SignedIn(
                    UserSession(
                        userId = "reg-id",
                        displayName = "Teiwaz_",
                        email = "teiwaz+commercial@example.com",
                        accountRoles = setOf(AccountRole.USER, AccountRole.COMMERCIAL),
                    ),
                ),
            ),
        )
        val collectJob = backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGuest)
        assertEquals("teiwaz+commercial@example.com", state.email)
        assertTrue(state.roles.contains(AccountRole.COMMERCIAL))
        assertTrue(state.access.canSendChatMessages)
        assertTrue(state.access.canEditProfile)
        assertTrue(state.access.canCreateMarketplaceListings)
        assertTrue(state.access.canCreateRideShareListings)
        assertTrue(state.access.canCreateGameEvents)
        assertTrue(state.access.canCreateShopListings)
        collectJob.cancel()
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = state.asStateFlow()

    override suspend fun setThemePreference(value: AppThemePreference) {
        state.value = state.value.copy(themePreference = value)
    }

    override suspend fun setPushEnabled(enabled: Boolean) {
        state.value = state.value.copy(pushEnabled = enabled)
    }

    override suspend fun setTelemetryEnabled(enabled: Boolean) {
        state.value = state.value.copy(telemetryEnabled = enabled)
    }

    override suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean) {
        state.value = state.value.copy(tacticalQuickLaunchEnabled = enabled)
    }
}

private class FakeSessionRepository(
    initialState: AuthState = AuthState.SignedOut,
) : SessionRepository {
    private val state = MutableStateFlow(initialState)
    override val authState = state.asStateFlow()

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
