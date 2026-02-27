package com.airsoft.social.feature.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.core.ui.ComingSoonScreen
import com.airsoft.social.feature.auth.impl.AuthPlaceholderScreen
import com.airsoft.social.feature.chats.impl.ChatsPlaceholderScreen
import com.airsoft.social.feature.events.impl.EventsPlaceholderScreen
import com.airsoft.social.feature.marketplace.impl.MarketplacePlaceholderScreen
import com.airsoft.social.feature.onboarding.impl.OnboardingPlaceholderScreen
import com.airsoft.social.feature.profile.api.ProfileFeatureApi
import com.airsoft.social.feature.profile.impl.EditProfileRoute
import com.airsoft.social.feature.profile.impl.ProfilePlaceholderScreen
import com.airsoft.social.feature.teams.impl.TeamsPlaceholderScreen
import com.airsoft.social.feature.tactical.impl.TacticalBridgeRoute
import kotlinx.coroutines.launch

private const val SELF_USER_ID = "self"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AirsoftNavHost(
    bootstrapRoute: String,
    tacticalOverviewPort: TacticalOverviewPort,
    onCompleteOnboarding: () -> Unit,
    onSignOut: () -> Unit,
    onOpenLegacyTactical: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    LaunchedEffect(bootstrapRoute) {
        if (currentRoute == null || currentRoute == bootstrapRoute) return@LaunchedEffect
        navController.navigate(bootstrapRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    val topLevel = AirsoftRoutes.topLevelDestinations
    val topLevelRoutes = topLevel.map { it.route }.toSet()
    val showBottomBar = currentRoute in topLevelRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Airsoft Social",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    )
                    DrawerItem("Profile", "P") {
                        navController.navigate(ProfileFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Players / Chats", "C") {
                        navController.navigate(com.airsoft.social.feature.chats.api.ChatsFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Teams", "T") {
                        navController.navigate(com.airsoft.social.feature.teams.api.TeamsFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Events", "E") {
                        navController.navigate(com.airsoft.social.feature.events.api.EventsFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Marketplace", "M") {
                        navController.navigate(com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DrawerItem("Tactical", "V") {
                        navController.navigate(AirsoftRoutes.TacticalReserved)
                        scope.launch { drawerState.close() }
                    }
                }
            }
        },
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text("Airsoft Social") },
                    navigationIcon = {
                        TextButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("Menu")
                        }
                    },
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        topLevel.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(destination.label.take(1)) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = bootstrapRoute,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(AirsoftRoutes.Splash) {
                    ComingSoonScreen(
                        title = "Splash",
                        body = "Bootstrap placeholder for the new app shell.",
                    )
                }
                composable(AirsoftRoutes.onboardingRoute) {
                    OnboardingPlaceholderScreen(
                        onPrimaryAction = {
                            onCompleteOnboarding()
                            navController.navigate(AirsoftRoutes.authRoute) {
                                popUpTo(AirsoftRoutes.onboardingRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(AirsoftRoutes.authRoute) {
                    AuthPlaceholderScreen(
                        onAuthenticated = {
                            navController.navigate(AirsoftRoutes.topLevelDestinations.first().route) {
                                popUpTo(AirsoftRoutes.authRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(com.airsoft.social.feature.chats.api.ChatsFeatureApi.ROUTE) {
                    ChatsPlaceholderScreen()
                }
                composable(com.airsoft.social.feature.teams.api.TeamsFeatureApi.ROUTE) {
                    TeamsPlaceholderScreen()
                }
                composable(com.airsoft.social.feature.events.api.EventsFeatureApi.ROUTE) {
                    EventsPlaceholderScreen()
                }
                composable(com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi.ROUTE) {
                    MarketplacePlaceholderScreen()
                }
                composable(ProfileFeatureApi.ROUTE) {
                    ProfilePlaceholderScreen(
                        onOpenEditProfile = { userId ->
                            navController.navigate(ProfileFeatureApi.profileEditRoute(userId))
                        },
                        onOpenPrivacySettings = { userId ->
                            navController.navigate(ProfileFeatureApi.profileEditRoute(userId))
                        },
                        onSignOut = {
                            onSignOut()
                            navController.navigate(AirsoftRoutes.authRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(
                    route = ProfileFeatureApi.ProfileEditRoutePattern,
                    arguments = listOf(
                        navArgument(ProfileFeatureApi.USER_ID_ARG) {
                            type = NavType.StringType
                            defaultValue = SELF_USER_ID
                        },
                    ),
                ) { entry ->
                    val userId = entry.arguments?.getString(ProfileFeatureApi.USER_ID_ARG) ?: SELF_USER_ID
                    EditProfileRoute(
                        userId = userId,
                        onSaved = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() },
                    )
                }
                composable(AirsoftRoutes.TacticalReserved) {
                    TacticalBridgeRoute(
                        tacticalOverviewPort = tacticalOverviewPort,
                        onOpenLegacyTactical = onOpenLegacyTactical,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    iconText: String,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Text(iconText) },
        selected = false,
        onClick = onClick,
    )
}
