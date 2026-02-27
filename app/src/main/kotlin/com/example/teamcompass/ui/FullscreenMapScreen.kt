package com.example.teamcompass.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.perf.TeamCompassPerfMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenMapScreen(
    state: UiState,
    onBack: () -> Unit,
    onSaveToSource: (List<KmlPoint>, List<KmlPoint>) -> Unit,
    onSaveAs: (Uri, List<KmlPoint>, List<KmlPoint>) -> Unit,
    onAddSharedMarker: (lat: Double, lon: Double, name: String, iconRaw: String) -> Unit,
    onOpenStructure: () -> Unit,
) {
    val activeMap = state.activeMap
    val snackbarHostState = remember { SnackbarHostState() }

    if (activeMap == null) {
        FullscreenMapEmptyStateScaffold(
            snackbarHostState = snackbarHostState,
            onBack = onBack,
        )
    } else {
        FullscreenMapContent(
            activeMap = activeMap,
            state = state,
            onBack = onBack,
            onSaveToSource = onSaveToSource,
            onSaveAs = onSaveAs,
            onAddSharedMarker = onAddSharedMarker,
            onOpenStructure = onOpenStructure,
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenMapContent(
    activeMap: TacticalMap,
    state: UiState,
    onBack: () -> Unit,
    onSaveToSource: (List<KmlPoint>, List<KmlPoint>) -> Unit,
    onSaveAs: (Uri, List<KmlPoint>, List<KmlPoint>) -> Unit,
    onAddSharedMarker: (lat: Double, lon: Double, name: String, iconRaw: String) -> Unit,
    onOpenStructure: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    var canvasSize by remember(activeMap.id) { mutableStateOf(IntSize.Zero) }
    var firstRenderStartedAtMs by remember(activeMap.id) { mutableLongStateOf(System.currentTimeMillis()) }
    var firstRenderRecorded by remember(activeMap.id) { mutableStateOf(false) }
    val decodeRequestSidePx = max(canvasSize.width, canvasSize.height).coerceAtLeast(1024)
    val overlayBitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = activeMap.id,
        key2 = activeMap.groundOverlay?.imageHref,
        key3 = decodeRequestSidePx,
    ) {
        value = null
        val overlay = activeMap.groundOverlay ?: return@produceState
        val imageFile = File(activeMap.dirPath, overlay.imageHref)
        if (!imageFile.exists()) return@produceState
        value = withContext(Dispatchers.IO) {
            MapBitmapCache.load(imageFile.absolutePath, decodeRequestSidePx)
        }
    }
    LaunchedEffect(activeMap.id) {
        firstRenderStartedAtMs = System.currentTimeMillis()
        firstRenderRecorded = false
    }
    LaunchedEffect(activeMap.id, overlayBitmap) {
        if (!firstRenderRecorded && overlayBitmap != null) {
            TeamCompassPerfMetrics.recordFullscreenMapFirstRender(
                durationMs = System.currentTimeMillis() - firstRenderStartedAtMs,
            )
            firstRenderRecorded = true
        }
    }

    val mapTitle = stringResource(R.string.map_title)
    val mapMarkerDefaultName = stringResource(R.string.map_marker_default_name)
    val mapEditDeleteModeOff = stringResource(R.string.map_edit_delete_mode_off)
    val mapEditDeleteModeOn = stringResource(R.string.map_edit_delete_mode_on)
    val mapEditHintDeleteMode = stringResource(R.string.map_edit_hint_delete_mode)
    val mapEditHintEditMode = stringResource(R.string.map_edit_hint_edit_mode)
    val mapChangesSaved = stringResource(R.string.map_changes_saved)

    var editMode by rememberSaveable(activeMap.id) { mutableStateOf(false) }
    var deleteMode by rememberSaveable(activeMap.id) { mutableStateOf(false) }
    var showEditMenu by rememberSaveable(activeMap.id) { mutableStateOf(false) }
    var selectedIconRaw by rememberSaveable(activeMap.id) { mutableStateOf(TacticalIconId.OBJECTIVE.raw) }
    var selectedColorArgb by rememberSaveable(activeMap.id) { mutableLongStateOf(DEFAULT_DRAFT_COLOR_ARGB) }

    var pointDialogVisible by rememberSaveable(activeMap.id) { mutableStateOf(false) }
    var pointName by rememberSaveable(activeMap.id) { mutableStateOf("") }
    var pointDescription by rememberSaveable(activeMap.id) { mutableStateOf("") }
    var pendingPoint by remember { mutableStateOf<GeoPoint?>(null) }

    var draftPoints by remember(activeMap.id) { mutableStateOf(emptyList<DraftKmlPoint>()) }
    var deletedPointIds by remember(activeMap.id) { mutableStateOf(emptySet<String>()) }

    var saveInFlight by remember(activeMap.id) { mutableStateOf(false) }
    var pendingSavedDraftIds by remember(activeMap.id) { mutableStateOf(emptySet<String>()) }
    var pendingDeletedPointIds by remember(activeMap.id) { mutableStateOf(emptySet<String>()) }
    var expectedPointsAfterSave by remember(activeMap.id) { mutableIntStateOf(activeMap.points.size) }
    var pendingSaveAsDrafts by remember(activeMap.id) { mutableStateOf<List<DraftKmlPoint>?>(null) }
    var pendingSaveAsDeletedPointIds by remember(activeMap.id) { mutableStateOf<Set<String>?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.google-earth.kmz")
    ) { uri ->
        val drafts = pendingSaveAsDrafts
        val deletedIds = pendingSaveAsDeletedPointIds ?: emptySet()
        pendingSaveAsDrafts = null
        pendingSaveAsDeletedPointIds = null
        if (uri != null && (!drafts.isNullOrEmpty() || deletedIds.isNotEmpty())) {
            val toSave = drafts.orEmpty().map { it.toKmlPoint() }
            val deletedPoints = activeMap.points.filter { deletedIds.contains(it.id) }
            pendingSavedDraftIds = drafts.orEmpty().map { it.id }.toSet()
            pendingDeletedPointIds = deletedPoints.map { it.id }.toSet()
            expectedPointsAfterSave = ((state.activeMap?.points?.size ?: 0) - deletedPoints.size + toSave.size).coerceAtLeast(0)
            saveInFlight = true
            onSaveAs(uri, toSave, deletedPoints)
        }
    }

    LaunchedEffect(state.isBusy, state.activeMap?.points?.size, saveInFlight) {
        if (!saveInFlight || state.isBusy) return@LaunchedEffect
        val pointsCount = state.activeMap?.points?.size ?: 0
        if (pointsCount == expectedPointsAfterSave) {
            draftPoints = draftPoints.filterNot { pendingSavedDraftIds.contains(it.id) }
            deletedPointIds = deletedPointIds - pendingDeletedPointIds
            snackbarHostState.showSnackbar(mapChangesSaved)
        }
        pendingSavedDraftIds = emptySet()
        pendingDeletedPointIds = emptySet()
        saveInFlight = false
    }

    val me = state.me
    val players = state.players
    val allyVisualsByUid = remember(players, state.roleProfiles, state.uid) {
        buildAllyVisualsByUid(
            players = players,
            roleProfiles = state.roleProfiles,
            selfUid = state.uid,
        )
    }
    val origin = remember(activeMap.id, activeMap.groundOverlay, activeMap.points, me, players) {
        computeFullscreenMapOrigin(
            activeMap = activeMap,
            me = me,
            players = players,
        )
    }

    var viewportState by remember(activeMap.id) {
        mutableStateOf(MapViewportState(scalePxPerMeter = 1f, offsetPx = Offset.Zero))
    }
    var viewportInitialized by remember(activeMap.id) { mutableStateOf(false) }

    val fitPoints = remember(activeMap.id, activeMap.groundOverlay, activeMap.points) {
        buildFitPoints(activeMap)
    }

    LaunchedEffect(activeMap.id, fitPoints, origin, canvasSize, viewportInitialized) {
        if (viewportInitialized) return@LaunchedEffect
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@LaunchedEffect
        viewportState = fitViewportToPoints(
            points = fitPoints.map { Pair(it.lat, it.lon) },
            originLat = origin.lat,
            originLon = origin.lon,
            canvasSize = canvasSize,
            minScalePxPerMeter = MIN_SCALE_PX_PER_METER,
            maxScalePxPerMeter = MAX_SCALE_PX_PER_METER,
        )
        viewportInitialized = true
    }

    val visibleImportedPoints = remember(activeMap.points, deletedPointIds) {
        activeMap.points.filterNot { deletedPointIds.contains(it.id) }
    }
    val allPoints = remember(visibleImportedPoints, draftPoints) {
        visibleImportedPoints + draftPoints.map { it.toKmlPoint() }
    }
    val draftById = remember(draftPoints) { draftPoints.associateBy { it.id } }
    val density = LocalDensity.current

    val markerOverlays = remember(allPoints, draftById, viewportState, canvasSize, origin) {
        buildFullscreenMapMarkerOverlays(
            allPoints = allPoints,
            draftById = draftById,
            viewportState = viewportState,
            canvasSize = canvasSize,
            origin = origin,
        )
    }

    fun openPointDialogAt(screen: Offset) {
        if (!editMode) return
        val init = buildFullscreenMapPointDialogInit(
            screen = screen,
            canvasSize = canvasSize,
            viewportState = viewportState,
            origin = origin,
            selectedIconRaw = selectedIconRaw,
            mapMarkerDefaultName = mapMarkerDefaultName,
        ) ?: return
        pendingPoint = init.pendingPoint
        pointName = init.pointName
        pointDescription = init.pointDescription
        pointDialogVisible = true
    }

    fun removeMarker(marker: MarkerOverlayUi) {
        val update = removeMarkerFromEditorState(
            marker = marker,
            draftPoints = draftPoints,
            deletedPointIds = deletedPointIds,
        )
        draftPoints = update.draftPoints
        deletedPointIds = update.deletedPointIds
    }

    val hasPendingMapEdits = draftPoints.isNotEmpty() || deletedPointIds.isNotEmpty()
    val markerTapThresholdPx = with(density) { 28.dp.toPx() }
    fun handleEditTap(press: Offset) {
        if (!editMode) return
        if (deleteMode) {
            findMarkerOverlayAt(
                markerOverlays = markerOverlays,
                screen = press,
                thresholdPx = markerTapThresholdPx,
            )?.let(::removeMarker)
        } else {
            openPointDialogAt(press)
        }
    }

    Scaffold(
        topBar = {
            FullscreenMapTopBar(
                title = activeMap.name.ifBlank { mapTitle },
                onBack = onBack,
                onOpenStructure = onOpenStructure,
                editMode = editMode,
                onToggleEditMode = {
                    editMode = !editMode
                    if (!editMode) {
                        deleteMode = false
                        showEditMenu = false
                        pointDialogVisible = false
                        pendingPoint = null
                    }
                },
                showEditMenu = showEditMenu,
                onShowEditMenu = { showEditMenu = true },
                onDismissEditMenu = { showEditMenu = false },
                deleteMode = deleteMode,
                deleteModeOffLabel = mapEditDeleteModeOff,
                deleteModeOnLabel = mapEditDeleteModeOn,
                saveChangesEnabled = hasPendingMapEdits && !state.isBusy,
                saveAsEnabled = hasPendingMapEdits && !state.isBusy,
                discardChangesEnabled = hasPendingMapEdits,
                onToggleDeleteMode = {
                    showEditMenu = false
                    deleteMode = !deleteMode
                },
                onSaveChanges = {
                    showEditMenu = false
                    val savePlan = buildFullscreenMapSaveToSourcePlan(
                        activeMapPoints = activeMap.points,
                        currentMapPointCount = state.activeMap?.points?.size ?: 0,
                        draftPoints = draftPoints,
                        deletedPointIds = deletedPointIds,
                    ) ?: return@FullscreenMapTopBar
                    pendingSavedDraftIds = savePlan.pendingSavedDraftIds
                    pendingDeletedPointIds = savePlan.pendingDeletedPointIds
                    expectedPointsAfterSave = savePlan.expectedPointsAfterSave
                    saveInFlight = true
                    onSaveToSource(savePlan.draftKmlPoints, savePlan.deletedPoints)
                },
                onSaveAs = {
                    showEditMenu = false
                    if (draftPoints.isEmpty() && deletedPointIds.isEmpty()) return@FullscreenMapTopBar
                    pendingSaveAsDrafts = draftPoints
                    pendingSaveAsDeletedPointIds = deletedPointIds
                    createDocumentLauncher.launch(buildSafeKmzDocumentName(activeMap.name))
                },
                onDiscardChanges = {
                    showEditMenu = false
                    deleteMode = false
                    draftPoints = emptyList()
                    deletedPointIds = emptySet()
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            FullscreenMapCanvasLayer(
                activeMap = activeMap,
                state = state,
                nowMs = nowMs,
                overlayBitmap = overlayBitmap,
                canvasSize = canvasSize,
                onCanvasSizeChange = { canvasSize = it },
                viewportState = viewportState,
                onViewportStateChange = { viewportState = it },
                onViewportInitialized = { viewportInitialized = true },
                origin = origin,
                markerOverlays = markerOverlays,
                editMode = editMode,
                deleteMode = deleteMode,
                onEditTap = ::handleEditTap,
                editHintDeleteMode = mapEditHintDeleteMode,
                editHintEditMode = mapEditHintEditMode,
                selectedColorArgb = selectedColorArgb,
                onColorSelected = { selectedColorArgb = it },
                selectedIconRaw = selectedIconRaw,
                onIconSelected = { selectedIconRaw = it },
                allyVisualsByUid = allyVisualsByUid,
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
    }

    val pointLatLon = pendingPoint
    if (pointDialogVisible && pointLatLon != null) {
        FullscreenMapAddMarkerDialog(
            pointName = pointName,
            onPointNameChange = { pointName = it },
            pointDescription = pointDescription,
            onPointDescriptionChange = { pointDescription = it },
            onDismiss = {
                pointDialogVisible = false
                pendingPoint = null
            },
            onConfirmAdd = {
                val latLon = pendingPoint ?: return@FullscreenMapAddMarkerDialog
                val markerName = pointName.trim()
                val markerDescription = pointDescription.trim()
                draftPoints = draftPoints + DraftKmlPoint(
                    id = UUID.randomUUID().toString(),
                    lat = latLon.lat,
                    lon = latLon.lon,
                    name = markerName,
                    description = markerDescription,
                    iconRaw = selectedIconRaw,
                    colorArgb = selectedColorArgb,
                )
                // Immediately publish team marker for all players during the session.
                if (!hasLiveMarkerDuplicate(
                        teamPoints = state.teamPoints,
                        privatePoints = state.privatePoints,
                        lat = latLon.lat,
                        lon = latLon.lon,
                        name = markerName,
                        iconRaw = selectedIconRaw,
                    )
                ) {
                    onAddSharedMarker(latLon.lat, latLon.lon, markerName, selectedIconRaw)
                }
                pointDialogVisible = false
                pendingPoint = null
            },
        )
    }
}

