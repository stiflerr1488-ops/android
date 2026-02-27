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
        // Step 1 UX: onboarding is non-blocking; first screen should be auth/registration.
        authState !is AuthState.SignedIn -> AppShellBootstrapTarget.Auth
        // Keep onboarding state in model, but don't gate entry into main flow.
        onboardingState is OnboardingState.Required -> AppShellBootstrapTarget.Main
        else -> AppShellBootstrapTarget.Main
    }
}
