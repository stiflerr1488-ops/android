package com.airsoft.social.feature.support.impl

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

data class SupportUiState(
    val selectedTopic: String = "Аккаунт",
    val topics: List<String> = listOf("Аккаунт", "Команды", "События", "Барахолка", "Радар"),
    val channelRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Тикет в приложении", "Структурированная форма обращения с вложениями", "план"),
        ShellWireframeRow("Жалоба модерации", "Жалоба на пользователя/команду/объявление/событие", "план"),
        ShellWireframeRow("FAQ и гайды", "Статические материалы и сценарии решения проблем", "каркас"),
    ),
    val faqRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Как вступить в команду?", "Инвайт / заявка / подтверждение ролей"),
        ShellWireframeRow("Как создать событие?", "Страница организатора + лимит состава + поля"),
        ShellWireframeRow("Как войти в режим радара?", "Нажмите «В БОЙ!» и перейдите в режим радара"),
    ),
)

sealed interface SupportAction {
    data class SelectTopic(val topic: String) : SupportAction
    data object OpenNotificationsCenterClicked : SupportAction
    data object OpenTicketsClicked : SupportAction
    data object OpenSupportChatClicked : SupportAction
    data object OpenFaqClicked : SupportAction
    data object OpenTicketDetailClicked : SupportAction
}

class SupportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun onAction(action: SupportAction) {
        when (action) {
            is SupportAction.SelectTopic -> _uiState.value = _uiState.value.copy(selectedTopic = action.topic)
            SupportAction.OpenNotificationsCenterClicked,
            SupportAction.OpenTicketsClicked,
            SupportAction.OpenSupportChatClicked,
            SupportAction.OpenFaqClicked,
            SupportAction.OpenTicketDetailClicked,
            -> Unit
        }
    }
}

@Composable
fun SupportShellRoute(
    onOpenNotifications: () -> Unit = {},
    onOpenTickets: () -> Unit = {},
    onOpenSupportChat: () -> Unit = {},
    onOpenFaq: () -> Unit = {},
    onOpenTicketDetail: () -> Unit = {},
    supportViewModel: SupportViewModel = viewModel(),
) {
    val uiState by supportViewModel.uiState.collectAsState()
    SupportShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                SupportAction.OpenNotificationsCenterClicked -> onOpenNotifications()
                SupportAction.OpenTicketsClicked -> onOpenTickets()
                SupportAction.OpenSupportChatClicked -> onOpenSupportChat()
                SupportAction.OpenFaqClicked -> onOpenFaq()
                SupportAction.OpenTicketDetailClicked -> onOpenTicketDetail()
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
        title = "Поддержка",
        subtitle = "Каркас центра поддержки: темы, тикеты, жалобы модерации и FAQ.",
        primaryActionLabel = "Создать тикет (заглушка)",
    ) {
        WireframeSection(
            title = "Темы",
            subtitle = "Пользователь выбирает область проблемы перед созданием тикета.",
        ) {
            WireframeChipRow(
                labels = uiState.topics.map { topic ->
                    if (topic == uiState.selectedTopic) "[$topic]" else topic
                },
            )
        }
        WireframeSection(
            title = "Каналы поддержки",
            subtitle = "Здесь будут жить тикеты, жалобы и запросы помощи.",
        ) {
            uiState.channelRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "FAQ / Решение проблем",
            subtitle = "Статическая база знаний и пошаговые подсказки.",
        ) {
            uiState.faqRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Действия",
            subtitle = "Заглушки для проверки wiring в shell.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val next = if (uiState.selectedTopic == "Радар") "Команды" else "Радар"
                        onAction(SupportAction.SelectTopic(next))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Переключить тему") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenNotificationsCenterClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть центр уведомлений") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenTicketsClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть тикеты поддержки") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenSupportChatClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть чат поддержки") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenFaqClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть FAQ") }
                OutlinedButton(
                    onClick = { onAction(SupportAction.OpenTicketDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть пример тикета") }
            }
        }
    }
}

