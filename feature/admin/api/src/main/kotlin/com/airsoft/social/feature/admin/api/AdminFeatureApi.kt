package com.airsoft.social.feature.admin.api

object AdminFeatureApi {
    const val ModerationRoute = "moderation"
    const val ModerationReportsQueueRoute = "moderation/reports"
    const val ModerationChatMonitoringRoute = "moderation/chat-monitoring"
    const val ModerationMarketQuarantineRoute = "moderation/market-quarantine"
    const val ModerationUsersSanctionsRoute = "moderation/users-sanctions"
    const val ModerationSupportInboxRoute = "moderation/support-inbox"
    const val AdminDashboardRoute = "admin/dashboard"
    const val AdminRbacMatrixRoute = "admin/rbac-matrix"
    const val AdminModeratorAssignmentsRoute = "admin/moderator-assignments"
    const val AdminPoliciesRoute = "admin/policies"
    const val AdminMarketplaceCategoriesRoute = "admin/marketplace-categories"
    const val AdminGdprRequestsRoute = "admin/gdpr-requests"
    const val AdminAuditLogRoute = "admin/audit-log"
    const val ReportIdArg = "reportId"
    const val ModerationReportDetailRoutePattern = "moderation/reports/{$ReportIdArg}"

    fun moderationReportDetailRoute(reportId: String): String = "moderation/reports/$reportId"
}
