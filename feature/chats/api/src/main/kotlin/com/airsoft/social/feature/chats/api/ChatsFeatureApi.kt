package com.airsoft.social.feature.chats.api

data class ChatsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object ChatsFeatureApi {
    const val ROUTE: String = "chats"
    const val SearchRoute: String = "chats/search"
    const val CHAT_ID_ARG: String = "chatId"
    const val USER_ID_ARG: String = "userId"
    const val ChatRoomRoutePattern: String = "chats/room/{$CHAT_ID_ARG}"
    const val PlayerCardRoutePattern: String = "players/card/{$USER_ID_ARG}"

    val contract = ChatsRouteContract(
        route = ROUTE,
        title = "Чаты",
        requiresAuth = true,
        requiresOnboarding = true,
    )

    fun chatRoomRoute(chatId: String): String = "chats/room/$chatId"

    fun playerCardRoute(userId: String): String = "players/card/$userId"
}
