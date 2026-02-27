package com.airsoft.social.feature.profile.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.ProfileRepository
import com.airsoft.social.core.model.GearCategory
import com.airsoft.social.core.model.GearCategorySummary
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.User
import com.airsoft.social.core.model.UserRole
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.profile.api.ProfileFeatureApi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class TeamInfoRow(
    val teamName: String,
    val role: String,
    val membersCount: Int,
    val status: String,
)

data class GameHistoryRow(
    val id: String,
    val date: Long,
    val eventName: String,
)

data class AchievementRow(
    val id: String,
    val title: String,
    val description: String,
)

data class TrustBadgeRow(
    val id: String,
    val title: String,
    val description: String,
)

data class ProfileUiState(
    val profileHeader: ProfileListRow = ProfileListRow(
        title = "Teiwaz_",
        subtitle = "[EW] EASY WINNER | Москва",
        trailing = "Онлайн",
    ),
    val teamInfo: TeamInfoRow? = TeamInfoRow(
        teamName = "[EW] EASY WINNER",
        role = "Командир",
        membersCount = 14,
        status = "Активна",
    ),
    val gearCategories: List<GearCategorySummary> = defaultGearCategorySummaries(),
    val gameHistory: List<GameHistoryRow> = defaultGameHistory(),
    val achievements: List<AchievementRow> = defaultAchievements(),
    val trustBadges: List<TrustBadgeRow> = defaultTrustBadges(),
)

sealed interface ProfileAction {
    data object OpenEditProfileDemoClicked : ProfileAction
    data object OpenInventoryDemoClicked : ProfileAction
    data object OpenAchievementsDemoClicked : ProfileAction
    data object OpenTrustBadgesDemoClicked : ProfileAction
    data object OpenPrivacySettingsClicked : ProfileAction
    data object SignOutClicked : ProfileAction
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeCurrentUser().collect { user ->
                if (user == null) return@collect
                val previous = _uiState.value
                _uiState.value = previous.copy(
                    profileHeader = ProfileListRow(
                        title = user.callsign,
                        subtitle = listOfNotNull(user.teamName, user.region).joinToString(" | "),
                        trailing = if (user.isOnline) "Онлайн" else "Офлайн",
                    ),
                    teamInfo = user.teamName?.let { teamName ->
                        TeamInfoRow(
                            teamName = teamName,
                            role = userPrimaryRoleLabel(user) ?: "Игрок",
                            membersCount = defaultTeamMemberCount(teamName),
                            status = if (user.isOnline) "Активна" else "Неактивна",
                        )
                    },
                )
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OpenEditProfileDemoClicked -> Unit
            ProfileAction.OpenInventoryDemoClicked -> Unit
            ProfileAction.OpenAchievementsDemoClicked -> Unit
            ProfileAction.OpenTrustBadgesDemoClicked -> Unit
            ProfileAction.OpenPrivacySettingsClicked -> Unit
            ProfileAction.SignOutClicked -> Unit
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProfilePlaceholderScreen(
    onOpenEditProfileDemo: () -> Unit = {},
    onOpenInventoryDemo: () -> Unit = {},
    onOpenAchievementsDemo: () -> Unit = {},
    onOpenTrustBadgesDemo: () -> Unit = {},
    onPrimaryAction: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by profileViewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileAction.OpenEditProfileDemoClicked -> onOpenEditProfileDemo()
                ProfileAction.OpenInventoryDemoClicked -> onOpenInventoryDemo()
                ProfileAction.OpenAchievementsDemoClicked -> onOpenAchievementsDemo()
                ProfileAction.OpenTrustBadgesDemoClicked -> onOpenTrustBadgesDemo()
                ProfileAction.OpenPrivacySettingsClicked -> onOpenEditProfileDemo()
                ProfileAction.SignOutClicked -> onPrimaryAction()
            }
            profileViewModel.onAction(action)
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
        subtitle = ProfileFeatureApi.contract.subtitle,
    ) {
        WireframeSection(
            title = "Профиль",
            subtitle = "Аватар, позывной, команда, регион и текущий статус",
        ) {
            WireframeItemRow(
                title = uiState.profileHeader.title,
                subtitle = uiState.profileHeader.subtitle,
                trailing = uiState.profileHeader.trailing,
            )
        }

        WireframeSection(
            title = "Команда",
            subtitle = "Показывается только текущая команда пользователя",
        ) {
            val team = uiState.teamInfo
            if (team != null) {
                WireframeItemRow(
                    title = team.teamName,
                    subtitle = "Роль: ${team.role} | ${team.membersCount} участников",
                    trailing = team.status,
                )
            } else {
                Text(
                    text = "Команда не выбрана",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        WireframeSection(
            title = "Снаряжение",
            subtitle = "Категории текущего набора экипировки",
        ) {
            GearCategoryGrid(uiState.gearCategories)
            TacticalPrimaryButton(
                label = "Добавить предмет",
                icon = Icons.Filled.Add,
                onClick = { onAction(ProfileAction.OpenInventoryDemoClicked) },
            )
        }

        WireframeSection(
            title = "История игр",
            subtitle = "Только дата и название события",
        ) {
            uiState.gameHistory.forEachIndexed { index, game ->
                GameHistoryItem(game)
                if (index < uiState.gameHistory.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        thickness = 1.dp,
                    )
                }
            }
        }

        WireframeSection(
            title = "Достижения",
            subtitle = "Игровые бейджи и подтверждённые достижения",
        ) {
            uiState.achievements.forEach { achievement ->
                WireframeItemRow(
                    title = achievement.title,
                    subtitle = achievement.description,
                )
            }
        }

        WireframeSection(
            title = "Бейджи доверия",
            subtitle = "Сигналы надёжности профиля и активности",
        ) {
            uiState.trustBadges.forEach { badge ->
                WireframeItemRow(
                    title = badge.title,
                    subtitle = badge.description,
                )
            }
        }

        WireframeSection(
            title = "Действия",
            subtitle = "Управление профилем и приватностью",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TacticalPrimaryButton(
                    label = "Редактировать профиль",
                    icon = Icons.Filled.Edit,
                    onClick = { onAction(ProfileAction.OpenEditProfileDemoClicked) },
                )
                TacticalOutlinedButton(
                    label = "Настройки приватности",
                    icon = Icons.Filled.Settings,
                    onClick = { onAction(ProfileAction.OpenPrivacySettingsClicked) },
                )
                TacticalOutlinedButton(
                    label = "Выйти",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = { onAction(ProfileAction.SignOutClicked) },
                )
            }
        }
    }
}

@Composable
private fun TacticalPrimaryButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = label)
    }
}

