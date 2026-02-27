package com.airsoft.social.feature.admin.impl

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.AccountAdminMember
import com.airsoft.social.core.data.AccountAdminRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val assignableRoles = listOf(
    AccountRole.MODERATOR,
    AccountRole.COMMERCIAL,
    AccountRole.ADMIN,
)

data class AdminRoleAssignmentsUiState(
    val actorUserId: String? = null,
    val actorCallsign: String = "Гость",
    val actorRoles: Set<AccountRole> = emptySet(),
    val actorIsRootAdmin: Boolean = false,
    val manageableRoles: Set<AccountRole> = emptySet(),
    val selectedRole: AccountRole = AccountRole.MODERATOR,
    val accounts: List<AccountAdminMember> = emptyList(),
    val message: String? = null,
    val adminToolsSupported: Boolean = false,
)

sealed interface AdminRoleAssignmentsAction {
    data object CycleRole : AdminRoleAssignmentsAction
    data class GrantSelectedRole(val targetUserId: String) : AdminRoleAssignmentsAction
    data class RevokeSelectedRole(val targetUserId: String) : AdminRoleAssignmentsAction
    data object ClearMessage : AdminRoleAssignmentsAction
}

@HiltViewModel
class AdminRoleAssignmentsViewModel @Inject constructor(
    private val accountAdminRepository: AccountAdminRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(
        AdminRoleAssignmentsUiState(
            adminToolsSupported = accountAdminRepository.isSupported,
        ),
    )

    val uiState: StateFlow<AdminRoleAssignmentsUiState> = combine(
        localState,
        sessionRepository.authState,
        accountAdminRepository.observeMembers(),
        accountAdminRepository.observeRootAdminUserId(),
    ) { local, authState, managedAccounts, rootAdminUserId ->
        val signedIn = authState as? AuthState.SignedIn
        val actorId = signedIn?.session?.userId
        val actorRecord = managedAccounts.firstOrNull { it.userId == actorId }
        val actorRoles = signedIn?.session?.accountRoles ?: emptySet()
        val actorIsRootAdmin = rootAdminUserId == actorId || actorRecord?.isRootAdmin == true
        val manageableRoles = when {
            actorIsRootAdmin -> setOf(AccountRole.ADMIN, AccountRole.MODERATOR, AccountRole.COMMERCIAL)
            AccountRole.ADMIN in actorRoles -> setOf(AccountRole.MODERATOR)
            else -> emptySet()
        }
        local.copy(
            actorUserId = actorId,
            actorCallsign = signedIn?.session?.displayName ?: "Гость",
            actorRoles = actorRoles,
            actorIsRootAdmin = actorIsRootAdmin,
            manageableRoles = manageableRoles,
            accounts = managedAccounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = localState.value,
    )

    fun onAction(action: AdminRoleAssignmentsAction) {
        when (action) {
            AdminRoleAssignmentsAction.CycleRole -> localState.update { state ->
                val rolePool = state.manageableRoles.takeIf { it.isNotEmpty() }?.toList() ?: assignableRoles
                val index = rolePool.indexOf(state.selectedRole).let { if (it < 0) 0 else it }
                state.copy(
                    selectedRole = rolePool[(index + 1) % rolePool.size],
                    message = null,
                )
            }

            is AdminRoleAssignmentsAction.GrantSelectedRole ->
                mutateRole(targetUserId = action.targetUserId, grant = true)

            is AdminRoleAssignmentsAction.RevokeSelectedRole ->
                mutateRole(targetUserId = action.targetUserId, grant = false)

            AdminRoleAssignmentsAction.ClearMessage ->
                localState.update { it.copy(message = null) }
        }
    }

    private fun mutateRole(
        targetUserId: String,
        grant: Boolean,
    ) {
        val actorUserId = uiState.value.actorUserId
        if (!accountAdminRepository.isSupported) {
            localState.update { it.copy(message = "Инструменты админки недоступны в текущем auth provider") }
            return
        }
        if (actorUserId == null) {
            localState.update { it.copy(message = "Сначала войдите в зарегистрированный аккаунт") }
            return
        }
        val selectedRole = uiState.value.selectedRole
        if (selectedRole !in uiState.value.manageableRoles) {
            localState.update {
                it.copy(message = "Недостаточно прав для роли ${selectedRole.toLabel()}")
            }
            return
        }
        viewModelScope.launch {
            val result = if (grant) {
                accountAdminRepository.grantRole(
                    actorUserId = actorUserId,
                    targetUserId = targetUserId,
                    role = selectedRole,
                )
            } else {
                accountAdminRepository.revokeRole(
                    actorUserId = actorUserId,
                    targetUserId = targetUserId,
                    role = selectedRole,
                )
            }
            val actionLabel = if (grant) "выдана" else "снята"
            val roleLabel = selectedRole.toLabel()
            val message = when (result) {
                is AppResult.Success -> "Роль $roleLabel $actionLabel"
                is AppResult.Failure -> result.error.toHumanText()
            }
            localState.update { it.copy(message = message) }
        }
    }
}

@Composable
fun AdminRoleAssignmentsShellRoute(
    viewModel: AdminRoleAssignmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    WireframePage(
        title = "Назначение ролей",
        subtitle = "Главный админ: admin/moder/commercial. Обычный admin: только moderator.",
        primaryActionLabel = "Сменить роль для действий",
        onPrimaryAction = { viewModel.onAction(AdminRoleAssignmentsAction.CycleRole) },
    ) {
        WireframeSection(
            title = "Текущий оператор",
            subtitle = "Права назначения ролей определяются ролью текущего аккаунта.",
        ) {
            WireframeMetricRow(
                items = listOf(
                    "Оператор" to uiState.actorCallsign,
                    "Root" to if (uiState.actorIsRootAdmin) "да" else "нет",
                    "Роль" to uiState.selectedRole.toLabel(),
                ),
            )
            WireframeChipRow(
                labels = uiState.actorRoles
                    .sortedBy { it.name }
                    .map(AccountRole::toLabel)
                    .ifEmpty { listOf("Нет ролей") },
            )
            WireframeItemRow(
                title = "Можно управлять ролями",
                subtitle = uiState.manageableRoles
                    .sortedBy { it.name }
                    .map(AccountRole::toLabel)
                    .ifEmpty { listOf("Нет прав управления") }
                    .joinToString(", "),
                trailing = uiState.selectedRole.toLabel(),
            )
            if (!uiState.adminToolsSupported) {
                WireframeItemRow(
                    title = "Ограничение провайдера",
                    subtitle = "Этот auth provider не поддерживает управление ролями.",
                    trailing = "Read-only",
                )
            }
        }

        WireframeSection(
            title = "Аккаунты",
            subtitle = "Список зарегистрированных аккаунтов на этом окружении.",
        ) {
            if (uiState.accounts.isEmpty()) {
                WireframeItemRow(
                    title = "Нет зарегистрированных аккаунтов",
                    subtitle = "Зарегистрируйте хотя бы один аккаунт по email.",
                )
            } else {
                val canUseRoleActions = uiState.actorUserId != null &&
                    uiState.adminToolsSupported &&
                    uiState.selectedRole in uiState.manageableRoles
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.accounts.forEach { account ->
                        val canModifyTarget = when {
                            !canUseRoleActions -> false
                            uiState.actorIsRootAdmin -> true
                            account.isRootAdmin -> false
                            else -> true
                        }
                        WireframeItemRow(
                            title = account.callsign,
                            subtitle = buildString {
                                append(account.email)
                                append(" | ")
                                append(
                                    account.roles
                                        .sortedBy { it.name }
                                        .map(AccountRole::toLabel)
                                        .joinToString(", "),
                                )
                            },
                            trailing = if (account.isRootAdmin) "Root" else null,
                        )
                        RowActions(
                            enabled = canModifyTarget,
                            onGrant = {
                                viewModel.onAction(
                                    AdminRoleAssignmentsAction.GrantSelectedRole(account.userId),
                                )
                            },
                            onRevoke = {
                                viewModel.onAction(
                                    AdminRoleAssignmentsAction.RevokeSelectedRole(account.userId),
                                )
                            },
                        )
                    }
                }
            }
        }

        uiState.message?.let { message ->
            WireframeSection(
                title = "Результат",
                subtitle = "Ответ после попытки изменить роль.",
            ) {
                WireframeItemRow(
                    title = message,
                    subtitle = "Проверьте список ролей выше.",
                )
                OutlinedButton(
                    onClick = { viewModel.onAction(AdminRoleAssignmentsAction.ClearMessage) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Очистить сообщение")
                }
            }
        }
    }
}

@Composable
private fun RowActions(
    enabled: Boolean,
    onGrant: () -> Unit,
    onRevoke: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            enabled = enabled,
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Выдать выбранную роль")
        }
        OutlinedButton(
            enabled = enabled,
            onClick = onRevoke,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Снять выбранную роль")
        }
    }
}

private fun AccountRole.toLabel(): String = when (this) {
    AccountRole.USER -> "Пользователь"
    AccountRole.COMMERCIAL -> "Коммерческий"
    AccountRole.MODERATOR -> "Модератор"
    AccountRole.ADMIN -> "Админ"
}

private fun AppError.toHumanText(): String = when (this) {
    AppError.Unknown -> "Неизвестная ошибка"
    AppError.Unsupported -> "Операция не поддерживается"
    AppError.Unauthorized -> "Недостаточно прав для действия"
    is AppError.Validation -> message
    is AppError.Network -> message ?: "Ошибка сети"
    is AppError.ThrowableError -> throwable.message ?: "Ошибка выполнения"
}
