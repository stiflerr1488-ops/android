package com.airsoft.social.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.ProfileRepository
import com.airsoft.social.core.data.SELF_USER_ID
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AchievementRow
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.GameHistoryRow
import com.airsoft.social.core.model.GearCategorySummary
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.TrustBadgeRow
import com.airsoft.social.core.model.User
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.profile.api.ProfileFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ProfileListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class TeamInfoRow(
    val teamName: String,
    val role: String,
    val status: String,
)

data class ProfileUiState(
    val currentUserId: String = SELF_USER_ID,
    val profileHeader: ProfileListRow? = null,
    val teamInfo: TeamInfoRow? = null,
    val gearCategories: List<GearCategorySummary> = emptyList(),
    val gameHistory: List<GameHistoryRow> = emptyList(),
    val achievements: List<AchievementRow> = emptyList(),
    val trustBadges: List<TrustBadgeRow> = emptyList(),
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = profileRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(ProfileUiState())
            } else {
                combine(
                    profileRepository.observeGearCategories(user.id),
                    profileRepository.observeGameHistory(user.id),
                    profileRepository.observeAchievements(user.id),
                    profileRepository.observeTrustBadges(user.id),
                ) { gear, history, achievements, trustBadges ->
                    user.toUiState(gear, history, achievements, trustBadges)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )
}

@Composable
fun ProfilePlaceholderScreen(
    onOpenEditProfile: (String) -> Unit,
    onOpenPrivacySettings: (String) -> Unit,
    onSignOut: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by profileViewModel.uiState.collectAsState()

    WireframePage(
        title = ProfileFeatureApi.contract.title,
        subtitle = ProfileFeatureApi.contract.subtitle,
    ) {
        WireframeSection(title = "Профиль") {
            uiState.profileHeader?.let { header ->
                WireframeItemRow(
                    title = header.title,
                    subtitle = header.subtitle,
                    trailing = header.trailing,
                )
            } ?: EmptyRow("Профиль пока не заполнен")
        }

        WireframeSection(title = "Команда") {
            uiState.teamInfo?.let { team ->
                WireframeItemRow(
                    title = team.teamName,
                    subtitle = "Роль: ${team.role}",
                    trailing = team.status,
                )
            } ?: EmptyRow("Команда не выбрана")
        }

        WireframeSection(title = "Снаряжение") {
            if (uiState.gearCategories.isEmpty()) {
                EmptyRow("Снаряжение не добавлено")
            } else {
                uiState.gearCategories.forEach { gear ->
                    WireframeItemRow(
                        title = gear.displayName,
                        subtitle = "Категория: ${gear.category}",
                        trailing = gear.count.toString(),
                    )
                }
            }
        }

        WireframeSection(title = "История игр") {
            if (uiState.gameHistory.isEmpty()) {
                EmptyRow("История игр пуста")
            } else {
                uiState.gameHistory.forEach { game ->
                    WireframeItemRow(
                        title = game.eventName,
                        subtitle = formatGameDate(game.date),
                    )
                }
            }
        }

        WireframeSection(title = "Достижения") {
            if (uiState.achievements.isEmpty()) {
                EmptyRow("Достижений пока нет")
            } else {
                uiState.achievements.forEach { row ->
                    WireframeItemRow(row.title, row.description)
                }
            }
        }

        WireframeSection(title = "Бейджи доверия") {
            if (uiState.trustBadges.isEmpty()) {
                EmptyRow("Бейджей доверия пока нет")
            } else {
                uiState.trustBadges.forEach { row ->
                    WireframeItemRow(row.title, row.description)
                }
            }
        }

        WireframeSection(title = "Действия") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOpenEditProfile(uiState.currentUserId) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Редактировать профиль") }
                OutlinedButton(
                    onClick = { onOpenPrivacySettings(uiState.currentUserId) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Настройки приватности") }
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Выйти") }
            }
        }
    }
}

