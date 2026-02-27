package com.airsoft.social.feature.directories.api

import com.airsoft.social.core.model.EditorMode

object DirectoriesFeatureApi {
    const val GameCalendarRoute = "games/calendar"
    const val GameCalendarSearchRoute = "games/calendar/search"
    const val GAME_ID_ARG = "gameId"
    const val GAME_EDITOR_MODE_ARG = "editorMode"
    const val GAME_EDITOR_REF_ID_ARG = "editorRefId"
    const val GameCalendarDetailRoutePattern = "games/calendar/detail/{$GAME_ID_ARG}"
    const val GameCalendarEditorRoutePattern =
        "games/calendar/editor/{$GAME_EDITOR_MODE_ARG}/{$GAME_EDITOR_REF_ID_ARG}"
    const val GameCalendarLogisticsRoutePattern = "games/calendar/logistics/{$GAME_ID_ARG}"
    const val RideShareRoute = "rideshare"
    const val RideShareSearchRoute = "rideshare/search"
    const val RIDE_SHARE_ID_ARG = "rideId"
    const val RIDE_SHARE_EDITOR_MODE_ARG = "editorMode"
    const val RIDE_SHARE_EDITOR_REF_ID_ARG = "editorRefId"
    const val RideShareTripDetailRoutePattern = "rideshare/trip/{$RIDE_SHARE_ID_ARG}"
    const val RideShareTripEditorRoutePattern =
        "rideshare/editor/{$RIDE_SHARE_EDITOR_MODE_ARG}/{$RIDE_SHARE_EDITOR_REF_ID_ARG}"
    const val RideShareMyRoute = "rideshare/my-route"
    const val PolygonsRoute = "polygons"
    const val PolygonsSearchRoute = "polygons/search"
    const val POLYGON_ID_ARG = "polygonId"
    const val POLYGON_EDITOR_MODE_ARG = "editorMode"
    const val POLYGON_EDITOR_REF_ID_ARG = "editorRefId"
    const val PolygonDetailRoutePattern = "polygons/detail/{$POLYGON_ID_ARG}"
    const val PolygonEditorRoutePattern =
        "polygons/editor/{$POLYGON_EDITOR_MODE_ARG}/{$POLYGON_EDITOR_REF_ID_ARG}"
    const val PolygonRulesMapRoutePattern = "polygons/rules-map/{$POLYGON_ID_ARG}"
    const val ShopsRoute = "shops"
    const val ShopsSearchRoute = "shops/search"
    const val SHOP_ID_ARG = "shopId"
    const val SHOP_EDITOR_MODE_ARG = "editorMode"
    const val SHOP_EDITOR_REF_ID_ARG = "editorRefId"
    const val ShopDetailRoutePattern = "shops/detail/{$SHOP_ID_ARG}"
    const val ShopEditorRoutePattern =
        "shops/editor/{$SHOP_EDITOR_MODE_ARG}/{$SHOP_EDITOR_REF_ID_ARG}"
    const val ServicesRoute = "services"
    const val ServicesSearchRoute = "services/search"
    const val SERVICE_ID_ARG = "serviceId"
    const val SERVICE_EDITOR_MODE_ARG = "editorMode"
    const val SERVICE_EDITOR_REF_ID_ARG = "editorRefId"
    const val ServiceDetailRoutePattern = "services/detail/{$SERVICE_ID_ARG}"
    const val ServiceEditorRoutePattern =
        "services/editor/{$SERVICE_EDITOR_MODE_ARG}/{$SERVICE_EDITOR_REF_ID_ARG}"

    fun rideShareTripDetailRoute(rideId: String): String = "rideshare/trip/$rideId"

    fun rideShareTripEditorRoute(mode: EditorMode, refId: String): String =
        "rideshare/editor/${mode.routeValue}/$refId"

    fun polygonDetailRoute(polygonId: String): String = "polygons/detail/$polygonId"

    fun polygonEditorRoute(mode: EditorMode, refId: String): String =
        "polygons/editor/${mode.routeValue}/$refId"

    fun polygonRulesMapRoute(polygonId: String): String = "polygons/rules-map/$polygonId"

    fun shopDetailRoute(shopId: String): String = "shops/detail/$shopId"

    fun shopEditorRoute(mode: EditorMode, refId: String): String =
        "shops/editor/${mode.routeValue}/$refId"

    fun serviceDetailRoute(serviceId: String): String = "services/detail/$serviceId"

    fun serviceEditorRoute(mode: EditorMode, refId: String): String =
        "services/editor/${mode.routeValue}/$refId"

    fun gameCalendarDetailRoute(gameId: String): String = "games/calendar/detail/$gameId"

    fun gameCalendarEditorRoute(mode: EditorMode, refId: String): String =
        "games/calendar/editor/${mode.routeValue}/$refId"

    fun gameCalendarLogisticsRoute(gameId: String): String = "games/calendar/logistics/$gameId"
}
