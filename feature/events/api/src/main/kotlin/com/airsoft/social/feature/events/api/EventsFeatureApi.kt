package com.airsoft.social.feature.events.api

data class EventsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object EventsFeatureApi {
    const val ROUTE: String = "events"
    val contract = EventsRouteContract(
        route = ROUTE,
        title = "Events",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}

