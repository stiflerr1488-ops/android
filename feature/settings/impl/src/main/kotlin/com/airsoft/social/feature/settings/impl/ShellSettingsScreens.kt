package com.airsoft.social.feature.settings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.data.SettingsRepository
import com.airsoft.social.core.model.AccountAccess
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AppSettings
import com.airsoft.social.core.model.AppThemePreference
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.DeviceDiagnosticsSnapshot
import com.airsoft.social.core.model.IntegrityVerdict
import com.airsoft.social.core.model.PermissionStatusSnapshot
import com.airsoft.social.core.model.SecurityStatusSnapshot
import com.airsoft.social.core.model.toAccountAccess
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.settings.api.SettingsFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShellWireframeRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

enum class SettingsThemeMode {
    System,
    Light,
    Dark,
}

data class SettingsUiState(
    val selectedTheme: SettingsThemeMode = SettingsThemeMode.System,
    val pushEnabled: Boolean = true,
    val telemetryEnabled: Boolean = false,
    val tacticalQuickLaunchEnabled: Boolean = true,
    val accountAccess: AccountAccess = AccountAccess(),
    val accountCallsign: String = "",
)

sealed interface SettingsAction {
    data object CycleTheme : SettingsAction
    data object TogglePush : SettingsAction
    data object ToggleTelemetry : SettingsAction
    data object ToggleTacticalQuickLaunch : SettingsAction
    data object OpenAccountClicked : SettingsAction
    data object OpenPrivacyClicked : SettingsAction
    data object OpenPermissionsClicked : SettingsAction
    data object OpenSecurityClicked : SettingsAction
    data object OpenBatteryClicked : SettingsAction
    data object OpenSupportClicked : SettingsAction
    data object OpenAboutClicked : SettingsAction
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.observeSettings(),
                sessionRepository.authState,
            ) { settings, authState ->
                settings.toUiState(authState)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.CycleTheme -> {
                val nextTheme = when (_uiState.value.selectedTheme) {
                    SettingsThemeMode.System -> SettingsThemeMode.Light
                    SettingsThemeMode.Light -> SettingsThemeMode.Dark
                    SettingsThemeMode.Dark -> SettingsThemeMode.System
                }
                viewModelScope.launch {
                    settingsRepository.setThemePreference(nextTheme.toPreference())
                }
            }

            SettingsAction.TogglePush -> {
                viewModelScope.launch {
                    settingsRepository.setPushEnabled(!_uiState.value.pushEnabled)
                }
            }

            SettingsAction.ToggleTelemetry -> {
                viewModelScope.launch {
                    settingsRepository.setTelemetryEnabled(!_uiState.value.telemetryEnabled)
                }
            }

            SettingsAction.ToggleTacticalQuickLaunch -> {
                viewModelScope.launch {
                    settingsRepository.setTacticalQuickLaunchEnabled(
                        !_uiState.value.tacticalQuickLaunchEnabled,
                    )
                }
            }

            SettingsAction.OpenAccountClicked -> Unit
            SettingsAction.OpenPrivacyClicked -> Unit
            SettingsAction.OpenPermissionsClicked -> Unit
            SettingsAction.OpenSecurityClicked -> Unit
            SettingsAction.OpenBatteryClicked -> Unit
            SettingsAction.OpenSupportClicked -> Unit
            SettingsAction.OpenAboutClicked -> Unit
        }
    }
}

@Composable
fun SettingsShellRoute(
    onOpenAccount: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenBattery: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    systemStatusViewModel: SettingsSystemStatusViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val permissionStatus by systemStatusViewModel.permissionStatus.collectAsState()
    val securityStatus by systemStatusViewModel.securityStatus.collectAsState()
    val deviceDiagnostics by systemStatusViewModel.deviceDiagnostics.collectAsState()
    LaunchedEffect(Unit) {
        systemStatusViewModel.refreshPermissions()
        systemStatusViewModel.refreshSecurity()
        systemStatusViewModel.refreshDiagnostics()
    }
    SettingsShellScreen(
        uiState = uiState,
        permissionStatus = permissionStatus,
        securityStatus = securityStatus,
        deviceDiagnostics = deviceDiagnostics,
        onAction = { action ->
            when (action) {
                SettingsAction.OpenAccountClicked -> onOpenAccount()
                SettingsAction.OpenPrivacyClicked -> onOpenPrivacy()
                SettingsAction.OpenPermissionsClicked -> onOpenPermissions()
                SettingsAction.OpenSecurityClicked -> onOpenSecurity()
                SettingsAction.OpenBatteryClicked -> onOpenBattery()
                SettingsAction.OpenSupportClicked -> onOpenSupport()
                SettingsAction.OpenAboutClicked -> onOpenAbout()
                else -> settingsViewModel.onAction(action)
            }
        },
    )
}

