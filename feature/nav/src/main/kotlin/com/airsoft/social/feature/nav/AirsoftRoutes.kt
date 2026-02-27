package com.airsoft.social.feature.nav

import com.airsoft.social.core.model.AppTab
import com.airsoft.social.feature.auth.api.AuthFeatureApi
import com.airsoft.social.feature.chats.api.ChatsFeatureApi
import com.airsoft.social.feature.events.api.EventsFeatureApi
import com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi
import com.airsoft.social.feature.onboarding.api.OnboardingFeatureApi
import com.airsoft.social.feature.profile.api.ProfileFeatureApi
import com.airsoft.social.feature.teams.api.TeamsFeatureApi
import com.airsoft.social.feature.tactical.api.TacticalFeatureApi

data class TopLevelDestinationSpec(
    val tab: AppTab,
    val route: String,
    val label: String,
)

object AirsoftRoutes {
    const val Splash = "splash"
    const val TacticalReserved = TacticalFeatureApi.ROUTE

    val topLevelDestinations: List<TopLevelDestinationSpec> = listOf(
        TopLevelDestinationSpec(AppTab.Chats, ChatsFeatureApi.ROUTE, "Chats"),
        TopLevelDestinationSpec(AppTab.Teams, TeamsFeatureApi.ROUTE, "Teams"),
        TopLevelDestinationSpec(AppTab.Events, EventsFeatureApi.ROUTE, "Events"),
        TopLevelDestinationSpec(AppTab.Marketplace, MarketplaceFeatureApi.ROUTE, "Market"),
        TopLevelDestinationSpec(AppTab.Profile, ProfileFeatureApi.ROUTE, "Profile"),
    )

    val onboardingRoute: String = OnboardingFeatureApi.ROUTE
    val authRoute: String = AuthFeatureApi.ROUTE
}

object AirsoftRouteRegistry {
    val topLevelDestinations: List<TopLevelDestinationSpec> = AirsoftRoutes.topLevelDestinations

    val allRoutes: Set<String> = buildSet {
        add(AirsoftRoutes.Splash)
        add(AirsoftRoutes.onboardingRoute)
        add(AirsoftRoutes.authRoute)
        add(AirsoftRoutes.TacticalReserved)
        add(ProfileFeatureApi.ProfileEditRoutePattern)
        addAll(topLevelDestinations.map { it.route })
    }
}

