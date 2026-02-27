package com.airsoft.social.feature.settings.impl

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.core.model.IntegrityVerdict
import com.airsoft.social.core.model.ThermalLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ACTION_OPEN_RULES = "open_rules"
private const val ACTION_OPEN_SUPPORT = "open_support"
private const val ACTION_OPEN_APP_PERMISSIONS = "open_app_permissions"
private const val ACTION_OPEN_APP_NOTIFICATIONS = "open_app_notifications"
private const val ACTION_OPEN_APP_INFO = "open_app_info"
private const val ACTION_OPEN_APP_BATTERY_USAGE = "open_app_battery_usage"

data class SettingsToolUiState(
    val toolId: String = "",
    val selectedTab: String = "",
    val tabs: List<String> = emptyList(),
    val metricRows: List<Pair<String, String>> = emptyList(),
    val mainRows: List<ShellWireframeRow> = emptyList(),
    val scenarioRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface SettingsToolAction {
    data object CycleTab : SettingsToolAction
}

data class SettingsToolSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryActionLabel: String,
    val tabs: List<String>,
    val metricRows: List<Pair<String, String>>,
    val mainSectionTitle: String,
    val mainSectionSubtitle: String,
    val mainRows: List<ShellWireframeRow>,
    val scenarioSectionTitle: String,
    val scenarioSectionSubtitle: String,
    val scenarioRows: List<ShellWireframeRow>,
    val cycleButtonLabel: String,
)

internal data class SettingsMetricRes(
    @param:StringRes val labelRes: Int,
    @param:StringRes val valueRes: Int,
)

internal data class SettingsRowRes(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val trailingRes: Int? = null,
)

internal data class SettingsActionButtonRes(
    val id: String,
    @param:StringRes val labelRes: Int,
)

internal data class SettingsToolSpecRes(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val primaryActionLabelRes: Int,
    val tabResIds: List<Int>,
    val metricRows: List<SettingsMetricRes>,
    @param:StringRes val mainSectionTitleRes: Int,
    @param:StringRes val mainSectionSubtitleRes: Int,
    val mainRows: List<SettingsRowRes>,
    @param:StringRes val scenarioSectionTitleRes: Int,
    @param:StringRes val scenarioSectionSubtitleRes: Int,
    val scenarioRows: List<SettingsRowRes>,
    @param:StringRes val cycleButtonLabelRes: Int,
    val extraActions: List<SettingsActionButtonRes> = emptyList(),
)

class SettingsToolViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsToolUiState())
    val uiState: StateFlow<SettingsToolUiState> = _uiState.asStateFlow()

    fun load(spec: SettingsToolSpec) {
        if (_uiState.value.toolId == spec.id) return
        _uiState.value = SettingsToolUiState(
            toolId = spec.id,
            selectedTab = spec.tabs.firstOrNull().orEmpty(),
            tabs = spec.tabs,
            metricRows = spec.metricRows,
            mainRows = spec.mainRows,
            scenarioRows = spec.scenarioRows,
        )
    }

    fun onAction(action: SettingsToolAction) {
        when (action) {
            SettingsToolAction.CycleTab -> {
                val current = _uiState.value
                if (current.tabs.isEmpty()) return
                val currentIndex = current.tabs.indexOf(current.selectedTab).coerceAtLeast(0)
                val nextTab = current.tabs[(currentIndex + 1) % current.tabs.size]
                _uiState.value = current.copy(selectedTab = nextTab)
            }
        }
    }
}

