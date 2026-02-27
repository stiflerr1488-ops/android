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
import com.airsoft.social.core.ui.ForceLandscapeOrientation
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


@Composable
internal fun TeamCompassJoinRoute(vm: TeamCompassViewModel) {
    val state by vm.ui.collectAsStateWithLifecycle()
    if (!state.isAuthReady) {
        LoadingScreen()
    } else {
        JoinScreen(
            callsign = state.callsign,
            onCallsignChange = vm::setCallsign,
            onCreate = { vm.createTeam() },
            onJoin = { code -> vm.joinTeam(code) },
            isBusy = state.isBusy,
            savedCodeHint = null,
        )
    }
}

@Composable
internal fun TeamCompassCompassRoute(
    vm: TeamCompassViewModel,
    nav: NavHostController,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    if (!state.isAuthReady) {
        LoadingScreen()
    } else if (state.teamCode == null) {
        // Redirect is handled by top-level TeamCompassApp navigation effect.
        LoadingScreen()
    } else {
        ForceLandscapeOrientation()
        CompassScreen(
            state = state,
            onRequestPermission = vm::setLocationPermission,
            onStartTracking = { vm.startTracking(state.defaultMode) },
            onStartBluetoothScan = vm::startBluetoothScan,
            onLeave = vm::leaveTeam,
            targetsProvider = { now -> vm.computeTargets(now) },
            onCopyCode = { copyToClipboard(ctx, state.teamCode ?: "") },
            onOpenSettings = { nav.navigate(ROUTE_SETTINGS) },
            onOpenFullscreenMap = { nav.navigate(ROUTE_FULLSCREEN_MAP) },
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

@Composable
internal fun TeamCompassSettingsRoute(
    vm: TeamCompassViewModel,
    nav: NavHostController,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    if (!state.isAuthReady || state.teamCode == null) {
        LoadingScreen()
    } else {
        SettingsScreen(
            state = state,
            onBack = { nav.popBackStack() },
            onDefaultMode = vm::setDefaultMode,
            onGamePolicy = vm::setGamePolicy,
            onSilentPolicy = vm::setSilentPolicy,
            onControlLayoutEdit = vm::setControlLayoutEdit,
            onResetControlPositions = vm::resetControlPositions,
        )
    }
}

@Composable
internal fun TeamCompassStructureRoute(
    vm: TeamCompassViewModel,
    nav: NavHostController,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    if (!state.isAuthReady) {
        LoadingScreen()
    } else if (state.teamCode == null) {
        // Redirect is handled by top-level TeamCompassApp navigation effect.
        LoadingScreen()
    } else {
        TeamStructureScreen(
            state = state,
            onBack = { nav.popBackStack() },
            onAssignTeamMemberRole = vm::assignTeamMemberRole,
            onAssignTeamMemberRolesBulk = vm::assignTeamMemberRolesBulk,
        )
    }
}

@Composable
internal fun TeamCompassFullscreenMapRoute(
    vm: TeamCompassViewModel,
    nav: NavHostController,
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    if (!state.isAuthReady) {
        LoadingScreen()
    } else if (state.teamCode == null) {
        // Redirect is handled by top-level TeamCompassApp navigation effect.
        LoadingScreen()
    } else {
        ForceLandscapeOrientation()
        FullscreenMapScreen(
            state = state,
            onBack = { nav.popBackStack() },
            onSaveToSource = vm::saveMapChangesToSource,
            onSaveAs = vm::saveMapChangesAs,
            onAddSharedMarker = { lat, lon, name, iconRaw ->
                vm.addPointAt(lat, lon, name, iconRaw, true)
            },
            onOpenStructure = { nav.navigate(ROUTE_STRUCTURE) },
        )
    }
}


private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    cm?.setPrimaryClip(ClipData.newPlainText("Team code", text))
}
