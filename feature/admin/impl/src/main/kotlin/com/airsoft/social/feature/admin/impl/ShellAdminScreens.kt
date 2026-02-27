package com.airsoft.social.feature.admin.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
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

data class ModerationUiState(
    val selectedQueue: String = "Жалобы",
    val queues: List<String> = listOf("Жалобы", "Объявления", "Пользователи", "Контент"),
    val summaryRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Новые жалобы", "12 за последние 24 часа", "12"),
        ShellWireframeRow("Очередь обработки", "34 кейса в работе", "34"),
        ShellWireframeRow("SLA", "Среднее время реакции 2ч 15м", "SLA"),
    ),
    val queuePreviewRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Жалоба на объявление", "Подозрение на мошенничество, маркет", "high"),
        ShellWireframeRow("Жалоба на игрока", "Оскорбления в чате команды", "medium"),
    ),
)

sealed interface ModerationAction {
    data object CycleQueue : ModerationAction
    data object OpenReportsQueueClicked : ModerationAction
    data object OpenChatMonitoringClicked : ModerationAction
    data object OpenMarketQuarantineClicked : ModerationAction
    data object OpenUsersSanctionsClicked : ModerationAction
    data object OpenSupportInboxClicked : ModerationAction
    data object OpenAdminDashboardClicked : ModerationAction
}

class ModerationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    fun onAction(action: ModerationAction) {
        when (action) {
            ModerationAction.CycleQueue -> {
                val current = _uiState.value
                val idx = current.queues.indexOf(current.selectedQueue)
                val next = current.queues[(idx + 1).mod(current.queues.size)]
                _uiState.value = current.copy(selectedQueue = next)
            }

            ModerationAction.OpenReportsQueueClicked,
            ModerationAction.OpenChatMonitoringClicked,
            ModerationAction.OpenMarketQuarantineClicked,
            ModerationAction.OpenUsersSanctionsClicked,
            ModerationAction.OpenSupportInboxClicked,
            ModerationAction.OpenAdminDashboardClicked,
            -> Unit
        }
    }
}