@Composable
private fun SettingsToolShellRoute(
    specRes: SettingsToolSpecRes,
    onPrimaryAction: (() -> Unit)? = null,
    onSecondaryAction: (String) -> Unit = {},
    supplementalSectionTitle: String? = null,
    supplementalSectionSubtitle: String? = null,
    supplementalRows: List<ShellWireframeRow> = emptyList(),
    settingsToolViewModel: SettingsToolViewModel = viewModel(),
) {
    val spec = specRes.resolve()
    LaunchedEffect(spec.id) { settingsToolViewModel.load(spec) }
    val uiState by settingsToolViewModel.uiState.collectAsState()

    WireframePage(
        title = spec.title,
        subtitle = spec.subtitle,
        primaryActionLabel = spec.primaryActionLabel,
        onPrimaryAction = { onPrimaryAction?.invoke() ?: Unit },
    ) {
        WireframeSection(
            title = stringResource(R.string.settings_tool_tabs_section_title),
            subtitle = stringResource(R.string.settings_tool_tabs_section_subtitle),
        ) {
            WireframeMetricRow(
                items = buildList {
                    add(stringResource(R.string.settings_tool_metric_current_tab) to uiState.selectedTab)
                    addAll(uiState.metricRows)
                },
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(
            title = spec.mainSectionTitle,
            subtitle = spec.mainSectionSubtitle,
        ) {
            uiState.mainRows.forEach { row ->
                WireframeItemRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    trailing = row.trailing,
                )
            }
        }
        WireframeSection(
            title = spec.scenarioSectionTitle,
            subtitle = spec.scenarioSectionSubtitle,
        ) {
            uiState.scenarioRows.forEach { row ->
                WireframeItemRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    trailing = row.trailing,
                )
            }
        }
        if (supplementalRows.isNotEmpty()) {
            WireframeSection(
                title = supplementalSectionTitle ?: stringResource(R.string.settings_live_status_title),
                subtitle = supplementalSectionSubtitle
                    ?: stringResource(R.string.settings_live_status_subtitle),
            ) {
                supplementalRows.forEach { row ->
                    WireframeItemRow(
                        title = row.title,
                        subtitle = row.subtitle,
                        trailing = row.trailing,
                    )
                }
            }
        }
        WireframeSection(
            title = stringResource(R.string.settings_tool_actions_section_title),
            subtitle = stringResource(R.string.settings_tool_actions_section_subtitle),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { settingsToolViewModel.onAction(SettingsToolAction.CycleTab) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(spec.cycleButtonLabel)
                }
                specRes.extraActions.forEach { action ->
                    OutlinedButton(
                        onClick = { onSecondaryAction(action.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(action.labelRes))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsPrivacyShellRoute(
    onOpenRules: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
) {
    SettingsToolShellRoute(
        specRes = settingsPrivacyToolSpecRes(),
        onSecondaryAction = { actionId ->
            when (actionId) {
                ACTION_OPEN_RULES -> onOpenRules()
                ACTION_OPEN_SUPPORT -> onOpenSupport()
            }
        },
    )
}

@Composable
fun SettingsPermissionsShellRoute() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val statusViewModel: SettingsSystemStatusViewModel = hiltViewModel()
    val permissionStatus by statusViewModel.permissionStatus.collectAsState()
    val enabledText = stringResource(R.string.settings_live_status_enabled)
    val disabledText = stringResource(R.string.settings_live_status_disabled)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        statusViewModel.refreshPermissions()
    }
    val requestPermissions: () -> Unit = {
        val missingPermissions = context.collectMissingRuntimePermissionsForSettings()
        if (missingPermissions.isEmpty()) {
            Toast.makeText(
                context,
                context.getString(R.string.settings_permissions_runtime_all_granted),
                Toast.LENGTH_SHORT,
            ).show()
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    LaunchedEffect(Unit) {
        statusViewModel.refreshPermissions()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                statusViewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    SettingsToolShellRoute(
        specRes = settingsPermissionsToolSpecRes(),
        onPrimaryAction = requestPermissions,
        onSecondaryAction = { actionId ->
            when (actionId) {
                ACTION_OPEN_APP_PERMISSIONS -> context.openAppPermissionSettingsBestEffort()
                ACTION_OPEN_APP_NOTIFICATIONS -> context.openAppNotificationSettingsBestEffort()
                ACTION_OPEN_APP_INFO -> context.openAppDetailsSettingsBestEffort()
            }
        },
        supplementalRows = listOf(
            ShellWireframeRow(
                title = stringResource(R.string.settings_permissions_live_runtime_title),
                subtitle = stringResource(
                    R.string.settings_permissions_live_runtime_subtitle,
                    permissionStatus.grantedRuntimePermissions,
                    permissionStatus.requiredRuntimePermissions,
                ),
                trailing = "${permissionStatus.grantedRuntimePermissions}/${permissionStatus.requiredRuntimePermissions}",
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_permissions_live_notifications_title),
                subtitle = stringResource(R.string.settings_permissions_live_notifications_subtitle),
                trailing = if (permissionStatus.notificationsEnabled) enabledText else disabledText,
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_permissions_live_background_location_title),
                subtitle = stringResource(R.string.settings_permissions_live_background_location_subtitle),
                trailing = if (permissionStatus.backgroundLocationEnabled) enabledText else disabledText,
            ),
        ),
    )
}

@Composable
fun SettingsSecurityShellRoute() {
    val statusViewModel: SettingsSystemStatusViewModel = hiltViewModel()
    val securityStatus by statusViewModel.securityStatus.collectAsState()
    LaunchedEffect(Unit) {
        statusViewModel.refreshSecurity()
    }
    val integrityText = when (securityStatus.integrityVerdict) {
        IntegrityVerdict.Ok -> stringResource(R.string.settings_live_integrity_ok)
        IntegrityVerdict.Warning -> stringResource(R.string.settings_live_integrity_warning)
        IntegrityVerdict.Unknown -> stringResource(R.string.settings_live_integrity_unknown)
    }
    val pinningText = if (securityStatus.certificatePinningEnabled) {
        stringResource(R.string.settings_live_status_enabled)
    } else {
        stringResource(R.string.settings_live_status_planned)
    }
    SettingsToolShellRoute(
        specRes = settingsSecurityToolSpecRes(),
        supplementalRows = listOf(
            ShellWireframeRow(
                title = stringResource(R.string.settings_security_live_sessions_title),
                subtitle = stringResource(
                    R.string.settings_security_live_sessions_subtitle,
                    securityStatus.activeSessions,
                ),
                trailing = securityStatus.activeSessions.toString(),
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_security_live_signals_title),
                subtitle = stringResource(
                    R.string.settings_security_live_signals_subtitle,
                    securityStatus.suspiciousLoginSignals,
                ),
                trailing = securityStatus.suspiciousLoginSignals.toString(),
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_security_live_integrity_title),
                subtitle = stringResource(R.string.settings_security_live_integrity_subtitle),
                trailing = integrityText,
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_security_live_pinning_title),
                subtitle = stringResource(R.string.settings_security_live_pinning_subtitle),
                trailing = pinningText,
            ),
        ),
    )
}

@Composable
fun SettingsBatteryShellRoute() {
    val context = LocalContext.current
    val statusViewModel: SettingsSystemStatusViewModel = hiltViewModel()
    val deviceDiagnostics by statusViewModel.deviceDiagnostics.collectAsState()
    LaunchedEffect(Unit) {
        statusViewModel.refreshDiagnostics()
    }
    val optimizationText = if (deviceDiagnostics.batteryOptimizationDisabled) {
        stringResource(R.string.settings_live_status_disabled)
    } else {
        stringResource(R.string.settings_live_status_enabled)
    }
    val standbyText = if (deviceDiagnostics.appStandbyRestricted) {
        stringResource(R.string.settings_live_status_restricted)
    } else {
        stringResource(R.string.settings_live_status_ok)
    }
    val thermalText = when (deviceDiagnostics.thermalLevel) {
        ThermalLevel.Normal -> stringResource(R.string.settings_live_thermal_normal)
        ThermalLevel.Warm -> stringResource(R.string.settings_live_thermal_warm)
        ThermalLevel.Hot -> stringResource(R.string.settings_live_thermal_hot)
    }
    SettingsToolShellRoute(
        specRes = settingsBatteryToolSpecRes(),
        onPrimaryAction = { context.openBatteryOptimizationSettingsBestEffort() },
        onSecondaryAction = { actionId ->
            when (actionId) {
                ACTION_OPEN_APP_BATTERY_USAGE -> context.openAppBatteryUsageSettingsBestEffort()
                ACTION_OPEN_APP_INFO -> context.openAppDetailsSettingsBestEffort()
            }
        },
        supplementalRows = listOf(
            ShellWireframeRow(
                title = stringResource(R.string.settings_battery_live_optimization_title),
                subtitle = stringResource(R.string.settings_battery_live_optimization_subtitle),
                trailing = optimizationText,
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_battery_live_standby_title),
                subtitle = stringResource(R.string.settings_battery_live_standby_subtitle),
                trailing = standbyText,
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_battery_live_thermal_title),
                subtitle = stringResource(R.string.settings_battery_live_thermal_subtitle),
                trailing = thermalText,
            ),
            ShellWireframeRow(
                title = stringResource(R.string.settings_battery_live_drain_title),
                subtitle = stringResource(R.string.settings_battery_live_drain_subtitle),
                trailing = "${deviceDiagnostics.estimatedBatteryDrainPercentPerHour}%",
            ),
        ),
    )
}

