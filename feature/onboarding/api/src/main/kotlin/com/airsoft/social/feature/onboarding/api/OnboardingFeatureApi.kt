package com.airsoft.social.feature.onboarding.api

data class OnboardingRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object OnboardingFeatureApi {
    const val ROUTE: String = "onboarding"
    val contract = OnboardingRouteContract(
        route = ROUTE,
        title = "Onboarding",
        requiresAuth = false,
        requiresOnboarding = false,
    )
}

