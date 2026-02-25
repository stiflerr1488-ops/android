package com.example.teamcompass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.teamcompass.R
import com.example.teamcompass.core.TrackingMode
import com.example.teamcompass.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UiState,
    onBack: () -> Unit,
    onDefaultMode: (TrackingMode) -> Unit,
    onGamePolicy: (intervalSec: Int, distanceM: Int) -> Unit,
    onSilentPolicy: (intervalSec: Int, distanceM: Int) -> Unit,
    onControlLayoutEdit: (Boolean) -> Unit,
    onResetControlPositions: () -> Unit,
    onApplyRightHandLayout: () -> Unit = {},
    onApplyLeftHandLayout: () -> Unit = {},
    onAutoBrightnessEnabled: (Boolean) -> Unit = {},
    onScreenBrightness: (Float) -> Unit = {},
    onThemeMode: (com.example.teamcompass.ui.theme.ThemeMode) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.label_settings), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                },
                actions = {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = stringResource(R.string.settings_parameters_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.padding(Spacing.xs))
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SettingsDefaultModeCard(
                state = state,
                onDefaultMode = onDefaultMode,
            )

            PolicyCard(
                title = stringResource(R.string.settings_profile_game_title),
                subtitle = stringResource(R.string.settings_profile_game_subtitle),
                intervalSec = state.gameIntervalSec,
                distanceM = state.gameDistanceM,
                intervalRange = 3..20,
                distanceRange = 5..50,
                onChange = onGamePolicy,
            )

            PolicyCard(
                title = stringResource(R.string.settings_profile_silent_title),
                subtitle = stringResource(R.string.settings_profile_silent_subtitle),
                intervalSec = state.silentIntervalSec,
                distanceM = state.silentDistanceM,
                intervalRange = 10..60,
                distanceRange = 10..150,
                onChange = onSilentPolicy,
            )

            SettingsControlsLayoutCard(
                state = state,
                onControlLayoutEdit = onControlLayoutEdit,
                onResetControlPositions = onResetControlPositions,
                onApplyRightHandLayout = onApplyRightHandLayout,
                onApplyLeftHandLayout = onApplyLeftHandLayout,
            )

            SettingsScreenAndThemeCard(
                state = state,
                onAutoBrightnessEnabled = onAutoBrightnessEnabled,
                onScreenBrightness = onScreenBrightness,
                onThemeMode = onThemeMode,
            )

            SettingsDiagnosticsCard(state)

            Text(
                stringResource(R.string.settings_hint_policy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