@Composable
fun ModerationShellRoute(
    onOpenReportsQueue: () -> Unit = {},
    onOpenChatMonitoring: () -> Unit = {},
    onOpenMarketQuarantine: () -> Unit = {},
    onOpenUsersSanctions: () -> Unit = {},
    onOpenSupportInbox: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    moderationViewModel: ModerationViewModel = viewModel(),
) {
    val uiState by moderationViewModel.uiState.collectAsState()
    WireframePage(
        title = "Модерация",
        subtitle = "Каркас панели модерации: метрики, очереди жалоб и приоритеты обработки.",
        primaryActionLabel = "Открыть очередь жалоб (заглушка)",
    ) {
        WireframeSection(title = "Сводка", subtitle = "Ключевые показатели по модерации.") {
            WireframeMetricRow(
                items = listOf(
                    "Очередь" to uiState.selectedQueue,
                    "Новых" to "12",
                    "SLA" to "2ч",
                ),
            )
            WireframeChipRow(
                labels = uiState.queues.map { if (it == uiState.selectedQueue) "[$it]" else it },
            )
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Превью очереди",
            subtitle = "Примеры кейсов в текущей очереди обработки.",
        ) {
            uiState.queuePreviewRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Переходы", subtitle = "Проверка маршрута очереди жалоб.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { moderationViewModel.onAction(ModerationAction.CycleQueue) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить очередь") }
                OutlinedButton(
                    onClick = onOpenReportsQueue,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть очередь жалоб") }
                OutlinedButton(
                    onClick = onOpenChatMonitoring,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Мониторинг чата") }
                OutlinedButton(
                    onClick = onOpenMarketQuarantine,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Карантин барахолки") }
                OutlinedButton(
                    onClick = onOpenUsersSanctions,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Пользователи / санкции") }
                OutlinedButton(
                    onClick = onOpenSupportInbox,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Support Inbox") }
                OutlinedButton(
                    onClick = onOpenAdminDashboard,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Админка / Dashboard") }
            }
        }
    }
}

data class ModerationReportsQueueUiState(
    val selectedPriority: String = "Все",
    val priorities: List<String> = listOf("Все", "Высокий", "Средний", "Низкий"),
    val reportRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("report-001", "Маркет: жалоба на продавца / предоплата", "high"),
        ShellWireframeRow("report-002", "Чат: оскорбления и спам", "medium"),
        ShellWireframeRow("report-003", "Услуги: недостоверное описание", "low"),
    ),
)

sealed interface ModerationReportsQueueAction {
    data object CyclePriority : ModerationReportsQueueAction
    data object OpenReportDetailClicked : ModerationReportsQueueAction
}

class ModerationReportsQueueViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ModerationReportsQueueUiState())
    val uiState: StateFlow<ModerationReportsQueueUiState> = _uiState.asStateFlow()

    fun onAction(action: ModerationReportsQueueAction) {
        when (action) {
            ModerationReportsQueueAction.CyclePriority -> {
                val current = _uiState.value
                val idx = current.priorities.indexOf(current.selectedPriority)
                val next = current.priorities[(idx + 1).mod(current.priorities.size)]
                _uiState.value = current.copy(selectedPriority = next)
            }

            ModerationReportsQueueAction.OpenReportDetailClicked -> Unit
        }
    }
}

@Composable
fun ModerationReportsQueueShellRoute(
    onOpenReportDetail: () -> Unit = {},
    moderationReportsQueueViewModel: ModerationReportsQueueViewModel = viewModel(),
) {
    val uiState by moderationReportsQueueViewModel.uiState.collectAsState()
    WireframePage(
        title = "Очередь жалоб",
        subtitle = "Каркас очереди модерации: фильтры приоритета и список кейсов.",
        primaryActionLabel = "Открыть первый кейс (заглушка)",
    ) {
        WireframeSection(title = "Приоритет", subtitle = "Фильтрация очереди по уровню риска.") {
            WireframeMetricRow(
                items = listOf(
                    "Приоритет" to uiState.selectedPriority,
                    "Кейсов" to uiState.reportRows.size.toString(),
                    "Очередь" to "Жалобы",
                ),
            )
            WireframeChipRow(
                labels = uiState.priorities.map { if (it == uiState.selectedPriority) "[$it]" else it },
            )
        }
        WireframeSection(title = "Кейсы", subtitle = "Список жалоб в очереди обработки.") {
            uiState.reportRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка перехода в карточку кейса.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { moderationReportsQueueViewModel.onAction(ModerationReportsQueueAction.CyclePriority) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить приоритет") }
                OutlinedButton(
                    onClick = onOpenReportDetail,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть кейс") }
            }
        }
    }
}

data class ModerationReportDetailUiState(
    val reportId: String = "",
    val selectedTab: String = "Сводка",
    val tabs: List<String> = listOf("Сводка", "Контекст", "Решение"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val contextRows: List<ShellWireframeRow> = emptyList(),
    val resolutionRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface ModerationReportDetailAction {
    data object CycleTab : ModerationReportDetailAction
}

class ModerationReportDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ModerationReportDetailUiState())
    val uiState: StateFlow<ModerationReportDetailUiState> = _uiState.asStateFlow()

    fun load(reportId: String) {
        if (_uiState.value.reportId == reportId) return
        _uiState.value = ModerationReportDetailUiState(
            reportId = reportId,
            summaryRows = listOf(
                ShellWireframeRow("Тип", "Жалоба на объявление / маркет", "market"),
                ShellWireframeRow("Приоритет", "Высокий", "high"),
                ShellWireframeRow("Статус", "В работе", "active"),
            ),
            contextRows = listOf(
                ShellWireframeRow("Источник", "Пользователь Ghost", "user"),
                ShellWireframeRow("Сущность", "Объявление m4a1-cyma-listing", "listing"),
                ShellWireframeRow("Причина", "Подозрение на мошенничество / предоплата", "reason"),
            ),
            resolutionRows = listOf(
                ShellWireframeRow("Действие", "Запросить подтверждение / чеки", "todo"),
                ShellWireframeRow("Коммуникация", "Оповестить продавца и автора жалобы", "notify"),
                ShellWireframeRow("Эскалация", "При повторе -> блокировка и бан-лист", "policy"),
            ),
        )
    }

    fun onAction(action: ModerationReportDetailAction) {
        when (action) {
            ModerationReportDetailAction.CycleTab -> {
                val current = _uiState.value
                val idx = current.tabs.indexOf(current.selectedTab)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
fun ModerationReportDetailSkeletonRoute(
    reportId: String,
    moderationReportDetailViewModel: ModerationReportDetailViewModel = viewModel(),
) {
    LaunchedEffect(reportId) { moderationReportDetailViewModel.load(reportId) }
    val uiState by moderationReportDetailViewModel.uiState.collectAsState()
    WireframePage(
        title = "Кейс модерации",
        subtitle = "Карточка жалобы / кейса модерации. ID: ${uiState.reportId}",
        primaryActionLabel = "Принять решение (заглушка)",
    ) {
        WireframeSection(title = "Вкладки", subtitle = "Сводка, контекст и решение.") {
            WireframeMetricRow(
                items = listOf(
                    "Вкладка" to uiState.selectedTab,
                    "Контекст" to uiState.contextRows.size.toString(),
                    "Решения" to uiState.resolutionRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(title = "Сводка", subtitle = "Тип кейса, приоритет и статус.") {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Контекст", subtitle = "Источник, сущность и причина жалобы.") {
            uiState.contextRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Решение", subtitle = "Шаги обработки и меры реагирования.") {
            uiState.resolutionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring карточки кейса.") {
            Button(
                onClick = { moderationReportDetailViewModel.onAction(ModerationReportDetailAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить вкладку кейса") }
        }
    }
}

data class OpsToolUiState(
    val toolId: String = "",
    val selectedSegment: String = "Обзор",
    val segments: List<String> = listOf("Обзор", "Очередь", "Действия"),
    val metricRows: List<Pair<String, String>> = emptyList(),
    val primaryRows: List<ShellWireframeRow> = emptyList(),
    val secondaryRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface OpsToolAction {
    data object CycleSegment : OpsToolAction
}

data class OpsToolSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryActionLabel: String,
    val segments: List<String>,
    val metricRows: List<Pair<String, String>>,
    val primarySectionTitle: String,
    val primarySectionSubtitle: String,
    val primaryRows: List<ShellWireframeRow>,
    val secondarySectionTitle: String,
    val secondarySectionSubtitle: String,
    val secondaryRows: List<ShellWireframeRow>,
    val actionButtonLabel: String,
)

class OpsToolViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OpsToolUiState())
    val uiState: StateFlow<OpsToolUiState> = _uiState.asStateFlow()

    fun load(spec: OpsToolSpec) {
        if (_uiState.value.toolId == spec.id) return
        _uiState.value = OpsToolUiState(
            toolId = spec.id,
            selectedSegment = spec.segments.firstOrNull() ?: "Обзор",
            segments = spec.segments,
            metricRows = spec.metricRows,
            primaryRows = spec.primaryRows,
            secondaryRows = spec.secondaryRows,
        )
    }

    fun onAction(action: OpsToolAction) {
        when (action) {
            OpsToolAction.CycleSegment -> {
                val current = _uiState.value
                if (current.segments.isEmpty()) return
                val idx = current.segments.indexOf(current.selectedSegment).coerceAtLeast(0)
                val next = current.segments[(idx + 1).mod(current.segments.size)]
                _uiState.value = current.copy(selectedSegment = next)
            }
        }
    }
}

@Composable
private fun OpsToolSkeletonRoute(
    spec: OpsToolSpec,
    opsToolViewModel: OpsToolViewModel = viewModel(),
) {
    LaunchedEffect(spec.id) { opsToolViewModel.load(spec) }
    val uiState by opsToolViewModel.uiState.collectAsState()

    WireframePage(
        title = spec.title,
        subtitle = spec.subtitle,
        primaryActionLabel = spec.primaryActionLabel,
    ) {
        WireframeSection(
            title = "Сводка",
            subtitle = "Режимы и базовые метрики инструмента.",
        ) {
            WireframeMetricRow(
                items = buildList {
                    add("Режим" to uiState.selectedSegment)
                    addAll(uiState.metricRows)
                },
            )
            WireframeChipRow(
                labels = uiState.segments.map { if (it == uiState.selectedSegment) "[$it]" else it },
            )
        }
        WireframeSection(
            title = spec.primarySectionTitle,
            subtitle = spec.primarySectionSubtitle,
        ) {
            uiState.primaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = spec.secondarySectionTitle,
            subtitle = spec.secondarySectionSubtitle,
        ) {
            uiState.secondaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Проверка state wiring и базовых сценариев.",
        ) {
            Button(
                onClick = { opsToolViewModel.onAction(OpsToolAction.CycleSegment) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(spec.actionButtonLabel) }
        }
    }
}

data class AdminDashboardUiState(
    val selectedSection: String = "Доступы",
    val sections: List<String> = listOf("Доступы", "Политики", "Аудит"),
    val summaryRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Модераторы", "18 активных, 3 заявки на расширение ролей", "18"),
        ShellWireframeRow("GDPR запросы", "5 в работе, 1 на эскалации", "5"),
        ShellWireframeRow("Политики", "12 опубликовано, 2 черновика", "12"),
    ),
)

sealed interface AdminDashboardAction {
    data object CycleSection : AdminDashboardAction
    data object OpenRbacMatrixClicked : AdminDashboardAction
    data object OpenModeratorAssignmentsClicked : AdminDashboardAction
    data object OpenPoliciesClicked : AdminDashboardAction
    data object OpenMarketplaceCategoriesClicked : AdminDashboardAction
    data object OpenGdprRequestsClicked : AdminDashboardAction
    data object OpenAuditLogClicked : AdminDashboardAction
}

class AdminDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun onAction(action: AdminDashboardAction) {
        when (action) {
            AdminDashboardAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }

            AdminDashboardAction.OpenRbacMatrixClicked,
            AdminDashboardAction.OpenModeratorAssignmentsClicked,
            AdminDashboardAction.OpenPoliciesClicked,
            AdminDashboardAction.OpenMarketplaceCategoriesClicked,
            AdminDashboardAction.OpenGdprRequestsClicked,
            AdminDashboardAction.OpenAuditLogClicked,
            -> Unit
        }
    }
}

@Composable
fun AdminDashboardShellRoute(
    onOpenRbacMatrix: () -> Unit = {},
    onOpenModeratorAssignments: () -> Unit = {},
    onOpenPolicies: () -> Unit = {},
    onOpenMarketplaceCategories: () -> Unit = {},
    onOpenGdprRequests: () -> Unit = {},
    onOpenAuditLog: () -> Unit = {},
    adminDashboardViewModel: AdminDashboardViewModel = viewModel(),
) {
    val uiState by adminDashboardViewModel.uiState.collectAsState()
    WireframePage(
        title = "Админка",
        subtitle = "Каркас админ-панели из FULL.md: RBAC, назначения модераторов, политики, GDPR и аудит.",
        primaryActionLabel = "Открыть Audit Log (заглушка)",
    ) {
        WireframeSection(title = "Сводка", subtitle = "Статус ключевых блоков админки.") {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Блоков" to "6",
                    "Алертов" to "1",
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Инструменты админа",
            subtitle = "Навигация по админ-функциям: роли, политики, категории, GDPR, audit.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { adminDashboardViewModel.onAction(AdminDashboardAction.CycleSection) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить раздел админки") }
                OutlinedButton(onClick = onOpenRbacMatrix, modifier = Modifier.fillMaxWidth()) {
                    Text("RBAC Matrix")
                }
                OutlinedButton(onClick = onOpenModeratorAssignments, modifier = Modifier.fillMaxWidth()) {
                    Text("Назначение модераторов")
                }
                OutlinedButton(onClick = onOpenPolicies, modifier = Modifier.fillMaxWidth()) {
                    Text("Политики")
                }
                OutlinedButton(onClick = onOpenMarketplaceCategories, modifier = Modifier.fillMaxWidth()) {
                    Text("Категории барахолки")
                }
                OutlinedButton(onClick = onOpenGdprRequests, modifier = Modifier.fillMaxWidth()) {
                    Text("GDPR запросы")
                }
                OutlinedButton(onClick = onOpenAuditLog, modifier = Modifier.fillMaxWidth()) {
                    Text("Audit Log")
                }
            }
        }
    }
}

private val moderationChatMonitoringSpec = OpsToolSpec(
    id = "moderation-chat-monitor",
    title = "Мониторинг чата",
    subtitle = "Каркас real-time мониторинга сообщений: сигналы риска, поток событий и быстрые действия модератора.",
    primaryActionLabel = "Открыть live-поток (заглушка)",
    segments = listOf("Поток", "Триггеры", "Действия"),
    metricRows = listOf("Событий/мин" to "48", "Флагов" to "7"),
    primarySectionTitle = "Подозрительные сообщения",
    primarySectionSubtitle = "Сообщения, попавшие в автофлаги по жалобам/правилам.",
    primaryRows = listOf(
        ShellWireframeRow("msg-441", "Командный чат [EW]: спам и оскорбления, 3 репорта", "high"),
        ShellWireframeRow("msg-442", "ЛС: подозрение на предоплату вне площадки", "risk"),
        ShellWireframeRow("msg-443", "Глобальный чат: flood / caps / реклама", "spam"),
    ),
    secondarySectionTitle = "Быстрые меры",
    secondarySectionSubtitle = "Действия модератора для чатов: скрытие, мут, эскалация.",
    secondaryRows = listOf(
        ShellWireframeRow("Скрыть сообщение", "Временное скрытие до проверки", "hide"),
        ShellWireframeRow("Мут 1ч", "Ограничение сообщений в конкретном чате", "mute"),
        ShellWireframeRow("Эскалация кейса", "Передача в очередь жалоб/админам", "queue"),
    ),
    actionButtonLabel = "Переключить режим мониторинга",
)

private val moderationMarketQuarantineSpec = OpsToolSpec(
    id = "moderation-market-quarantine",
    title = "Карантин барахолки",
    subtitle = "Каркас очереди подозрительных объявлений: ручная проверка, запрос доказательств и выпуск/отклонение.",
    primaryActionLabel = "Открыть кейс объявления (заглушка)",
    segments = listOf("Карантин", "Проверка", "Решение"),
    metricRows = listOf("В очереди" to "23", "SLA" to "3ч"),
    primarySectionTitle = "Очередь карантина",
    primarySectionSubtitle = "Объявления с риском мошенничества/дублей/подозрительных описаний.",
    primaryRows = listOf(
        ShellWireframeRow("listing-q-101", "M4A1 CYMA + предоплата / жалоба 2шт", "high"),
        ShellWireframeRow("listing-q-102", "Несоответствие фото и описания", "verify"),
        ShellWireframeRow("listing-q-103", "Подозрение на мультиаккаунты продавца", "fraud"),
    ),
    secondarySectionTitle = "Политики решения",
    secondarySectionSubtitle = "Шаблоны действий по рынку: publish / request / reject / ban.",
    secondaryRows = listOf(
        ShellWireframeRow("Выпустить", "Опубликовать после ручной проверки", "publish"),
        ShellWireframeRow("Запросить доказательства", "Фото/видео/серийник/чек", "request"),
        ShellWireframeRow("Отклонить и зафиксировать", "Причина + запись в audit log", "reject"),
    ),
    actionButtonLabel = "Переключить режим карантина",
)

private val moderationUsersSanctionsSpec = OpsToolSpec(
    id = "moderation-users-sanctions",
    title = "Пользователи / санкции",
    subtitle = "Каркас панели пользователей модерации: поиск, история жалоб, санкции и ограничения разделов.",
    primaryActionLabel = "Открыть карточку пользователя (заглушка)",
    segments = listOf("Поиск", "Санкции", "История"),
    metricRows = listOf("Кейсов" to "11", "Банов" to "4"),
    primarySectionTitle = "Пользователи в работе",
    primarySectionSubtitle = "Профили пользователей с активными кейсами и проверками.",
    primaryRows = listOf(
        ShellWireframeRow("Ghost", "2 жалобы за 30 дней (чат + маркет)", "watch"),
        ShellWireframeRow("Raven", "Повторный спам, вынесено предупреждение", "warn"),
        ShellWireframeRow("Fox", "Проверка описаний услуг и отзывов", "review"),
    ),
    secondarySectionTitle = "Санкции и ограничения",
    secondarySectionSubtitle = "Warning, section lock, временный/постоянный бан, эскалация.",
    secondaryRows = listOf(
        ShellWireframeRow("Warning", "Предупреждение с шаблоном причины", "soft"),
        ShellWireframeRow("Section lock", "Ограничение маркет/чат/услуги", "scope"),
        ShellWireframeRow("Ban", "Временная или постоянная блокировка", "ban"),
    ),
    actionButtonLabel = "Переключить режим панели пользователей",
)

private val moderationSupportInboxSpec = OpsToolSpec(
    id = "moderation-support-inbox",
    title = "Support Inbox",
    subtitle = "Каркас очереди тикетов поддержки и эскалаций в модерацию/техподдержку с SLA.",
    primaryActionLabel = "Открыть тикет (заглушка)",
    segments = listOf("Тикеты", "SLA", "FAQ"),
    metricRows = listOf("Открыто" to "18", "Просрочено" to "3"),
    primarySectionTitle = "Входящие тикеты",
    primarySectionSubtitle = "Жалобы, вопросы по аккаунту, календарю, барахолке и радару.",
    primaryRows = listOf(
        ShellWireframeRow("ticket-201", "Не могу войти в командный радар после обновления", "high"),
        ShellWireframeRow("ticket-202", "Жалоба на продавца / вернуть объявление", "market"),
        ShellWireframeRow("ticket-203", "Как работает календарная синхронизация", "faq"),
    ),
    secondarySectionTitle = "Маршрутизация тикетов",
    secondarySectionSubtitle = "Шаблоны FAQ, техподдержка, модерация, эскалация в админку.",
    secondaryRows = listOf(
        ShellWireframeRow("FAQ ответ", "Автоответ + ссылка на гайд", "faq"),
        ShellWireframeRow("Техподдержка", "Баг/устройство/логин -> tech queue", "tech"),
        ShellWireframeRow("Модерация", "Жалобы/санкции/контент -> moderation", "mod"),
    ),
    actionButtonLabel = "Переключить режим support inbox",
)

@Composable
fun ModerationChatMonitoringShellRoute() {
    OpsToolSkeletonRoute(spec = moderationChatMonitoringSpec)
}

@Composable
fun ModerationMarketQuarantineShellRoute() {
    OpsToolSkeletonRoute(spec = moderationMarketQuarantineSpec)
}

@Composable
fun ModerationUsersSanctionsShellRoute() {
    OpsToolSkeletonRoute(spec = moderationUsersSanctionsSpec)
}

@Composable
fun ModerationSupportInboxShellRoute() {
    OpsToolSkeletonRoute(spec = moderationSupportInboxSpec)
}

@Composable
fun AdminRbacMatrixShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-rbac",
            title = "RBAC Matrix",
            subtitle = "Матрица ролей и прав для модераторов, саппорта, создателей и администраторов.",
            primaryActionLabel = "Открыть роль (заглушка)",
            segments = listOf("Роли", "Права", "Ревью"),
            metricRows = listOf("Ролей" to "9", "Прав" to "54"),
            primarySectionTitle = "Роли и права",
            primarySectionSubtitle = "Кто к каким разделам имеет доступ и какие действия может выполнять.",
            primaryRows = listOf(
                ShellWireframeRow("Moderator", "Жалобы, карантин, чат-мониторинг, support inbox", "scope"),
                ShellWireframeRow("Admin", "RBAC, политики, GDPR, audit, категории", "full"),
                ShellWireframeRow("Support", "Тикеты, FAQ, эскалации без санкций", "limited"),
            ),
            secondarySectionTitle = "Изменения",
            secondarySectionSubtitle = "Черновики изменений прав и подтверждение ревью.",
            secondaryRows = listOf(
                ShellWireframeRow("Добавить quarantine доступ", "Роль Moderator, ожидает ревью", "pending"),
                ShellWireframeRow("Ограничить Support", "Убраны санкции пользователей", "approved"),
            ),
            actionButtonLabel = "Переключить режим RBAC",
        ),
    )
}

@Composable
fun AdminModeratorAssignmentsShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-assignments",
            title = "Назначение модераторов",
            subtitle = "Каркас панели назначения модераторов по очередям, регионам и графикам смен.",
            primaryActionLabel = "Назначить модератора (заглушка)",
            segments = listOf("Кандидаты", "Назначения", "Нагрузка"),
            metricRows = listOf("Кандидатов" to "6", "Смен" to "14"),
            primarySectionTitle = "Кандидаты",
            primarySectionSubtitle = "Верифицированные пользователи/команды для модерации и саппорта.",
            primaryRows = listOf(
                ShellWireframeRow("Teiwaz_", "МО, опыт CQB и событий", "candidate"),
                ShellWireframeRow("Raven", "Очередь жалоб и карантин маркетплейса", "candidate"),
                ShellWireframeRow("Ghost", "Support inbox и FAQ", "support"),
            ),
            secondarySectionTitle = "Текущие назначения",
            secondarySectionSubtitle = "Кто за что отвечает сейчас: очереди и окна дежурств.",
            secondaryRows = listOf(
                ShellWireframeRow("Маркет quarantine / МО", "Raven 18:00-23:00", "active"),
                ShellWireframeRow("Chat monitor / Общий чат", "Teiwaz_ 20:00-00:00", "active"),
                ShellWireframeRow("Support inbox", "Ghost дневная смена", "active"),
            ),
            actionButtonLabel = "Переключить режим назначений",
        ),
    )
}

@Composable
fun AdminPoliciesShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-policies",
            title = "Политики",
            subtitle = "Каркас редактора политик: правила сообщества, маркет, privacy/GDPR, санкции и реклама.",
            primaryActionLabel = "Открыть редактор политики (заглушка)",
            segments = listOf("Документы", "Черновики", "Публикация"),
            metricRows = listOf("Документов" to "12", "Черновиков" to "2"),
            primarySectionTitle = "Активные документы",
            primarySectionSubtitle = "Опубликованные версии правил и политик с датой вступления в силу.",
            primaryRows = listOf(
                ShellWireframeRow("Правила сообщества", "v1.4, обновлено 12.02", "live"),
                ShellWireframeRow("Политика барахолки", "v2.1, anti-fraud блок", "live"),
                ShellWireframeRow("Privacy / GDPR", "v1.8, экспорт и удаление данных", "live"),
            ),
            secondarySectionTitle = "Черновики и ревью",
            secondarySectionSubtitle = "Изменения политик и согласование перед публикацией.",
            secondaryRows = listOf(
                ShellWireframeRow("Санкции за повторный спам", "Черновик, ожидает ревью", "draft"),
                ShellWireframeRow("Маркировка рекламы", "Черновик, правки редактора", "draft"),
            ),
            actionButtonLabel = "Переключить режим политик",
        ),
    )
}

