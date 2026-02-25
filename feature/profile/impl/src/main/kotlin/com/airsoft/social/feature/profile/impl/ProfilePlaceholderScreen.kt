package com.airsoft.social.feature.profile.impl

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
import com.airsoft.social.feature.profile.api.ProfileFeatureApi

data class ProfileListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class ProfileUiState(
    val profileHeader: ProfileListRow = ProfileListRow(
        title = "Teiwaz_",
        subtitle = "[EW] EASY WINNER | Moscow | Commander",
        trailing = "online",
    ),
    val roleTags: List<String> = listOf("Assault", "CQB", "Organizer", "Video"),
    val selectedRoleTag: String = "Assault",
    val summaryMetricsTop: List<Pair<String, String>> = listOf(
        "Games" to "184",
        "Wins" to "68%",
        "Rating" to "7 221",
    ),
    val summaryMetricsBottom: List<Pair<String, String>> = listOf(
        "K/D" to "2.7",
        "Damage" to "1.9",
        "XP" to "51 878",
    ),
    val activityRows: List<ProfileListRow> = listOf(
        ProfileListRow("[EW] EASY WINNER", "Current team | members: 14 | role: commander", "main"),
        ProfileListRow("Night Raid North", "Joined event | Mar 1", "event"),
    ),
    val gearRows: List<ProfileListRow> = listOf(
        ProfileListRow("Primary: M4A1", "Tuning: hop-up, M110 spring", "Ready"),
        ProfileListRow("Sidearm: Glock 17", "CO2 | 3 magazines", "Ready"),
        ProfileListRow("Radio", "Baofeng UV-5R | team channel", "Set"),
    ),
)

sealed interface ProfileAction {
    data class SelectRoleTag(val tag: String) : ProfileAction
    data object OpenEditProfileDemoClicked : ProfileAction
    data object OpenInventoryDemoClicked : ProfileAction
    data object SignOutClicked : ProfileAction
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.SelectRoleTag -> _uiState.value = _uiState.value.copy(selectedRoleTag = action.tag)
            ProfileAction.OpenEditProfileDemoClicked -> Unit
            ProfileAction.OpenInventoryDemoClicked -> Unit
            ProfileAction.SignOutClicked -> Unit
        }
    }
}

@Composable
fun ProfilePlaceholderScreen(
    onOpenEditProfileDemo: () -> Unit = {},
    onOpenInventoryDemo: () -> Unit = {},
    onPrimaryAction: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val uiState by profileViewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileAction.OpenEditProfileDemoClicked -> onOpenEditProfileDemo()
                ProfileAction.OpenInventoryDemoClicked -> onOpenInventoryDemo()
                ProfileAction.SignOutClicked -> onPrimaryAction()
                is ProfileAction.SelectRoleTag -> profileViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
) {
    WireframePage(
        title = ProfileFeatureApi.contract.title,
        subtitle = "Player profile skeleton: summary, stats, teams, gear, and achievements.",
        primaryActionLabel = "Sign out",
        onPrimaryAction = { onAction(ProfileAction.SignOutClicked) },
    ) {
        WireframeSection(
            title = "Player Profile",
            subtitle = "Main profile card (avatar, callsign, team, region).",
        ) {
            WireframeItemRow(
                title = uiState.profileHeader.title,
                subtitle = uiState.profileHeader.subtitle,
                trailing = uiState.profileHeader.trailing,
            )
            WireframeChipRow(
                labels = uiState.roleTags.map { tag ->
                    if (tag == uiState.selectedRoleTag) "[$tag]" else tag
                },
            )
        }
        WireframeSection(
            title = "Summary / Stats",
            subtitle = "Reference-like summary page structure in wireframe form.",
        ) {
            WireframeMetricRow(items = uiState.summaryMetricsTop)
            WireframeMetricRow(items = uiState.summaryMetricsBottom)
        }
        WireframeSection(
            title = "Teams and Activity",
            subtitle = "Team history, roles, and recent events.",
        ) {
            uiState.activityRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Gear / Equipment",
            subtitle = "Inventory profile tied to team requirements and marketplace.",
        ) {
            uiState.gearRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary pages for profile editing and full inventory view.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ProfileAction.OpenEditProfileDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Edit Profile Page")
                }
                OutlinedButton(
                    onClick = { onAction(ProfileAction.OpenInventoryDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Inventory Page")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedRoleTag == "Assault") "CQB" else "Assault"
                        onAction(ProfileAction.SelectRoleTag(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Role Tag")
                }
            }
        }
    }
}

@Composable
fun EditProfileSkeletonScreen() {
    WireframePage(
        title = "Edit Profile",
        subtitle = "Skeleton for profile editing: identity, tags, region, privacy, social links.",
        primaryActionLabel = "Save Changes",
    ) {
        WireframeSection(
            title = "Identity Fields",
            subtitle = "Callsign, bio, avatar/banner, region, experience level.",
        ) {
            WireframeItemRow("Callsign", "Text field placeholder")
            WireframeItemRow("Bio", "Short player description / team role")
            WireframeItemRow("Region", "City + travel radius selector")
            WireframeItemRow("Avatar/Banner", "Image upload placeholders")
        }
        WireframeSection(
            title = "Gameplay Tags",
            subtitle = "Roles, formats, availability, special skills.",
        ) {
            WireframeChipRow(listOf("Assault", "Medic", "CQB", "Open field", "Night games", "Driver"))
        }
        WireframeSection(
            title = "Privacy and Visibility",
            subtitle = "Who can contact you, see stats, and invite to teams.",
        ) {
            WireframeItemRow("Messages", "Everyone / Contacts / Teams only")
            WireframeItemRow("Profile visibility", "Public / Private / Team-only")
            WireframeItemRow("Team invites", "Enabled | filters by role/region")
        }
    }
}

@Composable
fun ProfileInventorySkeletonScreen() {
    WireframePage(
        title = "Inventory",
        subtitle = "Skeleton for full equipment profile tied to team requirements and marketplace.",
        primaryActionLabel = "Add Item",
    ) {
        WireframeSection(
            title = "Primary Loadout",
            subtitle = "Primary replica, optics, magazines, batteries, configuration.",
        ) {
            WireframeItemRow("M4A1", "M110 spring | red dot | 6 mags", "Primary")
            WireframeItemRow("Glock 17", "CO2 | 3 mags | holster", "Secondary")
            WireframeItemRow("Radio kit", "Baofeng UV-5R | headset | PTT", "Comms")
        }
        WireframeSection(
            title = "Protective Gear",
            subtitle = "Eye pro, face protection, helmet, carrier, uniform.",
        ) {
            WireframeItemRow("Eye/Face", "Full seal + mesh lower")
            WireframeItemRow("Carrier", "Plate carrier + pouches set")
            WireframeItemRow("Uniform", "Multicam set | knee pads")
        }
        WireframeSection(
            title = "Marketplace Links",
            subtitle = "Future linkage between inventory and listings (sell/mark unavailable).",
        ) {
            WireframeChipRow(listOf("Sell from inventory", "Mark unavailable", "Clone to listing"))
        }
    }
}