@Composable
private fun SettingsToolSpecRes.resolve(): SettingsToolSpec = SettingsToolSpec(
    id = id,
    title = stringResource(titleRes),
    subtitle = stringResource(subtitleRes),
    primaryActionLabel = stringResource(primaryActionLabelRes),
    tabs = tabResIds.map { stringResource(it) },
    metricRows = metricRows.map { stringResource(it.labelRes) to stringResource(it.valueRes) },
    mainSectionTitle = stringResource(mainSectionTitleRes),
    mainSectionSubtitle = stringResource(mainSectionSubtitleRes),
    mainRows = mainRows.map { row ->
        ShellWireframeRow(
            title = stringResource(row.titleRes),
            subtitle = stringResource(row.subtitleRes),
            trailing = row.trailingRes?.let { stringResource(it) },
        )
    },
    scenarioSectionTitle = stringResource(scenarioSectionTitleRes),
    scenarioSectionSubtitle = stringResource(scenarioSectionSubtitleRes),
    scenarioRows = scenarioRows.map { row ->
        ShellWireframeRow(
            title = stringResource(row.titleRes),
            subtitle = stringResource(row.subtitleRes),
            trailing = row.trailingRes?.let { stringResource(it) },
        )
    },
    cycleButtonLabel = stringResource(cycleButtonLabelRes),
)

