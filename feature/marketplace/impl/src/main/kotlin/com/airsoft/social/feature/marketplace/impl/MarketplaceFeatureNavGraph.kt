package com.airsoft.social.feature.marketplace.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi

fun NavGraphBuilder.registerMarketplaceFeatureGraph(
    navigateToListingDetail: (String) -> Unit,
) {
    composable(MarketplaceFeatureApi.SearchRoute) {
        MarketplaceSearchRoute(
            onOpenListingDetail = { listingId ->
                navigateToListingDetail(
                    MarketplaceFeatureApi.marketplaceListingDetailRoute(listingId),
                )
            },
        )
    }
    composable(MarketplaceFeatureApi.ROUTE) {
        MarketplacePlaceholderScreen()
    }
}
