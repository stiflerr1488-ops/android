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
    const val Notifications = "notifications"
    const val Search = "search"
    const val Settings = "settings"
    const val Support = "support"
    const val About = "about"
    const val TacticalReserved = TacticalFeatureApi.ROUTE
    const val ChatRoomDemo = "chats/room-demo"
    const val PlayerCardDemo = "chats/player-demo"
    const val TeamDetailDemo = "teams/detail-demo"
    const val TeamCreateDemo = "teams/create-demo"
    const val EventDetailDemo = "events/detail-demo"
    const val EventCreateDemo = "events/create-demo"
    const val MarketplaceListingDetailDemo = "marketplace/listing-detail-demo"
    const val MarketplaceCreateListingDemo = "marketplace/create-listing-demo"
    const val ProfileEditDemo = "profile/edit-demo"
    const val ProfileInventoryDemo = "profile/inventory-demo"

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
        add(AirsoftRoutes.Notifications)
        add(AirsoftRoutes.Search)
        add(AirsoftRoutes.onboardingRoute)
        add(AirsoftRoutes.authRoute)
        add(AirsoftRoutes.Settings)
        add(AirsoftRoutes.Support)
        add(AirsoftRoutes.About)
        add(AirsoftRoutes.TacticalReserved)
        add(AirsoftRoutes.ChatRoomDemo)
        add(AirsoftRoutes.PlayerCardDemo)
        add(AirsoftRoutes.TeamDetailDemo)
        add(AirsoftRoutes.TeamCreateDemo)
        add(AirsoftRoutes.EventDetailDemo)
        add(AirsoftRoutes.EventCreateDemo)
        add(AirsoftRoutes.MarketplaceListingDetailDemo)
        add(AirsoftRoutes.MarketplaceCreateListingDemo)
        add(AirsoftRoutes.ProfileEditDemo)
        add(AirsoftRoutes.ProfileInventoryDemo)
        addAll(topLevelDestinations.map { it.route })
    }
}
