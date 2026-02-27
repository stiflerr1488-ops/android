package com.airsoft.social.feature.events.api

import com.airsoft.social.core.model.EditorMode

data class EventsRouteContract(
    val route: String,
    val title: String,
    val requiresAuth: Boolean,
    val requiresOnboarding: Boolean,
)

object EventsFeatureApi {
    const val ROUTE: String = "events"
    const val SearchRoute: String = "events/search"

    const val EVENT_ID_ARG: String = "eventId"
    const val EVENT_EDITOR_MODE_ARG: String = "editorMode"
    const val EVENT_EDITOR_REF_ID_ARG: String = "editorRefId"

    const val EventDetailRoutePattern: String = "events/detail/{$EVENT_ID_ARG}"
    const val EventEditorRoutePattern: String =
        "events/editor/{$EVENT_EDITOR_MODE_ARG}/{$EVENT_EDITOR_REF_ID_ARG}"

    val contract = EventsRouteContract(
        route = ROUTE,
        title = "Игры",
        requiresAuth = true,
        requiresOnboarding = true,
    )

    fun eventDetailRoute(eventId: String): String = "events/detail/$eventId"

    fun eventEditorRoute(mode: EditorMode, refId: String): String =
        "events/editor/${mode.routeValue}/$refId"
}
