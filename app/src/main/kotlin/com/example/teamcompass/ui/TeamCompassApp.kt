package com.example.teamcompass.ui

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamcompass.BuildConfig
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt
import java.util.Locale
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.ControlSize
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.components.TeamRosterBottomSheet
import com.example.teamcompass.ui.components.TeamRosterItemUi
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.CombatRole
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamOrgPath
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.VehicleRole


internal const val ROUTE_SPLASH = "splash"
internal const val ROUTE_JOIN = "join"
internal const val ROUTE_COMPASS = "compass"
internal const val ROUTE_SETTINGS = "settings"
internal const val ROUTE_STRUCTURE = "structure"
internal const val ROUTE_FULLSCREEN_MAP = "fullscreen_map"

private data class TeamCompassShellState(
    val isAuthReady: Boolean = false,
    val teamCode: String? = null,
    val backendAvailable: Boolean = true,
    val isBackendStale: Boolean = false,
)

@Composable
fun TeamCompassApp(
    vm: TeamCompassViewModel = viewModel(),
    onLegacyTacticalOverview: (isAuthReady: Boolean, teamCode: String?, backendAvailable: Boolean, isBackendStale: Boolean) -> Unit = { _, _, _, _ -> },
) {
    val shellStateFlow = remember(vm) {
        vm.ui
            .map { state ->
                TeamCompassShellState(
                    isAuthReady = state.isAuthReady,
                    teamCode = state.teamCode,
                    backendAvailable = state.telemetry.backendAvailable,
                    isBackendStale = state.telemetry.isBackendStale,
                )
            }
            .distinctUntilChanged()
    }
    val shellState by shellStateFlow.collectAsStateWithLifecycle(
        initialValue = TeamCompassShellState(),
    )
    val latestLegacyOverviewReporter by rememberUpdatedState(onLegacyTacticalOverview)

    LaunchedEffect(shellState) {
        latestLegacyOverviewReporter(
            shellState.isAuthReady,
            shellState.teamCode,
            shellState.backendAvailable,
            shellState.isBackendStale,
        )
    }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val hostWindow = remember(ctx) { ctx.findHostActivity()?.window }
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val backendDownMessage = stringResource(R.string.vm_error_backend_unavailable_retrying)
    val locationServicesDisabledMessage = stringResource(R.string.vm_error_location_services_disabled)
    val locationDisabledDuringTrackingMessage = stringResource(R.string.vm_error_location_disabled_during_tracking)
    val retryActionLabel = stringResource(R.string.action_retry)
    val nav = rememberNavController()
    val navigator = remember {
        TeamCompassNavigator(
            routeSplash = ROUTE_SPLASH,
            routeJoin = ROUTE_JOIN,
            routeCompass = ROUTE_COMPASS,
            routeSettings = ROUTE_SETTINGS,
            routeStructure = ROUTE_STRUCTURE,
            routeFullscreenMap = ROUTE_FULLSCREEN_MAP,
        )
    }
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val backendBannerState = when {
        !shellState.backendAvailable -> BackendHealthBannerState.DOWN
        shellState.isBackendStale -> BackendHealthBannerState.STALE
        else -> null
    }

    DisposableEffect(lifecycleOwner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshLocationReadiness()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(vm, hostWindow) {
        vm.bindAutoBrightnessWindow(hostWindow)
        onDispose {
            vm.bindAutoBrightnessWindow(null)
        }
    }

    LaunchedEffect(
        vm,
        backendDownMessage,
        locationServicesDisabledMessage,
        locationDisabledDuringTrackingMessage,
    ) {
        vm.events.collect { event ->
            when (event) {
                is UiEvent.Error -> {
                    val shouldSuppressSnackbar = event.message == backendDownMessage ||
                        event.message == locationServicesDisabledMessage ||
                        event.message == locationDisabledDuringTrackingMessage
                    if (!shouldSuppressSnackbar) {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = retryActionLabel,
                            duration = SnackbarDuration.Long,
                        )
                    }
                }
            }
        }
    }

    // Keep navigation synced with team presence.
    // Protected routes are preserved unless team membership is lost.
    // IMPORTANT: Never call popBackStack() *after* navigate() to remove the previous screen.
    // That pattern can accidentally pop the newly-navigated destination too, leaving an empty
    // back stack ("black screen"). We use popUpTo inside navigate() instead.
    LaunchedEffect(shellState.teamCode, shellState.isAuthReady, route) {
        when (
            val command = navigator.resolve(
                isAuthReady = shellState.isAuthReady,
                teamCode = shellState.teamCode,
                currentRoute = route,
            )
        ) {
            TeamCompassNavCommand.None -> Unit
            is TeamCompassNavCommand.Navigate -> {
                if (BuildConfig.DEBUG) {
                    Log.d(
                        "NavDebug",
                        "Navigating from $route to ${command.destination} (reason=${command.reason}, teamCode=${shellState.teamCode}, isAuthReady=${shellState.isAuthReady})",
                    )
                }
                nav.navigate(command.destination) {
                    launchSingleTop = command.launchSingleTop
                    popUpTo(command.popUpTo) { inclusive = command.inclusive }
                }
            }
        }
    }

    LaunchedEffect(route) {
        val nextMode = if (
            BuildConfig.TEAM_VIEW_MODE_V2_ENABLED &&
            route == ROUTE_FULLSCREEN_MAP
        ) {
            TeamViewMode.COMMAND
        } else {
            TeamViewMode.COMBAT
        }
        vm.setTeamViewMode(nextMode)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
        ) {
            NavHost(navController = nav, startDestination = ROUTE_SPLASH) {
                composable(ROUTE_SPLASH) {
                    TacticalSplash(
                        isAuthReady = shellState.isAuthReady,
                        onDone = {
                            val command = navigator.resolveSplashDone(teamCode = shellState.teamCode)
                            if (BuildConfig.DEBUG) {
                                Log.d(
                                    "NavDebug",
                                    "Navigating from $ROUTE_SPLASH to ${command.destination} (reason=${command.reason}, teamCode=${shellState.teamCode}, isAuthReady=${shellState.isAuthReady})",
                                )
                            }
                            nav.navigate(command.destination) {
                                launchSingleTop = command.launchSingleTop
                                popUpTo(command.popUpTo) { inclusive = command.inclusive }
                            }
                        }
                    )
                }

                composable(ROUTE_JOIN) {
                    TeamCompassJoinRoute(vm = vm)
                }

                composable(ROUTE_COMPASS) {
                    TeamCompassCompassRoute(vm = vm, nav = nav)
                }

                composable(ROUTE_SETTINGS) {
                    TeamCompassSettingsRoute(vm = vm, nav = nav)
                }

                composable(ROUTE_STRUCTURE) {
                    TeamCompassStructureRoute(vm = vm, nav = nav)
                }

                composable(ROUTE_FULLSCREEN_MAP) {
                    TeamCompassFullscreenMapRoute(vm = vm, nav = nav)
                }
            }

            if (backendBannerState != null) {
                BackendHealthBanner(
                    state = backendBannerState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
            }
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext
        .takeIf { it !== this }
        ?.findHostActivity()

    else -> null
}
