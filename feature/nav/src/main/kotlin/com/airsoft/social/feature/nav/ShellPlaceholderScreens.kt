package com.airsoft.social.feature.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShellWireframeRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class SettingsUiState(
    val selectedTheme: String = "System",
    val pushEnabled: Boolean = true,
    val telemetryEnabled: Boolean = false,
    val tacticalQuickLaunchEnabled: Boolean = true,
    val sessionRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Session mode", "Guest / account session switch placeholder", "Guest"),
        ShellWireframeRow("Profile visibility", "Public / team-only / hidden", "Public"),
    ),
)

sealed interface SettingsAction {
    data object CycleTheme : SettingsAction
    data object TogglePush : SettingsAction
    data object ToggleTelemetry : SettingsAction
    data object ToggleTacticalQuickLaunch : SettingsAction
    data object OpenSupportClicked : SettingsAction
    data object OpenAboutClicked : SettingsAction
}

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.CycleTheme -> {
                val current = _uiState.value.selectedTheme
                val next = when (current) {
                    "System" -> "Light"
                    "Light" -> "Dark"
                    else -> "System"
                }
                _uiState.value = _uiState.value.copy(selectedTheme = next)
            }

            SettingsAction.TogglePush -> {
                _uiState.value = _uiState.value.copy(pushEnabled = !_uiState.value.pushEnabled)
            }

            SettingsAction.ToggleTelemetry -> {
                _uiState.value = _uiState.value.copy(
                    telemetryEnabled = !_uiState.value.telemetryEnabled,
                )
            }

            SettingsAction.ToggleTacticalQuickLaunch -> {
                _uiState.value = _uiState.value.copy(
                    tacticalQuickLaunchEnabled = !_uiState.value.tacticalQuickLaunchEnabled,
                )
            }

            SettingsAction.OpenSupportClicked -> Unit
            SettingsAction.OpenAboutClicked -> Unit
        }
    }
}

@Composable
fun SettingsShellRoute(
    onOpenSupport: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    SettingsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SettingsAction.OpenSupportClicked -> onOpenSupport()
                SettingsAction.OpenAboutClicked -> onOpenAbout()
                else -> settingsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun SettingsShellScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    WireframePage(
        title = "Settings",
        subtitle = "App shell configuration skeleton: local preferences, account, privacy, and tactical shortcuts.",
        primaryActionLabel = "Apply (stub)",
    ) {
        WireframeSection(
            title = "Appearance",
            subtitle = "Theme mode and shell behavior toggles.",
        ) {
            WireframeItemRow("Theme", "Cycle system/light/dark", uiState.selectedTheme)
            WireframeItemRow(
                "Push notifications",
                "Alerts, invites, marketplace replies, event reminders",
                if (uiState.pushEnabled) "On" else "Off",
            )
            WireframeItemRow(
                "Telemetry",
                "Analytics/crash reporting toggle via port-based reporter",
                if (uiState.telemetryEnabled) "On" else "Off",
            )
            WireframeItemRow(
                "Quick tactical launch",
                "Drawer CTA and radar shortcut visibility",
                if (uiState.tacticalQuickLaunchEnabled) "On" else "Off",
            )
        }
        WireframeSection(
            title = "Session and Privacy",
            subtitle = "Account/session/privacy settings placeholders.",
        ) {
            uiState.sessionRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary shell pages linked from settings.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(SettingsAction.CycleTheme) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Cycle Theme") }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.TogglePush) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Toggle Push") }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.ToggleTelemetry) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Toggle Telemetry") }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.ToggleTacticalQuickLaunch) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Toggle Tactical Shortcut") }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenSupportClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Support") }
                OutlinedButton(
                    onClick = { onAction(SettingsAction.OpenAboutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open About") }
            }
        }
    }
}

data class SupportUiState(
    val selectedTopic: String = "Account",
    val topics: List<String> = listOf("Account", "Teams", "Events", "Marketplace", "Radar"),
    val channelRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("In-app ticket", "Structured support request form with attachments", "planned"),
        ShellWireframeRow("Moderation report", "Report user/team/listing/event", "planned"),
        ShellWireframeRow("FAQ and guides", "Static docs + troubleshooting flows", "skeleton"),
    ),
    val faqRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("How to join a team?", "Invite flow / request flow / role approvals"),
        ShellWireframeRow("How to create an event?", "Organizer page + roster cap + fields"),
        ShellWireframeRow("How to enter radar mode?", "Use V BOI! and switch to legacy tactical bridge"),
    ),
)

sealed interface SupportAction {
    data class SelectTopic(val topic: String) : SupportAction
    data object OpenNotificationsCenterClicked : SupportAction
}

class SupportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun onAction(action: SupportAction) {
        when (action) {
            is SupportAction.SelectTopic -> _uiState.value = _uiState.value.copy(selectedTopic = action.topic)
            SupportAction.OpenNotificationsCenterClicked -> Unit
        }
    }
}

@Composable
fun SupportShellRoute(
    onOpenNotifications: () -> Unit = {},
    supportViewModel: SupportViewModel = viewModel(),
) {
    val uiState by supportViewModel.uiState.collectAsState()
    SupportShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SupportAction.OpenNotificationsCenterClicked -> onOpenNotifications()
                is SupportAction.SelectTopic -> supportViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun SupportShellScreen(
    uiState: SupportUiState,
    onAction: (SupportAction) -> Unit,
) {
    WireframePage(
        title = "Support",
        subtitle = "Support center skeleton: topics, tickets, moderation reporting, and FAQ.",
        primaryActionLabel = "Create Ticket (stub)",
    ) {
        WireframeSection(
            title = "Topics",
            subtitle = "User chooses a problem area before opening a ticket.",
        ) {
            WireframeChipRow(
                labels = uiState.topics.map { topic ->
                    if (topic == uiState.selectedTopic) "[$topic]" else topic
                },
            )
        }
        WireframeSection(
            title = "Support Channels",
            subtitle = "Where tickets/reports/help requests will live.",
        ) {
            uiState.channelRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "FAQ / Troubleshooting",
            subtitle = "Static knowledge base and guided fixes.",
        ) {
            uiState.faqRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Actions",
            subtitle = "Placeholder controls for shell wiring.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val next = if (uiState.selectedTopic == "Radar") "Teams" else "Radar"
                        onAction(SupportAction.SelectTopic(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Toggle Topic") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenNotificationsCenterClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Notifications Center") }
            }
        }
    }
}

data class AboutUiState(
    val buildChannel: String = "Dev",
    val versionRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("App shell", "New modular shell behind feature flag", "step1"),
        ShellWireframeRow("Legacy tactical", "Bridge launch path retained in app module", "active"),
        ShellWireframeRow("Backend mode", "Port-first, Firebase adapters are temporary", "hybrid"),
    ),
    val moduleRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("core:*", "Ports, models, datastore, ui, network, database"),
        ShellWireframeRow("feature:*", "Screens, nav contracts, placeholder flows"),
        ShellWireframeRow("infra:firebase", "Temporary adapters for auth/realtime/telemetry"),
    ),
)

sealed interface AboutAction {
    data object ToggleBuildChannel : AboutAction
}

class AboutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    fun onAction(action: AboutAction) {
        when (action) {
            AboutAction.ToggleBuildChannel -> {
                val next = if (_uiState.value.buildChannel == "Dev") "QA" else "Dev"
                _uiState.value = _uiState.value.copy(buildChannel = next)
            }
        }
    }
}

@Composable
fun AboutShellRoute(
    aboutViewModel: AboutViewModel = viewModel(),
) {
    val uiState by aboutViewModel.uiState.collectAsState()
    AboutShellScreen(
        uiState = uiState,
        onAction = aboutViewModel::onAction,
    )
}

@Composable
private fun AboutShellScreen(
    uiState: AboutUiState,
    onAction: (AboutAction) -> Unit,
) {
    WireframePage(
        title = "About",
        subtitle = "About page skeleton: build info, architecture summary, legal/version blocks.",
        primaryActionLabel = "Toggle Channel",
        onPrimaryAction = { onAction(AboutAction.ToggleBuildChannel) },
    ) {
        WireframeSection(
            title = "Build Information",
            subtitle = "Version/build/channel placeholders for release management.",
        ) {
            WireframeItemRow("Package", "Current applicationId retained during strangler migration")
            WireframeItemRow("Channel", "Dev / QA / Prod placeholder", uiState.buildChannel)
            uiState.versionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Module Map",
            subtitle = "High-level architecture summary visible to internal testers.",
        ) {
            uiState.moduleRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Legal / Docs",
            subtitle = "Privacy policy, terms, support contacts, licenses.",
        ) {
            WireframeChipRow(
                labels = listOf("Privacy Policy", "Terms", "OSS Licenses", "Release Notes", "Roadmap"),
            )
        }
    }
}

data class SearchUiState(
    val selectedCategory: String = "Players",
    val categories: List<String> = listOf("Players", "Teams", "Events", "Market", "Fields"),
    val recentRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("night game moscow", "Recent query"),
        ShellWireframeRow("team cqb", "Recent query"),
        ShellWireframeRow("plate carrier multicam", "Recent query"),
    ),
    val suggestionRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Players near me", "Discovery / matchmaking"),
        ShellWireframeRow("Teams recruiting medics", "Team recruitment board"),
        ShellWireframeRow("Weekend events", "Upcoming games and tournaments"),
        ShellWireframeRow("Used replicas", "Marketplace listings"),
    ),
)

