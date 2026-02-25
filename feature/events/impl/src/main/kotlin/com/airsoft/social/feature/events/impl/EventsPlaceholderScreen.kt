package com.airsoft.social.feature.events.impl

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
import com.airsoft.social.feature.events.api.EventsFeatureApi

data class EventsListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class EventsUiState(
    val summaryMetrics: List<Pair<String, String>> = listOf(
        "This week" to "3",
        "Tournaments" to "1",
        "My entries" to "5",
    ),
    val filters: List<String> = listOf("All", "Trainings", "Tournaments", "CQB", "Open field", "Nearby"),
    val selectedFilter: String = "All",
    val upcomingGames: List<EventsListRow> = listOf(
        EventsListRow("Night Raid North", "Mar 1 | Forest Polygon | 32/60 participants", "Joined"),
        EventsListRow("CQB Sunday Cup", "Mar 3 | Arena 17 | Bracket mode", "Open"),
        EventsListRow("Team Training", "Mar 5 | Comms and movement drills", "Draft"),
    ),
)

sealed interface EventsAction {
    data class SelectFilter(val filter: String) : EventsAction
    data object OpenEventDetailDemoClicked : EventsAction
    data object OpenEventCreateDemoClicked : EventsAction
}

class EventsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun onAction(action: EventsAction) {
        when (action) {
            is EventsAction.SelectFilter -> _uiState.value = _uiState.value.copy(selectedFilter = action.filter)
            EventsAction.OpenEventDetailDemoClicked -> Unit
            EventsAction.OpenEventCreateDemoClicked -> Unit
        }
    }
}

@Composable
fun EventsPlaceholderScreen(
    onOpenEventDetailDemo: () -> Unit = {},
    onOpenEventCreateDemo: () -> Unit = {},
    eventsViewModel: EventsViewModel = viewModel(),
) {
    val uiState by eventsViewModel.uiState.collectAsState()

    EventsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                EventsAction.OpenEventDetailDemoClicked -> onOpenEventDetailDemo()
                EventsAction.OpenEventCreateDemoClicked -> onOpenEventCreateDemo()
                is EventsAction.SelectFilter -> eventsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun EventsScreen(
    uiState: EventsUiState,
    onAction: (EventsAction) -> Unit,
) {
    WireframePage(
        title = EventsFeatureApi.contract.title,
        subtitle = "Events page skeleton: calendar, tournaments, registration, and attendance states.",
    ) {
        WireframeSection(
            title = "Summary",
            subtitle = "Quick numbers for upcoming activities.",
        ) {
            WireframeMetricRow(
                items = uiState.summaryMetrics,
            )
        }
        WireframeSection(
            title = "Filters",
            subtitle = "Geo, format, skill level, and date controls will be added later.",
        ) {
            WireframeChipRow(
                uiState.filters.map { filter ->
                    if (filter == uiState.selectedFilter) "[$filter]" else filter
                },
            )
        }
        WireframeSection(
            title = "Upcoming Games",
            subtitle = "Event list/cards placeholder.",
        ) {
            uiState.upcomingGames.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary pages for event detail and create/edit flows.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(EventsAction.OpenEventDetailDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Event Detail Page")
                }
                OutlinedButton(
                    onClick = { onAction(EventsAction.OpenEventCreateDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Event Create/Edit Page")
                }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedFilter == "All") "Nearby" else "All"
                        onAction(EventsAction.SelectFilter(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Events Filter")
                }
            }
        }
    }
}

@Composable
fun EventDetailSkeletonScreen() {
    WireframePage(
        title = "Event Detail",
        subtitle = "Skeleton for event page: overview, schedule, teams, rules, and check-in.",
        primaryActionLabel = "Register",
    ) {
        WireframeSection(
            title = "Overview",
            subtitle = "Name, date, venue, organizer, capacity, fee.",
        ) {
            WireframeItemRow("Night Raid North", "Mar 1 | Forest Polygon | 32/60 registered", "Open")
            WireframeChipRow(listOf("Open field", "Night", "Squads", "Beginner-friendly"))
        }
        WireframeSection(
            title = "Timeline",
            subtitle = "Arrival, briefing, game phases, debrief, results.",
        ) {
            WireframeItemRow("08:00", "Arrival and chrono")
            WireframeItemRow("09:00", "Briefing and squad assignment")
            WireframeItemRow("10:00", "Phase 1 start")
            WireframeItemRow("18:00", "Debrief and results")
        }
        WireframeSection(
            title = "Rules / Logistics",
            subtitle = "Safety rules, parking, map zones, allowed gear classes.",
        ) {
            WireframeItemRow("Safety", "Mandatory chrono and eye protection")
            WireframeItemRow("Parking", "Gate C | follow organizer signs")
            WireframeItemRow("Map", "Three active zones + spawn restrictions")
        }
    }
}

@Composable
fun EventCreateEditSkeletonScreen() {
    WireframePage(
        title = "Event Create / Edit",
        subtitle = "Skeleton for organizer tools: setup, schedule, rules, capacity.",
        primaryActionLabel = "Publish Event",
    ) {
        WireframeSection(
            title = "Event Setup",
            subtitle = "Name, date, location, type, capacity, cost.",
        ) {
            WireframeItemRow("Title", "Text field placeholder")
            WireframeItemRow("Date & Time", "Date/time picker placeholder")
            WireframeItemRow("Location", "Map pin and address placeholder")
            WireframeItemRow("Capacity", "Min/max participants")
        }
        WireframeSection(
            title = "Registration Rules",
            subtitle = "Approval mode, waitlist, squad size, required profile fields.",
        ) {
            WireframeChipRow(listOf("Manual approval", "Waitlist", "Squad limit", "Profile required"))
        }
        WireframeSection(
            title = "Briefing Content",
            subtitle = "Rules, logistics, attachments, map images, safety notes.",
        ) {
            WireframeItemRow("Rules text", "Rich text editor placeholder")
            WireframeItemRow("Attachments", "Map images / PDFs / links")
            WireframeItemRow("Checklists", "Chrono, medics, comms, marshals")
        }
    }
}
