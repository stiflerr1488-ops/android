package com.airsoft.social.feature.creators.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airsoft.social.feature.creators.api.CreatorsFeatureApi

private const val DEFAULT_CREATOR_ID = "teiwaz-media"

fun NavGraphBuilder.registerCreatorsFeatureGraph(
    navigateToCreatorDetail: (String) -> Unit,
    navigateToCreatorStudio: (String) -> Unit,
) {
    composable(CreatorsFeatureApi.CreatorsRoute) {
        CreatorsShellRoute(
            onOpenCreatorDetail = {
                navigateToCreatorDetail(CreatorsFeatureApi.creatorDetailRoute(DEFAULT_CREATOR_ID))
            },
            onOpenCreatorStudio = {
                navigateToCreatorStudio(CreatorsFeatureApi.creatorStudioRoute(DEFAULT_CREATOR_ID))
            },
        )
    }
    composable(
        route = CreatorsFeatureApi.CreatorDetailRoutePattern,
        arguments = listOf(
            navArgument(CreatorsFeatureApi.CreatorIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val creatorId = backStackEntry.arguments
            ?.getString(CreatorsFeatureApi.CreatorIdArg)
            .orEmpty()
        CreatorDetailSkeletonRoute(creatorId = creatorId)
    }
    composable(
        route = CreatorsFeatureApi.CreatorStudioRoutePattern,
        arguments = listOf(
            navArgument(CreatorsFeatureApi.CreatorIdArg) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val creatorId = backStackEntry.arguments
            ?.getString(CreatorsFeatureApi.CreatorIdArg)
            .orEmpty()
        CreatorStudioSkeletonRoute(creatorId = creatorId)
    }
}
