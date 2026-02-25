package com.airsoft.social.feature.chats.impl

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
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.chats.api.ChatsFeatureApi

data class ChatsPreviewRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class ChatsUiState(
    val filters: List<String> = listOf("All", "Direct", "Team", "Unread", "Players"),
    val selectedFilter: String = "All",
    val activeDialogs: List<ChatsPreviewRow> = listOf(
        ChatsPreviewRow(
            title = "Team [EW] EASY WINNER",
            subtitle = "Saturday 09:00 assembly, confirm attendance",
            trailing = "12",
        ),
        ChatsPreviewRow(
            title = "Raven",
            subtitle = "Send camera mount photo",
            trailing = "2",
        ),
        ChatsPreviewRow(
            title = "North Tournament Organizers",
            subtitle = "Parking scheme updated",
            trailing = "new",
        ),
    ),
    val nearbyPlayers: List<ChatsPreviewRow> = listOf(
        ChatsPreviewRow("Ghost", "Assault | Moscow | Looking for team", "4.8"),
        ChatsPreviewRow("MedicFox", "Medic | Kazan | Weekend games", "4.6"),
        ChatsPreviewRow("Viper", "Marksman | SPB | Own gear", "4.9"),
    ),
)

sealed interface ChatsAction {
    data class SelectFilter(val filter: String) : ChatsAction
    data object OpenChatRoomDemoClicked : ChatsAction
    data object OpenPlayerCardDemoClicked : ChatsAction
}

class ChatsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    fun onAction(action: ChatsAction) {
        when (action) {
            is ChatsAction.SelectFilter -> {
                _uiState.value = _uiState.value.copy(selectedFilter = action.filter)
            }
            ChatsAction.OpenChatRoomDemoClicked -> Unit
            ChatsAction.OpenPlayerCardDemoClicked -> Unit
        }
    }
}

@Composable
fun ChatsPlaceholderScreen(
    onOpenChatRoomDemo: () -> Unit = {},
    onOpenPlayerCardDemo: () -> Unit = {},
    chatsViewModel: ChatsViewModel = viewModel(),
) {
    val uiState by chatsViewModel.uiState.collectAsState()

    ChatsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ChatsAction.OpenChatRoomDemoClicked -> onOpenChatRoomDemo()
                ChatsAction.OpenPlayerCardDemoClicked -> onOpenPlayerCardDemo()
                is ChatsAction.SelectFilter -> chatsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun ChatsScreen(
    uiState: ChatsUiState,
    onAction: (ChatsAction) -> Unit,
) {
    WireframePage(
        title = ChatsFeatureApi.contract.title,
        subtitle = "Structure skeleton for communication: direct chats, team channels, and player discovery.",
    ) {
        WireframeSection(
            title = "Filters",
            subtitle = "Quick tabs and search inputs for chats and players will live here.",
        ) {
            WireframeChipRow(
                labels = uiState.filters.map { filter ->
                    if (filter == uiState.selectedFilter) "[$filter]" else filter
                },
            )
        }
        WireframeSection(
            title = "Active Dialogs",
            subtitle = "Latest messages and unread markers (future data-driven list).",
        ) {
            uiState.activeDialogs.forEach { row ->
                WireframeItemRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    trailing = row.trailing,
                )
            }
        }
        WireframeSection(
            title = "Players Nearby / Looking For Team",
            subtitle = "Base layout for player catalog and quick invites.",
        ) {
            uiState.nearbyPlayers.forEach { row ->
                WireframeItemRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    trailing = row.trailing,
                )
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary pages for chat and player flows.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(ChatsAction.OpenChatRoomDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Chat Room Page")
                }
                OutlinedButton(
                    onClick = { onAction(ChatsAction.OpenPlayerCardDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Player Card Page")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedFilter == "All") "Unread" else "All"
                        onAction(ChatsAction.SelectFilter(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Preview Filter")
                }
            }
        }
    }
}

@Composable
fun ChatRoomSkeletonScreen() {
    WireframePage(
        title = "Chat Room",
        subtitle = "Skeleton for a conversation screen with thread, media, and quick actions.",
        primaryActionLabel = "Send Message",
    ) {
        WireframeSection(
            title = "Room Header",
            subtitle = "Participant info, status, pinned message, and mute settings.",
        ) {
            WireframeItemRow("Team [EW] EASY WINNER", "14 members | 6 online", "Muted off")
            WireframeChipRow(listOf("Pinned", "Files", "Media", "Mentions"))
        }
        WireframeSection(
            title = "Message Thread",
            subtitle = "Message list placeholder (text, attachments, replies).",
        ) {
            WireframeItemRow("Teiwaz_", "Meetpoint updated to gate C. Check map.", "20:14")
            WireframeItemRow("Raven", "Roger. Bringing extra comms battery.", "20:16")
            WireframeItemRow("MedicFox", "Need headcount confirmation by 22:00.", "20:18")
        }
        WireframeSection(
            title = "Composer Area",
            subtitle = "Text input, attachments, voice note, quick templates.",
        ) {
            WireframeChipRow(listOf("Template", "Attach", "Photo", "Location", "Voice"))
        }
    }
}

@Composable
fun PlayerCardSkeletonScreen() {
    WireframePage(
        title = "Player Card",
        subtitle = "Skeleton for public player profile preview from chats/discovery.",
        primaryActionLabel = "Start Chat",
    ) {
        WireframeSection(
            title = "Identity",
            subtitle = "Callsign, city, role tags, reputation.",
        ) {
            WireframeItemRow("Ghost", "Moscow | Assault | Weekend games", "4.8")
            WireframeChipRow(listOf("CQB", "Open field", "Driver", "Night games"))
        }
        WireframeSection(
            title = "Availability",
            subtitle = "Preferred days, distances, formats, and team status.",
        ) {
            WireframeItemRow("Schedule", "Fri evening / Sat full day / Sun morning")
            WireframeItemRow("Travel radius", "Up to 120 km from Moscow")
            WireframeItemRow("Status", "Looking for team / active game invites")
        }
        WireframeSection(
            title = "Quick Actions",
            subtitle = "Future actions: invite to team, add to squad, report/block.",
        ) {
            WireframeChipRow(listOf("Invite", "Bookmark", "Share", "Report"))
        }
    }
}
