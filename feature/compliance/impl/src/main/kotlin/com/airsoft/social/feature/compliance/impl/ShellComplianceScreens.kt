package com.airsoft.social.feature.compliance.impl

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

data class RulesComplianceUiState(
    val selectedGroup: String = "Безопасность",
    val groups: List<String> = listOf("Безопасность", "Контент", "Приватность"),
    val summaryRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Report / Block / Hide", "Жалобы и скрытие контента по всему приложению", "core"),
        ShellWireframeRow("Маркировка рекламы", "Теги «Реклама», «Партнёрство», disclosure", "ads"),
        ShellWireframeRow("Privacy by default", "Контакты скрыты, явное согласие на публикацию", "privacy"),
    ),
)

sealed interface RulesComplianceAction {
    data object CycleGroup : RulesComplianceAction
    data object OpenReportBlockHideClicked : RulesComplianceAction
    data object OpenAdvertisingLabelsClicked : RulesComplianceAction
    data object OpenAgeGateClicked : RulesComplianceAction
    data object OpenPrivacyDefaultsClicked : RulesComplianceAction
    data object OpenGdprRightsClicked : RulesComplianceAction
}

class RulesComplianceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RulesComplianceUiState())
    val uiState: StateFlow<RulesComplianceUiState> = _uiState.asStateFlow()

    fun onAction(action: RulesComplianceAction) {
        when (action) {
            RulesComplianceAction.CycleGroup -> {
                val current = _uiState.value
                val idx = current.groups.indexOf(current.selectedGroup)
                val next = current.groups[(idx + 1).mod(current.groups.size)]
                _uiState.value = current.copy(selectedGroup = next)
            }

            RulesComplianceAction.OpenReportBlockHideClicked,
            RulesComplianceAction.OpenAdvertisingLabelsClicked,
            RulesComplianceAction.OpenAgeGateClicked,
            RulesComplianceAction.OpenPrivacyDefaultsClicked,
            RulesComplianceAction.OpenGdprRightsClicked,
            -> Unit
        }
    }
}

@Composable
fun RulesComplianceShellRoute(
    onOpenReportBlockHide: () -> Unit = {},
    onOpenAdvertisingLabels: () -> Unit = {},
    onOpenAgeGate: () -> Unit = {},
    onOpenPrivacyDefaults: () -> Unit = {},
    onOpenGdprRights: () -> Unit = {},
    rulesComplianceViewModel: RulesComplianceViewModel = viewModel(),
) {
    val uiState by rulesComplianceViewModel.uiState.collectAsState()
    WireframePage(
        title = "Правила и безопасность",
        subtitle = "Каркас compliance-раздела: жалобы, маркировка рекламы, age-gate, приватность и права данных.",
        primaryActionLabel = "Открыть Report/Block/Hide (заглушка)",
    ) {
        WireframeSection(title = "Группа", subtitle = "Сценарии безопасности, контента и приватности.") {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedGroup,
                    "Блоков" to "5",
                    "Статус" to "wireframe",
                ),
            )
            WireframeChipRow(
                labels = uiState.groups.map { if (it == uiState.selectedGroup) "[$it]" else it },
            )
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Переходы", subtitle = "Проверка маршрутов compliance-подразделов из FULL.md.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { rulesComplianceViewModel.onAction(RulesComplianceAction.CycleGroup) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить группу") }
                OutlinedButton(onClick = onOpenReportBlockHide, modifier = Modifier.fillMaxWidth()) {
                    Text("Report / Block / Hide")
                }
                OutlinedButton(onClick = onOpenAdvertisingLabels, modifier = Modifier.fillMaxWidth()) {
                    Text("Маркировка рекламы")
                }
                OutlinedButton(onClick = onOpenAgeGate, modifier = Modifier.fillMaxWidth()) {
                    Text("Age-gate (18+)")
                }
                OutlinedButton(onClick = onOpenPrivacyDefaults, modifier = Modifier.fillMaxWidth()) {
                    Text("Privacy by default")
                }
                OutlinedButton(onClick = onOpenGdprRights, modifier = Modifier.fillMaxWidth()) {
                    Text("GDPR права пользователя")
                }
            }
        }
    }
}

data class ComplianceToolUiState(
    val toolId: String = "",
    val selectedTab: String = "Обзор",
    val tabs: List<String> = listOf("Обзор", "Правила", "Поток"),
    val metricRows: List<Pair<String, String>> = emptyList(),
    val mainRows: List<ShellWireframeRow> = emptyList(),
    val policyRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface ComplianceToolAction {
    data object CycleTab : ComplianceToolAction
}

data class ComplianceToolSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryActionLabel: String,
    val tabs: List<String>,
    val metricRows: List<Pair<String, String>>,
    val mainSectionTitle: String,
    val mainSectionSubtitle: String,
    val mainRows: List<ShellWireframeRow>,
    val policySectionTitle: String,
    val policySectionSubtitle: String,
    val policyRows: List<ShellWireframeRow>,
    val cycleButtonLabel: String,
)

class ComplianceToolViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ComplianceToolUiState())
    val uiState: StateFlow<ComplianceToolUiState> = _uiState.asStateFlow()

    fun load(spec: ComplianceToolSpec) {
        if (_uiState.value.toolId == spec.id) return
        _uiState.value = ComplianceToolUiState(
            toolId = spec.id,
            selectedTab = spec.tabs.firstOrNull() ?: "Обзор",
            tabs = spec.tabs,
            metricRows = spec.metricRows,
            mainRows = spec.mainRows,
            policyRows = spec.policyRows,
        )
    }

    fun onAction(action: ComplianceToolAction) {
        when (action) {
            ComplianceToolAction.CycleTab -> {
                val current = _uiState.value
                if (current.tabs.isEmpty()) return
                val idx = current.tabs.indexOf(current.selectedTab).coerceAtLeast(0)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
private fun ComplianceToolShellRoute(
    spec: ComplianceToolSpec,
    complianceToolViewModel: ComplianceToolViewModel = viewModel(),
) {
    LaunchedEffect(spec.id) { complianceToolViewModel.load(spec) }
    val uiState by complianceToolViewModel.uiState.collectAsState()
    WireframePage(
        title = spec.title,
        subtitle = spec.subtitle,
        primaryActionLabel = spec.primaryActionLabel,
    ) {
        WireframeSection(title = "Вкладки", subtitle = "Сводка, правила и сценарии применения.") {
            WireframeMetricRow(
                items = buildList {
                    add("Вкладка" to uiState.selectedTab)
                    addAll(uiState.metricRows)
                },
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(title = spec.mainSectionTitle, subtitle = spec.mainSectionSubtitle) {
            uiState.mainRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = spec.policySectionTitle, subtitle = spec.policySectionSubtitle) {
            uiState.policyRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring compliance-экрана.") {
            Button(
                onClick = { complianceToolViewModel.onAction(ComplianceToolAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(spec.cycleButtonLabel) }
        }
    }
}

@Composable
fun ComplianceReportBlockHideShellRoute() {
    ComplianceToolShellRoute(
        spec = ComplianceToolSpec(
            id = "compliance-rbh",
            title = "Report / Block / Hide",
            subtitle = "Каркас унифицированных действий безопасности для пользователей и контента во всех разделах.",
            primaryActionLabel = "Открыть форму жалобы (заглушка)",
            tabs = listOf("Действия", "Причины", "Поток"),
            metricRows = listOf("Причин" to "9", "Контекстов" to "6"),
            mainSectionTitle = "Действия пользователя",
            mainSectionSubtitle = "Пожаловаться, заблокировать пользователя, скрыть контент/тему.",
            mainRows = listOf(
                ShellWireframeRow("Report", "Жалоба на чат / объявление / профиль / событие", "report"),
                ShellWireframeRow("Block", "Блокировка пользователя и фильтрация его контента", "block"),
                ShellWireframeRow("Hide", "Скрытие карточки/ленты без жалобы", "hide"),
            ),
            policySectionTitle = "Причины и маршрутизация",
            policySectionSubtitle = "Категории причин и куда попадает кейс после отправки.",
            policyRows = listOf(
                ShellWireframeRow("Мошенничество", "Маркет quarantine + moderation queue", "market"),
                ShellWireframeRow("Оскорбления/спам", "Chat monitoring + reports queue", "chat"),
                ShellWireframeRow("Опасный контент", "Эскалация модератору/админу", "critical"),
            ),
            cycleButtonLabel = "Переключить вкладку RBH",
        ),
    )
}

@Composable
fun ComplianceAdvertisingLabelsShellRoute() {
    ComplianceToolShellRoute(
        spec = ComplianceToolSpec(
            id = "compliance-ads",
            title = "Маркировка рекламы",
            subtitle = "Каркас правил disclosure для рекламных материалов, партнёрств и промо-постов.",
            primaryActionLabel = "Открыть редактор маркировки (заглушка)",
            tabs = listOf("Метки", "Площадки", "Проверка"),
            metricRows = listOf("Маркеров" to "4", "Правил" to "7"),
            mainSectionTitle = "Метки disclosure",
            mainSectionSubtitle = "Видимые лейблы для рекламы, партнёрства и промо-материалов.",
            mainRows = listOf(
                ShellWireframeRow("Реклама", "Коммерческое размещение / промо-публикация", "ad"),
                ShellWireframeRow("Партнёрство", "Коллаборация с брендом/магазином/услугой", "partner"),
                ShellWireframeRow("Спонсорский контент", "Оплаченное размещение у создателей", "sponsored"),
            ),
            policySectionTitle = "Где применяется",
            policySectionSubtitle = "Лента, барахолка, создатели, объявления, карточки услуг/магазинов.",
            policyRows = listOf(
                ShellWireframeRow("Создатели", "Disclosure в карточке и публикациях", "creator"),
                ShellWireframeRow("Магазины/услуги", "Маркировка промо-блоков и баннеров", "directory"),
                ShellWireframeRow("Объявления", "Отдельный лейбл для партнёрских постов", "feed"),
            ),
            cycleButtonLabel = "Переключить вкладку маркировки",
        ),
    )
}

@Composable
fun ComplianceAgeGateShellRoute() {
    ComplianceToolShellRoute(
        spec = ComplianceToolSpec(
            id = "compliance-age-gate",
            title = "Age-gate (18+)",
            subtitle = "Каркас возрастных ограничений и экранов подтверждения возраста для чувствительного контента.",
            primaryActionLabel = "Открыть экран age-gate (заглушка)",
            tabs = listOf("Правила", "Контент", "Исключения"),
            metricRows = listOf("Сценариев" to "5", "Меток" to "3"),
            mainSectionTitle = "Контент с ограничениями",
            mainSectionSubtitle = "Типы контента, для которых требуется подтверждение возраста.",
            mainRows = listOf(
                ShellWireframeRow("Чувствительный контент", "Жёсткие кадры травм/последствий", "18+"),
                ShellWireframeRow("Коммерческие материалы", "Отдельные категории с возрастной маркировкой", "check"),
                ShellWireframeRow("Переходы по внешним ссылкам", "Предупреждение перед открытием", "warn"),
            ),
            policySectionTitle = "Механика age-gate",
            policySectionSubtitle = "Экран подтверждения, выбор региона/возраста, лог принятия.",
            policyRows = listOf(
                ShellWireframeRow("Экран предупреждения", "Сообщение + подтверждение 18+", "gate"),
                ShellWireframeRow("Настройки профиля", "Переиспользование age preferences", "profile"),
                ShellWireframeRow("Audit", "Запись показа и подтверждения age-gate", "audit"),
            ),
            cycleButtonLabel = "Переключить вкладку age-gate",
        ),
    )
}

@Composable
fun CompliancePrivacyDefaultsShellRoute() {
    ComplianceToolShellRoute(
        spec = ComplianceToolSpec(
            id = "compliance-privacy-defaults",
            title = "Privacy by default",
            subtitle = "Каркас приватных настроек по умолчанию: контакты, видимость профиля и безопасные дефолты.",
            primaryActionLabel = "Открыть настройки приватности (заглушка)",
            tabs = listOf("Дефолты", "Поля", "Исключения"),
            metricRows = listOf("Дефолтов" to "8", "Полей" to "14"),
            mainSectionTitle = "Безопасные значения по умолчанию",
            mainSectionSubtitle = "Что скрыто до явного согласия пользователя.",
            mainRows = listOf(
                ShellWireframeRow("Контакты", "Скрыты до явного разрешения владельца", "hidden"),
                ShellWireframeRow("Геолокация", "Только общий регион, без точной точки", "region"),
                ShellWireframeRow("Участие в командах", "Показывается только базовый статус", "limited"),
            ),
            policySectionTitle = "Управление видимостью",
            policySectionSubtitle = "Связь с профилем, командами, барахолкой и событиями.",
            policyRows = listOf(
                ShellWireframeRow("Профиль", "Публичный / только команда / скрытый", "profile"),
                ShellWireframeRow("Маркет", "Контакты продавца через безопасный чат", "market"),
                ShellWireframeRow("События", "RSVP видимость и роль в составе", "events"),
            ),
            cycleButtonLabel = "Переключить вкладку privacy",
        ),
    )
}

@Composable
fun ComplianceGdprRightsShellRoute() {
    ComplianceToolShellRoute(
        spec = ComplianceToolSpec(
            id = "compliance-gdpr-rights",
            title = "GDPR права пользователя",
            subtitle = "Каркас пользовательского центра прав данных: экспорт, удаление, согласия и ограничения обработки.",
            primaryActionLabel = "Создать GDPR запрос (заглушка)",
            tabs = listOf("Права", "Запросы", "Статусы"),
            metricRows = listOf("Типов запросов" to "4", "SLA" to "30д"),
            mainSectionTitle = "Доступные права",
            mainSectionSubtitle = "Какие запросы пользователь может отправить из интерфейса.",
            mainRows = listOf(
                ShellWireframeRow("Экспорт данных", "Скачать архив профиля/чатов/активности", "export"),
                ShellWireframeRow("Удаление аккаунта", "Запрос удаления данных и деактивация", "delete"),
                ShellWireframeRow("Ограничение обработки", "Отключение отдельных сценариев персонализации", "privacy"),
            ),
            policySectionTitle = "Статусы и подтверждения",
            policySectionSubtitle = "Верификация личности, подтверждение запроса и журнал исполнения.",
            policyRows = listOf(
                ShellWireframeRow("Подтверждение личности", "Почта/2FA/доп. проверка", "verify"),
                ShellWireframeRow("Статус запроса", "В работе / выполнен / требуется уточнение", "status"),
                ShellWireframeRow("История", "Лента предыдущих GDPR запросов", "history"),
            ),
            cycleButtonLabel = "Переключить вкладку GDPR прав",
        ),
    )
}