internal fun settingsPrivacyToolSpecRes(): SettingsToolSpecRes = SettingsToolSpecRes(
    id = "settings-privacy",
    titleRes = R.string.settings_privacy_title,
    subtitleRes = R.string.settings_privacy_subtitle,
    primaryActionLabelRes = R.string.settings_privacy_primary_action,
    tabResIds = listOf(
        R.string.settings_privacy_tab_profile,
        R.string.settings_privacy_tab_contacts,
        R.string.settings_privacy_tab_activity,
        R.string.settings_privacy_tab_rights,
    ),
    metricRows = listOf(
        SettingsMetricRes(R.string.settings_metric_params, R.string.settings_privacy_metric_params_value),
        SettingsMetricRes(R.string.settings_metric_restrictions, R.string.settings_privacy_metric_restrictions_value),
    ),
    mainSectionTitleRes = R.string.settings_privacy_main_title,
    mainSectionSubtitleRes = R.string.settings_privacy_main_subtitle,
    mainRows = listOf(
        SettingsRowRes(
            R.string.settings_privacy_row_profile_visibility_title,
            R.string.settings_privacy_row_profile_visibility_subtitle,
            R.string.settings_value_team_only,
        ),
        SettingsRowRes(
            R.string.settings_privacy_row_contacts_title,
            R.string.settings_privacy_row_contacts_subtitle,
            R.string.settings_value_hidden,
        ),
        SettingsRowRes(
            R.string.settings_privacy_row_region_title,
            R.string.settings_privacy_row_region_subtitle,
            R.string.settings_value_region_only,
        ),
        SettingsRowRes(
            R.string.settings_privacy_row_dm_invites_title,
            R.string.settings_privacy_row_dm_invites_subtitle,
            R.string.settings_value_allowed,
        ),
    ),
    scenarioSectionTitleRes = R.string.settings_privacy_rights_title,
    scenarioSectionSubtitleRes = R.string.settings_privacy_rights_subtitle,
    scenarioRows = listOf(
        SettingsRowRes(
            R.string.settings_privacy_rights_export_title,
            R.string.settings_privacy_rights_export_subtitle,
            R.string.settings_tag_gdpr,
        ),
        SettingsRowRes(
            R.string.settings_privacy_rights_delete_title,
            R.string.settings_privacy_rights_delete_subtitle,
            R.string.settings_tag_warning,
        ),
        SettingsRowRes(
            R.string.settings_privacy_rights_fix_title,
            R.string.settings_privacy_rights_fix_subtitle,
            R.string.settings_tag_profile,
        ),
        SettingsRowRes(
            R.string.settings_privacy_rights_retention_title,
            R.string.settings_privacy_rights_retention_subtitle,
            R.string.settings_tag_policy,
        ),
    ),
    cycleButtonLabelRes = R.string.settings_privacy_cycle_tab,
    extraActions = listOf(
        SettingsActionButtonRes(ACTION_OPEN_RULES, R.string.settings_privacy_action_open_rules),
        SettingsActionButtonRes(ACTION_OPEN_SUPPORT, R.string.settings_privacy_action_open_support),
    ),
)

