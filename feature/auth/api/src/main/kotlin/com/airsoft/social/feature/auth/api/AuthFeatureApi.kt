package com.airsoft.social.feature.auth.api

data class AuthRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object AuthFeatureApi {
    const val ROUTE: String = "auth"
    val contract = AuthRouteContract(
        route = ROUTE,
        title = "Auth",
        requiresAuth = false,
        requiresOnboarding = false,
    )
}

