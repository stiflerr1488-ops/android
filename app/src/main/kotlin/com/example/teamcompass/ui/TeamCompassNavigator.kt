package com.example.teamcompass.ui

internal sealed interface TeamCompassNavCommand {
    data object None : TeamCompassNavCommand

    data class Navigate(
        val destination: String,
        val popUpTo: String,
        val inclusive: Boolean = true,
        val launchSingleTop: Boolean = true,
        val reason: String,
    ) : TeamCompassNavCommand
}

/**
 * Pure navigation decision engine for top-level app routes.
 *
 * This class does not call NavController directly; it only returns a command
 * describing what transition should happen for a given state snapshot.
 */
internal class TeamCompassNavigator(
    private val routeSplash: String = "splash",
    private val routeJoin: String = "join",
    private val routeCompass: String = "compass",
    private val routeSettings: String = "settings",
    private val routeStructure: String = "structure",
    private val routeFullscreenMap: String = "fullscreen_map",
) {
    fun resolveSplashDone(teamCode: String?): TeamCompassNavCommand.Navigate {
        val destination = if (teamCode.isNullOrBlank()) routeJoin else routeCompass
        return TeamCompassNavCommand.Navigate(
            destination = destination,
            popUpTo = routeSplash,
            reason = "splash_done",
        )
    }

    fun resolve(
        isAuthReady: Boolean,
        teamCode: String?,
        currentRoute: String?,
    ): TeamCompassNavCommand {
        if (!isAuthReady) return TeamCompassNavCommand.None
        if (currentRoute == null || currentRoute == routeSplash) return TeamCompassNavCommand.None

        val desired = if (teamCode.isNullOrBlank()) routeJoin else routeCompass

        if (isProtectedRoute(currentRoute)) {
            return if (desired == routeJoin) {
                TeamCompassNavCommand.Navigate(
                    destination = routeJoin,
                    // Drop compass stack when team membership is lost while user is on a protected route
                    // (settings/structure/fullscreen map). This prevents back navigation from returning
                    // to a stale compass route with missing team context.
                    popUpTo = routeCompass,
                    reason = "missing_team_from_protected_route",
                )
            } else {
                TeamCompassNavCommand.None
            }
        }

        if (desired == currentRoute) return TeamCompassNavCommand.None

        val popRoute = if (desired == routeCompass) routeJoin else routeCompass
        return TeamCompassNavCommand.Navigate(
            destination = desired,
            popUpTo = popRoute,
            reason = "sync_team_membership",
        )
    }

    private fun isProtectedRoute(route: String): Boolean {
        return route == routeSettings || route == routeStructure || route == routeFullscreenMap
    }
}
