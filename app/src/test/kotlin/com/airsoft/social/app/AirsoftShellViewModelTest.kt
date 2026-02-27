package com.airsoft.social.app

import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.data.BootstrapRepository
import com.airsoft.social.core.data.OnboardingRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.data.SettingsRepository
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppTab
import com.airsoft.social.core.model.AppThemePreference
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.OnboardingState
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.telemetry.TelemetryReporter
import com.example.teamcompass.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AirsoftShellViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ui state reflects settings from repository`() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = AppSettings(
                themePreference = AppThemePreference.Dark,
                pushEnabled = false,
                telemetryEnabled = true,
                tacticalQuickLaunchEnabled = false,
            ),
        )
        val telemetryReporter = RecordingTelemetryReporter()
        val viewModel = buildViewModel(
            settingsRepository = settingsRepository,
            telemetryReporter = telemetryReporter,
        )
        val collectJob = launch { viewModel.uiState.collect { } }

        advanceUntilIdle()
        val uiState = viewModel.uiState.value

        assertEquals(AppThemePreference.Dark, uiState.appSettings.themePreference)
        assertFalse(uiState.appSettings.pushEnabled)
        assertFalse(uiState.appSettings.tacticalQuickLaunchEnabled)
        assertEquals(listOf(true), telemetryReporter.enabledValues)
        collectJob.cancel()
    }

    @Test
    fun `telemetry enablement is pushed to reporter when settings change`() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initial = AppSettings(telemetryEnabled = false),
        )
        val telemetryReporter = RecordingTelemetryReporter()
        val viewModel = buildViewModel(
            settingsRepository = settingsRepository,
            telemetryReporter = telemetryReporter,
        )
        val collectJob = launch { viewModel.uiState.collect { } }

        advanceUntilIdle()
        settingsRepository.setTelemetryEnabled(true)
        advanceUntilIdle()

        assertEquals(listOf(false, true), telemetryReporter.enabledValues)
        assertEquals(true, viewModel.uiState.value.appSettings.telemetryEnabled)
        collectJob.cancel()
    }

    private fun buildViewModel(
        settingsRepository: SettingsRepository,
        telemetryReporter: TelemetryReporter,
    ): AirsoftShellViewModel = AirsoftShellViewModel(
        sessionRepository = FakeSessionRepository(),
        onboardingRepository = FakeOnboardingRepository(),
        bootstrapRepository = FakeBootstrapRepository(),
        settingsRepository = settingsRepository,
        telemetryReporter = telemetryReporter,
    )
}

private class FakeSessionRepository(
    initial: AuthState = AuthState.SignedOut,
) : SessionRepository {
    private val state = MutableStateFlow(initial)
    override val authState: StateFlow<AuthState> = state.asStateFlow()

    override suspend fun continueAsGuest(callsign: String): AuthResult {
        state.value = AuthState.SignedIn(
            UserSession(
                userId = "mock-user",
                displayName = callsign.ifBlank { "Operator" },
            ),
        )
        return AuthResult.Success((state.value as AuthState.SignedIn).session)
    }

    override suspend fun registerWithEmail(
        callsign: String,
        email: String,
        password: String?,
    ): AuthResult {
        state.value = AuthState.SignedIn(
            UserSession(
                userId = "registered-user",
                displayName = callsign.ifBlank { "Operator" },
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

private class FakeOnboardingRepository(
    initial: OnboardingState = OnboardingState.Completed,
) : OnboardingRepository {
    private val state = MutableStateFlow(initial)
    override val onboardingState: StateFlow<OnboardingState> = state.asStateFlow()

    override suspend fun completeOnboarding() {
        state.value = OnboardingState.Completed
    }

    override suspend fun resetOnboarding() {
        state.value = OnboardingState.Required
    }
}

private class FakeBootstrapRepository(
    initial: Map<AppTab, Int> = emptyMap(),
) : BootstrapRepository {
    private val badges = MutableStateFlow(initial)

    override fun observeBadgeCounts(): Flow<Map<AppTab, Int>> = badges.asStateFlow()
}

private class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val settings = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = settings.asStateFlow()

    override suspend fun setThemePreference(value: AppThemePreference) {
        settings.value = settings.value.copy(themePreference = value)
    }

    override suspend fun setPushEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(pushEnabled = enabled)
    }

    override suspend fun setTelemetryEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(telemetryEnabled = enabled)
    }

    override suspend fun setTacticalQuickLaunchEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(tacticalQuickLaunchEnabled = enabled)
    }
}

private class RecordingTelemetryReporter : TelemetryReporter {
    val enabledValues: MutableList<Boolean> = mutableListOf()

    override fun setUser(id: String?) = Unit

    override fun logEvent(name: String, params: Map<String, Any?>) = Unit

    override fun setEnabled(enabled: Boolean) {
        enabledValues += enabled
    }
}