private fun User.toUiState(
    gearCategories: List<GearCategorySummary>,
    gameHistory: List<GameHistoryRow>,
    achievements: List<AchievementRow>,
    trustBadges: List<TrustBadgeRow>,
): ProfileUiState = ProfileUiState(
    currentUserId = id,
    profileHeader = ProfileListRow(
        title = callsign,
        subtitle = listOfNotNull(teamName, region).joinToString(" | ").ifBlank { "Профиль заполнен частично" },
        trailing = if (isOnline) "Онлайн" else "Офлайн",
    ),
    teamInfo = teamName?.let {
        TeamInfoRow(
            teamName = it,
            role = roles.firstOrNull()?.name ?: "Игрок",
            status = if (isOnline) "Активен" else "Неактивен",
        )
    },
    gearCategories = gearCategories,
    gameHistory = gameHistory,
    achievements = achievements,
    trustBadges = trustBadges,
)

@Composable
private fun EmptyRow(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

data class EditProfileUiState(
    val userId: String = SELF_USER_ID,
    val callsign: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String? = null,
    val region: String? = null,
    val exitRadiusKm: Int? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isSaving: Boolean = false,
    val isGuest: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface EditProfileAction {
    data class CallsignChanged(val value: String) : EditProfileAction
    data class FirstNameChanged(val value: String) : EditProfileAction
    data class LastNameChanged(val value: String) : EditProfileAction
    data class BioChanged(val value: String) : EditProfileAction
    data class RegionChanged(val value: String) : EditProfileAction
    data class ExitRadiusChanged(val value: String) : EditProfileAction
    data class AvatarUrlChanged(val value: String) : EditProfileAction
    data class BannerUrlChanged(val value: String) : EditProfileAction
    data class ToggleShowPhone(val enabled: Boolean) : EditProfileAction
    data class ToggleShowEmail(val enabled: Boolean) : EditProfileAction
    data class ToggleShowTelegram(val enabled: Boolean) : EditProfileAction
    data object SaveChangesClicked : EditProfileAction
}

sealed interface EditProfileEffect {
    data object Saved : EditProfileEffect
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EditProfileEffect>()
    val effects = _effects.asSharedFlow()

    private var initialState = EditProfileUiState()

    fun load(userId: String) {
        viewModelScope.launch {
            val authState = sessionRepository.currentAuthState()
            val isGuest = (authState as? AuthState.SignedIn)?.session?.isGuest == true
            val targetUserId = userId.takeIf { it.isNotBlank() } ?: SELF_USER_ID
            val user = profileRepository.observeUser(targetUserId).first()
            val loaded = if (user != null) {
                EditProfileUiState(
                    userId = targetUserId,
                    callsign = user.callsign,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    bio = user.bio,
                    region = user.region,
                    exitRadiusKm = user.exitRadiusKm,
                    avatarUrl = user.avatarUrl,
                    bannerUrl = user.bannerUrl,
                    privacySettings = user.privacySettings,
                    isGuest = isGuest,
                )
            } else {
                EditProfileUiState(userId = targetUserId, isGuest = isGuest)
            }
            initialState = loaded
            _uiState.value = loaded
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.CallsignChanged -> _uiState.update { it.copy(callsign = action.value) }
            is EditProfileAction.FirstNameChanged -> _uiState.update { it.copy(firstName = action.value) }
            is EditProfileAction.LastNameChanged -> _uiState.update { it.copy(lastName = action.value) }
            is EditProfileAction.BioChanged -> _uiState.update { it.copy(bio = action.value.ifBlank { null }) }
            is EditProfileAction.RegionChanged -> _uiState.update { it.copy(region = action.value.ifBlank { null }) }
            is EditProfileAction.ExitRadiusChanged -> _uiState.update { it.copy(exitRadiusKm = action.value.toIntOrNull()) }
            is EditProfileAction.AvatarUrlChanged -> _uiState.update { it.copy(avatarUrl = action.value.ifBlank { null }) }
            is EditProfileAction.BannerUrlChanged -> _uiState.update { it.copy(bannerUrl = action.value.ifBlank { null }) }
            is EditProfileAction.ToggleShowPhone -> _uiState.update { it.copy(privacySettings = it.privacySettings.copy(showPhone = action.enabled)) }
            is EditProfileAction.ToggleShowEmail -> _uiState.update { it.copy(privacySettings = it.privacySettings.copy(showEmail = action.enabled)) }
            is EditProfileAction.ToggleShowTelegram -> _uiState.update { it.copy(privacySettings = it.privacySettings.copy(showTelegram = action.enabled)) }
            EditProfileAction.SaveChangesClicked -> save()
        }
    }

    fun resetToInitial() {
        _uiState.value = initialState
    }

    private fun save() {
        val state = _uiState.value
        if (state.isGuest) {
            _uiState.update { it.copy(errorMessage = "Гостевой аккаунт не может редактировать профиль") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = profileRepository.updateUserProfile(
                userId = state.userId,
                callsign = state.callsign,
                firstName = state.firstName,
                lastName = state.lastName,
                bio = state.bio,
                region = state.region,
                exitRadiusKm = state.exitRadiusKm,
                avatarUrl = state.avatarUrl,
                bannerUrl = state.bannerUrl,
                privacySettings = state.privacySettings,
            )
            when (result) {
                is AppResult.Success -> {
                    initialState = _uiState.value.copy(isSaving = false, errorMessage = null)
                    _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                    _effects.emit(EditProfileEffect.Saved)
                }
                is AppResult.Failure -> _uiState.update { it.copy(isSaving = false, errorMessage = "Не удалось сохранить профиль") }
            }
        }
    }
}

@Composable
fun EditProfileRoute(
    userId: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) { viewModel.load(userId) }
    LaunchedEffect(Unit) { viewModel.effects.collect { onSaved() } }
    val uiState by viewModel.uiState.collectAsState()
    WireframePage(
        title = "Редактирование профиля",
        subtitle = "Данные привязаны к текущему пользователю",
    ) {
        WireframeSection(title = "Основные данные") {
            OutlinedTextField(uiState.callsign, { viewModel.onAction(EditProfileAction.CallsignChanged(it)) }, label = { Text("Позывной") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.firstName, { viewModel.onAction(EditProfileAction.FirstNameChanged(it)) }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.lastName, { viewModel.onAction(EditProfileAction.LastNameChanged(it)) }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.bio.orEmpty(), { viewModel.onAction(EditProfileAction.BioChanged(it)) }, label = { Text("Био") }, modifier = Modifier.fillMaxWidth())
        }
        WireframeSection(title = "Регион") {
            OutlinedTextField(uiState.region.orEmpty(), { viewModel.onAction(EditProfileAction.RegionChanged(it)) }, label = { Text("Регион") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = uiState.exitRadiusKm?.toString().orEmpty(),
                onValueChange = { viewModel.onAction(EditProfileAction.ExitRadiusChanged(it)) },
                label = { Text("Радиус выезда (км)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        WireframeSection(title = "Фото") {
            OutlinedTextField(uiState.avatarUrl.orEmpty(), { viewModel.onAction(EditProfileAction.AvatarUrlChanged(it)) }, label = { Text("Avatar URL") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.bannerUrl.orEmpty(), { viewModel.onAction(EditProfileAction.BannerUrlChanged(it)) }, label = { Text("Banner URL") }, modifier = Modifier.fillMaxWidth())
        }
        WireframeSection(title = "Приватность") {
            PrivacyToggle("Показывать телефон", uiState.privacySettings.showPhone) { viewModel.onAction(EditProfileAction.ToggleShowPhone(it)) }
            PrivacyToggle("Показывать email", uiState.privacySettings.showEmail) { viewModel.onAction(EditProfileAction.ToggleShowEmail(it)) }
            PrivacyToggle("Показывать Telegram", uiState.privacySettings.showTelegram) { viewModel.onAction(EditProfileAction.ToggleShowTelegram(it)) }
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.onAction(EditProfileAction.SaveChangesClicked) }, modifier = Modifier.fillMaxWidth()) { Text(if (uiState.isSaving) "Сохранение..." else "Сохранить") }
            OutlinedButton(
                onClick = {
                    viewModel.resetToInitial()
                    onCancel()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Отмена") }
        }
    }
}

@Composable
private fun PrivacyToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider()
}

private fun formatGameDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("ru"))
    return formatter.format(Date(timestamp))
}
