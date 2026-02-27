package com.airsoft.social.core.data

import com.airsoft.social.core.auth.AccountAdministrationGateway
import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.model.AccountRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class AccountAdminMember(
    val userId: String,
    val callsign: String,
    val email: String,
    val roles: Set<AccountRole>,
    val isRootAdmin: Boolean,
)

interface AccountAdminRepository {
    val isSupported: Boolean
    fun observeMembers(): Flow<List<AccountAdminMember>>
    fun observeRootAdminUserId(): Flow<String?>

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

class AuthGatewayBackedAccountAdminRepository(
    authGateway: AuthGateway,
) : AccountAdminRepository {
    private val adminGateway = authGateway as? AccountAdministrationGateway

    override val isSupported: Boolean = adminGateway != null

    override fun observeMembers(): Flow<List<AccountAdminMember>> =
        adminGateway?.managedAccounts
            ?.map { members ->
                members.map { member ->
                    AccountAdminMember(
                        userId = member.userId,
                        callsign = member.callsign,
                        email = member.email,
                        roles = member.roles,
                        isRootAdmin = member.isRootAdmin,
                    )
                }
            }
            ?: flowOf(emptyList())

    override fun observeRootAdminUserId(): Flow<String?> =
        adminGateway?.rootAdminUserId ?: flowOf(null)

    override suspend fun grantRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> = adminGateway?.grantRole(
        actorUserId = actorUserId,
        targetUserId = targetUserId,
        role = role,
    ) ?: AppResult.Failure(AppError.Unsupported)

    override suspend fun revokeRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> = adminGateway?.revokeRole(
        actorUserId = actorUserId,
        targetUserId = targetUserId,
        role = role,
    ) ?: AppResult.Failure(AppError.Unsupported)
}
