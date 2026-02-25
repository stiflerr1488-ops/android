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
import com.example.teamcompass.domain.TeamViewMode

private const val COMPASS_TICK_MS = 1_500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompassScreen(
    state: UiState,
    onRequestPermission: (Boolean) -> Unit,
    onStartTracking: () -> Unit,
    onStartBluetoothScan: () -> Unit,
    onLeave: () -> Unit,
    targetsProvider: (Long) -> List<CompassTarget>,
    onCopyCode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFullscreenMap: () -> Unit,
    onTogglePlayerMode: () -> Unit,
    onSos: () -> Unit,
    onAddPointAt: (lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) -> Unit,
    onUpdatePoint: (id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) -> Unit,
    onDeletePoint: (id: String, isTeam: Boolean) -> Unit,
    onQuickCommand: (QuickCommandType) -> Unit,
    onEnemyPing: (lat: Double, lon: Double, type: QuickCommandType) -> Unit,
    onEnemyMarkEnabled: (Boolean) -> Unit,
    onImportMap: (Uri) -> Unit,
    onMapEnabled: (Boolean) -> Unit,
    onMapOpacity: (Float) -> Unit,
    onClearMap: () -> Unit,
    onMarkHelpSeen: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionActions = rememberCompassPermissionActions(
        state = state,
        context = context,
        onRequestPermission = onRequestPermission,
        onStartTracking = onStartTracking,
        onStartBluetoothScan = onStartBluetoothScan,
    )

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(COMPASS_TICK_MS)
        }
    }

    val targets = remember(state.players, state.me, state.myHeadingDeg, now) { targetsProvider(now) }

    var showTargetsSheet by rememberSaveable { mutableStateOf(false) }
    var showAllTeamMembers by rememberSaveable { mutableStateOf(false) }
    var showMarkerPalette by rememberSaveable { mutableStateOf(false) }
    var armedEnemyMarkType by remember { mutableStateOf<QuickCommandType?>(null) }
    var localEnemyPings by remember { mutableStateOf<List<LocalEnemyPingUi>>(emptyList()) }
    val localPingPreviewTtlMs = 6_000L
    val ignoreRadarTapUntilMs = remember { java.util.concurrent.atomic.AtomicLong(0L) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val rosterComputed = rememberCompassScreenRosterComputed(
        state = state,
        nowMs = now,
    )
    val allyVisualsByUid = rosterComputed.allyVisualsByUid
    val teamRoster = rosterComputed.teamRoster
    var showStatusDialog by rememberSaveable { mutableStateOf(false) }
    var showMapsDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpDialog by rememberSaveable(state.showCompassHelpOnce) { mutableStateOf(state.showCompassHelpOnce) }

    LaunchedEffect(state.showCompassHelpOnce) {
        if (state.showCompassHelpOnce) showHelpDialog = true
    }

    // KMZ editing layer (points) вЂ” similar to the tactical map editor UI.
    var editMode by rememberSaveable { mutableStateOf(false) }
    var defaultPointIconRaw by rememberSaveable { mutableStateOf(TacticalIconId.FLAG.raw) }
    var defaultPointForTeam by rememberSaveable { mutableStateOf(true) }
    var pointDialog by remember { mutableStateOf<PointDialogState?>(null) }
    var pointAction by remember { mutableStateOf<PointActionState?>(null) }

    val importMapLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportMap(uri)
        }
    }

    // Radar range (pinch to zoom): 30m .. 500m with sticky tactical steps.
    var rangeMeters by remember { mutableFloatStateOf(500f) }
    val zoomStops = remember { listOf(30f, 70f, 150f, 300f, 500f) }
    var zoomStickyStop by remember { mutableStateOf<Float?>(null) }

    // For tap->enemy ping conversion
    var radarSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }
    val defaultPointLabel = stringResource(R.string.point_title)

    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val radarContentDescription = stringResource(R.string.radar_content_description)
    val noGpsFixText = stringResource(R.string.toast_no_gps_fix)

    // Prune optimistic local pings (expired or acknowledged by server echo).
    LaunchedEffect(state.enemyPings, state.uid, now) {
        val filtered = pruneLocalEnemyPingPreviews(
            state = state,
            localEnemyPings = localEnemyPings,
            now = now,
        )
        if (filtered != localEnemyPings) localEnemyPings = filtered
    }

    val overlayComputed = rememberCompassScreenOverlayComputed(
        state = state,
        localEnemyPings = localEnemyPings,
        rangeMeters = rangeMeters,
        radarSize = radarSize,
        defaultPointLabel = defaultPointLabel,
        nowMs = now,
    )
    val bluetoothDevicesCount = overlayComputed.bluetoothDevicesCount
    val enemyOverlays = overlayComputed.enemyOverlays
    val pointMarkers = overlayComputed.pointMarkers
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAIN: radar is the primary UI (full screen).
        CompassScreenRadarSurface(
            state = state,
            targets = targets,
            allyVisualsByUid = allyVisualsByUid,
            enemyOverlays = enemyOverlays,
            pointMarkers = pointMarkers,
            nowMs = now,
            editMode = editMode,
            showMarkerPalette = showMarkerPalette,
            armedEnemyMarkType = armedEnemyMarkType,
            onArmedEnemyMarkTypeChange = { armedEnemyMarkType = it },
            rangeMeters = rangeMeters,
            onRangeMetersChange = { rangeMeters = it },
            zoomStops = zoomStops,
            zoomStickyStop = zoomStickyStop,
            onZoomStickyStopChange = { zoomStickyStop = it },
            radarContentDescription = radarContentDescription,
            radarSize = radarSize,
            onRadarSizeChange = { radarSize = it },
            ignoreRadarTapUntilMs = ignoreRadarTapUntilMs,
            context = context,
            noGpsFixText = noGpsFixText,
            density = density,
            defaultPointForTeam = defaultPointForTeam,
            defaultPointIconRaw = defaultPointIconRaw,
            localPingPreviewTtlMs = localPingPreviewTtlMs,
            localEnemyPings = localEnemyPings,
            onLocalEnemyPingsChange = { localEnemyPings = it },
            onPointDialogChange = { pointDialog = it },
            onPointActionChange = { pointAction = it },
            onEnemyPing = onEnemyPing,
            onEnemyMarkEnabled = onEnemyMarkEnabled,
        )

        CompassScreenOverlayLayer(
            state = state,
            teamRoster = teamRoster,
            bluetoothDevicesCount = bluetoothDevicesCount,
            nowMs = now,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = { menuExpanded = it },
            editMode = editMode,
            onEditModeChange = { editMode = it },
            showMarkerPalette = showMarkerPalette,
            onShowMarkerPaletteChange = { showMarkerPalette = it },
            armedEnemyMarkType = armedEnemyMarkType,
            onArmedEnemyMarkTypeChange = { armedEnemyMarkType = it },
            onPaletteSelect = { type ->
                ignoreRadarTapUntilMs.set(System.currentTimeMillis() + 450L)
                armedEnemyMarkType = type
                onQuickCommand(type)
                showMarkerPalette = false
            },
            onPaletteCancel = {
                ignoreRadarTapUntilMs.set(System.currentTimeMillis() + 450L)
                showMarkerPalette = false
            },
            formatCoord = ::formatCoord,
            onShowStatusDialog = { showStatusDialog = true },
            onShowHelpDialog = { showHelpDialog = true },
            onAddPointAtCurrentLocation = {
                val me = state.me
                if (me != null) {
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
            },
            onShowMapsDialog = { showMapsDialog = true },
            onOpenSettings = onOpenSettings,
            onCopyCode = onCopyCode,
            onLeave = onLeave,
            onEnemyMarkEnabled = onEnemyMarkEnabled,
            onTogglePlayerMode = onTogglePlayerMode,
            onSos = onSos,
            onRequestBluetoothScan = permissionActions.requestBluetoothScan,
            showAllTeamMembers = showAllTeamMembers,
            onShowAllTeamMembersChange = { showAllTeamMembers = it },
            showTargetsSheet = showTargetsSheet,
            onShowTargetsSheetChange = { showTargetsSheet = it },
            onOpenTeamRoster = {
                showAllTeamMembers = true
                showTargetsSheet = true
            },
            onEnableLocation = permissionActions.openLocationSettings,
        )

        CompassScreenDialogLayer(
            state = state,
            rangeMeters = rangeMeters,
            showMarkerPalette = showMarkerPalette,
            showStatusDialog = showStatusDialog,
            onShowStatusDialogChange = { showStatusDialog = it },
            showHelpDialog = showHelpDialog,
            onShowHelpDialogChange = { showHelpDialog = it },
            onMarkHelpSeen = onMarkHelpSeen,
            pointAction = pointAction,
            onPointActionChange = { pointAction = it },
            pointDialog = pointDialog,
            onPointDialogChange = { pointDialog = it },
            defaultPointLabel = defaultPointLabel,
            defaultPointIconRaw = defaultPointIconRaw,
            defaultPointForTeam = defaultPointForTeam,
            onDefaultPointPrefsChanged = { iconRaw, forTeam ->
                defaultPointIconRaw = iconRaw
                defaultPointForTeam = forTeam
            },
            showMapsDialog = showMapsDialog,
            onShowMapsDialogChange = { showMapsDialog = it },
            onLaunchMapImport = {
                importMapLauncher.launch(
                    arrayOf(
                        "application/vnd.google-earth.kmz",
                        "application/vnd.google-earth.kml+xml",
                        "application/octet-stream",
                        "*/*",
                    )
                )
            },
            onCopyCode = onCopyCode,
            onAddPointAt = onAddPointAt,
            onUpdatePoint = onUpdatePoint,
            onDeletePoint = onDeletePoint,
            onMapEnabled = onMapEnabled,
            onOpenFullscreenMap = onOpenFullscreenMap,
            onMapOpacity = onMapOpacity,
            onClearMap = onClearMap,
        )
    }
}