private fun AppSettings.toUiState(authState: AuthState): SettingsUiState = SettingsUiState(
    selectedTheme = when (themePreference) {
        AppThemePreference.System -> SettingsThemeMode.System
        AppThemePreference.Light -> SettingsThemeMode.Light
        AppThemePreference.Dark -> SettingsThemeMode.Dark
    },
    pushEnabled = pushEnabled,
    telemetryEnabled = telemetryEnabled,
    tacticalQuickLaunchEnabled = tacticalQuickLaunchEnabled,
    accountAccess = authState.toAccountAccess(),
    accountCallsign = (authState as? AuthState.SignedIn)?.session?.displayName.orEmpty(),
)

private fun SettingsThemeMode.toPreference(): AppThemePreference = when (this) {
    SettingsThemeMode.System -> AppThemePreference.System
    SettingsThemeMode.Light -> AppThemePreference.Light
    SettingsThemeMode.Dark -> AppThemePreference.Dark
}

@Composable
private fun SettingsShellScreen(
    uiState: SettingsUiState,
    permissionStatus: PermissionStatusSnapshot,
    securityStatus: SecurityStatusSnapshot,
    deviceDiagnostics: DeviceDiagnosticsSnapshot,
    onAction: (SettingsAction) -> Unit,
) {
    val onLabel = stringResource(R.string.settings_value_on)
    val offLabel = stringResource(R.string.settings_value_off)
    val themeLabel = when (uiState.selectedTheme) {
        SettingsThemeMode.System -> stringResource(R.string.settings_theme_system)
        SettingsThemeMode.Light -> stringResource(R.string.settings_theme_light)
        SettingsThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
    }
    val accountModeLabel = if (uiState.accountAccess.isGuest) {
        stringResource(R.string.settings_session_mode_guest)
    } else {
        stringResource(R.string.settings_session_mode_registered)
    }
    val accountCallsignLabel = uiState.accountCallsign.ifBlank {
        stringResource(R.string.settings_session_callsign_guest)
    }
    val accountRolesLabel = uiState.accountAccess.roles
        .sortedBy { it.name }
        .map { role ->
            when (role) {
                AccountRole.USER -> stringResource(R.string.settings_role_user)
                AccountRole.COMMERCIAL -> stringResource(R.string.settings_role_commercial)
                AccountRole.MODERATOR -> stringResource(R.string.settings_role_moderator)
                AccountRole.ADMIN -> stringResource(R.string.settings_role_admin)
            }
        }
        .ifEmpty { listOf(stringResource(R.string.settings_session_roles_empty)) }
        .joinToString(", ")
    val accountWriteScopes = buildList {
        if (uiState.accountAccess.canSendChatMessages) {
            add(stringResource(R.string.settings_session_write_chat))
        }
        if (uiState.accountAccess.canCreateMarketplaceListings) {
            add(stringResource(R.string.settings_session_write_marketplace))
        }
        if (uiState.accountAccess.canCreateRideShareListings) {
            add(stringResource(R.string.settings_session_write_rideshare))
        }
        if (uiState.accountAccess.canCreateGameEvents) {
            add(stringResource(R.string.settings_session_write_events))
        }
        if (uiState.accountAccess.canCreateShopListings) {
            add(stringResource(R.string.settings_session_write_shops))
        }
    }
    val accountWritesLabel = accountWriteScopes.ifEmpty {
        listOf(stringResource(R.string.settings_session_write_readonly))
    }.joinToString(", ")
    val sessionRows = listOf(
        ShellWireframeRow(
            title = stringResource(R.string.settings_session_mode_title),
            subtitle = stringResource(R.string.settings_session_mode_subtitle_dynamic),
            trailing = accountModeLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_session_callsign_title),
            subtitle = stringResource(R.string.settings_session_callsign_subtitle),
            trailing = accountCallsignLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_session_roles_title),
            subtitle = stringResource(R.string.settings_session_roles_subtitle),
            trailing = accountRolesLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_session_write_title),
            subtitle = stringResource(R.string.settings_session_write_subtitle),
            trailing = accountWritesLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_profile_visibility_title),
            subtitle = stringResource(R.string.settings_profile_visibility_subtitle),
            trailing = stringResource(R.string.settings_value_public),
        ),
    )
    val integrityLabel = when (securityStatus.integrityVerdict) {
        IntegrityVerdict.Ok -> stringResource(R.string.settings_live_integrity_ok)
        IntegrityVerdict.Warning -> stringResource(R.string.settings_live_integrity_warning)
        IntegrityVerdict.Unknown -> stringResource(R.string.settings_live_integrity_unknown)
    }
    val diagnosticsRows = listOf(
        ShellWireframeRow(
            title = stringResource(R.string.settings_root_diag_permissions_title),
            subtitle = stringResource(
                R.string.settings_root_diag_permissions_subtitle,
                permissionStatus.grantedRuntimePermissions,
                permissionStatus.requiredRuntimePermissions,
            ),
            trailing = "${permissionStatus.grantedRuntimePermissions}/${permissionStatus.requiredRuntimePermissions}",
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_root_diag_notifications_title),
            subtitle = stringResource(R.string.settings_root_diag_notifications_subtitle),
            trailing = if (permissionStatus.notificationsEnabled) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_root_diag_integrity_title),
            subtitle = stringResource(R.string.settings_root_diag_integrity_subtitle),
            trailing = integrityLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_root_diag_battery_title),
            subtitle = stringResource(
                R.string.settings_root_diag_battery_subtitle,
                deviceDiagnostics.estimatedBatteryDrainPercentPerHour,
            ),
            trailing = if (deviceDiagnostics.batteryOptimizationDisabled) {
                stringResource(R.string.settings_live_status_disabled)
            } else {
                stringResource(R.string.settings_live_status_enabled)
            },
        ),
    )

    WireframePage(
        title = stringResource(R.string.settings_root_title),
        subtitle = stringResource(R.string.settings_root_subtitle),
        primaryActionLabel = stringResource(R.string.settings_root_primary_action),
    ) {
        WireframeSection(
            title = stringResource(R.string.settings_appearance_title),
            subtitle = stringResource(R.string.settings_appearance_subtitle),
        ) {
            WireframeItemRow(
                title = stringResource(R.string.settings_theme_title),
                subtitle = stringResource(R.string.settings_theme_subtitle),
                trailing = themeLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_push_title),
                subtitle = stringResource(R.string.settings_push_subtitle),
                trailing = if (uiState.pushEnabled) onLabel else offLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_telemetry_title),
                subtitle = stringResource(R.string.settings_telemetry_subtitle),
                trailing = if (uiState.telemetryEnabled) onLabel else offLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_tactical_quick_launch_title),
                subtitle = stringResource(R.string.settings_tactical_quick_launch_subtitle),
                trailing = if (uiState.tacticalQuickLaunchEnabled) onLabel else offLabel,
            )
        }
        WireframeSection(
            title = stringResource(R.string.settings_session_privacy_title),
            subtitle = stringResource(R.string.settings_session_privacy_subtitle),
        ) {
            sessionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = stringResource(R.string.settings_root_diag_title),
            subtitle = stringResource(R.string.settings_root_diag_subtitle),
        ) {
            diagnosticsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = stringResource(R.string.settings_links_title),
            subtitle = stringResource(R.string.settings_links_subtitle),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(SettingsAction.CycleTheme) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_cycle_theme)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.TogglePush) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_toggle_push)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.ToggleTelemetry) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_toggle_telemetry)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.ToggleTacticalQuickLaunch) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_toggle_tactical_shortcut)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenAccountClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_account)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenPrivacyClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_privacy)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenPermissionsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_permissions)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenSecurityClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_security)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenBatteryClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_battery)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenSupportClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_support)) }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenAboutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.settings_action_open_about)) }
            }
        }
    }
}

