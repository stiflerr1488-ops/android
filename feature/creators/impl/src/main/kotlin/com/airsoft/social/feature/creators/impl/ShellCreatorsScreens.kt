package com.airsoft.social.feature.creators.impl

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

data class CreatorsUiState(
    val selectedSegment: String = "Каталог",
    val segments: List<String> = listOf("Каталог", "Заявки", "Партнёры"),
    val featuredRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Teiwaz_", "Страйкбольные обзоры, CQB / лес, Москва", "creator"),
        ShellWireframeRow("North Raid Media", "Съёмка игр и монтаж aftermovie", "studio"),
    ),
    val requestRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Заявка: медиа-покрытие Night Raid", "Ожидает ответа 2 создателей", "open"),
        ShellWireframeRow("Заявка: фото команды [EW]", "Обсуждение бюджета и слотов", "chat"),
    ),
)

sealed interface CreatorsAction {
    data object CycleSegment : CreatorsAction
    data object OpenCreatorDetailClicked : CreatorsAction
    data object OpenCreatorStudioClicked : CreatorsAction
}

class CreatorsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreatorsUiState())
    val uiState: StateFlow<CreatorsUiState> = _uiState.asStateFlow()

    fun onAction(action: CreatorsAction) {
        when (action) {
            CreatorsAction.CycleSegment -> {
                val current = _uiState.value
                val idx = current.segments.indexOf(current.selectedSegment)
                val next = current.segments[(idx + 1).mod(current.segments.size)]
                _uiState.value = current.copy(selectedSegment = next)
            }

            CreatorsAction.OpenCreatorDetailClicked,
            CreatorsAction.OpenCreatorStudioClicked,
            -> Unit
        }
    }
}