@Composable
fun AdminMarketplaceCategoriesShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-marketplace-categories",
            title = "Категории барахолки",
            subtitle = "Управление категориями/подкатегориями marketplace, обязательными полями и фильтрами.",
            primaryActionLabel = "Создать категорию (заглушка)",
            segments = listOf("Категории", "Поля", "Миграции"),
            metricRows = listOf("Категорий" to "11", "Полей" to "38"),
            primarySectionTitle = "Каталог категорий",
            primarySectionSubtitle = "Структура барахолки и ключевые фильтры для поиска.",
            primaryRows = listOf(
                ShellWireframeRow("Приводы", "AEG/GBB, серийник, состояние", "gear"),
                ShellWireframeRow("Оптика", "Кратность, крепление, состояние", "optic"),
                ShellWireframeRow("Снаряга", "Размер, бренд, состояние", "gear"),
            ),
            secondarySectionTitle = "Изменения структуры",
            secondarySectionSubtitle = "Миграции схемы фильтров и совместимость старых объявлений.",
            secondaryRows = listOf(
                ShellWireframeRow("Добавить поле 'Серийный номер'", "Категория Приводы", "migration"),
                ShellWireframeRow("Слить подкатегории масок", "Снаряга -> Защита лица", "plan"),
            ),
            actionButtonLabel = "Переключить режим категорий",
        ),
    )
}

