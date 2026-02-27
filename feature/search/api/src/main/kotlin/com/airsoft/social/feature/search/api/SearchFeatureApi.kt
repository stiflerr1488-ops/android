package com.airsoft.social.feature.search.api

data class SearchRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object SearchFeatureApi {
    const val ROUTE: String = "search"
    const val FILTERS_ROUTE: String = "search/filters"
    const val SAVED_SEARCHES_ROUTE: String = "search/saved"

    val contract = SearchRouteContract(
        route = ROUTE,
        title = "Поиск",
        requiresAuth = true,
        requiresOnboarding = true,
    )
}