@Composable
private fun TacticalOutlinedButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = label)
    }
}

@Composable
private fun GearCategoryGrid(
    categories: List<GearCategorySummary>,
) {
    categories.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowItems.forEach { item ->
                GearCategoryCard(
                    item = item,
                    modifier = Modifier.weight(1f),
                )
            }
            if (rowItems.size == 1) {
                Column(modifier = Modifier.weight(1f)) {}
            }
        }
    }
}

@Composable
private fun GearCategoryCard(
    item: GearCategorySummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = gearCategoryIcon(item.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${item.displayName} (${item.count})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = item.category.name.lowercase().replace('_', ' '),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            thickness = 1.dp,
        )
    }
}

@Composable
private fun GameHistoryItem(
    game: GameHistoryRow,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = formatGameDate(game.date),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = game.eventName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

data class EditProfileUiState(
    val userId: String = "self",
    val callsign: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String? = null,
    val region: String? = null,
    val exitRadiusKm: Int? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val privacySettings: PrivacySettings = PrivacySettings(),
)

sealed interface EditProfileAction {
    data class CallsignChanged(val value: String) : EditProfileAction
    data class FirstNameChanged(val value: String) : EditProfileAction
    data class LastNameChanged(val value: String) : EditProfileAction
    data class BioChanged(val value: String) : EditProfileAction
    data class RegionChanged(val value: String) : EditProfileAction
    data class ExitRadiusChanged(val value: String) : EditProfileAction
    data object ToggleAvatarPlaceholder : EditProfileAction
    data object ToggleBannerPlaceholder : EditProfileAction
    data class ToggleShowPhone(val enabled: Boolean) : EditProfileAction
    data class ToggleShowEmail(val enabled: Boolean) : EditProfileAction
    data class ToggleShowTelegram(val enabled: Boolean) : EditProfileAction
    data class MessageVisibilityChanged(val mode: MessageVisibilityMode) : EditProfileAction
    data object SaveChangesClicked : EditProfileAction
    data object CancelClicked : EditProfileAction
}

enum class MessageVisibilityMode(val title: String) {
    ALL("Все"),
    CONTACTS("Контакты"),
    TEAM_ONLY("Только команды"),
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val user = profileRepository.observeUser(userId).first()
            _uiState.value = if (user != null) {
                EditProfileUiState(
                    userId = userId,
                    callsign = user.callsign,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    bio = user.bio,
                    region = user.region,
                    avatarUrl = user.avatarUrl,
                    bannerUrl = null,
                    privacySettings = user.privacySettings,
                )
            } else {
                EditProfileUiState(userId = userId)
            }
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.CallsignChanged -> {
                _uiState.value = _uiState.value.copy(callsign = action.value)
            }

            is EditProfileAction.FirstNameChanged -> {
                _uiState.value = _uiState.value.copy(firstName = action.value)
            }

            is EditProfileAction.LastNameChanged -> {
                _uiState.value = _uiState.value.copy(lastName = action.value)
            }

            is EditProfileAction.BioChanged -> {
                _uiState.value = _uiState.value.copy(bio = action.value.ifBlank { null })
            }

            is EditProfileAction.RegionChanged -> {
                _uiState.value = _uiState.value.copy(region = action.value.ifBlank { null })
            }

            is EditProfileAction.ExitRadiusChanged -> {
                _uiState.value = _uiState.value.copy(
                    exitRadiusKm = action.value.toIntOrNull(),
                )
            }

            EditProfileAction.ToggleAvatarPlaceholder -> {
                _uiState.value = _uiState.value.copy(
                    avatarUrl = if (_uiState.value.avatarUrl == null) {
                        "https://airsoft.social/profile/avatar-placeholder.jpg"
                    } else {
                        null
                    },
                )
            }

            EditProfileAction.ToggleBannerPlaceholder -> {
                _uiState.value = _uiState.value.copy(
                    bannerUrl = if (_uiState.value.bannerUrl == null) {
                        "https://airsoft.social/profile/banner-placeholder.jpg"
                    } else {
                        null
                    },
                )
            }

            is EditProfileAction.ToggleShowPhone -> {
                _uiState.value = _uiState.value.copy(
                    privacySettings = _uiState.value.privacySettings.copy(showPhone = action.enabled),
                )
            }

            is EditProfileAction.ToggleShowEmail -> {
                _uiState.value = _uiState.value.copy(
                    privacySettings = _uiState.value.privacySettings.copy(showEmail = action.enabled),
                )
            }

            is EditProfileAction.ToggleShowTelegram -> {
                _uiState.value = _uiState.value.copy(
                    privacySettings = _uiState.value.privacySettings.copy(showTelegram = action.enabled),
                )
            }

            is EditProfileAction.MessageVisibilityChanged -> {
                _uiState.value = _uiState.value.copy(
                    privacySettings = privacySettingsWithMessageMode(
                        _uiState.value.privacySettings,
                        action.mode,
                    ),
                )
            }

            EditProfileAction.SaveChangesClicked -> Unit
            EditProfileAction.CancelClicked -> Unit
        }
    }
}

