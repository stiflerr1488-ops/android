package com.airsoft.social.feature.profile.api

data class ProfileRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object ProfileFeatureApi {
    const val ROUTE: String = "profile"
    val contract = ProfileRouteContract(
        route = ROUTE,
        title = "Profile",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}