@Composable
fun AdminGdprRequestsShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-gdpr",
            title = "GDPR запросы",
            subtitle = "Каркас очереди GDPR: экспорт/удаление данных, верификация личности и журнал исполнения.",
            primaryActionLabel = "Открыть GDPR кейс (заглушка)",
            segments = listOf("Очередь", "Верификация", "Экспорт"),
            metricRows = listOf("Запросов" to "5", "Просрочено" to "1"),
            primarySectionTitle = "Очередь запросов",
            primarySectionSubtitle = "Экспорт данных, удаление аккаунта и ограничения обработки.",
            primaryRows = listOf(
                ShellWireframeRow("gdpr-001", "Экспорт данных пользователя Ghost", "export"),
                ShellWireframeRow("gdpr-002", "Удаление аккаунта Raven", "delete"),
                ShellWireframeRow("gdpr-003", "Ограничение персонализации", "privacy"),
            ),
            secondarySectionTitle = "Исполнение",
            secondarySectionSubtitle = "Проверка личности, формирование архива, подтверждение удаления.",
            secondaryRows = listOf(
                ShellWireframeRow("Верификация личности", "Почта / документы / 2FA", "verify"),
                ShellWireframeRow("Экспорт архивом", "Готовность и срок скачивания", "archive"),
                ShellWireframeRow("Подтверждение удаления", "Запись в audit log и уведомление", "done"),
            ),
            actionButtonLabel = "Переключить режим GDPR",
        ),
    )
}

