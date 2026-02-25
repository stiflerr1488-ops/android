package com.airsoft.social.feature.teams.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.teams.api.TeamsFeatureApi

data class TeamsListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class TeamsUiState(
    val recruitingFilters: List<String> = listOf("All", "Assault", "Medic", "DMR", "Weekends"),
    val selectedRecruitingFilter: String = "All",
    val myTeamMetrics: List<Pair<String, String>> = listOf(
        "Members" to "14",
        "Activity" to "82%",
        "Games/mo" to "6",
    ),
    val myTeamCard: TeamsListRow = TeamsListRow(
        title = "[EW] EASY WINNER",
        subtitle = "Assault squad | Recruiting for 2 roles",
        trailing = "online",
    ),
    val roster: List<TeamsListRow> = listOf(
        TeamsListRow("Teiwaz_", "Commander | confirmed", "Lead"),
        TeamsListRow("Raven", "Assault | online", "A1"),
        TeamsListRow("MedicFox", "Medic | pending", "M"),
        TeamsListRow("Viper", "Marksman | confirmed", "DMR"),
    ),
    val recruitingFeed: List<TeamsListRow> = listOf(
        TeamsListRow("[North Wolves] Need Medic", "Wednesday training | SPB", "apply"),
        TeamsListRow("[Black Rain] Need Assault", "CQB format | Moscow", "apply"),
    ),
)

sealed interface TeamsAction {
    data class SelectRecruitingFilter(val filter: String) : TeamsAction
    data object OpenTeamDetailDemoClicked : TeamsAction
    data object OpenTeamCreateDemoClicked : TeamsAction
}

class TeamsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TeamsUiState())
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    fun onAction(action: TeamsAction) {
        when (action) {
            is TeamsAction.SelectRecruitingFilter -> {
                _uiState.value = _uiState.value.copy(selectedRecruitingFilter = action.filter)
            }
            TeamsAction.OpenTeamDetailDemoClicked -> Unit
            TeamsAction.OpenTeamCreateDemoClicked -> Unit
        }
    }
}

@Composable
fun TeamsPlaceholderScreen(
    onOpenTeamDetailDemo: () -> Unit = {},
    onOpenTeamCreateDemo: () -> Unit = {},
    teamsViewModel: TeamsViewModel = viewModel(),
) {
    val uiState by teamsViewModel.uiState.collectAsState()

    TeamsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                TeamsAction.OpenTeamDetailDemoClicked -> onOpenTeamDetailDemo()
                TeamsAction.OpenTeamCreateDemoClicked -> onOpenTeamCreateDemo()
                is TeamsAction.SelectRecruitingFilter -> teamsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun TeamsScreen(
    uiState: TeamsUiState,
    onAction: (TeamsAction) -> Unit,
) {
    WireframePage(
        title = TeamsFeatureApi.contract.title,
        subtitle = "Teams page skeleton: team profile, roster, recruiting, and join requests.",
    ) {
        WireframeSection(
            title = "My Team",
            subtitle = "Team card and quick metrics placeholder.",
        ) {
            WireframeMetricRow(
                items = uiState.myTeamMetrics,
            )
            WireframeItemRow(
                title = uiState.myTeamCard.title,
                subtitle = uiState.myTeamCard.subtitle,
                trailing = uiState.myTeamCard.trailing,
            )
        }
        WireframeSection(
            title = "Roster",
            subtitle = "Future roster, roles, and attendance statuses.",
        ) {
            uiState.roster.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Recruiting Feed",
            subtitle = "Public team recruiting posts and player responses.",
        ) {
            WireframeChipRow(
                uiState.recruitingFilters.map { filter ->
                    if (filter == uiState.selectedRecruitingFilter) "[$filter]" else filter
                },
            )
            uiState.recruitingFeed.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary pages for team profile and create/edit flows.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(TeamsAction.OpenTeamDetailDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Team Detail Page")
                }
                OutlinedButton(
                    onClick = { onAction(TeamsAction.OpenTeamCreateDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Team Create/Edit Page")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedRecruitingFilter == "All") "Medic" else "All"
                        onAction(TeamsAction.SelectRecruitingFilter(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Recruiting Filter")
                }
            }
        }
    }
}

@Composable
fun TeamDetailSkeletonScreen() {
    WireframePage(
        title = "Team Detail",
        subtitle = "Skeleton for team profile page with tabs and management actions.",
        primaryActionLabel = "Apply to Team",
    ) {
        WireframeSection(
            title = "Team Header",
            subtitle = "Name, emblem, region, role focus, privacy level.",
        ) {
            WireframeItemRow("[EW] EASY WINNER", "Moscow | Assault / mixed format | Public", "14 members")
            WireframeChipRow(listOf("Open field", "CQB", "Training", "Tournaments"))
        }
        WireframeSection(
            title = "Roster and Roles",
            subtitle = "Slot structure for leadership, medics, assault, recon.",
        ) {
            WireframeItemRow("Leadership", "Teiwaz_ | Raven")
            WireframeItemRow("Core assault", "6 members active on weekends")
            WireframeItemRow("Support roles", "1 medic, 1 comms, 1 driver")
        }
        WireframeSection(
            title = "Requirements",
            subtitle = "Gear, safety, communication, and attendance requirements.",
        ) {
            WireframeItemRow("Safety", "Chrono + eye protection mandatory")
            WireframeItemRow("Comms", "Team radio channel required")
            WireframeItemRow("Attendance", "At least 2 games per month")
        }
    }
}

@Composable
fun TeamCreateEditSkeletonScreen() {
    WireframePage(
        title = "Team Create / Edit",
        subtitle = "Skeleton for create team, branding, roles, and recruiting settings.",
        primaryActionLabel = "Save Draft",
    ) {
        WireframeSection(
            title = "Basic Info Form",
            subtitle = "Name, tag, region, description, visibility.",
        ) {
            WireframeItemRow("Team name", "Text field placeholder")
            WireframeItemRow("Tag", "Short tag field placeholder")
            WireframeItemRow("Region", "City / travel radius selector")
        }
        WireframeSection(
            title = "Roles and Slots",
            subtitle = "Desired roster composition and open positions.",
        ) {
            WireframeItemRow("Assault", "6 slots | 2 open")
            WireframeItemRow("Medic", "2 slots | 1 open")
            WireframeItemRow("DMR", "2 slots | 1 open")
        }
        WireframeSection(
            title = "Recruiting Rules",
            subtitle = "Auto-accept, questionnaire, required fields, trial status.",
        ) {
            WireframeChipRow(listOf("Questionnaire", "Manual review", "Trial period", "Age 18+"))
        }
    }
}
