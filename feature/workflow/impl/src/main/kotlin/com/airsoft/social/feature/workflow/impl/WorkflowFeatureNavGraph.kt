package com.airsoft.social.feature.workflow.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.workflow.api.WorkflowFeatureApi

private const val DEFAULT_SAVED_FILTER_ID = "teams-weekend-cqb"
private const val DEFAULT_DRAFT_ID = "event-night-raid-draft"

fun NavGraphBuilder.registerWorkflowFeatureGraph(
    navigateToSavedFilterDetail: (String) -> Unit,
    navigateToDraftDetail: (String) -> Unit,
) {
    composable(WorkflowFeatureApi.SavedFiltersRoute) {
        SavedFiltersShellRoute(
            onOpenFilterDetail = {
                navigateToSavedFilterDetail(
                    WorkflowFeatureApi.savedFilterDetailRoute(DEFAULT_SAVED_FILTER_ID),
                )
            },
        )
    }
    composable(
        route = WorkflowFeatureApi.SavedFilterDetailRoutePattern,
        arguments = listOf(
            navArgument(WorkflowFeatureApi.SavedFilterIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val savedFilterId = backStackEntry.arguments
            ?.getString(WorkflowFeatureApi.SavedFilterIdArg)
            .orEmpty()
        SavedFilterDetailSkeletonRoute(savedFilterId = savedFilterId)
    }
    composable(WorkflowFeatureApi.DraftsRoute) {
        DraftsShellRoute(
            onOpenDraftDetail = {
                navigateToDraftDetail(WorkflowFeatureApi.draftDetailRoute(DEFAULT_DRAFT_ID))
            },
        )
    }
    composable(
        route = WorkflowFeatureApi.DraftDetailRoutePattern,
        arguments = listOf(
            navArgument(WorkflowFeatureApi.DraftIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val draftId = backStackEntry.arguments
            ?.getString(WorkflowFeatureApi.DraftIdArg)
            .orEmpty()
        DraftDetailSkeletonRoute(draftId = draftId)
    }
}