data class SupportTicketsUiState(
    val selectedQueue: String = "Мои",
    val queues: List<String> = listOf("Мои", "Открытые", "Ожидают ответ", "Закрытые"),
    val ticketRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("ticket-201", "Не могу войти в радар после обновления", "high"),
        ShellWireframeRow("ticket-202", "Жалоба на продавца / барахолка", "market"),
        ShellWireframeRow("ticket-203", "Синхронизация календаря и напоминаний", "faq"),
    ),
)

sealed interface SupportTicketsAction {
    data object CycleQueue : SupportTicketsAction
}

class SupportTicketsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportTicketsUiState())
    val uiState: StateFlow<SupportTicketsUiState> = _uiState.asStateFlow()

    fun onAction(action: SupportTicketsAction) {
        when (action) {
            SupportTicketsAction.CycleQueue -> {
                val current = _uiState.value
                val idx = current.queues.indexOf(current.selectedQueue)
                val next = current.queues[(idx + 1).mod(current.queues.size)]
                _uiState.value = current.copy(selectedQueue = next)
            }
        }
    }
}

@Composable
fun SupportTicketsShellRoute(
    onOpenTicketDetail: () -> Unit = {},
    supportTicketsViewModel: SupportTicketsViewModel = viewModel(),
) {
    val uiState by supportTicketsViewModel.uiState.collectAsState()
    WireframePage(
        title = "Тикеты поддержки",
        subtitle = "Каркас очереди тикетов пользователя: фильтры статусов, приоритеты и история переписки.",
        primaryActionLabel = "Создать тикет (заглушка)",
    ) {
        WireframeSection(title = "Очередь", subtitle = "Мои тикеты и фильтры по статусу.") {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.selectedQueue,
                    "Тикетов" to uiState.ticketRows.size.toString(),
                    "Просрочено" to "1",
                ),
            )
            WireframeChipRow(
                labels = uiState.queues.map { if (it == uiState.selectedQueue) "[$it]" else it },
            )
        }
        WireframeSection(title = "Список тикетов", subtitle = "Обращения по аккаунту, маркету, радару и календарю.") {
            uiState.ticketRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка перехода в карточку тикета.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { supportTicketsViewModel.onAction(SupportTicketsAction.CycleQueue) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить фильтр тикетов") }
                OutlinedButton(onClick = onOpenTicketDetail, modifier = Modifier.fillMaxWidth()) {
                    Text("Открыть первый тикет")
                }
            }
        }
    }
}

data class SupportTicketDetailUiState(
    val ticketId: String = "",
    val selectedTab: String = "Сводка",
    val tabs: List<String> = listOf("Сводка", "Диалог", "SLA"),
    val summaryRows: List<ShellWireframeRow> = emptyList(),
    val chatRows: List<ShellWireframeRow> = emptyList(),
    val slaRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface SupportTicketDetailAction {
    data object CycleTab : SupportTicketDetailAction
}

class SupportTicketDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportTicketDetailUiState())
    val uiState: StateFlow<SupportTicketDetailUiState> = _uiState.asStateFlow()

    fun load(ticketId: String) {
        if (_uiState.value.ticketId == ticketId) return
        _uiState.value = SupportTicketDetailUiState(
            ticketId = ticketId,
            summaryRows = listOf(
                ShellWireframeRow("Тема", "Радар / вход в командный режим", "radar"),
                ShellWireframeRow("Статус", "В работе", "active"),
                ShellWireframeRow("Приоритет", "Высокий", "high"),
            ),
            chatRows = listOf(
                ShellWireframeRow("Пользователь", "После обновления не входит в радар", "msg"),
                ShellWireframeRow("Support", "Запросили модель устройства и логи", "reply"),
                ShellWireframeRow("Пользователь", "Отправил скрин и описание шагов", "msg"),
            ),
            slaRows = listOf(
                ShellWireframeRow("Создан", "Сегодня 02:13", "time"),
                ShellWireframeRow("SLA ответа", "До 04:13", "2ч"),
                ShellWireframeRow("Следующий шаг", "Эскалация в техподдержку при повторе", "next"),
            ),
        )
    }

    fun onAction(action: SupportTicketDetailAction) {
        when (action) {
            SupportTicketDetailAction.CycleTab -> {
                val current = _uiState.value
                val idx = current.tabs.indexOf(current.selectedTab)
                val next = current.tabs[(idx + 1).mod(current.tabs.size)]
                _uiState.value = current.copy(selectedTab = next)
            }
        }
    }
}