data class SettingsAccountUiState(
    val isGuest: Boolean = true,
    val callsign: String = "",
    val email: String? = null,
    val roles: Set<AccountRole> = emptySet(),
    val access: AccountAccess = AccountAccess(),
)

enum class SettingsAccountAccessDeniedReason {
    CommercialRoleRequired,
    ModeratorRoleRequired,
    AdminRoleRequired,
}

@HiltViewModel
class SettingsAccountViewModel @Inject constructor(
    sessionRepository: SessionRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsAccountUiState> = sessionRepository.authState
        .map(AuthState::toSettingsAccountUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsAccountUiState(),
        )
}

@Composable
fun SettingsAccountShellRoute(
    accessDeniedReasonArg: String? = null,
    onOpenAuth: (String?) -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: SettingsAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accessDeniedReason = accessDeniedReasonArg.toSettingsAccountAccessDeniedReason()
    val authReason = if (uiState.isGuest) AUTH_REASON_REGISTRATION_REQUIRED else null
    SettingsAccountScreen(
        uiState = uiState,
        accessDeniedReason = accessDeniedReason,
        onOpenAuth = { onOpenAuth(authReason) },
        onOpenSupport = onOpenSupport,
        onOpenAdminDashboard = onOpenAdminDashboard,
        onSignOut = onSignOut,
    )
}

