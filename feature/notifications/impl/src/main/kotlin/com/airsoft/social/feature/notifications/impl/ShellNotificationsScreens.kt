package com.airsoft.social.feature.notifications.impl

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
import androidx.compose.ui.res.stringResource
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

data class NotificationsUiState(
    val selectedFilter: String = "Непрочитанные",
    val filters: List<String> = listOf("Непрочитанные", "Все", "Упоминания", "Приглашения", "Системные"),
    val notificationRows: List<ShellWireframeRow> = listOf(
        ShellWireframeRow("Приглашение в команду", "EW Rangers пригласили вас на просмотр состава", "новое"),
        ShellWireframeRow("Обновление события", "Night Raid North изменил время старта", "1ч"),
        ShellWireframeRow("Ответ по барахолке", "Продавец ответил на предложение по M4A1", "3ч"),
        ShellWireframeRow("Системное уведомление", "Новая сборка shell доступна для тестирования", "dev"),
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
        title = "Уведомления",
        subtitle = "Каркас центра уведомлений для приглашений, упоминаний, барахолки и системных сообщений.",
        primaryActionLabel = "Отметить всё прочитанным (заглушка)",
    ) {
        WireframeSection(
            title = "Фильтры",
            subtitle = "Выбор категории уведомлений.",
        ) {
            WireframeChipRow(
                labels = uiState.filters.map { filter ->
                    if (filter == uiState.selectedFilter) "[$filter]" else filter
                },
            )
        }
        WireframeSection(
            title = "Входящие",
            subtitle = "Заглушка единой ленты уведомлений.",
        ) {
            uiState.notificationRows.forEach { row -> WireframeItemRow(row.title, row.subtitle, row.trailing) }
        }
        WireframeSection(
            title = "Демо-действие",
            subtitle = "Переключение фильтра для проверки ViewModel/action wiring.",
        ) {
            Button(
                onClick = {
                    val next = if (uiState.selectedFilter == "Непрочитанные") "Все" else "Непрочитанные"
                    onAction(NotificationsAction.SelectFilter(next))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Переключить фильтр")
            }
        }
    }
}

