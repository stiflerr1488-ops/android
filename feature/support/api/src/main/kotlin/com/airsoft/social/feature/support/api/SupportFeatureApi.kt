package com.airsoft.social.feature.support.api

object SupportFeatureApi {
    const val SupportRoute = "support"
    const val SupportTicketsRoute = "support/tickets"
    const val SupportChatRoute = "support/chat"
    const val SupportFaqRoute = "support/faq"
    const val SupportTicketIdArg = "supportTicketId"
    const val SupportTicketDetailRoutePattern = "support/tickets/{$SupportTicketIdArg}"
    const val AboutRoute = "about"

    val allRoutes: Set<String> = setOf(
        SupportRoute,
        SupportTicketsRoute,
        SupportChatRoute,
        SupportFaqRoute,
        SupportTicketDetailRoutePattern,
        AboutRoute,
    )

    fun supportTicketDetailRoute(ticketId: String): String = "support/tickets/$ticketId"
}
