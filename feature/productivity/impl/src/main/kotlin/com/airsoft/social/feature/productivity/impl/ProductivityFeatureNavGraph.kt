package com.airsoft.social.feature.productivity.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.productivity.api.ProductivityFeatureApi

private const val DEFAULT_ANNOUNCEMENT_ID = "safety-2025-update"

fun NavGraphBuilder.registerProductivityFeatureGraph(
    onOpenCalendar: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenTeams: () -> Unit,
    navigateToAnnouncementDetail: (String) -> Unit,
) {
    composable(ProductivityFeatureApi.DashboardRoute) {
        DashboardShellRoute(
            onOpenCalendar = onOpenCalendar,
            onOpenChats = onOpenChats,
            onOpenTeams = onOpenTeams,
            onOpenAnnouncements = {
                navigateToAnnouncementDetail(
                    ProductivityFeatureApi.announcementDetailRoute(DEFAULT_ANNOUNCEMENT_ID),
                )
            },
        )
    }
    composable(ProductivityFeatureApi.AnnouncementsRoute) {
        AnnouncementsShellRoute(
            onOpenAnnouncementDetail = {
                navigateToAnnouncementDetail(
                    ProductivityFeatureApi.announcementDetailRoute(DEFAULT_ANNOUNCEMENT_ID),
                )
            },
        )
    }
    composable(
        route = ProductivityFeatureApi.AnnouncementDetailRoutePattern,
        arguments = listOf(
            navArgument(ProductivityFeatureApi.AnnouncementIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val announcementId = backStackEntry.arguments
            ?.getString(ProductivityFeatureApi.AnnouncementIdArg)
            .orEmpty()
        AnnouncementDetailSkeletonRoute(announcementId = announcementId)
    }
}
