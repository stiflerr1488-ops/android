package com.airsoft.social.feature.support.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.support.api.SupportFeatureApi

private const val DEFAULT_SUPPORT_TICKET_ID = "ticket-201"

fun NavGraphBuilder.registerSupportFeatureGraph(
    onOpenNotifications: () -> Unit,
    navigateToSupportRoute: (String) -> Unit,
) {
    composable(SupportFeatureApi.SupportRoute) {
        SupportShellRoute(
            onOpenNotifications = onOpenNotifications,
            onOpenTickets = { navigateToSupportRoute(SupportFeatureApi.SupportTicketsRoute) },
            onOpenSupportChat = { navigateToSupportRoute(SupportFeatureApi.SupportChatRoute) },
            onOpenFaq = { navigateToSupportRoute(SupportFeatureApi.SupportFaqRoute) },
            onOpenTicketDetail = {
                navigateToSupportRoute(
                    SupportFeatureApi.supportTicketDetailRoute(DEFAULT_SUPPORT_TICKET_ID),
                )
            },
        )
    }
    composable(SupportFeatureApi.SupportTicketsRoute) {
        SupportTicketsShellRoute(
            onOpenTicketDetail = {
                navigateToSupportRoute(
                    SupportFeatureApi.supportTicketDetailRoute(DEFAULT_SUPPORT_TICKET_ID),
                )
            },
        )
    }
    composable(SupportFeatureApi.SupportChatRoute) {
        SupportChatShellRoute()
    }
    composable(SupportFeatureApi.SupportFaqRoute) {
        SupportFaqShellRoute()
    }
    composable(
        route = SupportFeatureApi.SupportTicketDetailRoutePattern,
        arguments = listOf(
            navArgument(SupportFeatureApi.SupportTicketIdArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        SupportTicketDetailSkeletonRoute(
            ticketId = backStackEntry.arguments
                ?.getString(SupportFeatureApi.SupportTicketIdArg)
                .orEmpty(),
        )
    }
    composable(SupportFeatureApi.AboutRoute) {
        AboutShellRoute()
    }
}
