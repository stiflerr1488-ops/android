package com.airsoft.social.feature.directories.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.feature.directories.api.DirectoriesFeatureApi

private const val DEFAULT_GAME_ID = "night-raid-north"
private const val DEFAULT_GAME_EDITOR_REF_ID = "draft-game-001"

private const val DEFAULT_RIDE_ID = "night-raid-north-trip"
private const val DEFAULT_RIDE_EDITOR_REF_ID = "draft-ride-001"

private const val DEFAULT_POLYGON_ID = "polygon-severny"
private const val DEFAULT_POLYGON_EDITOR_REF_ID = "draft-polygon-001"

private const val DEFAULT_SHOP_ID = "airsoft-hub"
private const val DEFAULT_SHOP_EDITOR_REF_ID = "draft-shop-001"

private const val DEFAULT_SERVICE_ID = "north-tech-tuning"
private const val DEFAULT_SERVICE_EDITOR_REF_ID = "draft-service-001"

fun NavGraphBuilder.registerDirectoriesFeatureGraph(
    navigateToDirectoryRoute: (String) -> Unit,
    onOpenEvents: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    composable(DirectoriesFeatureApi.GameCalendarSearchRoute) {
        GameCalendarSearchShellRoute(
            onOpenCalendar = { navigateToDirectoryRoute(DirectoriesFeatureApi.GameCalendarRoute) },
        )
    }
    composable(DirectoriesFeatureApi.RideShareSearchRoute) {
        RideShareSearchShellRoute(
            onOpenRideShare = { navigateToDirectoryRoute(DirectoriesFeatureApi.RideShareRoute) },
        )
    }
    composable(DirectoriesFeatureApi.PolygonsSearchRoute) {
        PolygonsSearchShellRoute(
            onOpenPolygons = { navigateToDirectoryRoute(DirectoriesFeatureApi.PolygonsRoute) },
        )
    }
    composable(DirectoriesFeatureApi.ShopsSearchRoute) {
        ShopsSearchShellRoute(
            onOpenShops = { navigateToDirectoryRoute(DirectoriesFeatureApi.ShopsRoute) },
        )
    }
    composable(DirectoriesFeatureApi.ServicesSearchRoute) {
        ServicesSearchShellRoute(
            onOpenServices = { navigateToDirectoryRoute(DirectoriesFeatureApi.ServicesRoute) },
        )
    }
    composable(DirectoriesFeatureApi.GameCalendarRoute) {
        GameCalendarShellRoute(
            onOpenEventsPage = onOpenEvents,
            onOpenGameDetail = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.gameCalendarDetailRoute(DEFAULT_GAME_ID))
            },
            onOpenGameEditor = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.gameCalendarEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_GAME_EDITOR_REF_ID,
                    ),
                )
            },
            onOpenGameLogistics = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.gameCalendarLogisticsRoute(DEFAULT_GAME_ID))
            },
        )
    }
    composable(
        route = DirectoriesFeatureApi.GameCalendarDetailRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.GAME_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val gameId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.GAME_ID_ARG)
            .orEmpty()
        GameCalendarDetailSkeletonRoute(gameId = gameId)
    }
    composable(
        route = DirectoriesFeatureApi.GameCalendarEditorRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.GAME_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(DirectoriesFeatureApi.GAME_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.GAME_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.GAME_EDITOR_REF_ID_ARG)
            .orEmpty()
        GameCalendarEditorSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
    composable(
        route = DirectoriesFeatureApi.GameCalendarLogisticsRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.GAME_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val gameId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.GAME_ID_ARG)
            .orEmpty()
        GameCalendarLogisticsSkeletonRoute(gameId = gameId)
    }
    composable(DirectoriesFeatureApi.RideShareRoute) {
        RideShareShellRoute(
            onOpenCalendar = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.GameCalendarRoute)
            },
            onOpenPolygons = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.PolygonsRoute)
            },
            onOpenTripDetail = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.rideShareTripDetailRoute(DEFAULT_RIDE_ID),
                )
            },
            onOpenCreateTrip = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.rideShareTripEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_RIDE_EDITOR_REF_ID,
                    ),
                )
            },
            onOpenMyRoute = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.RideShareMyRoute)
            },
        )
    }
    composable(DirectoriesFeatureApi.RideShareMyRoute) {
        RideShareMyRouteSkeletonRoute()
    }
    composable(
        route = DirectoriesFeatureApi.RideShareTripDetailRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.RIDE_SHARE_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val rideId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.RIDE_SHARE_ID_ARG)
            .orEmpty()
        RideShareTripDetailSkeletonRoute(rideId = rideId)
    }
    composable(
        route = DirectoriesFeatureApi.RideShareTripEditorRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.RIDE_SHARE_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(DirectoriesFeatureApi.RIDE_SHARE_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.RIDE_SHARE_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.RIDE_SHARE_EDITOR_REF_ID_ARG)
            .orEmpty()
        RideShareTripEditorSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
    composable(DirectoriesFeatureApi.PolygonsRoute) {
        PolygonsShellRoute(
            onOpenCalendar = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.GameCalendarRoute)
            },
            onOpenPolygonDetail = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.polygonDetailRoute(DEFAULT_POLYGON_ID))
            },
            onOpenPolygonEditor = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.polygonEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_POLYGON_EDITOR_REF_ID,
                    ),
                )
            },
            onOpenPolygonRulesMap = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.polygonRulesMapRoute(DEFAULT_POLYGON_ID),
                )
            },
        )
    }
    composable(
        route = DirectoriesFeatureApi.PolygonDetailRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.POLYGON_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val polygonId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.POLYGON_ID_ARG)
            .orEmpty()
        PolygonDetailSkeletonRoute(polygonId = polygonId)
    }
    composable(
        route = DirectoriesFeatureApi.PolygonEditorRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.POLYGON_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(DirectoriesFeatureApi.POLYGON_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.POLYGON_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.POLYGON_EDITOR_REF_ID_ARG)
            .orEmpty()
        PolygonEditorSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
    composable(
        route = DirectoriesFeatureApi.PolygonRulesMapRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.POLYGON_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val polygonId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.POLYGON_ID_ARG)
            .orEmpty()
        PolygonRulesMapSkeletonRoute(polygonId = polygonId)
    }
    composable(DirectoriesFeatureApi.ShopsRoute) {
        ShopsShellRoute(
            onOpenMarketplace = onOpenMarketplace,
            onOpenShopDetail = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.shopDetailRoute(DEFAULT_SHOP_ID))
            },
            onOpenShopEditor = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.shopEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_SHOP_EDITOR_REF_ID,
                    ),
                )
            },
        )
    }
    composable(
        route = DirectoriesFeatureApi.ShopDetailRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.SHOP_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val shopId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SHOP_ID_ARG)
            .orEmpty()
        ShopDetailSkeletonRoute(shopId = shopId)
    }
    composable(
        route = DirectoriesFeatureApi.ShopEditorRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.SHOP_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(DirectoriesFeatureApi.SHOP_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SHOP_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SHOP_EDITOR_REF_ID_ARG)
            .orEmpty()
        ShopEditorSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
    composable(DirectoriesFeatureApi.ServicesRoute) {
        ServicesShellRoute(
            onOpenProfile = onOpenProfile,
            onOpenServiceDetail = {
                navigateToDirectoryRoute(DirectoriesFeatureApi.serviceDetailRoute(DEFAULT_SERVICE_ID))
            },
            onOpenServiceEditor = {
                navigateToDirectoryRoute(
                    DirectoriesFeatureApi.serviceEditorRoute(
                        mode = EditorMode.Create,
                        refId = DEFAULT_SERVICE_EDITOR_REF_ID,
                    ),
                )
            },
        )
    }
    composable(
        route = DirectoriesFeatureApi.ServiceDetailRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.SERVICE_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val serviceId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SERVICE_ID_ARG)
            .orEmpty()
        ServiceDetailSkeletonRoute(serviceId = serviceId)
    }
    composable(
        route = DirectoriesFeatureApi.ServiceEditorRoutePattern,
        arguments = listOf(
            navArgument(DirectoriesFeatureApi.SERVICE_EDITOR_MODE_ARG) { type = NavType.StringType },
            navArgument(DirectoriesFeatureApi.SERVICE_EDITOR_REF_ID_ARG) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val editorModeRaw = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SERVICE_EDITOR_MODE_ARG)
            .orEmpty()
        val editorMode = EditorMode.fromRouteValue(editorModeRaw) ?: EditorMode.Draft
        val editorRefId = backStackEntry.arguments
            ?.getString(DirectoriesFeatureApi.SERVICE_EDITOR_REF_ID_ARG)
            .orEmpty()
        ServiceEditorSkeletonRoute(
            editorMode = editorMode,
            editorRefId = editorRefId,
        )
    }
}
