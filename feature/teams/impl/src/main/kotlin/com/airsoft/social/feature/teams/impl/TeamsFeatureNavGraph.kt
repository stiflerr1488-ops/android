package com.airsoft.social.feature.teams.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.feature.teams.api.TeamsFeatureApi

private const val DEFAULT_TEAM_ID = "ew-easy-winner"
private const val DEFAULT_EDITOR_REF_ID = "draft-team-ew-001"

fun NavGraphBuilder.registerTeamsFeatureGraph(
    navigateToTeamDetail: (String) -> Unit,
    navigateToTeamEditor: (String) -> Unit,
) {
    composable(TeamsFeatureApi.SearchRoute) {
        TeamsSearchRoute(
            onOpenTeamDetail = { teamId ->
                navigateToTeamDetail(TeamsFeatureApi.teamDetailRoute(teamId))
            },
        )
    }
    composable(TeamsFeatureApi.ROUTE) {
        TeamsPlaceholderScreen(
            onOpenTeamDetailDemo = {
                navigateToTeamDetail(TeamsFeatureApi.teamDetailRoute(DEFAULT_TEAM_ID))
            },
            onOpenTeamCreateDemo = {
                navigateToTeamEditor(
                    TeamsFeatureApi.teamEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_EDITOR_REF_ID,
                    ),
                )
            },
        )
    }
    composable(
        route = TeamsFeatureApi.TeamDetailRoutePattern,
        arguments = listOf(
            navArgument(TeamsFeatureApi.TEAM_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val teamId = backStackEntry.arguments
            ?.getString(TeamsFeatureApi.TEAM_ID_ARG)
            .orEmpty()
        TeamDetailSkeletonRoute(teamId = teamId)
    }
    composable(
        route = TeamsFeatureApi.TeamEditorRoutePattern,
        arguments = listOf(
            navArgument(TeamsFeatureApi.TEAM_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(TeamsFeatureApi.TEAM_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(TeamsFeatureApi.TEAM_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(TeamsFeatureApi.TEAM_EDITOR_REF_ID_ARG)
            .orEmpty()
        TeamCreateEditSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
}