@Composable
fun EditProfileSkeletonRoute(
    userId: String,
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        editProfileViewModel.load(userId)
    }
    val uiState by editProfileViewModel.uiState.collectAsState()
    EditProfileSkeletonScreen(
        uiState = uiState,
        onAction = editProfileViewModel::onAction,
    )
}

@Composable
private fun EditProfileSkeletonScreen(
    uiState: EditProfileUiState,
    onAction: (EditProfileAction) -> Unit,
) {
    WireframePage(
        title = "Редактирование профиля",
        subtitle = "Базовые поля, регион, фото и приватность",
    ) {
        WireframeSection(title = "Основные данные") {
            OutlinedTextField(
                value = uiState.callsign,
                onValueChange = { onAction(EditProfileAction.CallsignChanged(it)) },
                label = { Text("Позывной") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = { onAction(EditProfileAction.FirstNameChanged(it)) },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = { onAction(EditProfileAction.LastNameChanged(it)) },
                label = { Text("Фамилия") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.bio.orEmpty(),
                onValueChange = { onAction(EditProfileAction.BioChanged(it)) },
                label = { Text("Био") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }

        WireframeSection(title = "Регион") {
            OutlinedTextField(
                value = uiState.region.orEmpty(),
                onValueChange = { onAction(EditProfileAction.RegionChanged(it)) },
                label = { Text("Регион") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.exitRadiusKm?.toString().orEmpty(),
                onValueChange = { onAction(EditProfileAction.ExitRadiusChanged(it)) },
                label = { Text("Радиус выезда (км)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        WireframeSection(title = "Фото") {
            TacticalPrimaryButton(
                label = "Загрузить аватар",
                onClick = { onAction(EditProfileAction.ToggleAvatarPlaceholder) },
            )
            Text(
                text = if (uiState.avatarUrl == null) "Аватар не выбран" else "Аватар добавлен",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TacticalPrimaryButton(
                label = "Загрузить баннер",
                onClick = { onAction(EditProfileAction.ToggleBannerPlaceholder) },
            )
            Text(
                text = if (uiState.bannerUrl == null) "Баннер не выбран" else "Баннер добавлен",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WireframeSection(title = "Приватность") {
            PrivacyToggle(
                title = "Показывать телефон",
                checked = uiState.privacySettings.showPhone,
                onCheckedChange = { onAction(EditProfileAction.ToggleShowPhone(it)) },
            )
            PrivacyToggle(
                title = "Показывать email",
                checked = uiState.privacySettings.showEmail,
                onCheckedChange = { onAction(EditProfileAction.ToggleShowEmail(it)) },
            )
            PrivacyToggle(
                title = "Показывать Telegram",
                checked = uiState.privacySettings.showTelegram,
                onCheckedChange = { onAction(EditProfileAction.ToggleShowTelegram(it)) },
            )
            MessageVisibilityDropdown(
                selected = messageVisibilityMode(uiState.privacySettings),
                onSelected = { mode ->
                    onAction(EditProfileAction.MessageVisibilityChanged(mode))
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TacticalPrimaryButton(
                label = "Сохранить",
                onClick = { onAction(EditProfileAction.SaveChangesClicked) },
            )
            TacticalOutlinedButton(
                label = "Отмена",
                onClick = { onAction(EditProfileAction.CancelClicked) },
            )
        }
    }
}

@Composable
private fun MessageVisibilityDropdown(
    selected: MessageVisibilityMode,
    onSelected: (MessageVisibilityMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.title,
            onValueChange = {},
            readOnly = true,
            label = { Text("Сообщения") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MessageVisibilityMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.title) },
                    onClick = {
                        onSelected(mode)
                        expanded = false
                    },
                )
            }
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        thickness = 1.dp,
    )
}

@Composable
fun ProfileInventorySkeletonRoute(
    userId: String,
    profileInventoryViewModel: ProfileInventoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        profileInventoryViewModel.load(userId)
    }
    val uiState by profileInventoryViewModel.uiState.collectAsState()
    ProfileInventorySkeletonScreen(
        uiState = uiState,
        onAction = profileInventoryViewModel::onAction,
    )
}

data class ProfileInventoryUiState(
    val userId: String = "self",
    val selectedLoadout: String = "Основной",
    val sellFromInventoryEnabled: Boolean = true,
)

sealed interface ProfileInventoryAction {
    data class SelectLoadout(val loadout: String) : ProfileInventoryAction
    data object ToggleSellFromInventory : ProfileInventoryAction
}

@HiltViewModel
class ProfileInventoryViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileInventoryUiState())
    val uiState: StateFlow<ProfileInventoryUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        if (_uiState.value.userId == userId) return
        viewModelScope.launch {
            val user = profileRepository.observeUser(userId).first()
            _uiState.value = ProfileInventoryUiState(
                userId = userId,
                sellFromInventoryEnabled = user?.roles?.contains(UserRole.SELLER) ?: true,
            )
        }
    }

    fun onAction(action: ProfileInventoryAction) {
        when (action) {
            is ProfileInventoryAction.SelectLoadout -> {
                _uiState.value = _uiState.value.copy(selectedLoadout = action.loadout)
            }

            ProfileInventoryAction.ToggleSellFromInventory -> {
                _uiState.value = _uiState.value.copy(
                    sellFromInventoryEnabled = !_uiState.value.sellFromInventoryEnabled,
                )
            }
        }
    }
}

@Composable
private fun ProfileInventorySkeletonScreen(
    uiState: ProfileInventoryUiState,
    onAction: (ProfileInventoryAction) -> Unit,
) {
    WireframePage(
        title = "Инвентарь",
        subtitle = "Каркас детального инвентаря пользователя",
        primaryActionLabel = "Добавить предмет",
    ) {
        WireframeSection(title = "Основной комплект") {
            WireframeItemRow(
                title = "M4A1",
                subtitle = "Пружина M110 | Коллиматор | 6 магазинов",
                trailing = if (uiState.selectedLoadout == "Основной") "Основной*" else "Основной",
            )
            WireframeItemRow(
                title = "Glock 17",
                subtitle = "CO2 | 3 магазина | кобура",
                trailing = "Вторичный",
            )
            WireframeItemRow(
                title = "Комплект связи",
                subtitle = "Baofeng UV-5R | гарнитура | PTT",
                trailing = "Связь",
            )
        }
        WireframeSection(title = "Связь с барахолкой") {
            WireframeChipRow(
                labels = listOf(
                    if (uiState.sellFromInventoryEnabled) "[Продать из инвентаря]" else "Продать из инвентаря",
                    "Снять с продажи",
                    "Клонировать в объявление",
                ),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TacticalOutlinedButton(
                    label = "Переключить комплект",
                    onClick = {
                        val next =
                            if (uiState.selectedLoadout == "Основной") "Вторичный" else "Основной"
                        onAction(ProfileInventoryAction.SelectLoadout(next))
                    },
                )
                TacticalOutlinedButton(
                    label = "Переключить продажу из инвентаря",
                    onClick = { onAction(ProfileInventoryAction.ToggleSellFromInventory) },
                )
            }
        }
    }
}

@Composable
fun ProfileAchievementsSkeletonRoute(
    userId: String,
    profileAchievementsViewModel: ProfileAchievementsViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        profileAchievementsViewModel.load(userId)
    }
    val uiState by profileAchievementsViewModel.uiState.collectAsState()
    ProfileAchievementsSkeletonScreen(
        uiState = uiState,
        onAction = profileAchievementsViewModel::onAction,
    )
}

data class ProfileAchievementsUiState(
    val userId: String = "self",
    val selectedCategory: String = "Все",
    val categories: List<String> = listOf("Все", "Игры", "Команда", "Организация", "Маркет"),
    val achievementRows: List<ProfileListRow> = listOf(
        ProfileListRow("100 игр", "Сыграно более 100 игр", "легенда"),
        ProfileListRow("Надёжный участник", "Подтверждает участие и приезжает вовремя", "доверие"),
        ProfileListRow("Командный лидер", "Организовал 10 командных выездов", "команда"),
    ),
)

sealed interface ProfileAchievementsAction {
    data object CycleCategory : ProfileAchievementsAction
}

@HiltViewModel
class ProfileAchievementsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileAchievementsUiState())
    val uiState: StateFlow<ProfileAchievementsUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        if (_uiState.value.userId == userId) return
        viewModelScope.launch {
            val user = profileRepository.observeUser(userId).first()
            _uiState.value = ProfileAchievementsUiState(
                userId = userId,
                selectedCategory = if (user?.teamName != null) "Команда" else "Все",
            )
        }
    }

    fun onAction(action: ProfileAchievementsAction) {
        when (action) {
            ProfileAchievementsAction.CycleCategory -> {
                val current = _uiState.value
                val idx = current.categories.indexOf(current.selectedCategory)
                val next = current.categories[(idx + 1).mod(current.categories.size)]
                _uiState.value = current.copy(selectedCategory = next)
            }
        }
    }
}

@Composable
private fun ProfileAchievementsSkeletonScreen(
    uiState: ProfileAchievementsUiState,
    onAction: (ProfileAchievementsAction) -> Unit,
) {
    WireframePage(
        title = "Достижения",
        subtitle = "Каркас достижения игрока. ID пользователя: ${uiState.userId}",
        primaryActionLabel = "Открыть витрину достижений (заглушка)",
    ) {
        WireframeSection(
            title = "Категория",
            subtitle = "Фильтр достижений по типу активности",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Категория" to uiState.selectedCategory,
                    "Достижений" to uiState.achievementRows.size.toString(),
                    "Профиль" to uiState.userId,
                ),
            )
            WireframeChipRow(
                labels = uiState.categories.map {
                    if (it == uiState.selectedCategory) "[$it]" else it
                },
            )
        }
        WireframeSection(
            title = "Список достижений",
            subtitle = "Заглушка для бейджей/ачивок с прогрессом и условиями",
        ) {
            uiState.achievementRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring достижений") {
            TacticalOutlinedButton(
                label = "Сменить категорию достижений",
                onClick = { onAction(ProfileAchievementsAction.CycleCategory) },
            )
        }
    }
}

