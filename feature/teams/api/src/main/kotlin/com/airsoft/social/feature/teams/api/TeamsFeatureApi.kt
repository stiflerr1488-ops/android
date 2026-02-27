package com.airsoft.social.feature.teams.api

import com.airsoft.social.core.model.EditorMode

data class TeamsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object TeamsFeatureApi {
    const val ROUTE: String = "teams"
    const val SearchRoute: String = "teams/search"

    const val TEAM_ID_ARG: String = "teamId"
    const val TEAM_EDITOR_MODE_ARG: String = "editorMode"
    const val TEAM_EDITOR_REF_ID_ARG: String = "editorRefId"

    const val TeamDetailRoutePattern: String = "teams/detail/{$TEAM_ID_ARG}"
    const val TeamEditorRoutePattern: String =
        "teams/editor/{$TEAM_EDITOR_MODE_ARG}/{$TEAM_EDITOR_REF_ID_ARG}"

    val contract = TeamsRouteContract(
        route = ROUTE,
        title = "Команды",
        requiresAuth = true,
        requiresOnboarding = true,
    )

    fun teamDetailRoute(teamId: String): String = "teams/detail/$teamId"

    fun teamEditorRoute(mode: EditorMode, refId: String): String =
        "teams/editor/${mode.routeValue}/$refId"
}
