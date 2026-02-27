package com.airsoft.social.feature.nav

import com.airsoft.social.core.model.AccountAccess
import com.airsoft.social.feature.admin.api.AdminFeatureApi
import com.airsoft.social.feature.directories.api.DirectoriesFeatureApi

internal enum class RouteAccessRequirement {
    None,
    Registration,
    CommercialRole,
    ModeratorRole,
    AdminRole,
}

internal data class RouteAccessDecision(
    val allowed: Boolean,
    val requirement: RouteAccessRequirement = RouteAccessRequirement.None,
)

internal fun AccountAccess.routeAccessDecision(route: String): RouteAccessDecision = when {
    isProfileEditRoute(route) -> if (canEditProfile) allowRoute() else requireRegistration()
    isTeamEditorRoute(route) -> if (canEditProfile) allowRoute() else requireRegistration()
    isMarketplaceEditorRoute(route) ->
        if (canCreateMarketplaceListings) allowRoute() else requireRegistration()

    isRideShareEditorRoute(route) ->
        if (canCreateRideShareListings) allowRoute() else requireRegistration()

    isEventEditorRoute(route) || isCalendarGameEditorRoute(route) ->
        if (canCreateGameEvents) allowRoute() else requireCommercialRole()

    isShopEditorRoute(route) || isServiceEditorRoute(route) ->
        if (canCreateShopListings) allowRoute() else requireCommercialRole()

    isModerationRoute(route) ->
        if (isModerator) allowRoute() else requireModeratorRole()

    isAdminRoute(route) ->
        if (isAdmin) allowRoute() else requireAdminRole()

    else -> allowRoute()
}

internal fun AccountAccess.canOpenRoute(route: String): Boolean =
    routeAccessDecision(route).allowed

private fun allowRoute(): RouteAccessDecision = RouteAccessDecision(allowed = true)

private fun AccountAccess.requireRegistration(): RouteAccessDecision =
    RouteAccessDecision(
        allowed = false,
        requirement = RouteAccessRequirement.Registration,
    )

private fun AccountAccess.requireCommercialRole(): RouteAccessDecision = when {
    !isRegistered -> requireRegistration()
    else -> RouteAccessDecision(
        allowed = false,
        requirement = RouteAccessRequirement.CommercialRole,
    )
}

private fun AccountAccess.requireModeratorRole(): RouteAccessDecision = when {
    !isRegistered -> requireRegistration()
    else -> RouteAccessDecision(
        allowed = false,
        requirement = RouteAccessRequirement.ModeratorRole,
    )
}

private fun AccountAccess.requireAdminRole(): RouteAccessDecision = when {
    !isRegistered -> requireRegistration()
    else -> RouteAccessDecision(
        allowed = false,
        requirement = RouteAccessRequirement.AdminRole,
    )
}

private fun isProfileEditRoute(route: String): Boolean =
    route.startsWith("profile/") && route.endsWith("/edit")

private fun isModerationRoute(route: String): Boolean =
    route == AdminFeatureApi.ModerationRoute || route.startsWith("${AdminFeatureApi.ModerationRoute}/")

private fun isTeamEditorRoute(route: String): Boolean =
    route.startsWith("teams/editor/")

private fun isAdminRoute(route: String): Boolean =
    route.startsWith("admin/")

private fun isEventEditorRoute(route: String): Boolean =
    route.startsWith("events/editor/")

private fun isMarketplaceEditorRoute(route: String): Boolean =
    route.startsWith("marketplace/editor/")

private fun isRideShareEditorRoute(route: String): Boolean =
    route.startsWith(
        DirectoriesFeatureApi.RideShareTripEditorRoutePattern.substringBefore("{"),
    )

private fun isCalendarGameEditorRoute(route: String): Boolean =
    route.startsWith(
        DirectoriesFeatureApi.GameCalendarEditorRoutePattern.substringBefore("{"),
    )

private fun isShopEditorRoute(route: String): Boolean =
    route.startsWith(
        DirectoriesFeatureApi.ShopEditorRoutePattern.substringBefore("{"),
    )

private fun isServiceEditorRoute(route: String): Boolean =
    route.startsWith(
        DirectoriesFeatureApi.ServiceEditorRoutePattern.substringBefore("{"),
    )
