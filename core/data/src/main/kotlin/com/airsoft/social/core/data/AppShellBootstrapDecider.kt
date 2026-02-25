package com.airsoft.social.core.data

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.OnboardingState

enum class AppShellBootstrapTarget {
    Onboarding,
    Auth,
    Main,
}

class AppShellBootstrapDecider {
    fun decide(
        onboardingState: OnboardingState,
        authState: AuthState,
    ): AppShellBootstrapTarget = when {
        onboardingState is OnboardingState.Required -> AppShellBootstrapTarget.Onboarding
        authState !is AuthState.SignedIn -> AppShellBootstrapTarget.Auth
        else -> AppShellBootstrapTarget.Main
    }
}

