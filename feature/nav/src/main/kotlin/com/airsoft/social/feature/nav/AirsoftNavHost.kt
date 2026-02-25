package com.airsoft.social.feature.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.core.ui.ComingSoonScreen
import com.airsoft.social.feature.auth.impl.AuthPlaceholderScreen
import com.airsoft.social.feature.chats.impl.ChatRoomSkeletonScreen
import com.airsoft.social.feature.chats.impl.ChatsPlaceholderScreen
import com.airsoft.social.feature.chats.impl.PlayerCardSkeletonScreen
import com.airsoft.social.feature.events.impl.EventCreateEditSkeletonScreen
import com.airsoft.social.feature.events.impl.EventDetailSkeletonScreen
import com.airsoft.social.feature.events.impl.EventsPlaceholderScreen
import com.airsoft.social.feature.marketplace.impl.MarketplaceCreateListingSkeletonScreen
import com.airsoft.social.feature.marketplace.impl.MarketplaceListingDetailSkeletonScreen
import com.airsoft.social.feature.marketplace.impl.MarketplacePlaceholderScreen
import com.airsoft.social.feature.onboarding.impl.OnboardingPlaceholderScreen
import com.airsoft.social.feature.profile.impl.EditProfileSkeletonScreen
import com.airsoft.social.feature.profile.impl.ProfileInventorySkeletonScreen
import com.airsoft.social.feature.profile.impl.ProfilePlaceholderScreen
import com.airsoft.social.feature.teams.impl.TeamCreateEditSkeletonScreen
import com.airsoft.social.feature.teams.impl.TeamDetailSkeletonScreen
import com.airsoft.social.feature.teams.impl.TeamsPlaceholderScreen
import com.airsoft.social.feature.tactical.impl.TacticalBridgeRoute
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AirsoftNavHost(
    bootstrapRoute: String,
    tacticalOverviewPort: TacticalOverviewPort,
    onCompleteOnboarding: () -> Unit,
    onMockSignIn: () -> Unit,
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
        if (currentRoute == null) return@LaunchedEffect
        if (currentRoute == bootstrapRoute) return@LaunchedEffect
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
                        navController.navigate(com.airsoft.social.feature.profile.api.ProfileFeatureApi.ROUTE)
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
                    DrawerItem("Events / Tournaments", "E") {
                        navController.navigate(com.airsoft.social.feature.events.api.EventsFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Marketplace", "M") {
                        navController.navigate(com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi.ROUTE)
                        scope.launch { drawerState.close() }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DrawerItem("Settings", "S") {
                        navController.navigate(AirsoftRoutes.Settings)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Support", "?") {
                        navController.navigate(AirsoftRoutes.Support)
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("About", "i") {
                        navController.navigate(AirsoftRoutes.About)
                        scope.launch { drawerState.close() }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            navController.navigate(AirsoftRoutes.TacticalReserved)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7A00),
                            contentColor = Color(0xFF121212),
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("V BOI!")
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
                    actions = {
                        TextButton(onClick = { navController.navigate(AirsoftRoutes.Search) }) {
                            Text("Search")
                        }
                        TextButton(onClick = { navController.navigate(AirsoftRoutes.Notifications) }) {
                            Text("Inbox")
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
                composable(AirsoftRoutes.Notifications) {
                    NotificationsShellRoute()
                }
                composable(AirsoftRoutes.Search) {
                    SearchShellRoute(
                        onOpenPlayers = {
                            navController.navigate(com.airsoft.social.feature.chats.api.ChatsFeatureApi.ROUTE)
                        },
                        onOpenTeams = {
                            navController.navigate(com.airsoft.social.feature.teams.api.TeamsFeatureApi.ROUTE)
                        },
                        onOpenEvents = {
                            navController.navigate(com.airsoft.social.feature.events.api.EventsFeatureApi.ROUTE)
                        },
                        onOpenMarketplace = {
                            navController.navigate(com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi.ROUTE)
                        },
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
                        onPrimaryAction = {
                            onMockSignIn()
                            navController.navigate(AirsoftRoutes.topLevelDestinations.first().route) {
                                popUpTo(AirsoftRoutes.authRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(com.airsoft.social.feature.chats.api.ChatsFeatureApi.ROUTE) {
                    ChatsPlaceholderScreen(
                        onOpenChatRoomDemo = { navController.navigate(AirsoftRoutes.ChatRoomDemo) },
                        onOpenPlayerCardDemo = { navController.navigate(AirsoftRoutes.PlayerCardDemo) },
                    )
                }
                composable(com.airsoft.social.feature.teams.api.TeamsFeatureApi.ROUTE) {
                    TeamsPlaceholderScreen(
                        onOpenTeamDetailDemo = { navController.navigate(AirsoftRoutes.TeamDetailDemo) },
                        onOpenTeamCreateDemo = { navController.navigate(AirsoftRoutes.TeamCreateDemo) },
                    )
                }
                composable(com.airsoft.social.feature.events.api.EventsFeatureApi.ROUTE) {
                    EventsPlaceholderScreen(
                        onOpenEventDetailDemo = { navController.navigate(AirsoftRoutes.EventDetailDemo) },
                        onOpenEventCreateDemo = { navController.navigate(AirsoftRoutes.EventCreateDemo) },
                    )
                }
                composable(com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi.ROUTE) {
                    MarketplacePlaceholderScreen(
                        onOpenListingDetailDemo = {
                            navController.navigate(AirsoftRoutes.MarketplaceListingDetailDemo)
                        },
                        onOpenCreateListingDemo = {
                            navController.navigate(AirsoftRoutes.MarketplaceCreateListingDemo)
                        },
                    )
                }
                composable(com.airsoft.social.feature.profile.api.ProfileFeatureApi.ROUTE) {
                    ProfilePlaceholderScreen(
                        onOpenEditProfileDemo = { navController.navigate(AirsoftRoutes.ProfileEditDemo) },
                        onOpenInventoryDemo = { navController.navigate(AirsoftRoutes.ProfileInventoryDemo) },
                        onPrimaryAction = {
                            onSignOut()
                            navController.navigate(AirsoftRoutes.authRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(AirsoftRoutes.Settings) {
                    SettingsShellRoute(
                        onOpenSupport = { navController.navigate(AirsoftRoutes.Support) },
                        onOpenAbout = { navController.navigate(AirsoftRoutes.About) },
                    )
                }
                composable(AirsoftRoutes.Support) {
                    SupportShellRoute(
                        onOpenNotifications = { navController.navigate(AirsoftRoutes.Notifications) },
                    )
                }
                composable(AirsoftRoutes.About) {
                    AboutShellRoute()
                }
                composable(AirsoftRoutes.ChatRoomDemo) { ChatRoomSkeletonScreen() }
                composable(AirsoftRoutes.PlayerCardDemo) { PlayerCardSkeletonScreen() }
                composable(AirsoftRoutes.TeamDetailDemo) { TeamDetailSkeletonScreen() }
                composable(AirsoftRoutes.TeamCreateDemo) { TeamCreateEditSkeletonScreen() }
                composable(AirsoftRoutes.EventDetailDemo) { EventDetailSkeletonScreen() }
                composable(AirsoftRoutes.EventCreateDemo) { EventCreateEditSkeletonScreen() }
                composable(AirsoftRoutes.MarketplaceListingDetailDemo) {
                    MarketplaceListingDetailSkeletonScreen()
                }
                composable(AirsoftRoutes.MarketplaceCreateListingDemo) {
                    MarketplaceCreateListingSkeletonScreen()
                }
                composable(AirsoftRoutes.ProfileEditDemo) { EditProfileSkeletonScreen() }
                composable(AirsoftRoutes.ProfileInventoryDemo) { ProfileInventorySkeletonScreen() }
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
