package com.airsoft.social.core.data

import com.airsoft.social.core.auth.AccountAdministrationGateway
import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.auth.ManagedAccount
import com.airsoft.social.core.auth.SignInRequest
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthGatewayBackedAccountAdminRepositoryTest {
    @Test
    fun `unsupported gateway returns unsupported result and empty flows`() = runTest {
        val repository = AuthGatewayBackedAccountAdminRepository(
            authGateway = FakePlainAuthGateway(),
        )

        val members = repository.observeMembers().first()
        val rootAdmin = repository.observeRootAdminUserId().first()
        val grantResult = repository.grantRole(
            actorUserId = "a",
            targetUserId = "b",
            role = AccountRole.MODERATOR,
        )

        assertFalse(repository.isSupported)
        assertTrue(members.isEmpty())
        assertEquals(null, rootAdmin)
        assertTrue(grantResult is AppResult.Failure)
    }

    @Test
    fun `repository maps managed accounts and delegates grant`() = runTest {
        val gateway = FakeAdminAuthGateway()
        gateway.membersState.value = listOf(
            ManagedAccount(
                userId = "u-1",
                callsign = "Root",
                email = "root@example.com",
                roles = setOf(AccountRole.USER, AccountRole.ADMIN),
                isRootAdmin = true,
            ),
        )
        gateway.rootState.value = "u-1"
        val repository = AuthGatewayBackedAccountAdminRepository(
            authGateway = gateway,
        )

        val members = repository.observeMembers().first()
        val rootAdmin = repository.observeRootAdminUserId().first()
        val result = repository.grantRole(
            actorUserId = "u-1",
            targetUserId = "u-2",
            role = AccountRole.MODERATOR,
        )

        assertTrue(repository.isSupported)
        assertEquals(1, members.size)
        assertEquals("Root", members.first().callsign)
        assertEquals("u-1", rootAdmin)
        assertTrue(result is AppResult.Success)
        assertEquals(AccountRole.MODERATOR, gateway.lastGrantedRole)
    }
}

private class FakePlainAuthGateway : AuthGateway {
    override val authState: Flow<AuthState> = MutableStateFlow(AuthState.SignedOut).asStateFlow()

    override suspend fun signIn(request: SignInRequest): AuthResult =
        AuthResult.Failure("unsupported")

    override suspend fun signOut() = Unit

    override suspend fun currentSession(): UserSession? = null
}

private class FakeAdminAuthGateway : AuthGateway, AccountAdministrationGateway {
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val membersState = MutableStateFlow<List<ManagedAccount>>(emptyList())
    val rootState = MutableStateFlow<String?>(null)
    var lastGrantedRole: AccountRole? = null

    override val authState: Flow<AuthState> = authStateFlow.asStateFlow()
    override val managedAccounts: Flow<List<ManagedAccount>> = membersState.asStateFlow()
    override val rootAdminUserId: Flow<String?> = rootState.asStateFlow()

    override suspend fun signIn(request: SignInRequest): AuthResult =
        AuthResult.Failure("not used")

    override suspend fun signOut() {
        authStateFlow.value = AuthState.SignedOut
    }

    override suspend fun currentSession(): UserSession? = null

    override suspend fun grantRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> {
        lastGrantedRole = role
        return AppResult.Success(Unit)
    }

    override suspend fun revokeRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> = AppResult.Success(Unit)
}
