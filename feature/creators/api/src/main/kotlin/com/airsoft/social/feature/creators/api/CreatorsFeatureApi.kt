package com.airsoft.social.feature.creators.api

object CreatorsFeatureApi {
    const val CreatorsRoute = "creators"
    const val CreatorIdArg = "creatorId"
    const val CreatorDetailRoutePattern = "creators/detail/{$CreatorIdArg}"
    const val CreatorStudioRoutePattern = "creators/studio/{$CreatorIdArg}"

    fun creatorDetailRoute(creatorId: String): String = "creators/detail/$creatorId"
    fun creatorStudioRoute(creatorId: String): String = "creators/studio/$creatorId"
}
