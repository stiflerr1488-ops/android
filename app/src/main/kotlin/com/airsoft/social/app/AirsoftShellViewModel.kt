package com.airsoft.social.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.AppShellBootstrapDecider
import com.airsoft.social.core.data.AppShellBootstrapTarget
import com.airsoft.social.core.data.BootstrapRepository
import com.airsoft.social.core.data.OnboardingRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.telemetry.TelemetryReporter
import com.airsoft.social.feature.nav.AirsoftRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AirsoftShellViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val onboardingRepository: OnboardingRepository,
    private val bootstrapRepository: BootstrapRepository,
    private val telemetryReporter: TelemetryReporter,
) : ViewModel() {
    private val decider = AppShellBootstrapDecider()
    private val events = MutableStateFlow(0)

    val uiState: StateFlow<AirsoftShellUiState> = combine(
        sessionRepository.authState,
        onboardingRepository.onboardingState,
        bootstrapRepository.observeBadgeCounts(),
        events,
    ) { authState, onboardingState, badgeCounts, _ ->
        val target = decider.decide(
            onboardingState = onboardingState,
            authState = authState,
        )
        AirsoftShellUiState(
            authState = authState,
            onboardingState = onboardingState,
            bootstrapRoute = target.toRoute(),
            tabBadges = badgeCounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AirsoftShellUiState(),
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.completeOnboarding()
            telemetryReporter.logEvent("new_shell_onboarding_completed")
            events.value += 1
        }
    }

    fun signInGuest() {
        viewModelScope.launch {
            sessionRepository.signInGuest(callsign = "Operator")
            events.value += 1
        }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionRepository.signOut()
            events.value += 1
        }
    }

    fun onOpenLegacyTacticalRequested() {
        telemetryReporter.logEvent(
            name = "new_shell_open_legacy_tactical_requested",
            params = mapOf("source" to "tactical_bridge"),
        )
    }
}

private fun AppShellBootstrapTarget.toRoute(): String = when (this) {
    AppShellBootstrapTarget.Onboarding -> AirsoftRoutes.onboardingRoute
    AppShellBootstrapTarget.Auth -> AirsoftRoutes.authRoute
    AppShellBootstrapTarget.Main -> AirsoftRoutes.topLevelDestinations.first().route
}
