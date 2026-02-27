package com.airsoft.social.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airsoft.social.feature.nav.AirsoftNavHost
import com.airsoft.social.core.tactical.NoopTacticalOverviewPort
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.feature.tactical.api.NoopTacticalLegacyBridgeLauncher
import com.airsoft.social.feature.tactical.api.TacticalLegacyBridgeLauncher

@Composable
fun AirsoftRootApp(
    viewModel: AirsoftShellViewModel = hiltViewModel(),
    tacticalOverviewPort: TacticalOverviewPort = NoopTacticalOverviewPort,
    tacticalLegacyBridgeLauncher: TacticalLegacyBridgeLauncher = NoopTacticalLegacyBridgeLauncher,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AirsoftNavHost(
        bootstrapRoute = uiState.bootstrapRoute,
        tacticalOverviewPort = tacticalOverviewPort,
        onCompleteOnboarding = viewModel::completeOnboarding,
        onSignOut = viewModel::signOut,
        onOpenLegacyTactical = {
            viewModel.onOpenLegacyTacticalRequested()
            tacticalLegacyBridgeLauncher.openLegacyTactical()
        },
    )
}
