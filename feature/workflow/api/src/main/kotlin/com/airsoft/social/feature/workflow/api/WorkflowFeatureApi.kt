package com.airsoft.social.feature.workflow.api

object WorkflowFeatureApi {
    const val SavedFiltersRoute = "saved-filters"
    const val SavedFilterIdArg = "savedFilterId"
    const val SavedFilterDetailRoutePattern = "saved-filters/detail/{$SavedFilterIdArg}"

    const val DraftsRoute = "drafts"
    const val DraftIdArg = "draftId"
    const val DraftDetailRoutePattern = "drafts/detail/{$DraftIdArg}"

    fun savedFilterDetailRoute(savedFilterId: String): String = "saved-filters/detail/$savedFilterId"
    fun draftDetailRoute(draftId: String): String = "drafts/detail/$draftId"
}
