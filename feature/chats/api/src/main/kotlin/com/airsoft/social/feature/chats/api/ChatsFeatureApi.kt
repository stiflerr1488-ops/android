package com.airsoft.social.feature.chats.api

data class ChatsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object ChatsFeatureApi {
    const val ROUTE: String = "chats"
    val contract = ChatsRouteContract(
        route = ROUTE,
        title = "Chats",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}

