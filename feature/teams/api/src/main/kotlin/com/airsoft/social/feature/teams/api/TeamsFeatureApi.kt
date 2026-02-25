package com.airsoft.social.feature.teams.api

data class TeamsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object TeamsFeatureApi {
    const val ROUTE: String = "teams"
    val contract = TeamsRouteContract(
        route = ROUTE,
        title = "Teams",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}

