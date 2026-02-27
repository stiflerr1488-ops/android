package com.airsoft.social.feature.productivity.api

object ProductivityFeatureApi {
    const val DashboardRoute = "dashboard"
    const val AnnouncementsRoute = "announcements"
    const val AnnouncementIdArg = "announcementId"
    const val AnnouncementDetailRoutePattern = "announcements/detail/{$AnnouncementIdArg}"

    fun announcementDetailRoute(announcementId: String): String = "announcements/detail/$announcementId"
}