@Composable
fun SupportTicketDetailSkeletonRoute(
    ticketId: String,
    supportTicketDetailViewModel: SupportTicketDetailViewModel = viewModel(),
) {
    LaunchedEffect(ticketId) { supportTicketDetailViewModel.load(ticketId) }
    val uiState by supportTicketDetailViewModel.uiState.collectAsState()
    WireframePage(
        title = "Тикет поддержки",
        subtitle = "Карточка тикета поддержки. ID: ${uiState.ticketId}",
        primaryActionLabel = "Ответить в тикет (заглушка)",
    ) {
        WireframeSection(title = "Вкладки", subtitle = "Сводка, диалог и SLA тикета.") {
            WireframeMetricRow(
                items = listOf(
                    "Вкладка" to uiState.selectedTab,
                    "Сообщений" to uiState.chatRows.size.toString(),
                    "SLA" to "2ч",
                ),
            )
            WireframeChipRow(
                labels = uiState.tabs.map { if (it == uiState.selectedTab) "[$it]" else it },
            )
        }
        WireframeSection(title = "Сводка", subtitle = "Тема, статус и приоритет обращения.") {
            uiState.summaryRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Диалог", subtitle = "Лента переписки пользователя и поддержки.") {
            uiState.chatRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "SLA", subtitle = "Сроки, таймеры и шаги эскалации.") {
            uiState.slaRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring карточки тикета.") {
            Button(
                onClick = { supportTicketDetailViewModel.onAction(SupportTicketDetailAction.CycleTab) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить вкладку тикета") }
        }
    }
}

data class SupportChatUiState(
    val selectedMode: String = "Оператор",
    val modes: List<String> = listOf("Оператор", "Бот", "История"),
    val chatRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Пользователь", "Нужна помощь с режимом радара и входом в команду", "msg"),
        ShellWireframeRow("Support", "Проверьте доступ к гео и кнопку «В БОЙ!»", "reply"),
        ShellWireframeRow("Пользователь", "После этого открылся экран входа в команду", "ok"),
    ),
)

sealed interface SupportChatAction {
    data object CycleMode : SupportChatAction
}

class SupportChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportChatUiState())
    val uiState: StateFlow<SupportChatUiState> = _uiState.asStateFlow()

    fun onAction(action: SupportChatAction) {
        when (action) {
            SupportChatAction.CycleMode -> {
                val current = _uiState.value
                val idx = current.modes.indexOf(current.selectedMode)
                val next = current.modes[(idx + 1).mod(current.modes.size)]
                _uiState.value = current.copy(selectedMode = next)
            }
        }
    }
}

@Composable
fun SupportChatShellRoute(
    supportChatViewModel: SupportChatViewModel = viewModel(),
) {
    val uiState by supportChatViewModel.uiState.collectAsState()
    WireframePage(
        title = "Чат поддержки",
        subtitle = "Каркас live-чата поддержки: оператор, бот-ответы и история диалога.",
        primaryActionLabel = "Отправить сообщение (заглушка)",
    ) {
        WireframeSection(title = "Режим", subtitle = "Оператор / бот / история коммуникации.") {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.selectedMode,
                    "Сообщений" to uiState.chatRows.size.toString(),
                    "SLA" to "Онлайн",
                ),
            )
            WireframeChipRow(
                labels = uiState.modes.map { if (it == uiState.selectedMode) "[$it]" else it },
            )
        }
        WireframeSection(title = "Диалог", subtitle = "Скелет сообщений чата поддержки.") {
            uiState.chatRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring чата поддержки.") {
            Button(
                onClick = { supportChatViewModel.onAction(SupportChatAction.CycleMode) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить режим чата") }
        }
    }
}

