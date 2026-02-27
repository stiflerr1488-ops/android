package com.airsoft.social.core.auth

import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.model.AccountRole
import kotlinx.coroutines.flow.Flow

data class ManagedAccount(
    val userId: String,
    val callsign: String,
    val email: String,
    val roles: Set<AccountRole>,
    val isRootAdmin: Boolean = false,
)

interface AccountAdministrationGateway {
    val managedAccounts: Flow<List<ManagedAccount>>
    val rootAdminUserId: Flow<String?>

    suspend fun grantRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit>

    suspend fun revokeRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit>
}