internal fun settingsPermissionsToolSpecRes(): SettingsToolSpecRes = SettingsToolSpecRes(
    id = "settings-permissions",
    titleRes = R.string.settings_permissions_title,
    subtitleRes = R.string.settings_permissions_subtitle,
    primaryActionLabelRes = R.string.settings_permissions_primary_action_request_runtime,
    tabResIds = listOf(
        R.string.settings_permissions_tab_permissions,
        R.string.settings_permissions_tab_statuses,
        R.string.settings_permissions_tab_guides,
        R.string.settings_permissions_tab_system,
    ),
    metricRows = listOf(
        SettingsMetricRes(R.string.settings_metric_critical, R.string.settings_permissions_metric_critical_value),
        SettingsMetricRes(R.string.settings_metric_granted, R.string.settings_permissions_metric_granted_value),
    ),
    mainSectionTitleRes = R.string.settings_permissions_main_title,
    mainSectionSubtitleRes = R.string.settings_permissions_main_subtitle,
    mainRows = listOf(
        SettingsRowRes(
            R.string.settings_permissions_row_location_title,
            R.string.settings_permissions_row_location_subtitle,
            R.string.settings_value_partial,
        ),
        SettingsRowRes(
            R.string.settings_permissions_row_notifications_title,
            R.string.settings_permissions_row_notifications_subtitle,
            R.string.settings_value_enabled,
        ),
        SettingsRowRes(
            R.string.settings_permissions_row_camera_title,
            R.string.settings_permissions_row_camera_subtitle,
            R.string.settings_value_enabled,
        ),
        SettingsRowRes(
            R.string.settings_permissions_row_media_title,
            R.string.settings_permissions_row_media_subtitle,
            R.string.settings_value_enabled,
        ),
        SettingsRowRes(
            R.string.settings_permissions_row_bluetooth_title,
            R.string.settings_permissions_row_bluetooth_subtitle,
            R.string.settings_value_request,
        ),
        SettingsRowRes(
            R.string.settings_permissions_row_network_title,
            R.string.settings_permissions_row_network_subtitle,
            R.string.settings_tag_info,
        ),
    ),
    scenarioSectionTitleRes = R.string.settings_permissions_fallbacks_title,
    scenarioSectionSubtitleRes = R.string.settings_permissions_fallbacks_subtitle,
    scenarioRows = listOf(
        SettingsRowRes(
            R.string.settings_permissions_fallback_no_geo_title,
            R.string.settings_permissions_fallback_no_geo_subtitle,
            R.string.settings_tag_fallback,
        ),
        SettingsRowRes(
            R.string.settings_permissions_fallback_no_push_title,
            R.string.settings_permissions_fallback_no_push_subtitle,
            R.string.settings_tag_fallback,
        ),
        SettingsRowRes(
            R.string.settings_permissions_fallback_no_camera_title,
            R.string.settings_permissions_fallback_no_camera_subtitle,
            R.string.settings_tag_fallback,
        ),
    ),
    cycleButtonLabelRes = R.string.settings_permissions_cycle_tab,
    extraActions = listOf(
        SettingsActionButtonRes(
            ACTION_OPEN_APP_PERMISSIONS,
            R.string.settings_permissions_action_open_permissions,
        ),
        SettingsActionButtonRes(ACTION_OPEN_APP_NOTIFICATIONS, R.string.settings_permissions_action_notifications),
        SettingsActionButtonRes(ACTION_OPEN_APP_INFO, R.string.settings_permissions_action_app_info),
    ),
)

