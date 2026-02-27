package com.airsoft.social.feature.admin.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.admin.api.AdminFeatureApi

private const val DEFAULT_REPORT_ID = "report-001"

fun NavGraphBuilder.registerAdminFeatureGraph(
    navigateToAdminRoute: (String) -> Unit,
) {
    composable(AdminFeatureApi.ModerationRoute) {
        ModerationShellRoute(
            onOpenReportsQueue = { navigateToAdminRoute(AdminFeatureApi.ModerationReportsQueueRoute) },
            onOpenChatMonitoring = { navigateToAdminRoute(AdminFeatureApi.ModerationChatMonitoringRoute) },
            onOpenMarketQuarantine = { navigateToAdminRoute(AdminFeatureApi.ModerationMarketQuarantineRoute) },
            onOpenUsersSanctions = { navigateToAdminRoute(AdminFeatureApi.ModerationUsersSanctionsRoute) },
            onOpenSupportInbox = { navigateToAdminRoute(AdminFeatureApi.ModerationSupportInboxRoute) },
            onOpenAdminDashboard = { navigateToAdminRoute(AdminFeatureApi.AdminDashboardRoute) },
        )
    }
    composable(AdminFeatureApi.ModerationReportsQueueRoute) {
        ModerationReportsQueueShellRoute(
            onOpenReportDetail = {
                navigateToAdminRoute(AdminFeatureApi.moderationReportDetailRoute(DEFAULT_REPORT_ID))
            },
        )
    }
    composable(
        route = AdminFeatureApi.ModerationReportDetailRoutePattern,
        arguments = listOf(
            navArgument(AdminFeatureApi.ReportIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val reportId = backStackEntry.arguments
            ?.getString(AdminFeatureApi.ReportIdArg)
            .orEmpty()
        ModerationReportDetailSkeletonRoute(reportId = reportId)
    }
    composable(AdminFeatureApi.ModerationChatMonitoringRoute) {
        ModerationChatMonitoringShellRoute()
    }
    composable(AdminFeatureApi.ModerationMarketQuarantineRoute) {
        ModerationMarketQuarantineShellRoute()
    }
    composable(AdminFeatureApi.ModerationUsersSanctionsRoute) {
        ModerationUsersSanctionsShellRoute()
    }
    composable(AdminFeatureApi.ModerationSupportInboxRoute) {
        ModerationSupportInboxShellRoute()
    }
    composable(AdminFeatureApi.AdminDashboardRoute) {
        AdminDashboardShellRoute(
            onOpenRbacMatrix = { navigateToAdminRoute(AdminFeatureApi.AdminRbacMatrixRoute) },
            onOpenModeratorAssignments = {
                navigateToAdminRoute(AdminFeatureApi.AdminModeratorAssignmentsRoute)
            },
            onOpenPolicies = { navigateToAdminRoute(AdminFeatureApi.AdminPoliciesRoute) },
            onOpenMarketplaceCategories = {
                navigateToAdminRoute(AdminFeatureApi.AdminMarketplaceCategoriesRoute)
            },
            onOpenGdprRequests = { navigateToAdminRoute(AdminFeatureApi.AdminGdprRequestsRoute) },
            onOpenAuditLog = { navigateToAdminRoute(AdminFeatureApi.AdminAuditLogRoute) },
        )
    }
    composable(AdminFeatureApi.AdminRbacMatrixRoute) {
        AdminRbacMatrixShellRoute()
    }
    composable(AdminFeatureApi.AdminModeratorAssignmentsRoute) {
        AdminRoleAssignmentsShellRoute()
    }
    composable(AdminFeatureApi.AdminPoliciesRoute) {
        AdminPoliciesShellRoute()
    }
    composable(AdminFeatureApi.AdminMarketplaceCategoriesRoute) {
        AdminMarketplaceCategoriesShellRoute()
    }
    composable(AdminFeatureApi.AdminGdprRequestsRoute) {
        AdminGdprRequestsShellRoute()
    }
    composable(AdminFeatureApi.AdminAuditLogRoute) {
        AdminAuditLogShellRoute()
    }
}