sealed interface SearchAction {
    data class SelectCategory(val category: String) : SearchAction
    data object OpenPlayersClicked : SearchAction
    data object OpenTeamsClicked : SearchAction
    data object OpenEventsClicked : SearchAction
    data object OpenMarketplaceClicked : SearchAction
}

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.SelectCategory -> _uiState.value = _uiState.value.copy(selectedCategory = action.category)
            SearchAction.OpenPlayersClicked -> Unit
            SearchAction.OpenTeamsClicked -> Unit
            SearchAction.OpenEventsClicked -> Unit
            SearchAction.OpenMarketplaceClicked -> Unit
        }
    }
}

@Composable
fun SearchShellRoute(
    onOpenPlayers: () -> Unit = {},
    onOpenTeams: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
) {
    val uiState by searchViewModel.uiState.collectAsState()
    SearchShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SearchAction.OpenPlayersClicked -> onOpenPlayers()
                SearchAction.OpenTeamsClicked -> onOpenTeams()
                SearchAction.OpenEventsClicked -> onOpenEvents()
                SearchAction.OpenMarketplaceClicked -> onOpenMarketplace()
                is SearchAction.SelectCategory -> searchViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun SearchShellScreen(
    uiState: SearchUiState,
    onAction: (SearchAction) -> Unit,
) {
    WireframePage(
        title = "Search",
        subtitle = "Global search skeleton for players, teams, events, marketplace, and fields.",
        primaryActionLabel = "Run Search (stub)",
    ) {
        WireframeSection(
            title = "Search Scope",
            subtitle = "Category chips and future typed search input.",
        ) {
            WireframeChipRow(
                labels = uiState.categories.map { category ->
                    if (category == uiState.selectedCategory) "[$category]" else category
                },
            )
        }
        WireframeSection(
            title = "Recent Searches",
            subtitle = "Search history placeholders from local storage.",
        ) {
            uiState.recentRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Suggested Entry Points",
            subtitle = "Quick jump into major app sections while search backend is not ready.",
        ) {
            uiState.suggestionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(SearchAction.OpenPlayersClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Players / Chats") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenTeamsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Teams") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenEventsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Events") }
                OutlinedButton(
                    onClick = { onAction(SearchAction.OpenMarketplaceClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Marketplace") }
                OutlinedButton(
                    onClick = {
                        val next = if (uiState.selectedCategory == "Players") "Market" else "Players"
                        onAction(SearchAction.SelectCategory(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Toggle Category") }
            }
        }
    }
}

data class NotificationsUiState(
    val selectedFilter: String = "Unread",
    val filters: List<String> = listOf("Unread", "All", "Mentions", "Invites", "System"),
    val notificationRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Team invite", "EW Rangers invited you to roster review", "new"),
        ShellWireframeRow("Event update", "Night Raid North changed start time", "1h"),
        ShellWireframeRow("Marketplace reply", "Seller answered your offer on M4A1", "3h"),
        ShellWireframeRow("System notice", "New shell build available for testing", "dev"),
    ),
)

sealed interface NotificationsAction {
    data class SelectFilter(val filter: String) : NotificationsAction
}

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.SelectFilter -> {
                _uiState.value = _uiState.value.copy(selectedFilter = action.filter)
            }
        }
    }
}

@Composable
fun NotificationsShellRoute(
    notificationsViewModel: NotificationsViewModel = viewModel(),
) {
    val uiState by notificationsViewModel.uiState.collectAsState()
    NotificationsShellScreen(
        uiState = uiState,
        onAction = notificationsViewModel::onAction,
    )
}

@Composable
private fun NotificationsShellScreen(
    uiState: NotificationsUiState,
    onAction: (NotificationsAction) -> Unit,
) {
    WireframePage(
        title = "Notifications",
        subtitle = "Notification center skeleton for invites, mentions, marketplace, and system alerts.",
        primaryActionLabel = "Mark all as read (stub)",
    ) {
        WireframeSection(
            title = "Filters",
            subtitle = "Notification bucket selection.",
        ) {
            WireframeChipRow(
                labels = uiState.filters.map { filter ->
                    if (filter == uiState.selectedFilter) "[$filter]" else filter
                },
            )
        }
        WireframeSection(
            title = "Inbox",
            subtitle = "Unified notification feed placeholder.",
        ) {
            uiState.notificationRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Demo Action",
            subtitle = "Toggle filter to validate ViewModel/action wiring.",
        ) {
            Button(
                onClick = {
                    val next = if (uiState.selectedFilter == "Unread") "All" else "Unread"
                    onAction(NotificationsAction.SelectFilter(next))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Toggle Filter")
            }
        }
    }
}