@Composable
fun CreatorsShellRoute(
    onOpenCreatorDetail: () -> Unit = {},
    onOpenCreatorStudio: () -> Unit = {},
    creatorsViewModel: CreatorsViewModel = viewModel(),
) {
    val uiState by creatorsViewModel.uiState.collectAsState()
    CreatorsShellScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                CreatorsAction.OpenCreatorDetailClicked -> onOpenCreatorDetail()
                CreatorsAction.OpenCreatorStudioClicked -> onOpenCreatorStudio()
                else -> creatorsViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun CreatorsShellScreen(
    uiState: CreatorsUiState,
    onAction: (CreatorsAction) -> Unit,
) {
    WireframePage(
        title = "Создатели",
        subtitle = "Каркас каталога создателей контента: профили, заявки на съёмку и партнёрские форматы.",
        primaryActionLabel = "Создать заявку для создателя (заглушка)",
    ) {
        WireframeSection(
            title = "Сегмент",
            subtitle = "Переключение между каталогом, заявками и партнёрскими сценариями.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Режим" to uiState.selectedSegment,
                    "Профилей" to uiState.featuredRows.size.toString(),
                    "Заявок" to uiState.requestRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.segments.map { if (it == uiState.selectedSegment) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Рекомендуемые создатели",
            subtitle = "Каталог creator-профилей с кратким описанием специализации.",
        ) {
            uiState.featuredRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Активные заявки",
            subtitle = "Запросы на фото/видео/обзоры и переговоры по сотрудничеству.",
        ) {
            uiState.requestRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Переходы",
            subtitle = "Проверка маршрутов деталей создателя и студии/медиа-кита.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(CreatorsAction.CycleSegment) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сменить сегмент") }
                OutlinedButton(
                    onClick = { onAction(CreatorsAction.OpenCreatorDetailClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть профиль создателя") }
                OutlinedButton(
                    onClick = { onAction(CreatorsAction.OpenCreatorStudioClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Открыть студию / медиа-кит") }
            }
        }
    }
}

data class CreatorDetailUiState(
    val creatorId: String = "",
    val selectedSection: String = "Профиль",
    val sections: List<String> = listOf("Профиль", "Портфолио", "Условия"),
    val profileRows: List<ShellWireframeRow> = emptyList(),
    val portfolioRows: List<ShellWireframeRow> = emptyList(),
    val termsRows: List<ShellWireframeRow> = emptyList(),
)

sealed interface CreatorDetailAction {
    data object CycleSection : CreatorDetailAction
}

class CreatorDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreatorDetailUiState())
    val uiState: StateFlow<CreatorDetailUiState> = _uiState.asStateFlow()

    fun load(creatorId: String) {
        if (_uiState.value.creatorId == creatorId) return
        _uiState.value = CreatorDetailUiState(
            creatorId = creatorId,
            profileRows = listOf(
                ShellWireframeRow("Позывной / бренд", "Teiwaz_", "creator"),
                ShellWireframeRow("Специализация", "Обзоры, игровые репортажи, CQB/лес", "media"),
                ShellWireframeRow("География", "Москва, МО, выезд по договорённости", "geo"),
            ),
            portfolioRows = listOf(
                ShellWireframeRow("Aftermovie Night Raid", "Видео 12 мин, ночная игра", "video"),
                ShellWireframeRow("Обзор AEG setup", "Короткий vertical-ролик / shorts", "short"),
                ShellWireframeRow("Фотоотчёт турнира", "Галерея 180 кадров", "photo"),
            ),
            termsRows = listOf(
                ShellWireframeRow("Формат работы", "Почасово или пакет на событие", "work"),
                ShellWireframeRow("Срок отдачи", "Тизер 24ч, полный монтаж 3-7 дней", "sla"),
                ShellWireframeRow("Бронирование", "Через заявку и подтверждение слота", "booking"),
            ),
        )
    }

    fun onAction(action: CreatorDetailAction) {
        when (action) {
            CreatorDetailAction.CycleSection -> {
                val current = _uiState.value
                val idx = current.sections.indexOf(current.selectedSection)
                val next = current.sections[(idx + 1).mod(current.sections.size)]
                _uiState.value = current.copy(selectedSection = next)
            }
        }
    }
}

@Composable
fun CreatorDetailSkeletonRoute(
    creatorId: String,
    creatorDetailViewModel: CreatorDetailViewModel = viewModel(),
) {
    LaunchedEffect(creatorId) { creatorDetailViewModel.load(creatorId) }
    val uiState by creatorDetailViewModel.uiState.collectAsState()
    WireframePage(
        title = "Профиль создателя",
        subtitle = "Карточка создателя контента. ID: ${uiState.creatorId}",
        primaryActionLabel = "Связаться / создать заявку (заглушка)",
    ) {
        WireframeSection(title = "Разделы", subtitle = "Профиль, портфолио и условия работы.") {
            WireframeMetricRow(
                items = listOf(
                    "Раздел" to uiState.selectedSection,
                    "Работы" to uiState.portfolioRows.size.toString(),
                    "Условия" to uiState.termsRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.sections.map { if (it == uiState.selectedSection) "[$it]" else it },
            )
        }
        WireframeSection(title = "Профиль", subtitle = "Основные данные и специализация.") {
            uiState.profileRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Портфолио", subtitle = "Ключевые работы и форматы контента.") {
            uiState.portfolioRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Условия", subtitle = "Формат сотрудничества и SLA.") {
            uiState.termsRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring карточки создателя.") {
            Button(
                onClick = { creatorDetailViewModel.onAction(CreatorDetailAction.CycleSection) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Переключить раздел профиля") }
        }
    }
}

data class CreatorStudioUiState(
    val creatorId: String = "",
    val studioRows: List<ShellWireframeRow> = emptyList(),
    val packageRows: List<ShellWireframeRow> = emptyList(),
)

class CreatorStudioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreatorStudioUiState())
    val uiState: StateFlow<CreatorStudioUiState> = _uiState.asStateFlow()

    fun load(creatorId: String) {
        if (_uiState.value.creatorId == creatorId) return
        _uiState.value = CreatorStudioUiState(
            creatorId = creatorId,
            studioRows = listOf(
                ShellWireframeRow("Медиа-кит", "Лого, ссылки, контакты, портфолио-пакет", "kit"),
                ShellWireframeRow("Оборудование", "Камеры, стаб, звук, экшн-камеры", "gear"),
                ShellWireframeRow("Команда", "1 оператор + монтаж / ассистент по запросу", "crew"),
            ),
            packageRows = listOf(
                ShellWireframeRow("Тизер + aftermovie", "Съёмка события + монтаж 2 роликов", "package"),
                ShellWireframeRow("Фотоотчёт", "Съёмка + обработка 100-200 фото", "photo"),
            ),
        )
    }
}

@Composable
fun CreatorStudioSkeletonRoute(
    creatorId: String,
    creatorStudioViewModel: CreatorStudioViewModel = viewModel(),
) {
    LaunchedEffect(creatorId) { creatorStudioViewModel.load(creatorId) }
    val uiState by creatorStudioViewModel.uiState.collectAsState()
    WireframePage(
        title = "Студия / медиа-кит",
        subtitle = "Рабочий профиль создателя и пакетные предложения. ID: ${uiState.creatorId}",
        primaryActionLabel = "Отправить бриф (заглушка)",
    ) {
        WireframeSection(title = "Профиль студии", subtitle = "Оборудование, команда и медиаматериалы.") {
            uiState.studioRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(title = "Пакеты услуг", subtitle = "Типовые пакеты съёмки и контента.") {
            uiState.packageRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
    }
}

