package com.airsoft.social.feature.marketplace.api

import com.airsoft.social.core.model.EditorMode

data class MarketplaceRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object MarketplaceFeatureApi {
    const val ROUTE: String = "marketplace"
    const val SearchRoute: String = "marketplace/search"

    const val LISTING_ID_ARG: String = "listingId"
    const val MARKETPLACE_EDITOR_MODE_ARG: String = "editorMode"
    const val MARKETPLACE_EDITOR_REF_ID_ARG: String = "editorRefId"

    const val MarketplaceListingDetailRoutePattern: String = "marketplace/listing/{$LISTING_ID_ARG}"
    const val MarketplaceEditorRoutePattern: String =
        "marketplace/editor/{$MARKETPLACE_EDITOR_MODE_ARG}/{$MARKETPLACE_EDITOR_REF_ID_ARG}"

    val contract = MarketplaceRouteContract(
        route = ROUTE,
        title = "Барахолка",
        requiresAuth = true,
        requiresOnboarding = true,
    )

    fun marketplaceListingDetailRoute(listingId: String): String = "marketplace/listing/$listingId"

    fun marketplaceEditorRoute(mode: EditorMode, refId: String): String =
        "marketplace/editor/${mode.routeValue}/$refId"
}