@Composable
fun ProfileTrustBadgesSkeletonRoute(
    userId: String,
    profileTrustBadgesViewModel: ProfileTrustBadgesViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        profileTrustBadgesViewModel.load(userId)
    }
    val uiState by profileTrustBadgesViewModel.uiState.collectAsState()
    ProfileTrustBadgesSkeletonScreen(
        uiState = uiState,
        onAction = profileTrustBadgesViewModel::onAction,
    )
}

data class ProfileTrustBadgesUiState(
    val userId: String = "self",
    val selectedScope: String = "Профиль",
    val scopes: List<String> = listOf("Профиль", "Команда", "Маркет", "Сделки"),
    val badgeRows: List<ProfileListRow> = listOf(
        ProfileListRow("Проверенный профиль", "Телефон/аккаунт подтверждены", "verified"),
        ProfileListRow("Надёжный продавец", "Успешные сделки без жалоб", "seller"),
        ProfileListRow("Пунктуальный игрок", "Регулярное подтверждение участия", "team"),
    ),
    val signalRows: List<ProfileListRow> = listOf(
        ProfileListRow("Жалобы за 90 дней", "0 подтверждённых нарушений", "0"),
        ProfileListRow("Споры по сделкам", "1 закрыт без санкций", "1"),
    ),
)

