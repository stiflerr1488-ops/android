package com.example.teamcompass.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.LocationPoint
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_JOIN = "join"
private const val ROUTE_COMPASS = "compass"
private const val ROUTE_SETTINGS = "settings"

private enum class TargetsSortMode { DISTANCE, FRESHNESS }

@Composable
fun TeamCompassApp(vm: TeamCompassViewModel = viewModel()) {
    val state by vm.ui.collectAsState()

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is UiEvent.Error -> snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = "Повторить"
                )
            }
        }
    }

    // Keep navigation synced with team presence (but don't yank user out of Settings).
    // IMPORTANT: Never call popBackStack() *after* navigate() to remove the previous screen.
    // That pattern can accidentally pop the newly-navigated destination too, leaving an empty
    // back stack ("black screen"). We use popUpTo inside navigate() instead.
    LaunchedEffect(state.teamCode, state.isAuthReady, route) {
        if (!state.isAuthReady) return@LaunchedEffect
        if (route == null || route == ROUTE_SPLASH) return@LaunchedEffect
        if (route == ROUTE_SETTINGS) return@LaunchedEffect

        val desired = if (state.teamCode == null) ROUTE_JOIN else ROUTE_COMPASS
        if (desired == route) return@LaunchedEffect

        val popRoute = if (desired == ROUTE_COMPASS) ROUTE_JOIN else ROUTE_COMPASS
        nav.navigate(desired) {
            launchSingleTop = true
            popUpTo(popRoute) { inclusive = true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController = nav, startDestination = ROUTE_SPLASH) {
                composable(ROUTE_SPLASH) {
                    TacticalSplash(
                        isAuthReady = state.isAuthReady,
                        hasTeam = state.teamCode != null,
                        onDone = { dest ->
                            nav.navigate(dest) {
                                popUpTo(ROUTE_SPLASH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(ROUTE_JOIN) {
                    if (!state.isAuthReady) {
                        LoadingScreen()
                    } else {
                        JoinScreen(
                            callsign = state.callsign,
                            onCallsignChange = vm::setCallsign,
                            onCreate = { vm.createTeam() },
                            onJoin = { code -> vm.joinTeam(code) },
                            isBusy = state.isBusy,
                            savedCodeHint = null
                        )
                    }
                }

                composable(ROUTE_COMPASS) {
                    if (!state.isAuthReady) {
                        LoadingScreen()
                    } else if (state.teamCode == null) {
                        // If team was cleared, bounce back to Join.
                        LaunchedEffect(Unit) { nav.navigate(ROUTE_JOIN) { popUpTo(ROUTE_COMPASS) { inclusive = true } } }
                    } else {
                        CompassScreen(
                            state = state,
                            onRequestPermission = vm::setLocationPermission,
                            onStartTracking = { vm.startTracking(state.defaultMode) },
                            onStopTracking = vm::stopTracking,
                            onLeave = vm::leaveTeam,
                            targetsProvider = { now -> vm.computeTargets(now) },
                            onCopyCode = { copyToClipboard(ctx, state.teamCode ?: "") },
                            onOpenSettings = { nav.navigate(ROUTE_SETTINGS) },
                            onTogglePlayerMode = vm::togglePlayerMode,
                            onSos = vm::toggleSos,
                            onAddPointAt = vm::addPointAt,
                            onUpdatePoint = vm::updatePoint,
                            onDeletePoint = vm::deletePoint,
                            onQuickCommand = vm::sendQuickCommand,
                            onEnemyPing = vm::addEnemyPing,
                            onEnemyMarkEnabled = vm::setEnemyMarkEnabled,
                            onImportMap = vm::importTacticalMap,
                            onMapEnabled = vm::setMapEnabled,
                            onMapOpacity = vm::setMapOpacity,
                            onClearMap = vm::clearTacticalMap,
                            onMarkHelpSeen = vm::markCompassHelpSeen,
                        )
                    }
                }

                composable(ROUTE_SETTINGS) {
                    SettingsScreen(
                        state = state,
                        onBack = { nav.popBackStack() },
                        onDefaultMode = vm::setDefaultMode,
                        onGamePolicy = vm::setGamePolicy,
                        onSilentPolicy = vm::setSilentPolicy
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    cm?.setPrimaryClip(ClipData.newPlainText("Team code", text))
}

private data class PointMarkerUi(
    val id: String,
    val isTeam: Boolean,
    val createdBy: String?,
    val lat: Double,
    val lon: Double,
    val label: String,
    val iconRaw: String,
    val posPx: Offset,
    val inRange: Boolean,
)

private data class PointDialogState(
    val id: String? = null,
    val isTeam: Boolean = true,
    val createdBy: String? = null,
    val lat: Double,
    val lon: Double,
    val initialLabel: String = "",
    val initialIconRaw: String = TacticalIconId.FLAG.raw,
)

private data class PointActionState(
    val marker: PointMarkerUi,
    val pressLat: Double,
    val pressLon: Double,
)

@Composable
private fun TacticalSplash(
    isAuthReady: Boolean,
    hasTeam: Boolean,
    onDone: (dest: String) -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    val latestOnDone by rememberUpdatedState(onDone)

    LaunchedEffect(isAuthReady) {
        if (!isAuthReady || started) return@LaunchedEffect
        started = true
        // Keep splash short; do not block when auth is already ready.
        delay(250)
        latestOnDone(if (hasTeam) ROUTE_COMPASS else ROUTE_JOIN)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_compass),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("TeamCompass", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "ТАКТИЧЕСКИЙ РАДАР • КОМАНДА",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.lg - Spacing.xs))

            AnimatedVisibility(
                visible = !isAuthReady,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(10.dp))
                    Text("Подключаемся…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Подключаемся…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun JoinScreen(
    callsign: String,
    onCallsignChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
    isBusy: Boolean,
    savedCodeHint: String?
) {
    var code by remember { mutableStateOf(savedCodeHint ?: "") }
    val normalizedCallsign = callsign.trim()
    val callsignValid = normalizedCallsign.length in 3..16 && normalizedCallsign.all { it.isLetterOrDigit() || it == '_' || it == '-' }
    val callsignError = normalizedCallsign.isNotEmpty() && !callsignValid
    val codeNormalized = code.filter(Char::isDigit).take(6)
    val codeError = codeNormalized.isNotEmpty() && codeNormalized.length != 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.md)
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(Spacing.xl + Spacing.xs)
                        .clip(RoundedCornerShape(Spacing.md))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_compass),
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.lg + Spacing.xs)
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Column {
                    Text("TeamCompass", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Командный радар для леса", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(Spacing.lg - Spacing.xs))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.lg - Spacing.xs),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    Text("Вход в команду", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Шаг 1 из 2: укажите позывной. Затем создайте команду или войдите по коду.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = callsign,
                        onValueChange = onCallsignChange,
                        label = { Text("Позывной") },
                        singleLine = true,
                        enabled = !isBusy,
                        isError = callsignError,
                        supportingText = {
                            if (callsignError) {
                                Text("Допустимо 3–16 символов: буквы, цифры, _ или -")
                            } else {
                                Text("3–16 символов: буквы, цифры, _ или -")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.md),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Text("Новая команда", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Создать новый код и поделиться им с группой.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = onCreate,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(Spacing.sm),
                                    enabled = !isBusy && callsignValid
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null)
                                    Spacer(Modifier.width(Spacing.xs))
                                    Text("Создать")
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.md),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Text("Вход по коду", fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = code,
                                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                                    label = { Text("Код (6 цифр)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    enabled = !isBusy,
                                    isError = codeError,
                                    supportingText = {
                                        if (codeError) {
                                            Text("Код должен содержать 6 цифр")
                                        } else {
                                            Text("Введи 6 цифр без пробелов")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                FilledTonalButton(
                                    onClick = { onJoin(codeNormalized) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(Spacing.sm),
                                    enabled = !isBusy && callsignValid && codeNormalized.length == 6
                                ) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null)
                                    Spacer(Modifier.width(Spacing.xs))
                                    Text("Войти")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "Позывной сохраняется на устройстве. Код можно отправить в чат.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isBusy,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f))
            )
        }

        AnimatedVisibility(
            visible = isBusy,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120))
        ) {
            Card(
                shape = RoundedCornerShape(Spacing.lg - Spacing.xs),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.lg - Spacing.xs, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(Spacing.md + Spacing.xs), strokeWidth = 2.dp)
                    Text("Подключаем к команде…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompassScreen(
    state: UiState,
    onRequestPermission: (Boolean) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onLeave: () -> Unit,
    targetsProvider: (Long) -> List<CompassTarget>,
    onCopyCode: () -> Unit,
    onOpenSettings: () -> Unit,
    onTogglePlayerMode: () -> Unit,
    onSos: () -> Unit,
    onAddPointAt: (lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) -> Unit,
    onUpdatePoint: (id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) -> Unit,
    onDeletePoint: (id: String, isTeam: Boolean) -> Unit,
    onQuickCommand: (QuickCommandType) -> Unit,
    onEnemyPing: (lat: Double, lon: Double) -> Unit,
    onEnemyMarkEnabled: (Boolean) -> Unit,
    onImportMap: (Uri) -> Unit,
    onMapEnabled: (Boolean) -> Unit,
    onMapOpacity: (Float) -> Unit,
    onClearMap: () -> Unit,
    onMarkHelpSeen: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onRequestPermission(granted)
    }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val targets = remember(state.players, state.me, state.myHeadingDeg, now) { targetsProvider(now) }

    var showList by remember { mutableStateOf(false) }
    var listQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(TargetsSortMode.DISTANCE) }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredTargets = remember(targets, listQuery, sortMode) {
        val normalizedQuery = listQuery.trim().lowercase(Locale.getDefault())
        val matching = targets.filter { t ->
            normalizedQuery.isBlank() || t.nick.lowercase(Locale.getDefault()).contains(normalizedQuery)
        }
        when (sortMode) {
            TargetsSortMode.DISTANCE -> matching.sortedWith(
                compareByDescending<CompassTarget> { it.sosActive }
                    .thenBy { it.staleness == Staleness.HIDDEN }
                    .thenBy { it.distanceMeters }
            )

            TargetsSortMode.FRESHNESS -> matching.sortedWith(
                compareByDescending<CompassTarget> { it.sosActive }
                    .thenBy { it.lastSeenSec }
                    .thenBy { it.distanceMeters }
            )
        }
    }

    var showStatusDialog by remember { mutableStateOf(false) }
    var showQuickCmdDialog by remember { mutableStateOf(false) }
    var showMapsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(state.showCompassHelpOnce) }

    LaunchedEffect(state.showCompassHelpOnce) {
        if (state.showCompassHelpOnce) showHelpDialog = true
    }

    // KMZ editing layer (points) — similar to the tactical map editor UI.
    var editMode by remember { mutableStateOf(false) }
    var defaultPointIconRaw by remember { mutableStateOf(TacticalIconId.FLAG.raw) }
    var defaultPointForTeam by remember { mutableStateOf(true) }
    var pointDialog by remember { mutableStateOf<PointDialogState?>(null) }
    var pointAction by remember { mutableStateOf<PointActionState?>(null) }

    val importMapLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportMap(uri)
        }
    }

    // Radar range (pinch to zoom): 10m .. 1000m
    var rangeMeters by remember { mutableStateOf(1000f) }

    // For tap->enemy ping conversion
    var radarSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }

    val density = androidx.compose.ui.platform.LocalDensity.current.density

    // --- Derived UI overlays (enemy halo + points with icons) ---
    val enemyOverlays = remember(state.enemyPings, state.me, state.myHeadingDeg, now) {
        val me = state.me
        val heading = state.myHeadingDeg ?: 0.0
        if (me == null) emptyList() else {
            val pts = mutableListOf<RadarOverlay>()
            state.enemyPings.forEach { p ->
                if (now - p.createdAtMs > 60_000L) return@forEach
                val lp = com.example.teamcompass.core.LocationPoint(p.lat, p.lon, 0.0, 0.0, null, now)
                val dist = com.example.teamcompass.core.GeoMath.distanceMeters(me, lp)
                val bearing = com.example.teamcompass.core.GeoMath.bearingDegrees(me, lp)
                val rel = com.example.teamcompass.core.GeoMath.normalizeRelativeDegrees(bearing - heading)
                pts.add(
                    RadarOverlay(
                        id = p.id,
                        label = "Противник",
                        icon = "",
                        relativeBearingDeg = rel,
                        distanceMeters = dist,
                        kind = RadarOverlayKind.ENEMY_PING,
                    )
                )
            }
            pts
        }
    }

    val pointMarkers = remember(state.teamPoints, state.privatePoints, state.me, state.myHeadingDeg, rangeMeters, radarSize) {
        val me = state.me
        val heading = state.myHeadingDeg ?: 0.0
        val w = radarSize.width.toFloat()
        val h = radarSize.height.toFloat()
        if (me == null || w <= 0f || h <= 0f) emptyList() else {
            val r = min(w, h) * 0.49f
            val cx = w / 2f
            val cy = h / 2f

            fun toScreen(lat: Double, lon: Double): Pair<Offset, Boolean> {
                val lp = com.example.teamcompass.core.LocationPoint(lat, lon, 0.0, 0.0, null, 0L)
                val dist = com.example.teamcompass.core.GeoMath.distanceMeters(me, lp).toFloat()
                val bearing = com.example.teamcompass.core.GeoMath.bearingDegrees(me, lp)
                val rel = com.example.teamcompass.core.GeoMath.normalizeRelativeDegrees(bearing - heading).toFloat()
                val distN = (dist / rangeMeters).coerceIn(0f, 1f)
                val rad = (rel * PI.toFloat() / 180f)
                val dx = sin(rad)
                val dy = -cos(rad)
                return Pair(Offset(cx + dx * (r * distN), cy + dy * (r * distN)), dist <= rangeMeters)
            }

            val out = mutableListOf<PointMarkerUi>()
            state.teamPoints.forEach { mp ->
                val (pos, inRange) = toScreen(mp.lat, mp.lon)
                out.add(
                    PointMarkerUi(
                        id = mp.id,
                        isTeam = true,
                        createdBy = mp.createdBy,
                        lat = mp.lat,
                        lon = mp.lon,
                        label = mp.label.ifBlank { "Точка" },
                        iconRaw = mp.icon,
                        posPx = pos,
                        inRange = inRange,
                    )
                )
            }
            state.privatePoints.forEach { mp ->
                val (pos, inRange) = toScreen(mp.lat, mp.lon)
                out.add(
                    PointMarkerUi(
                        id = mp.id,
                        isTeam = false,
                        createdBy = mp.createdBy,
                        lat = mp.lat,
                        lon = mp.lon,
                        label = mp.label.ifBlank { "Точка" },
                        iconRaw = mp.icon,
                        posPx = pos,
                        inRange = inRange,
                    )
                )
            }
            out
        }
    }
    val latestMarkers by rememberUpdatedState(pointMarkers)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAIN: radar is the primary UI (full screen).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Радар команды. Жесты: pinch для зума, tap для отметки противника, long press в режиме редактирования."
                }
                .onSizeChanged { radarSize = it }
                .pointerInput(state.enemyMarkEnabled, rangeMeters, state.me, state.myHeadingDeg) {
                    // Pinch to zoom
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            rangeMeters = (rangeMeters / zoom).coerceIn(10f, 1000f)
                        }
                    }
                }
                .pointerInput(editMode, state.enemyMarkEnabled, rangeMeters, state.me, state.myHeadingDeg, radarSize) {
                    fun screenToLatLon(offset: Offset): Pair<Double, Double>? {
                        val me = state.me ?: return null
                        val heading = state.myHeadingDeg ?: 0.0
                        val w = radarSize.width.toFloat()
                        val h = radarSize.height.toFloat()
                        if (w <= 0f || h <= 0f) return null
                        // Keep this in sync with CompassRing() radius (hero radar size).
                        val r = min(w, h) * 0.49f
                        val cx = w / 2f
                        val cy = h / 2f
                        val vx = offset.x - cx
                        val vy = offset.y - cy
                        val len = sqrt(vx * vx + vy * vy)
                        if (len > r) return null

                        val relRad = kotlin.math.atan2(vx.toDouble(), (-vy).toDouble())
                        val relDeg = Math.toDegrees(relRad)
                        val dist = (len / r) * rangeMeters
                        val absBearing = com.example.teamcompass.core.GeoMath.normalizeDegrees0to360(relDeg + heading)
                        val dest = destinationPoint(me.lat, me.lon, absBearing, dist.toDouble())
                        return Pair(dest.first, dest.second)
                    }

                    // Tap: enemy ping (if enabled) OR add point (if edit mode)
                    detectTapGestures(
                        onTap = { offset ->
                            val ll = screenToLatLon(offset) ?: return@detectTapGestures
                            if (editMode) {
                                pointDialog = PointDialogState(
                                    id = null,
                                    isTeam = defaultPointForTeam,
                                    createdBy = state.uid,
                                    lat = ll.first,
                                    lon = ll.second,
                                    initialLabel = "",
                                    initialIconRaw = defaultPointIconRaw,
                                )
                                return@detectTapGestures
                            }

                            if (state.enemyMarkEnabled) {
                                onEnemyPing(ll.first, ll.second)
                            }
                        },
                        onLongPress = { offset ->
                            if (!editMode) return@detectTapGestures
                            val ll = screenToLatLon(offset) ?: return@detectTapGestures

                            // Find nearest marker by screen distance.
                            val best = latestMarkers.minByOrNull { m ->
                                val dx = m.posPx.x - offset.x
                                val dy = m.posPx.y - offset.y
                                dx * dx + dy * dy
                            }
                            val thresholdPx = 48f * density
                            if (best != null) {
                                val dx = best.posPx.x - offset.x
                                val dy = best.posPx.y - offset.y
                                val d2 = dx * dx + dy * dy
                                if (d2 <= thresholdPx * thresholdPx) {
                                    pointAction = PointActionState(best, ll.first, ll.second)
                                }
                            }
                        }
                    )
                }
        ) {
            val loadedMapRender by produceState<TacticalMapRender?>(initialValue = null, key1 = state.activeMap?.id) {
                value = null
                val m = state.activeMap ?: return@produceState
                val ov = m.groundOverlay ?: return@produceState
                val img = File(m.dirPath, ov.imageHref)
                if (!img.exists()) return@produceState
                val bmp = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(img.absolutePath)
                }
                if (bmp != null) {
                    value = TacticalMapRender(
                        overlay = ov,
                        bitmap = bmp,
                        opacity = state.mapOpacity,
                        points = m.points,
                        lines = m.lines,
                        polygons = m.polygons,
                    )
                }
            }
            val tactical = if (state.mapEnabled) loadedMapRender?.copy(opacity = state.mapOpacity) else null

            Box(Modifier.fillMaxSize()) {
                CompassRing(
                    modifier = Modifier.fillMaxSize(),
                    targets = targets,
                    overlays = enemyOverlays,
                    rangeMeters = rangeMeters,
                    me = state.me,
                    myHeadingDeg = state.myHeadingDeg,
                    tacticalMap = tactical,
                    mySosActive = state.mySosUntilMs > now,
                    nowMs = now,
                )

                // Live shared points (team + private) with "normal" icons.
                latestMarkers.forEach { m ->
                    androidx.compose.runtime.key("p_" + m.isTeam + "_" + m.id) {
                        RadarPointMarker(marker = m)
                    }
                }
            }
        }

        // LEFT rail: primary controls
        Card(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.hasLocationPermission) {
                    RailButton(
                        icon = Icons.Default.GpsFixed,
                        label = "GPS",
                        onClick = {
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                } else {
                    RailButton(
                        icon = if (state.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        label = if (state.isTracking) "Стоп" else "Старт",
                        onClick = { if (state.isTracking) onStopTracking() else onStartTracking() }
                    )
                }

                RailButton(
                    icon = Icons.Default.SwapHoriz,
                    label = if (state.playerMode == com.example.teamcompass.core.PlayerMode.DEAD) "Мёртв" else "Игра",
                    onClick = onTogglePlayerMode
                )

                val sosActive = state.mySosUntilMs > now
                RailButton(
                    icon = Icons.Default.Warning,
                    label = if (sosActive) "SOS: ON" else "SOS",
                    onClick = onSos
                )
            }
        }

        // RIGHT rail: list + burger menu
        Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RailButton(
                    icon = Icons.Default.Groups,
                    label = "Список",
                    onClick = { showList = !showList }
                )

                Box {
                    RailButton(
                        icon = Icons.Default.Menu,
                        label = "Меню",
                        onClick = { menuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Статус") },
                            onClick = {
                                menuExpanded = false
                                showStatusDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Как пользоваться") },
                            onClick = {
                                menuExpanded = false
                                showHelpDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = "Как пользоваться") }
                        )
                        DropdownMenuItem(
                            text = { Text(if (editMode) "Редактирование: ВКЛ" else "Редактирование: ВЫКЛ") },
                            onClick = {
                                menuExpanded = false
                                val next = !editMode
                                editMode = next
                                if (next && state.enemyMarkEnabled) onEnemyMarkEnabled(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Добавить точку здесь") },
                            onClick = {
                                menuExpanded = false
                                val me = state.me ?: return@DropdownMenuItem
                                pointDialog = PointDialogState(
                                    id = null,
                                    isTeam = defaultPointForTeam,
                                    createdBy = state.uid,
                                    lat = me.lat,
                                    lon = me.lon,
                                    initialLabel = "",
                                    initialIconRaw = defaultPointIconRaw,
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Быстрые команды") },
                            onClick = {
                                menuExpanded = false
                                showQuickCmdDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Карты (KMZ)") },
                            onClick = {
                                menuExpanded = false
                                showMapsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (state.enemyMarkEnabled) "Противник: ВКЛ" else "Противник: ВЫКЛ") },
                            onClick = {
                                menuExpanded = false
                                val next = !state.enemyMarkEnabled
                                onEnemyMarkEnabled(next)
                                if (next) editMode = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Настройки") },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") }
                        )
                        DropdownMenuItem(
                            text = { Text("Копировать код") },
                            onClick = {
                                menuExpanded = false
                                onCopyCode()
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = "Копировать код") }
                        )
                        DropdownMenuItem(
                            text = { Text("Покинуть команду") },
                            onClick = {
                                menuExpanded = false
                                onLeave()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = "Покинуть команду") }
                        )
                    }
                }
            }
        }

        // Active quick command overlay (60s)
        val cmd = state.activeCommand
        if (cmd != null && now - cmd.createdAtMs <= 60_000L) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            ) {
                val txt = when (cmd.type) {
                    QuickCommandType.RALLY -> "Сбор на точке"
                    QuickCommandType.RETREAT -> "Отходим"
                    QuickCommandType.ATTACK -> "Атакуем"
                }
                Text(
                    txt,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // LIST PANEL (overlay; radar remains full screen)
        AnimatedVisibility(
            visible = showList,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(160))
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 92.dp, top = 10.dp, bottom = 10.dp)
                    .widthIn(max = 360.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            ) {
                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Список", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showList = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть список")
                        }
                    }
                    Spacer(Modifier.height(Spacing.xs))

                    OutlinedTextField(
                        value = listQuery,
                        onValueChange = { listQuery = it.take(24) },
                        label = { Text("Поиск по позывному") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(Spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { sortMode = TargetsSortMode.DISTANCE },
                            label = { Text(if (sortMode == TargetsSortMode.DISTANCE) "✓ По дистанции" else "По дистанции") }
                        )
                        AssistChip(
                            onClick = { sortMode = TargetsSortMode.FRESHNESS },
                            label = { Text(if (sortMode == TargetsSortMode.FRESHNESS) "✓ По давности" else "По давности") }
                        )
                    }

                    Spacer(Modifier.height(Spacing.xs))

                    if (filteredTargets.isEmpty()) {
                        Text(
                            if (targets.isEmpty()) "Пусто" else "Ничего не найдено",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTargets, key = { it.uid }) { t ->
                                TargetRow(t)
                            }
                        }
                    }
                }
            }
        }

        if (showStatusDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showStatusDialog = false },
                confirmButton = {
                    FilledTonalButton(onClick = { showStatusDialog = false }) { Text("Закрыть") }
                },
                title = { Text("Статус") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Команда: ${state.teamCode}")
                        Text("Позывной: ${state.callsign.ifBlank { "Игрок" }}")
                        Text("Трекинг: ${if (state.isTracking) "вкл" else "выкл"}")
                        val modeTxt = if (state.playerMode == com.example.teamcompass.core.PlayerMode.DEAD) "Мёртв" else if (state.isAnchored) "В игре (закреплён)" else "В игре"
                        Text("Режим: $modeTxt")
                        Text("Зум: ${rangeMeters.roundToInt()} м")
                        CoordinatesLine(me = state.me, headingDeg = state.myHeadingDeg)
                        Text(
                            if (state.enemyMarkEnabled) "Противник: включено (тап по радару ставит ореол на 1 мин)" else "Противник: выключено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = onCopyCode) { Text("Копировать код") }
                    }
                }
            )
        }

        if (showHelpDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showHelpDialog = false
                    onMarkHelpSeen()
                },
                confirmButton = {
                    FilledTonalButton(onClick = {
                        showHelpDialog = false
                        onMarkHelpSeen()
                    }) { Text("Понятно") }
                },
                title = { Text("Как пользоваться радаром") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text("Жесты", fontWeight = FontWeight.SemiBold)
                        Text("• Pinch — изменить радиус радара (10–1000 м).", style = MaterialTheme.typography.bodySmall)
                        Text("• Tap при «Противник: ВКЛ» — поставить ореол на 60 секунд.", style = MaterialTheme.typography.bodySmall)
                        Text("• Long press в режиме редактирования — действия с точкой.", style = MaterialTheme.typography.bodySmall)
                        Divider()
                        Text("Легенда", fontWeight = FontWeight.SemiBold)
                        Text("• Командные точки — общие для команды.", style = MaterialTheme.typography.bodySmall)
                        Text("• Личные точки — видны только тебе.", style = MaterialTheme.typography.bodySmall)
                        Text("• SOS в списке/радаре имеет повышенный приоритет.", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Подсказка: открой меню и переключи «Противник» или «Редактирование» в зависимости от задачи.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            )
        }

        if (showQuickCmdDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showQuickCmdDialog = false },
                confirmButton = { },
                title = { Text("Быстрые команды") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilledTonalButton(onClick = { onQuickCommand(QuickCommandType.RALLY); showQuickCmdDialog = false }) { Text("Сбор на точке") }
                        FilledTonalButton(onClick = { onQuickCommand(QuickCommandType.RETREAT); showQuickCmdDialog = false }) { Text("Отходим") }
                        FilledTonalButton(onClick = { onQuickCommand(QuickCommandType.ATTACK); showQuickCmdDialog = false }) { Text("Атакуем") }
                    }
                },
                dismissButton = {
                    FilledTonalButton(onClick = { showQuickCmdDialog = false }) { Text("Отмена") }
                }
            )
        }

        // Long-press actions on a point marker.
        pointAction?.let { pa ->
            val m = pa.marker
            // Backward compatible: if createdBy is missing, treat it as editable.
            val isAuthor = (!m.isTeam) || (m.createdBy == null || m.createdBy == state.uid)
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pointAction = null },
                title = { Text(m.label) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        val scope = if (m.isTeam) "Командная" else "Личная"
                        Text("$scope точка", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        if (m.isTeam && !isAuthor) {
                            Text(
                                "Редактировать/удалять командную точку может только автор. Ты можешь сделать себе локальную копию и править её.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    if (m.isTeam && !isAuthor) {
                        FilledTonalButton(
                            onClick = {
                                // Copy shared point into my private layer.
                                onAddPointAt(m.lat, m.lon, m.label, m.iconRaw, false)
                                pointAction = null
                            }
                        ) { Text("Скопировать себе") }
                    } else {
                        FilledTonalButton(
                            onClick = {
                                pointDialog = PointDialogState(
                                    id = m.id,
                                    isTeam = m.isTeam,
                                    createdBy = m.createdBy,
                                    lat = m.lat,
                                    lon = m.lon,
                                    initialLabel = m.label,
                                    initialIconRaw = m.iconRaw,
                                )
                                pointAction = null
                            }
                        ) { Text("Редактировать") }
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!(m.isTeam && !isAuthor)) {
                            FilledTonalButton(
                                onClick = {
                                    // Move marker to the pressed location.
                                    onUpdatePoint(m.id, pa.pressLat, pa.pressLon, m.label, m.iconRaw, m.isTeam)
                                    pointAction = null
                                }
                            ) { Text("Переместить") }
                            FilledTonalButton(
                                onClick = {
                                    onDeletePoint(m.id, m.isTeam)
                                    pointAction = null
                                }
                            ) { Text("Удалить") }
                        }
                        FilledTonalButton(onClick = { pointAction = null }) { Text("Закрыть") }
                    }
                }
            )
        }

        // Create/edit point dialog.
        pointDialog?.let { pd ->
            var label by remember(pd.id, pd.lat, pd.lon) { mutableStateOf(pd.initialLabel) }
            var iconRaw by remember(pd.id, pd.lat, pd.lon) { mutableStateOf(pd.initialIconRaw) }
            var forTeam by remember(pd.id, pd.lat, pd.lon) { mutableStateOf(pd.isTeam) }
            val icons = TacticalIconId.entries

            val isEdit = pd.id != null

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pointDialog = null },
                title = { Text(if (isEdit) "Точка" else "Новая точка") },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            val name = label.trim().ifBlank { "Точка" }.take(24)
                            if (pd.id == null) {
                                onAddPointAt(pd.lat, pd.lon, name, iconRaw, forTeam)
                                defaultPointIconRaw = iconRaw
                                defaultPointForTeam = forTeam
                            } else {
                                onUpdatePoint(pd.id, pd.lat, pd.lon, name, iconRaw, pd.isTeam)
                            }
                            pointDialog = null
                        }
                    ) { Text(if (isEdit) "Сохранить" else "Добавить") }
                },
                dismissButton = {
                    FilledTonalButton(onClick = { pointDialog = null }) { Text("Отмена") }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it.take(24) },
                            label = { Text("Подпись") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Icon grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            icons.chunked(4).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { ic ->
                                        val selected = ic.raw.equals(iconRaw, ignoreCase = true)
                                        FilledTonalIconButton(
                                            onClick = { iconRaw = ic.raw },
                                            modifier = Modifier.size(54.dp),
                                            shape = RoundedCornerShape(Spacing.md)
                                        ) {
                                            Icon(ic.vector, contentDescription = ic.label)
                                        }
                                    }
                                }
                            }
                        }

                        if (!isEdit) {
                            BinaryChoiceButtons(
                                modifier = Modifier.fillMaxWidth(),
                                leftText = "Для команды",
                                rightText = "Для себя",
                                leftSelected = forTeam,
                                onLeftClick = { forTeam = true },
                                onRightClick = { forTeam = false },
                            )
                        } else {
                            Text(
                                if (pd.isTeam) "Командная точка" else "Личная точка",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }

        if (showMapsDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showMapsDialog = false },
                confirmButton = {
                    FilledTonalButton(onClick = { showMapsDialog = false }) { Text("Закрыть") }
                },
                dismissButton = {
                    FilledTonalButton(
                        onClick = { importMapLauncher.launch(arrayOf("application/vnd.google-earth.kmz", "application/vnd.google-earth.kml+xml", "application/octet-stream", "*/*")) },
                        enabled = !state.isBusy
                    ) { Text("Импортировать KMZ/KML") }
                },
                title = { Text("Карты (KMZ)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        val m = state.activeMap
                        if (m == null) {
                            Text(
                                "Карта не загружена. Импортируй KMZ с подложкой (GroundOverlay) и/или метками.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text("Активная: ${m.name}")

                            val ov = m.groundOverlay
                            if (ov != null) {
                                val (wM, hM) = latLonBoxSizeMeters(ov.north, ov.south, ov.east, ov.west)
                                Text(
                                    "Подложка: ${wM.toInt()}×${hM.toInt()} м",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    "В этом KMZ нет GroundOverlay (картинки).",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Text("Показывать")
                                androidx.compose.material3.Switch(
                                    checked = state.mapEnabled,
                                    onCheckedChange = onMapEnabled
                                )
                            }

                            Text(
                                "Прозрачность: ${(state.mapOpacity * 100).roundToInt()}%",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            androidx.compose.material3.Slider(
                                value = state.mapOpacity,
                                onValueChange = onMapOpacity,
                                valueRange = 0.1f..1.0f
                            )

                            FilledTonalButton(onClick = onClearMap) { Text("Убрать карту") }
                        }

                        Text(
                            "Карта рисуется офлайн прямо под радаром и масштабируется вместе с зумом (10м–1км).",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RadarPointMarker(marker: PointMarkerUi) {
    // Keep UI clean: show markers only when inside current radar range.
    if (!marker.inRange) return

    val icon = tacticalIconOrNull(marker.iconRaw) ?: TacticalIconId.FLAG
    val bg = if (marker.isTeam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val fg = if (marker.isTeam) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary

    val halfPx = with(androidx.compose.ui.platform.LocalDensity.current) { 22.dp.toPx() }
    val x = marker.posPx.x
    val y = marker.posPx.y

    Column(
        modifier = Modifier
            .offset { IntOffset((x - halfPx).roundToInt(), (y - halfPx).roundToInt()) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon.vector, contentDescription = marker.label, tint = fg)
        }
        if (marker.label.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                marker.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.80f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun BinaryChoiceButtons(
    modifier: Modifier = Modifier,
    leftText: String,
    rightText: String,
    leftSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        val w = Modifier.weight(1f)
        if (leftSelected) {
            Button(onClick = onLeftClick, modifier = w) { Text(leftText) }
            OutlinedButton(onClick = onRightClick, modifier = w) { Text(rightText) }
        } else {
            OutlinedButton(onClick = onLeftClick, modifier = w) { Text(leftText) }
            Button(onClick = onRightClick, modifier = w) { Text(rightText) }
        }
    }
}


@Composable
private fun RailButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(Spacing.lg - Spacing.xs)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun TargetRow(t: CompassTarget) {
    // List colors by gameplay state (requested):
    // - DEAD: transparent red
    // - Anchored: blue
    // - In-game: green
    val base = when {
        // DEAD: transparent red
        t.mode == com.example.teamcompass.core.PlayerMode.DEAD -> androidx.compose.ui.graphics.Color(0xFFFF4D4D).copy(alpha = 0.65f)
        t.anchored -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        else -> androidx.compose.ui.graphics.Color(0xFF22C55E)
    }

    val stAlpha = when (t.staleness) {
        Staleness.FRESH -> 1.0f
        Staleness.SUSPECT -> 0.85f
        Staleness.STALE -> 0.55f
        Staleness.HIDDEN -> 0.35f
    }
    val tone = base.copy(alpha = (stAlpha * base.alpha).coerceIn(0f, 1f))

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(t.nick.take(1).uppercase(), color = tone, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(t.nick, fontWeight = FontWeight.SemiBold)
            val dist = if (t.staleness == Staleness.HIDDEN) "—" else "${t.distanceMeters.roundToInt()} м"
            val seen = "${t.lastSeenSec} сек назад"
            val acc = if (t.lowAccuracy) " • низкая точность" else ""
            val mode = when {
                t.mode == com.example.teamcompass.core.PlayerMode.DEAD -> " • мёртв"
                t.anchored -> " • закреп"
                else -> ""
            }
            Text(
                "$dist • $seen$acc$mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (t.lowAccuracy) {
            Icon(Icons.Default.Warning, contentDescription = "Предупреждение", tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun CoordinatesLine(
    me: LocationPoint?,
    headingDeg: Double?,
) {
    val text = if (me == null) {
        "GPS: —"
    } else {
        val lat = formatCoord(me.lat)
        val lon = formatCoord(me.lon)
        val acc = me.accMeters.roundToInt()
        buildString {
            append("GPS: ")
            append(lat)
            append(", ")
            append(lon)
            if (acc > 0) append(" • ±${acc}м")
            val h = headingDeg?.let { (((it % 360.0) + 360.0) % 360.0).roundToInt() }
            if (h != null) append(" • ${h}°")
        }
    }

    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun formatCoord(v: Double): String = String.format(Locale.US, "%.5f", v)
