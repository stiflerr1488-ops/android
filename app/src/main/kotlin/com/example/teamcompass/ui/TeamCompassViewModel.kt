package com.example.teamcompass.ui

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import android.util.Log
import android.view.Window
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.teamcompass.BuildConfig
import com.example.teamcompass.R
import com.example.teamcompass.auth.FirebaseIdentityLinkingService
import com.example.teamcompass.auth.IdentityLinkingService
import com.example.teamcompass.bluetooth.BluetoothScanner
import com.example.teamcompass.core.PlayerMode
import com.example.teamcompass.core.TargetFilterPreset
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TeamRolePatch
import com.example.teamcompass.domain.TeamViewMode
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.p2p.P2PTransportManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class TeamCompassViewModel internal constructor(
    private val app: Application,
    private val teamRepository: TeamRepository,
    private val trackingController: TrackingController,
    private val prefs: UserPrefs,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val autoStart: Boolean = false,
    private val actionTraceIdProvider: ActionTraceIdProvider = UuidActionTraceIdProvider(),
    private val structuredLogger: StructuredLogger = CrashlyticsStructuredLogger(),
    private val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        Log.e(TAG, "Unhandled VM coroutine exception", t)
    },
    private val identityLinkingService: IdentityLinkingService = FirebaseIdentityLinkingService(auth),
    private val p2pTransportManager: P2PTransportManager? = null,
    private val initializeAutoStartOverride: (suspend TeamCompassViewModel.() -> Unit)? = null,
    private val savedStateHandle: SavedStateHandle? = null,
) : AndroidViewModel(app) {

    @Inject
    constructor(
        app: Application,
        teamRepository: TeamRepository,
        trackingController: TrackingController,
        prefs: UserPrefs,
        auth: FirebaseAuth,
        coroutineExceptionHandler: CoroutineExceptionHandler,
        p2pTransportManager: P2PTransportManager,
        savedStateHandle: SavedStateHandle,
    ) : this(
        app = app,
        teamRepository = teamRepository,
        trackingController = trackingController,
        prefs = prefs,
        auth = auth,
        autoStart = true,
        actionTraceIdProvider = UuidActionTraceIdProvider(),
        structuredLogger = CrashlyticsStructuredLogger(),
        coroutineExceptionHandler = coroutineExceptionHandler,
        identityLinkingService = FirebaseIdentityLinkingService(auth),
        p2pTransportManager = p2pTransportManager,
        initializeAutoStartOverride = null,
        savedStateHandle = savedStateHandle,
    )

    private val savedStateBinder = TeamCompassSavedStateBinder()
    private val restored = savedStateBinder.restore(savedStateHandle)

    private val sensorManager = app.getSystemService(SensorManager::class.java)
    private val displayManager = app.getSystemService(DisplayManager::class.java)
    private val vibrator = app.getSystemService(Vibrator::class.java)
    private val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _ui = MutableStateFlow(
        UiState(
            tracking = TrackingUiState(
                defaultMode = restored.defaultMode,
                isTracking = restored.isTracking,
            ),
            team = TeamUiState(
                teamCode = restored.teamCode,
                playerMode = restored.playerMode,
                mySosUntilMs = restored.mySosUntilMs,
            ),
            filter = FilterUiState(targetFilterState = restored.targetFilterState),
        )
    )
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var bindingsStarted = false
    private var targetFilterDirtyByUser = false
    private val joinRateLimiter = JoinRateLimiter()

    private val authDelegate = AuthDelegate(FirebaseAuthGateway(auth))
    private val sessionCoordinator = SessionCoordinator(teamRepository, opTimeoutMs = 15_000L)
    private val teamSessionDelegate = TeamSessionDelegate(sessionCoordinator, p2pTransportManager)
    private val mapCoordinator = MapCoordinator()
    private val alertsCoordinator = AlertsCoordinator()
    private val targetFilterCoordinator = TargetFilterCoordinator(BuildConfig.TACTICAL_FILTERS_V1_ENABLED)
    private val locationReadinessCoordinator = LocationReadinessCoordinator(app)
    private val backendAvailabilityCoordinator = BackendAvailabilityCoordinator(
        backendHealthDelegate = BackendHealthDelegate(
            backendHealthMonitor = BackendHealthMonitor(teamRepository),
            staleWarningMs = STALE_WARNING_MS,
        ),
        scope = viewModelScope,
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
    )
    private val headingSensorCoordinator = HeadingSensorCoordinator(
        sensorManager = sensorManager,
        displayManager = displayManager,
        rotationSensor = rotationSensor,
        onHeadingChanged = { heading ->
            updateState { it.copy(tracking = it.tracking.copy(myHeadingDeg = heading)) }
            trackingController.updateHeading(heading)
            refreshTargetsFromState()
        },
    )
    private val autoBrightnessBinding = AutoBrightnessBinding(
        app = app,
        onInitError = { err -> Log.w(TAG, "AutoBrightness init failed", err) },
    )
    private val bluetoothScanCoordinator = BluetoothScanCoordinator(
        application = app,
        teamRepository = teamRepository,
        scope = viewModelScope,
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
        bluetoothScanner = BluetoothScanner(app),
    )
    private val mapActionsCoordinator = MapActionsCoordinator(
        application = app,
        mapCoordinator = mapCoordinator,
        scope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler,
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
    )
    private val tacticalActionsCoordinator = TacticalActionsCoordinator(
        teamRepository = teamRepository,
        scope = viewModelScope,
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
        handleActionFailure = ::handleActionFailure,
        newTraceId = ::newTraceId,
        logActionStart = ::logActionStart,
        logActionSuccess = ::logActionSuccess,
        logActionFailure = ::logActionFailure,
    )
    private val trackingCoordinator = TrackingCoordinator(trackingController)
    private val p2pInboundCoordinator = P2PInboundCoordinator(
        scope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler,
        observeInbound = teamSessionDelegate::observeP2PInbound,
        updateState = ::updateState,
        onStreamFailure = { err, teamCode, uid ->
            logActionFailure("p2pInbound", newTraceId("p2pInbound"), err, err.message, teamCode, uid)
        },
    )
    private val memberPrefsSyncCoordinator = MemberPrefsSyncCoordinator(
        worker = MemberPrefsSyncWorker(
            teamRepository = teamRepository,
            tacticalFiltersEnabled = BuildConfig.TACTICAL_FILTERS_V1_ENABLED,
        ),
        scope = viewModelScope,
        toMemberPrefs = targetFilterCoordinator::toMemberPrefs,
        onRemotePrefs = ::onRemoteMemberPrefs,
        onObserverFailure = { err -> Log.w(TAG, "memberPrefs observer failed; retrying", err) },
        isUserDirty = { targetFilterDirtyByUser },
        onSyncSuccess = { targetFilterDirtyByUser = false },
        onSyncFailure = { failure, userInitiated ->
            if (userInitiated) handleActionFailure(tr(R.string.vm_error_targets_filter_save_failed), failure)
        },
    )
    private val teamCompassAlertEffectsCoordinator = TeamCompassAlertEffectsCoordinator(
        scope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler,
        application = app,
        alertsCoordinator = alertsCoordinator,
        eventNotificationManager = EventNotificationManager(app),
        readState = ::readState,
        vibrator = vibrator,
        tone = tone,
        logTag = TAG,
    )
    private val teamSnapshotSyncCoordinator = TeamSnapshotSyncCoordinator(
        teamSnapshotObserver = TeamSnapshotObserver(
            teamRepository = teamRepository,
            initialRetryDelayMs = 1_000L,
            maxRetryDelayMs = 10_000L,
        ),
        backendAvailabilityCoordinator = backendAvailabilityCoordinator,
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
        onSnapshotSideEffects = { enemyPings ->
            refreshTargetsFromState()
            teamCompassAlertEffectsCoordinator.processEnemyPingAlerts(enemyPings)
            teamCompassAlertEffectsCoordinator.processSosAlerts()
        },
    )
    private val identityLinkingCoordinator = IdentityLinkingCoordinator(
        identityLinkingService = identityLinkingService,
        nextTraceId = { newTraceId("identityLinking") },
        onStart = { traceId, teamCode, uid -> logActionStart("identityLinkingEligibility", traceId, teamCode, uid) },
        onSuccess = { traceId, teamCode, uid -> logActionSuccess("identityLinkingEligibility", traceId, teamCode, uid) },
        onFailure = { traceId, teamCode, uid, err ->
            logActionFailure("identityLinkingEligibility", traceId, err, err.message, teamCode, uid)
        },
        onEligiblePrompt = { uid, teamCode, reason ->
            Log.i(TAG, "Identity linking eligible (uid=$uid, teamCode=$teamCode, reason=$reason)")
        },
    )
    private val teamCompassDeviceUiCoordinator = TeamCompassDeviceUiCoordinator(
        scope = viewModelScope,
        application = app,
        headingSensorCoordinator = headingSensorCoordinator,
        locationReadinessCoordinator = locationReadinessCoordinator,
        autoBrightnessBinding = autoBrightnessBinding,
        bluetoothScanCoordinatorProvider = { bluetoothScanCoordinator },
        readState = ::readState,
        updateState = ::updateState,
        emitError = ::emitError,
        persistAutoBrightnessEnabled = { enabled -> launchPrefsWrite { setAutoBrightnessEnabled(enabled) } },
        persistScreenBrightness = { brightness -> launchPrefsWrite { setScreenBrightness(brightness) } },
        persistHasStartedOnce = { value -> launchPrefsWrite { setHasStartedOnce(value) } },
        startTracking = { mode, persist -> startTracking(mode, persist) },
        servicesDisabledError = { tr(R.string.vm_error_location_services_disabled) },
        trackingDisabledError = { tr(R.string.vm_error_location_disabled_during_tracking) },
        bluetoothUnavailableError = { tr(R.string.vm_error_bluetooth_unavailable) },
        pollIntervalMs = { enabled -> locationServicePollIntervalMs(enabled) },
        logTag = TAG,
    )
    private val teamCompassSessionListeningCoordinator = TeamCompassSessionListeningCoordinator(
        scope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler,
        application = app,
        teamSessionDelegate = teamSessionDelegate,
        readState = ::readState,
        updateState = ::updateState,
        normalizeTeamCode = sessionCoordinator::normalizeTeamCode,
        emitError = ::emitError,
        clearStoredTeamCode = { launchPrefsWrite { setTeamCode(null) } },
        startBackendAvailability = backendAvailabilityCoordinator::start,
        stopBackendAvailability = backendAvailabilityCoordinator::stop,
        startP2PInbound = { teamCode, uid -> p2pInboundCoordinator.start(teamCode, uid) },
        stopP2PInbound = p2pInboundCoordinator::stop,
        startMemberPrefsSync = { teamCode, uid ->
            memberPrefsSyncCoordinator.start(teamCode, uid, _ui.map { it.filter.targetFilterState })
        },
        stopMemberPrefsSync = memberPrefsSyncCoordinator::stop,
        collectTeamSnapshot = { teamCode, uid, backendDownMessage ->
            teamSnapshotSyncCoordinator.collect(teamCode, uid, backendDownMessage)
        },
        newTraceId = ::newTraceId,
        logActionStart = ::logActionStart,
        logActionSuccess = ::logActionSuccess,
        logActionFailure = ::logActionFailure,
        logTag = TAG,
    )

    init {
        savedStateBinder.bind(
            scope = viewModelScope,
            savedStateHandle = savedStateHandle,
            uiStateFlow = _ui,
            coroutineExceptionHandler = coroutineExceptionHandler,
            logWarning = { m, e -> if (e != null) Log.w(TAG, m, e) else Log.w(TAG, m) },
        )
        if (autoStart) runAutoStartInitialization()
    }

    private fun runAutoStartInitialization() {
        val traceId = newTraceId("initializeAutoStart")
        logActionStart("initializeAutoStart", traceId, readState().teamCode, readState().uid)
        viewModelScope.launch {
            try {
                initializeAutoStartOverride?.invoke(this@TeamCompassViewModel) ?: initializeAutoStart()
                logActionSuccess("initializeAutoStart", traceId, readState().teamCode, readState().uid)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (err: Throwable) {
                logActionFailure("initializeAutoStart", traceId, err, err.message, readState().teamCode, readState().uid)
                emitError("Initialization failed: ${err.message ?: err::class.java.simpleName}", err)
            }
        }
    }

    private suspend fun initializeAutoStart() {
        startRuntimeBindingsIfNeeded()
        refreshLocationReadiness()
        teamCompassDeviceUiCoordinator.startHeading()
        teamCompassDeviceUiCoordinator.startLocationServiceMonitor()
        ensureAuth()
    }

    private fun startRuntimeBindingsIfNeeded() {
        if (bindingsStarted) return
        bindingsStarted = true
        launchPrefsBindings(
            scope = viewModelScope,
            coroutineExceptionHandler = coroutineExceptionHandler,
            prefs = prefs,
            readState = ::readState,
            updateState = ::updateState,
            normalizeTeamCode = sessionCoordinator::normalizeTeamCode,
            startListening = { code -> teamCompassSessionListeningCoordinator.startListening(code) },
            stopListening = ::stopListening,
            autoBrightnessBinding = autoBrightnessBinding,
        )
        launchTrackingControllerBindings(
            scope = viewModelScope,
            coroutineExceptionHandler = coroutineExceptionHandler,
            trackingController = trackingController,
            updateState = ::updateState,
            refreshTargetsFromState = ::refreshTargetsFromState,
        )
    }

    fun ensureAuth() {
        authDelegate.ensureAuth(
            onReady = { uid ->
                updateState { it.copy(auth = it.auth.copy(isReady = true, uid = uid)) }
                readState().teamCode?.let { code -> teamCompassSessionListeningCoordinator.startListening(code) }
            },
            onFailure = { err ->
                updateState { it.copy(auth = it.auth.copy(isReady = false)) }
                emitError(tr(R.string.vm_error_auth_failed_format, err.message.orEmpty()), err)
            },
        )
    }

    fun setCallsign(value: String) {
        val v = value.trim().take(24)
        updateState { it.copy(team = it.team.copy(callsign = v)) }
        launchPrefsWrite { setCallsign(v) }
    }

    fun createTeam() {
        if (readState().isBusy) return
        val uid = readState().uid
        if (uid.isNullOrBlank()) {
            emitError(tr(R.string.vm_error_auth_not_ready))
            ensureAuth()
            return
        }
        val callsign = readState().callsign.ifBlank { tr(R.string.default_callsign_player) }
        val traceId = newTraceId("createTeam")
        logActionStart("createTeam", traceId, null, uid)
        updateState { it.copy(team = it.team.copy(isBusy = true)) }
        viewModelScope.launch(coroutineExceptionHandler) {
            when (val result = sessionCoordinator.createTeam(uid, callsign)) {
                is TeamActionResult.Success -> {
                    val code = sessionCoordinator.normalizeTeamCode(result.value) ?: result.value
                    updateState { state ->
                        state.copy(team = state.team.copy(teamCode = code, isBusy = false), lastError = null)
                    }
                    launchPrefsWrite { setTeamCode(code) }
                    joinRateLimiter.reset()
                    teamCompassSessionListeningCoordinator.startListeningWithoutMembershipEnsure(code)
                    identityLinkingCoordinator.evaluate(code, uid)
                    logActionSuccess("createTeam", traceId, code, uid)
                }
                is TeamActionResult.Failure -> {
                    updateState { it.copy(team = it.team.copy(isBusy = false)) }
                    handleActionFailure(tr(R.string.vm_error_create_team_failed), result.details)
                    logActionFailure("createTeam", traceId, result.details.cause, result.details.message, null, uid)
                }
            }
        }
    }

    fun joinTeam(codeRaw: String, alsoCreateMember: Boolean = true) {
        if (readState().isBusy) return
        val uid = readState().uid
        if (uid.isNullOrBlank()) {
            emitError(tr(R.string.vm_error_auth_not_ready))
            ensureAuth()
            return
        }
        val code = sessionCoordinator.normalizeTeamCode(codeRaw)
        if (code == null) {
            emitError(tr(R.string.join_code_error))
            return
        }
        if (!joinRateLimiter.canAttempt(code)) {
            emitError(tr(R.string.vm_error_join_rate_limited))
            return
        }
        val callsign = readState().callsign.ifBlank { tr(R.string.default_callsign_player) }
        val traceId = newTraceId("joinTeam")
        logActionStart("joinTeam", traceId, code, uid)
        updateState { it.copy(team = it.team.copy(isBusy = true)) }
        viewModelScope.launch(coroutineExceptionHandler) {
            val joinResult = if (alsoCreateMember) sessionCoordinator.joinTeam(code, uid, callsign) else TeamActionResult.Success(Unit)
            when (joinResult) {
                is TeamActionResult.Success -> {
                    updateState { state -> state.copy(team = state.team.copy(teamCode = code, isBusy = false), lastError = null) }
                    launchPrefsWrite { setTeamCode(code) }
                    teamCompassSessionListeningCoordinator.startListeningWithoutMembershipEnsure(code)
                    identityLinkingCoordinator.evaluate(code, uid)
                    logActionSuccess("joinTeam", traceId, code, uid)
                }
                is TeamActionResult.Failure -> {
                    updateState { it.copy(team = it.team.copy(isBusy = false)) }
                    handleActionFailure(tr(R.string.vm_error_join_team_failed), joinResult.details)
                    logActionFailure("joinTeam", traceId, joinResult.details.cause, joinResult.details.message, code, uid)
                }
            }
        }
    }

    fun leaveTeam() {
        stopTracking()
        stopListening()
        teamCompassSessionListeningCoordinator.clearTeamSessionState(clearEnemyMarkEnabled = true)
        updateState { it.copy(team = it.team.copy(teamCode = null)) }
        launchPrefsWrite { setTeamCode(null) }
        joinRateLimiter.reset()
    }
    fun setDefaultMode(mode: TrackingMode) {
        updateState { it.copy(tracking = it.tracking.copy(defaultMode = mode)) }
        launchPrefsWrite { setDefaultMode(mode) }
    }

    fun setGamePolicy(intervalSec: Int, distanceM: Int) {
        val i = intervalSec.coerceIn(1, 60)
        val d = distanceM.coerceIn(1, 500)
        updateState { it.copy(settings = it.settings.copy(gameIntervalSec = i, gameDistanceM = d)) }
        launchPrefsWrite { setGamePolicy(i, d) }
    }

    fun setSilentPolicy(intervalSec: Int, distanceM: Int) {
        val i = intervalSec.coerceIn(1, 120)
        val d = distanceM.coerceIn(1, 500)
        updateState { it.copy(settings = it.settings.copy(silentIntervalSec = i, silentDistanceM = d)) }
        launchPrefsWrite { setSilentPolicy(i, d) }
    }

    fun setLocationPermission(granted: Boolean) {
        updateState { it.copy(tracking = it.tracking.copy(hasLocationPermission = granted)) }
        if (!granted) stopTracking()
    }

    fun refreshLocationReadiness() {
        val update = locationReadinessCoordinator.refreshReadiness(
            state = readState(),
            permissionError = tr(R.string.vm_error_location_permission_required),
            servicesDisabledError = tr(R.string.vm_error_location_services_disabled),
            trackingDisabledError = tr(R.string.vm_error_location_disabled_during_tracking),
        )
        updateState { update.updatedState }
        if (update.shouldRefreshPreview) refreshTargetsFromState()
    }

    fun togglePlayerMode() {
        setPlayerMode(if (readState().playerMode == PlayerMode.GAME) PlayerMode.DEAD else PlayerMode.GAME)
    }

    fun setPlayerMode(mode: PlayerMode) {
        updateState { it.copy(team = it.team.copy(playerMode = mode)) }
        if (mode == PlayerMode.DEAD) teamCompassAlertEffectsCoordinator.startDeadReminder()
        else teamCompassAlertEffectsCoordinator.stopDeadReminder()
        trackingController.updateStatus(mode, readState().mySosUntilMs, forceSend = true)
        refreshTargetsFromState()
    }

    fun toggleSos() {
        if (readState().mySosUntilMs > System.currentTimeMillis()) clearSos() else triggerSos()
    }

    fun triggerSos() {
        val until = System.currentTimeMillis() + 120_000L
        updateState { it.copy(team = it.team.copy(mySosUntilMs = until)) }
        trackingController.updateStatus(readState().playerMode, until, forceSend = true)
    }

    fun clearSos() {
        updateState { it.copy(team = it.team.copy(mySosUntilMs = 0L)) }
        trackingController.updateStatus(readState().playerMode, 0L, forceSend = true)
    }

    fun setEnemyMarkEnabled(enabled: Boolean) {
        updateState { it.copy(map = it.map.copy(enemyMarkEnabled = enabled)) }
    }

    fun importTacticalMap(uri: android.net.Uri) = mapActionsCoordinator.importMap(uri)
    fun clearTacticalMap() = mapActionsCoordinator.clearMap()
    fun setMapEnabled(enabled: Boolean) = mapActionsCoordinator.setMapEnabled(enabled)
    fun setMapOpacity(opacity: Float) = mapActionsCoordinator.setMapOpacity(opacity)

    fun saveMapChangesToSource(newPoints: List<KmlPoint>, deletedPoints: List<KmlPoint> = emptyList()) {
        mapActionsCoordinator.saveChangesToSource(newPoints = newPoints, deletedPoints = deletedPoints)
    }

    fun saveMapChangesAs(
        uri: android.net.Uri,
        newPoints: List<KmlPoint>,
        deletedPoints: List<KmlPoint> = emptyList(),
    ) {
        mapActionsCoordinator.saveChangesAs(uri = uri, newPoints = newPoints, deletedPoints = deletedPoints)
    }

    fun addPointAt(lat: Double, lon: Double, label: String, icon: String, forTeam: Boolean) {
        tacticalActionsCoordinator.addPointAt(
            lat = lat,
            lon = lon,
            label = label,
            icon = icon,
            forTeam = forTeam,
            addPointFailedMessage = tr(R.string.vm_error_add_point_failed),
            invalidInputMessage = tr(R.string.error_invalid_input),
        )
    }

    fun updatePoint(id: String, lat: Double, lon: Double, label: String, icon: String, isTeam: Boolean) {
        tacticalActionsCoordinator.updatePoint(
            id = id,
            lat = lat,
            lon = lon,
            label = label,
            icon = icon,
            isTeam = isTeam,
            onlyAuthorEditMessage = tr(R.string.vm_error_only_author_edit_team_point),
            updatePointFailedMessage = tr(R.string.vm_error_update_point_failed),
            invalidInputMessage = tr(R.string.error_invalid_input),
        )
    }

    fun deletePoint(id: String, isTeam: Boolean) {
        tacticalActionsCoordinator.deletePoint(
            id = id,
            isTeam = isTeam,
            onlyAuthorDeleteMessage = tr(R.string.vm_error_only_author_delete_team_point),
            deletePointFailedMessage = tr(R.string.vm_error_delete_point_failed),
        )
    }

    fun sendQuickCommand(type: QuickCommandType) {
        tacticalActionsCoordinator.sendQuickCommand(type, tr(R.string.vm_error_quick_command_failed))
    }

    fun addEnemyPing(lat: Double, lon: Double, type: QuickCommandType) {
        tacticalActionsCoordinator.addEnemyPing(
            lat = lat,
            lon = lon,
            type = type,
            enemyMarkFailedMessage = tr(R.string.vm_error_enemy_mark_failed),
            invalidInputMessage = tr(R.string.error_invalid_input),
        )
    }

    fun computeTargets(nowMs: Long): List<com.example.teamcompass.core.CompassTarget> {
        return targetFilterCoordinator.buildTargetsForState(readState(), nowMs).second
    }

    fun setTargetPreset(preset: TargetFilterPreset) {
        targetFilterDirtyByUser = true
        updateState { it.copy(filter = it.filter.copy(targetFilterState = it.targetFilterState.copy(preset = preset))) }
        refreshTargetsFromState()
    }

    fun setNearRadius(nearRadiusM: Int) {
        targetFilterDirtyByUser = true
        updateState {
            it.copy(filter = it.filter.copy(targetFilterState = it.targetFilterState.copy(nearRadiusM = nearRadiusM.coerceIn(50, 500))))
        }
        refreshTargetsFromState()
    }

    fun setShowDead(showDead: Boolean) {
        targetFilterDirtyByUser = true
        updateState { it.copy(filter = it.filter.copy(targetFilterState = it.targetFilterState.copy(showDead = showDead))) }
        refreshTargetsFromState()
    }

    fun setShowStale(showStale: Boolean) {
        targetFilterDirtyByUser = true
        updateState { it.copy(filter = it.filter.copy(targetFilterState = it.targetFilterState.copy(showStale = showStale))) }
        refreshTargetsFromState()
    }

    fun setFocusMode(focusMode: Boolean) {
        targetFilterDirtyByUser = true
        updateState { it.copy(filter = it.filter.copy(targetFilterState = it.targetFilterState.copy(focusMode = focusMode))) }
        refreshTargetsFromState()
    }

    fun startTracking(mode: TrackingMode, persistMode: Boolean = true) {
        val readiness = locationReadinessCoordinator.refreshReadiness(
            state = readState(),
            permissionError = tr(R.string.vm_error_location_permission_required),
            servicesDisabledError = tr(R.string.vm_error_location_services_disabled),
            trackingDisabledError = tr(R.string.vm_error_location_disabled_during_tracking),
        )
        updateState { readiness.updatedState }
        val state = readState()
        if (!state.hasLocationPermission) {
            emitError(tr(R.string.vm_error_location_permission_required))
            return
        }
        if (!state.isLocationServiceEnabled) {
            emitError(tr(R.string.vm_error_location_services_disabled))
            return
        }
        val teamCode = state.teamCode ?: return
        val uid = state.uid ?: return
        if (persistMode) {
            updateState { it.copy(tracking = it.tracking.copy(defaultMode = mode)) }
            launchPrefsWrite { setDefaultMode(mode) }
        }
        teamCompassDeviceUiCoordinator.startHeading()
        teamCompassDeviceUiCoordinator.startLocationServiceMonitor()
        trackingCoordinator.start(
            request = TrackingCoordinator.StartRequest(
                teamCode = teamCode,
                uid = uid,
                callsign = state.callsign,
                mode = mode,
                gameIntervalSec = state.gameIntervalSec,
                gameDistanceM = state.gameDistanceM,
                silentIntervalSec = state.silentIntervalSec,
                silentDistanceM = state.silentDistanceM,
                playerMode = state.playerMode,
                sosUntilMs = state.mySosUntilMs,
            ),
            isCurrentlyTracking = state.isTracking,
        )
        if (!state.hasStartedOnce) teamCompassDeviceUiCoordinator.setHasStartedOnce(true)
    }

    fun stopTracking() {
        trackingCoordinator.stop()
    }

    fun startBluetoothScan() {
        teamCompassDeviceUiCoordinator.startBluetoothScan()
    }

    fun bindAutoBrightnessWindow(window: Window?) {
        teamCompassDeviceUiCoordinator.bindAutoBrightnessWindow(window)
    }

    fun setTeamViewMode(mode: TeamViewMode) {
        updateState { it.copy(team = it.team.copy(viewMode = mode)) }
    }

    fun setControlLayoutEdit(enabled: Boolean) {
        updateState { it.copy(settings = it.settings.copy(controlLayoutEditEnabled = enabled)) }
        launchPrefsWrite { setControlLayoutEdit(enabled) }
    }

    fun resetControlPositions() {
        updateState { it.copy(settings = it.settings.copy(controlPositions = defaultCompassControlPositions())) }
        launchPrefsWrite { resetControlPositions() }
    }

    fun markCompassHelpSeen() {
        updateState { it.copy(settings = it.settings.copy(showCompassHelpOnce = false)) }
        launchPrefsWrite { setShowCompassHelpOnce(false) }
    }

    fun assignTeamMemberRole(targetUid: String, patch: TeamRolePatch) {
        val teamCode = readState().teamCode ?: return
        val actorUid = readState().uid ?: return
        viewModelScope.launch(coroutineExceptionHandler) {
            when (val result = teamRepository.assignTeamMemberRole(teamCode, actorUid, targetUid, patch)) {
                is TeamActionResult.Success -> updateState { state ->
                    val next = state.roleProfiles.filterNot { it.uid == result.value.uid } + result.value
                    state.copy(team = state.team.copy(roleProfiles = next))
                }
                is TeamActionResult.Failure -> handleActionFailure(tr(R.string.vm_error_assign_role_failed), result.details)
            }
        }
    }

    fun assignTeamMemberRolesBulk(targetUids: List<String>, patch: TeamRolePatch) {
        val targets = targetUids.distinct().filter { it.isNotBlank() }
        if (targets.isEmpty()) return
        val teamCode = readState().teamCode ?: return
        val actorUid = readState().uid ?: return
        viewModelScope.launch(coroutineExceptionHandler) {
            var success = 0
            var fail = 0
            targets.forEach { targetUid ->
                when (teamRepository.assignTeamMemberRole(teamCode, actorUid, targetUid, patch)) {
                    is TeamActionResult.Success -> success++
                    is TeamActionResult.Failure -> fail++
                }
            }
            when {
                fail == 0 -> Unit
                success > 0 -> emitError(tr(R.string.vm_error_bulk_assign_partial_format, success, fail))
                else -> emitError(tr(R.string.vm_error_bulk_assign_failed_format, fail))
            }
        }
    }
    fun dismissError() {
        updateState { it.copy(lastError = null) }
    }

    fun setUiForTest(update: (UiState) -> UiState) {
        updateState(update)
    }

    fun refreshBackendStaleFlagForTest(nowMs: Long = System.currentTimeMillis()) {
        backendAvailabilityCoordinator.refreshStaleFlag(
            nowMs = nowMs,
            backendDownMessage = tr(R.string.vm_error_backend_unavailable_retrying),
        )
    }

    private fun onRemoteMemberPrefs(prefs: TeamMemberPrefs?) {
        val next = targetFilterCoordinator.fromRemotePrefs(prefs)
        targetFilterDirtyByUser = false
        updateState { state ->
            if (state.filter.targetFilterState == next) state
            else state.copy(filter = state.filter.copy(targetFilterState = next))
        }
        refreshTargetsFromState()
    }

    private fun refreshTargetsFromState(nowMs: Long = System.currentTimeMillis()) {
        val (prioritized, display) = targetFilterCoordinator.buildTargetsForState(readState(), nowMs)
        updateState { state ->
            state.copy(
                filter = state.filter.copy(
                    prioritizedTargets = prioritized,
                    displayTargets = display,
                ),
            )
        }
    }

    private fun stopListening() {
        teamCompassSessionListeningCoordinator.stopListening()
    }

    private fun readState(): UiState = _ui.value

    private fun updateState(update: (UiState) -> UiState) {
        _ui.update(update)
    }

    private fun emitError(message: String) {
        emitError(message, null)
    }

    private fun emitError(message: String, cause: Throwable?) {
        if (cause != null) Log.w(TAG, message, cause) else Log.w(TAG, message)
        _ui.update { it.copy(lastError = message) }
        _events.tryEmit(UiEvent.Error(message))
    }

    private fun handleActionFailure(defaultMessage: String, failure: TeamActionFailure) {
        emitError(TeamActionErrorPolicy.toUserMessage(app, defaultMessage, failure), failure.cause)
    }

    private fun newTraceId(action: String): String = actionTraceIdProvider.nextTraceId(action)

    private fun logActionStart(action: String, traceId: String, teamCode: String?, uid: String?) {
        structuredLogger.logStart(action, traceId, teamCode, uid, readState().telemetry.backendAvailable)
    }

    private fun logActionSuccess(action: String, traceId: String, teamCode: String?, uid: String?) {
        structuredLogger.logSuccess(action, traceId, teamCode, uid, readState().telemetry.backendAvailable)
    }

    private fun logActionFailure(
        action: String,
        traceId: String,
        throwable: Throwable?,
        message: String?,
        teamCode: String?,
        uid: String?,
    ) {
        structuredLogger.logFailure(
            action = action,
            traceId = traceId,
            teamCode = teamCode,
            uid = uid,
            backendAvailable = readState().telemetry.backendAvailable,
            throwable = throwable,
            message = message,
        )
    }

    private fun launchPrefsWrite(write: suspend UserPrefs.() -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            prefs.write()
        }
    }

    private fun tr(@StringRes resId: Int, vararg args: Any): String {
        return if (args.isEmpty()) app.getString(resId) else app.getString(resId, *args)
    }

    override fun onCleared() {
        stopListening()
        trackingCoordinator.stop()
        teamCompassAlertEffectsCoordinator.shutdown()
        teamCompassDeviceUiCoordinator.onCleared()
        runCatching { tone.release() }
        super.onCleared()
    }

    companion object {
        const val STALE_WARNING_MS: Long = 30_000L

        fun locationServicePollIntervalMs(isTracking: Boolean): Long {
            return if (isTracking) 2_000L else 12_000L
        }

        private const val TAG = "TeamCompassVM"
    }
}