internal fun settingsSecurityToolSpecRes(): SettingsToolSpecRes = SettingsToolSpecRes(
    id = "settings-security",
    titleRes = R.string.settings_security_title,
    subtitleRes = R.string.settings_security_subtitle,
    primaryActionLabelRes = R.string.settings_security_primary_action,
    tabResIds = listOf(
        R.string.settings_security_tab_sessions,
        R.string.settings_security_tab_device,
        R.string.settings_security_tab_network,
        R.string.settings_security_tab_protection,
    ),
    metricRows = listOf(
        SettingsMetricRes(R.string.settings_metric_sessions, R.string.settings_security_metric_sessions_value),
        SettingsMetricRes(R.string.settings_metric_risks, R.string.settings_security_metric_risks_value),
    ),
    mainSectionTitleRes = R.string.settings_security_main_title,
    mainSectionSubtitleRes = R.string.settings_security_main_subtitle,
    mainRows = listOf(
        SettingsRowRes(
            R.string.settings_security_row_current_device_title,
            R.string.settings_security_row_current_device_subtitle,
            R.string.settings_tag_active,
        ),
        SettingsRowRes(
            R.string.settings_security_row_login_history_title,
            R.string.settings_security_row_login_history_subtitle,
            R.string.settings_tag_log,
        ),
        SettingsRowRes(
            R.string.settings_security_row_tokens_title,
            R.string.settings_security_row_tokens_subtitle,
            R.string.settings_tag_token,
        ),
        SettingsRowRes(
            R.string.settings_security_row_https_title,
            R.string.settings_security_row_https_subtitle,
            R.string.settings_tag_enabled,
        ),
        SettingsRowRes(
            R.string.settings_security_row_pinning_title,
            R.string.settings_security_row_pinning_subtitle,
            R.string.settings_tag_planned,
        ),
    ),
    scenarioSectionTitleRes = R.string.settings_security_checks_title,
    scenarioSectionSubtitleRes = R.string.settings_security_checks_subtitle,
    scenarioRows = listOf(
        SettingsRowRes(
            R.string.settings_security_check_integrity_title,
            R.string.settings_security_check_integrity_subtitle,
            R.string.settings_tag_integrity,
        ),
        SettingsRowRes(
            R.string.settings_security_check_suspicious_login_title,
            R.string.settings_security_check_suspicious_login_subtitle,
            R.string.settings_tag_alert,
        ),
        SettingsRowRes(
            R.string.settings_security_check_refresh_session_title,
            R.string.settings_security_check_refresh_session_subtitle,
            R.string.settings_tag_token,
        ),
        SettingsRowRes(
            R.string.settings_security_check_play_integrity_title,
            R.string.settings_security_check_play_integrity_subtitle,
            R.string.settings_tag_planned,
        ),
    ),
    cycleButtonLabelRes = R.string.settings_security_cycle_tab,
)