data class SupportFaqUiState(
    val selectedCategory: String = "Старт",
    val categories: List<String> = listOf("Старт", "Радар", "Команды", "Маркет", "Календарь"),
    val articleRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Как начать пользоваться Airsoft Social?", "Вход, гость, разделы, профиль", "guide"),
        ShellWireframeRow("Как перейти в режим радара?", "Кнопка «В БОЙ!» и вход в команду", "radar"),
        ShellWireframeRow("Как создать событие и включить напоминания?", "Календарь игр + sync", "events"),
    ),
)

sealed interface SupportFaqAction {
    data object CycleCategory : SupportFaqAction
}

class SupportFaqViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SupportFaqUiState())
    val uiState: StateFlow<SupportFaqUiState> = _uiState.asStateFlow()

    fun onAction(action: SupportFaqAction) {
        when (action) {
            SupportFaqAction.CycleCategory -> {
                val current = _uiState.value
                val idx = current.categories.indexOf(current.selectedCategory)
                val next = current.categories[(idx + 1).mod(current.categories.size)]
                _uiState.value = current.copy(selectedCategory = next)
            }
        }
    }
}

@Composable
fun SupportFaqShellRoute(
    supportFaqViewModel: SupportFaqViewModel = viewModel(),
) {
    val uiState by supportFaqViewModel.uiState.collectAsState()
    WireframePage(
        title = "FAQ",
        subtitle = "Каркас базы знаний: статьи по входу, радару, командам, маркету и календарю.",
        primaryActionLabel = "Открыть статью (заглушка)",
    ) {
        WireframeSection(title = "Категории", subtitle = "Контекстные разделы базы знаний.") {
            WireframeMetricRow(
                items = listOf(
                    "Категория" to uiState.selectedCategory,
                    "Статей" to uiState.articleRows.size.toString(),
                    "Режим" to "FAQ",
                ),
            )
            WireframeChipRow(
                labels = uiState.categories.map { if (it == uiState.selectedCategory) "[$it]" else it },
            )
        }
        WireframeSection(title = "Статьи", subtitle = "Быстрые инструкции и сценарии решения проблем.") {
            uiState.articleRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring FAQ.") {
            Button(
                onClick = { supportFaqViewModel.onAction(SupportFaqAction.CycleCategory) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить категорию FAQ") }
        }
    }
}

data class AboutUiState(
    val buildChannel: String = "Dev",
    val versionRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Новый shell", "Новый модульный shell за feature flag", "step1"),
        ShellWireframeRow("Legacy tactical", "Bridge-запуск сохранён в app-модуле", "active"),
        ShellWireframeRow("Режим backend", "Port-first, Firebase adapters временные", "hybrid"),
    ),
    val moduleRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("core:*", "Порты, модели, datastore, ui, network, database"),
        ShellWireframeRow("feature:*", "Экраны, nav-контракты, placeholder-флоу"),
        ShellWireframeRow("infra:firebase", "Временные адаптеры для auth/realtime/telemetry"),
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
        title = "О приложении",
        subtitle = "Каркас страницы с информацией о сборке, архитектуре и версиях.",
        primaryActionLabel = "Сменить канал",
        onPrimaryAction = { onAction(AboutAction.ToggleBuildChannel) },
    ) {
        WireframeSection(
            title = "Информация о сборке",
            subtitle = "Заглушки версии, сборки и канала для release management.",
        ) {
            WireframeItemRow("Пакет", "Текущий applicationId сохранён во время strangler-миграции")
            WireframeItemRow("Канал", "Заглушка Dev / QA / Prod", uiState.buildChannel)
            uiState.versionRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Карта модулей",
            subtitle = "Краткая архитектурная сводка для внутренних тестеров.",
        ) {
            uiState.moduleRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Документы",
            subtitle = "Политика, правила, контакты поддержки, лицензии.",
        ) {
            WireframeChipRow(
                labels = listOf("Политика", "Правила", "OSS-лицензии", "Заметки к релизу", "Roadmap"),
            )
        }
    }
}