sealed interface ProfileTrustBadgesAction {
    data object CycleScope : ProfileTrustBadgesAction
}

@HiltViewModel
class ProfileTrustBadgesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileTrustBadgesUiState())
    val uiState: StateFlow<ProfileTrustBadgesUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        if (_uiState.value.userId == userId) return
        viewModelScope.launch {
            val user = profileRepository.observeUser(userId).first()
            _uiState.value = ProfileTrustBadgesUiState(
                userId = userId,
                selectedScope = if (user?.roles?.contains(UserRole.SELLER) == true) {
                    "Маркет"
                } else {
                    "Профиль"
                },
            )
        }
    }

    fun onAction(action: ProfileTrustBadgesAction) {
        when (action) {
            ProfileTrustBadgesAction.CycleScope -> {
                val current = _uiState.value
                val idx = current.scopes.indexOf(current.selectedScope)
                val next = current.scopes[(idx + 1).mod(current.scopes.size)]
                _uiState.value = current.copy(selectedScope = next)
            }
        }
    }
}

@Composable
private fun ProfileTrustBadgesSkeletonScreen(
    uiState: ProfileTrustBadgesUiState,
    onAction: (ProfileTrustBadgesAction) -> Unit,
) {
    WireframePage(
        title = "Бейджи доверия",
        subtitle = "Каркас доверительных сигналов и бейджей. ID пользователя: ${uiState.userId}",
        primaryActionLabel = "Открыть правила бейджей (заглушка)",
    ) {
        WireframeSection(
            title = "Область",
            subtitle = "Фокус отображения доверительных сигналов",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Область" to uiState.selectedScope,
                    "Бейджей" to uiState.badgeRows.size.toString(),
                    "Сигналов" to uiState.signalRows.size.toString(),
                ),
            )
            WireframeChipRow(
                labels = uiState.scopes.map { if (it == uiState.selectedScope) "[$it]" else it },
            )
        }
        WireframeSection(
            title = "Активные бейджи",
            subtitle = "Бейджи доверия по профилю/команде/маркету",
        ) {
            uiState.badgeRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Сигналы доверия",
            subtitle = "Агрегированные сигналы для антифрода и репутации",
        ) {
            uiState.signalRows.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка state wiring бейджей доверия") {
            TacticalOutlinedButton(
                label = "Сменить область бейджей",
                onClick = { onAction(ProfileTrustBadgesAction.CycleScope) },
            )
        }
    }
}

