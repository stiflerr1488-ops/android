package com.airsoft.social.feature.events.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.airsoft.social.feature.events.api.EventsFeatureApi

fun NavGraphBuilder.registerEventsFeatureGraph(
    navigateToEventDetail: (String) -> Unit,
) {
    composable(EventsFeatureApi.SearchRoute) {
        EventsSearchRoute(
            onOpenEventDetail = { eventId ->
                navigateToEventDetail(EventsFeatureApi.eventDetailRoute(eventId))
            },
        )
    }
    composable(EventsFeatureApi.ROUTE) {
        EventsPlaceholderScreen()
    }
}