internal fun settingsBatteryToolSpecRes(): SettingsToolSpecRes = SettingsToolSpecRes(
    id = "settings-battery",
    titleRes = R.string.settings_battery_title,
    subtitleRes = R.string.settings_battery_subtitle,
    primaryActionLabelRes = R.string.settings_battery_primary_action,
    tabResIds = listOf(
        R.string.settings_battery_tab_battery,
        R.string.settings_battery_tab_push_quiet_hours,
        R.string.settings_battery_tab_performance,
        R.string.settings_battery_tab_diagnostics,
    ),
    metricRows = listOf(
        SettingsMetricRes(R.string.settings_metric_profiles, R.string.settings_battery_metric_profiles_value),
        SettingsMetricRes(R.string.settings_metric_signals, R.string.settings_battery_metric_signals_value),
    ),
    mainSectionTitleRes = R.string.settings_battery_main_title,
    mainSectionSubtitleRes = R.string.settings_battery_main_subtitle,
    mainRows = listOf(
        SettingsRowRes(
            R.string.settings_battery_row_default_title,
            R.string.settings_battery_row_default_subtitle,
            R.string.settings_tag_default,
        ),
        SettingsRowRes(
            R.string.settings_battery_row_radar_title,
            R.string.settings_battery_row_radar_subtitle,
            R.string.settings_tag_combat,
        ),
        SettingsRowRes(
            R.string.settings_battery_row_eco_title,
            R.string.settings_battery_row_eco_subtitle,
            R.string.settings_tag_eco,
        ),
        SettingsRowRes(
            R.string.settings_battery_row_quiet_hours_title,
            R.string.settings_battery_row_quiet_hours_subtitle,
            R.string.settings_tag_preview,
        ),
    ),
    scenarioSectionTitleRes = R.string.settings_battery_diag_title,
    scenarioSectionSubtitleRes = R.string.settings_battery_diag_subtitle,
    scenarioRows = listOf(
        SettingsRowRes(
            R.string.settings_battery_diag_optimization_title,
            R.string.settings_battery_diag_optimization_subtitle,
            R.string.settings_tag_android,
        ),
        SettingsRowRes(
            R.string.settings_battery_diag_background_limits_title,
            R.string.settings_battery_diag_background_limits_subtitle,
            R.string.settings_tag_android,
        ),
        SettingsRowRes(
            R.string.settings_battery_diag_overheat_title,
            R.string.settings_battery_diag_overheat_subtitle,
            R.string.settings_tag_diag,
        ),
        SettingsRowRes(
            R.string.settings_battery_diag_metrics_title,
            R.string.settings_battery_diag_metrics_subtitle,
            R.string.settings_tag_metrics,
        ),
    ),
    cycleButtonLabelRes = R.string.settings_battery_cycle_tab,
    extraActions = listOf(
        SettingsActionButtonRes(ACTION_OPEN_APP_BATTERY_USAGE, R.string.settings_battery_action_app_battery_usage),
        SettingsActionButtonRes(ACTION_OPEN_APP_INFO, R.string.settings_battery_action_app_info),
    ),
)

private fun Context.openAppPermissionSettingsBestEffort(): Boolean {
    val intents = listOf(
        Intent("android.settings.APP_PERMISSION_SETTINGS")
            .putExtra("android.provider.extra.APP_PACKAGE", packageName),
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPackageUri()),
    )
    return startBestEffort(intents)
}

private fun Context.openAppNotificationSettingsBestEffort(): Boolean {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return startBestEffort(
        listOf(
            intent,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPackageUri()),
        ),
    )
}

private fun Context.openBatteryOptimizationSettingsBestEffort(): Boolean {
    return startBestEffort(
        listOf(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPackageUri()),
        ),
    )
}

private fun Context.openAppBatteryUsageSettingsBestEffort(): Boolean {
    val appBatteryIntent = Intent("android.settings.APP_BATTERY_SETTINGS").apply {
        data = appPackageUri()
        putExtra("android.provider.extra.APP_PACKAGE", packageName)
    }
    return startBestEffort(
        listOf(
            appBatteryIntent,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPackageUri()),
        ),
    )
}

private fun Context.openAppDetailsSettingsBestEffort(): Boolean {
    return startBestEffort(
        listOf(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPackageUri())),
    )
}

internal fun requiredRuntimePermissionsForSettings(sdkInt: Int): List<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    add(Manifest.permission.CAMERA)
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
        add(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        @Suppress("DEPRECATION")
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (sdkInt >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_CONNECT)
    }
}.distinct()

private fun Context.collectMissingRuntimePermissionsForSettings(): List<String> {
    val requiredPermissions = requiredRuntimePermissionsForSettings(Build.VERSION.SDK_INT)
    val declaredPermissions = declaredManifestPermissionsOrEmpty()
    return requiredPermissions.filter { permission ->
        (declaredPermissions.isEmpty() || permission in declaredPermissions) &&
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }
}

private fun Context.declaredManifestPermissionsOrEmpty(): Set<String> = runCatching {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    }
    packageInfo.requestedPermissions?.toSet().orEmpty()
}.getOrDefault(emptySet())

private fun Context.startBestEffort(intents: List<Intent>): Boolean {
    for (intent in intents) {
        try {
            val finalIntent = intent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(finalIntent)
            return true
        } catch (_: ActivityNotFoundException) {
            // Try next fallback intent.
        } catch (_: SecurityException) {
            // Try next fallback intent.
        }
    }
    Toast.makeText(
        this,
        getString(R.string.settings_system_action_unavailable),
        Toast.LENGTH_SHORT,
    ).show()
    return false
}

private fun Context.appPackageUri(): Uri = Uri.fromParts("package", packageName, null)