private fun userPrimaryRoleLabel(user: User): String? =
    user.roles.firstOrNull()?.let(::userRoleTagLabel)

private fun userRoleTagLabel(role: UserRole): String = when (role) {
    UserRole.PLAYER -> "Игрок"
    UserRole.CAPTAIN -> "Командир"
    UserRole.ORGANIZER -> "Организатор"
    UserRole.SELLER -> "Продавец"
    UserRole.TECH_MASTER -> "Техмастер"
    UserRole.SHOP_PARTNER -> "Партнёр"
    UserRole.MODERATOR -> "Модератор"
    UserRole.ADMIN -> "Админ"
}

private fun defaultTeamMemberCount(teamName: String): Int = when (teamName) {
    "[EW] EASY WINNER" -> 14
    else -> 10
}

private fun gearCategoryIcon(category: GearCategory): ImageVector = when (category) {
    GearCategory.PRIMARY_WEAPONS -> Icons.Filled.Navigation
    GearCategory.SECONDARY_WEAPONS -> Icons.Filled.Build
    GearCategory.RIGS -> Icons.Filled.ShoppingBag
    GearCategory.HELMETS -> Icons.Filled.HealthAndSafety
    GearCategory.RADIOS -> Icons.Filled.Radio
    GearCategory.FLASHLIGHTS -> Icons.Filled.Visibility
    GearCategory.ARMOR -> Icons.Filled.HealthAndSafety
    GearCategory.OPTICS -> Icons.Filled.Visibility
    GearCategory.SPARE_PARTS -> Icons.Filled.Build
    GearCategory.CONSUMABLES -> Icons.Filled.ShoppingBag
    GearCategory.OTHER -> Icons.Filled.Settings
}