@Composable
private fun SettingsAccountScreen(
    uiState: SettingsAccountUiState,
    accessDeniedReason: SettingsAccountAccessDeniedReason?,
    onOpenAuth: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onSignOut: () -> Unit,
) {
    val modeLabel = if (uiState.isGuest) {
        stringResource(R.string.settings_session_mode_guest)
    } else {
        stringResource(R.string.settings_session_mode_registered)
    }
    val callsignLabel = uiState.callsign.ifBlank {
        stringResource(R.string.settings_session_callsign_guest)
    }
    val emailLabel = uiState.email ?: stringResource(R.string.settings_account_email_missing)
    val rolesLabel = uiState.roles
        .sortedBy { it.name }
        .map { role ->
            when (role) {
                AccountRole.USER -> stringResource(R.string.settings_role_user)
                AccountRole.COMMERCIAL -> stringResource(R.string.settings_role_commercial)
                AccountRole.MODERATOR -> stringResource(R.string.settings_role_moderator)
                AccountRole.ADMIN -> stringResource(R.string.settings_role_admin)
            }
        }
        .ifEmpty { listOf(stringResource(R.string.settings_session_roles_empty)) }
        .joinToString(", ")
    val onLabel = stringResource(R.string.settings_value_on)
    val offLabel = stringResource(R.string.settings_value_off)

    val rightsRows = listOf(
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_chat_title),
            subtitle = stringResource(R.string.settings_account_right_chat_subtitle),
            trailing = if (uiState.access.canSendChatMessages) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_profile_title),
            subtitle = stringResource(R.string.settings_account_right_profile_subtitle),
            trailing = if (uiState.access.canEditProfile) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_marketplace_title),
            subtitle = stringResource(R.string.settings_account_right_marketplace_subtitle),
            trailing = if (uiState.access.canCreateMarketplaceListings) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_rideshare_title),
            subtitle = stringResource(R.string.settings_account_right_rideshare_subtitle),
            trailing = if (uiState.access.canCreateRideShareListings) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_events_title),
            subtitle = stringResource(R.string.settings_account_right_events_subtitle),
            trailing = if (uiState.access.canCreateGameEvents) onLabel else offLabel,
        ),
        ShellWireframeRow(
            title = stringResource(R.string.settings_account_right_shops_title),
            subtitle = stringResource(R.string.settings_account_right_shops_subtitle),
            trailing = if (uiState.access.canCreateShopListings) onLabel else offLabel,
        ),
    )
    val accessDeniedRow = when (accessDeniedReason) {
        SettingsAccountAccessDeniedReason.CommercialRoleRequired -> ShellWireframeRow(
            title = stringResource(R.string.settings_account_access_commercial_title),
            subtitle = stringResource(R.string.settings_account_access_commercial_subtitle),
            trailing = stringResource(R.string.settings_role_commercial),
        )
        SettingsAccountAccessDeniedReason.ModeratorRoleRequired -> ShellWireframeRow(
            title = stringResource(R.string.settings_account_access_moderator_title),
            subtitle = stringResource(R.string.settings_account_access_moderator_subtitle),
            trailing = stringResource(R.string.settings_role_moderator),
        )
        SettingsAccountAccessDeniedReason.AdminRoleRequired -> ShellWireframeRow(
            title = stringResource(R.string.settings_account_access_admin_title),
            subtitle = stringResource(R.string.settings_account_access_admin_subtitle),
            trailing = stringResource(R.string.settings_role_admin),
        )
        null -> null
    }
    val supportForRoleNeeded = !uiState.isGuest && accessDeniedRow != null

    WireframePage(
        title = stringResource(R.string.settings_account_title),
        subtitle = stringResource(R.string.settings_account_subtitle),
        primaryActionLabel = when {
            uiState.isGuest -> stringResource(R.string.settings_account_primary_register)
            supportForRoleNeeded -> stringResource(R.string.settings_account_primary_open_support)
            else -> stringResource(R.string.settings_account_primary_sign_out)
        },
        onPrimaryAction = {
            when {
                uiState.isGuest -> onOpenAuth()
                supportForRoleNeeded -> onOpenSupport()
                else -> onSignOut()
            }
        },
    ) {
        if (accessDeniedRow != null) {
            WireframeSection(
                title = stringResource(R.string.settings_account_access_required_title),
                subtitle = stringResource(R.string.settings_account_access_required_subtitle),
            ) {
                WireframeItemRow(
                    title = accessDeniedRow.title,
                    subtitle = accessDeniedRow.subtitle,
                    trailing = accessDeniedRow.trailing,
                )
            }
        }
        WireframeSection(
            title = stringResource(R.string.settings_account_session_title),
            subtitle = stringResource(R.string.settings_account_session_subtitle),
        ) {
            WireframeItemRow(
                title = stringResource(R.string.settings_session_mode_title),
                subtitle = stringResource(R.string.settings_session_mode_subtitle_dynamic),
                trailing = modeLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_session_callsign_title),
                subtitle = stringResource(R.string.settings_session_callsign_subtitle),
                trailing = callsignLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_account_email_title),
                subtitle = stringResource(R.string.settings_account_email_subtitle),
                trailing = emailLabel,
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_session_roles_title),
                subtitle = stringResource(R.string.settings_session_roles_subtitle),
                trailing = rolesLabel,
            )
        }

        WireframeSection(
            title = stringResource(R.string.settings_account_rights_title),
            subtitle = stringResource(R.string.settings_account_rights_subtitle),
        ) {
            rightsRows.forEach { row ->
                WireframeItemRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    trailing = row.trailing,
                )
            }
        }

        WireframeSection(
            title = stringResource(R.string.settings_account_recovery_title),
            subtitle = stringResource(R.string.settings_account_recovery_subtitle),
        ) {
            WireframeItemRow(
                title = stringResource(R.string.settings_account_recovery_email_title),
                subtitle = stringResource(R.string.settings_account_recovery_email_subtitle),
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_account_recovery_roles_title),
                subtitle = stringResource(R.string.settings_account_recovery_roles_subtitle),
            )
            WireframeItemRow(
                title = stringResource(R.string.settings_account_recovery_note_title),
                subtitle = stringResource(R.string.settings_account_recovery_note_subtitle),
            )
        }

        WireframeSection(
            title = stringResource(R.string.settings_account_actions_title),
            subtitle = stringResource(R.string.settings_account_actions_subtitle),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.isGuest) {
                    OutlinedButton(
                        onClick = onOpenAuth,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_account_action_open_auth))
                    }
                }
                if (supportForRoleNeeded) {
                    OutlinedButton(
                        onClick = onOpenSupport,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_account_action_open_support))
                    }
                }
                if (uiState.access.isAdmin) {
                    OutlinedButton(
                        onClick = onOpenAdminDashboard,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_account_action_open_admin))
                    }
                }
                if (!uiState.isGuest) {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_account_action_sign_out))
                    }
                }
            }
        }
    }
}

private fun AuthState.toSettingsAccountUiState(): SettingsAccountUiState = when (this) {
    is AuthState.SignedIn -> SettingsAccountUiState(
        isGuest = session.email.isNullOrBlank(),
        callsign = session.displayName,
        email = session.email,
        roles = session.accountRoles,
        access = session.toAccountAccess(),
    )

    AuthState.SignedOut,
    AuthState.Unknown,
    -> SettingsAccountUiState()
}

private fun String?.toSettingsAccountAccessDeniedReason(): SettingsAccountAccessDeniedReason? = when (this) {
    SettingsFeatureApi.AccountReasonCommercialRoleRequired ->
        SettingsAccountAccessDeniedReason.CommercialRoleRequired

    SettingsFeatureApi.AccountReasonModeratorRoleRequired ->
        SettingsAccountAccessDeniedReason.ModeratorRoleRequired

    SettingsFeatureApi.AccountReasonAdminRoleRequired ->
        SettingsAccountAccessDeniedReason.AdminRoleRequired

    else -> null
}

private const val AUTH_REASON_REGISTRATION_REQUIRED = "registration_required"

