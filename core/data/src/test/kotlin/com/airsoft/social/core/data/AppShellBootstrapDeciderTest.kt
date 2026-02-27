package com.airsoft.social.core.data

import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.OnboardingState
import com.airsoft.social.core.model.UserSession
import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellBootstrapDeciderTest {
    private val decider = AppShellBootstrapDecider()

    @Test
    fun `required onboarding does not block auth when signed out`() {
        val target = decider.decide(
            onboardingState = OnboardingState.Required,
            authState = AuthState.SignedOut,
        )

        assertEquals(AppShellBootstrapTarget.Auth, target)
    }

    @Test
    fun `signed out with completed onboarding routes to auth`() {
        val target = decider.decide(
            onboardingState = OnboardingState.Completed,
            authState = AuthState.SignedOut,
        )

        assertEquals(AppShellBootstrapTarget.Auth, target)
    }

    @Test
    fun `signed in and onboarded routes to main`() {
        val target = decider.decide(
            onboardingState = OnboardingState.Completed,
            authState = AuthState.SignedIn(UserSession("u1", "Ghost")),
        )

        assertEquals(AppShellBootstrapTarget.Main, target)
    }

    @Test
    fun `signed in with required onboarding still routes to main in step1 ux`() {
        val target = decider.decide(
            onboardingState = OnboardingState.Required,
            authState = AuthState.SignedIn(UserSession("u1", "Ghost")),
        )

        assertEquals(AppShellBootstrapTarget.Main, target)
    }
}