@Composable
fun AdminAuditLogShellRoute() {
    OpsToolSkeletonRoute(
        spec = OpsToolSpec(
            id = "admin-audit-log",
            title = "Audit Log",
            subtitle = "Каркас журнала действий модераторов/админов с фильтрами и экспортом.",
            primaryActionLabel = "Открыть запись аудита (заглушка)",
            segments = listOf("Лента", "Фильтры", "Экспорт"),
            metricRows = listOf("Записей/сутки" to "320", "Критичных" to "9"),
            primarySectionTitle = "Последние действия",
            primarySectionSubtitle = "Санкции, карантин, RBAC, политики, GDPR и support эскалации.",
            primaryRows = listOf(
                ShellWireframeRow("audit-901", "Moderator Raven: hide listing-q-102", "mod"),
                ShellWireframeRow("audit-902", "Admin Teiwaz_: update RBAC matrix", "admin"),
                ShellWireframeRow("audit-903", "Support Ghost: escalate ticket-202", "support"),
            ),
            secondarySectionTitle = "Фильтры и экспорт",
            secondarySectionSubtitle = "По actor/entity/периоду + экспорт для расследований и отчётов.",
            secondaryRows = listOf(
                ShellWireframeRow("Actor filter", "Moderator / Admin / Support", "filter"),
                ShellWireframeRow("Entity filter", "User, Listing, Policy, GDPR, Report", "filter"),
                ShellWireframeRow("CSV/JSON export", "Выгрузка журнала по периоду", "export"),
            ),
            actionButtonLabel = "Переключить режим audit log",
        ),
    )
}
