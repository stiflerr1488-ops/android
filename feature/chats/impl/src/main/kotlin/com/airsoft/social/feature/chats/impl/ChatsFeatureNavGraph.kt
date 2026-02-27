package com.airsoft.social.feature.chats.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.chats.api.ChatsFeatureApi

fun NavGraphBuilder.registerChatsFeatureGraph(
    navigateToChatRoom: (String) -> Unit,
    navigateToPlayerCard: (String) -> Unit,
    onOpenAuth: () -> Unit = {},
) {
    composable(ChatsFeatureApi.ROUTE) {
        ChatsPlaceholderScreen()
    }
    composable(ChatsFeatureApi.SearchRoute) {
        ChatsSearchRoute(
            onOpenChatRoom = { chatId ->
                navigateToChatRoom(ChatsFeatureApi.chatRoomRoute(chatId))
            },
            onOpenPlayerCard = { userId ->
                navigateToPlayerCard(ChatsFeatureApi.playerCardRoute(userId))
            },
        )
    }
    composable(
        route = ChatsFeatureApi.ChatRoomRoutePattern,
        arguments = listOf(
            navArgument(ChatsFeatureApi.CHAT_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val chatId = backStackEntry.arguments
            ?.getString(ChatsFeatureApi.CHAT_ID_ARG)
            .orEmpty()
        ChatRoomSkeletonRoute(
            chatId = chatId,
            onOpenAuth = onOpenAuth,
        )
    }
    composable(
        route = ChatsFeatureApi.PlayerCardRoutePattern,
        arguments = listOf(
            navArgument(ChatsFeatureApi.USER_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val userId = backStackEntry.arguments
            ?.getString(ChatsFeatureApi.USER_ID_ARG)
            .orEmpty()
        PlayerCardSkeletonRoute(playerId = userId)
    }
}
