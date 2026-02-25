package com.airsoft.social.feature.marketplace.api

data class MarketplaceRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object MarketplaceFeatureApi {
    const val ROUTE: String = "marketplace"
    val contract = MarketplaceRouteContract(
        route = ROUTE,
        title = "Marketplace",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}

