package com.example.teamcompass.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TeamCompassNavigatorTest {

    private val navigator = TeamCompassNavigator()

    @Test
    fun resolveSplashDone_whenTeamMissing_navigatesToJoin() {
        val command = navigator.resolveSplashDone(teamCode = null)

        assertEquals(
            TeamCompassNavCommand.Navigate(
                destination = "join",
                popUpTo = "splash",
                reason = "splash_done",
            ),
            command,
        )
    }

    @Test
    fun resolveSplashDone_whenTeamPresent_navigatesToCompass() {
        val command = navigator.resolveSplashDone(teamCode = "123456")

        assertEquals(
            TeamCompassNavCommand.Navigate(
                destination = "compass",
                popUpTo = "splash",
                reason = "splash_done",
            ),
            command,
        )
    }

    @Test
    fun resolve_whenAuthNotReady_returnsNone() {
        val command = navigator.resolve(
            isAuthReady = false,
            teamCode = null,
            currentRoute = "join",
        )

        assertEquals(TeamCompassNavCommand.None, command)
    }

    @Test
    fun resolve_whenCurrentRouteIsSplash_returnsNone() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = "123456",
            currentRoute = "splash",
        )

        assertEquals(TeamCompassNavCommand.None, command)
    }

    @Test
    fun resolve_whenTeamMissingFromCompass_navigatesToJoin() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = null,
            currentRoute = "compass",
        )

        assertEquals(
            TeamCompassNavCommand.Navigate(
                destination = "join",
                popUpTo = "compass",
                reason = "sync_team_membership",
            ),
            command,
        )
    }

    @Test
    fun resolve_whenTeamPresentFromJoin_navigatesToCompass() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = "123456",
            currentRoute = "join",
        )

        assertEquals(
            TeamCompassNavCommand.Navigate(
                destination = "compass",
                popUpTo = "join",
                reason = "sync_team_membership",
            ),
            command,
        )
    }

    @Test
    fun resolve_whenProtectedRouteAndTeamMissing_navigatesToJoinPoppingProtectedRoute() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = null,
            currentRoute = "settings",
        )

        assertEquals(
            TeamCompassNavCommand.Navigate(
                destination = "join",
                popUpTo = "compass",
                reason = "missing_team_from_protected_route",
            ),
            command,
        )
    }

    @Test
    fun resolve_whenProtectedRouteAndTeamPresent_returnsNone() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = "123456",
            currentRoute = "fullscreen_map",
        )

        assertEquals(TeamCompassNavCommand.None, command)
    }

    @Test
    fun resolve_whenAlreadyOnDesiredRoute_returnsNone() {
        val command = navigator.resolve(
            isAuthReady = true,
            teamCode = null,
            currentRoute = "join",
        )

        assertEquals(TeamCompassNavCommand.None, command)
    }
}