private fun messageVisibilityMode(settings: PrivacySettings): MessageVisibilityMode = when {
    !settings.allowDirectMessages -> MessageVisibilityMode.TEAM_ONLY
    settings.allowDirectMessages && !settings.allowTeamInvites -> MessageVisibilityMode.CONTACTS
    else -> MessageVisibilityMode.ALL
}

private fun privacySettingsWithMessageMode(
    settings: PrivacySettings,
    mode: MessageVisibilityMode,
): PrivacySettings = when (mode) {
    MessageVisibilityMode.ALL -> settings.copy(
        allowDirectMessages = true,
        allowTeamInvites = true,
    )

    MessageVisibilityMode.CONTACTS -> settings.copy(
        allowDirectMessages = true,
        allowTeamInvites = false,
    )

    MessageVisibilityMode.TEAM_ONLY -> settings.copy(
        allowDirectMessages = false,
        allowTeamInvites = true,
    )
}

private fun formatGameDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("ru"))
    return formatter.format(Date(timestamp))
}

private fun defaultGearCategorySummaries(): List<GearCategorySummary> = listOf(
    GearCategorySummary(
        category = GearCategory.PRIMARY_WEAPONS,
        displayName = "Приводы",
        icon = "navigation",
        count = 2,
    ),
    GearCategorySummary(
        category = GearCategory.SECONDARY_WEAPONS,
        displayName = "Пистолеты",
        icon = "build",
        count = 2,
    ),
    GearCategorySummary(
        category = GearCategory.RIGS,
        displayName = "Разгрузки",
        icon = "shopping_bag",
        count = 3,
    ),
    GearCategorySummary(
        category = GearCategory.HELMETS,
        displayName = "Шлемы",
        icon = "health_and_safety",
        count = 1,
    ),
    GearCategorySummary(
        category = GearCategory.RADIOS,
        displayName = "Рации",
        icon = "radio",
        count = 2,
    ),
    GearCategorySummary(
        category = GearCategory.FLASHLIGHTS,
        displayName = "Фонари",
        icon = "visibility",
        count = 3,
    ),
)

private fun defaultGameHistory(): List<GameHistoryRow> = listOf(
    GameHistoryRow(
        id = "history-1",
        date = 1_771_146_000_000L,
        eventName = "Night Raid",
    ),
    GameHistoryRow(
        id = "history-2",
        date = 1_770_714_000_000L,
        eventName = "CQB Tournament",
    ),
)

private fun defaultAchievements(): List<AchievementRow> = listOf(
    AchievementRow(
        id = "ach-100-games",
        title = "100 игр",
        description = "Сыграно более ста игр",
    ),
    AchievementRow(
        id = "ach-reliable",
        title = "Надёжный участник",
        description = "Систематически подтверждает участие",
    ),
)

private fun defaultTrustBadges(): List<TrustBadgeRow> = listOf(
    TrustBadgeRow(
        id = "trust-verified",
        title = "Проверенный профиль",
        description = "Телефон и аккаунт подтверждены",
    ),
    TrustBadgeRow(
        id = "trust-seller",
        title = "Надёжный продавец",
        description = "Успешные сделки без жалоб",
    ),
)
