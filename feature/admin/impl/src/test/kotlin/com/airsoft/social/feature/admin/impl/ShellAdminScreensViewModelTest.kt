package com.airsoft.social.feature.admin.impl

import com.airsoft.social.core.auth.AccountAdministrationGateway
import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.auth.ManagedAccount
import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShellAdminScreensViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `moderation viewmodel cycles queue`() {
        val viewModel = ModerationViewModel()

        val initial = viewModel.uiState.value.selectedQueue
        viewModel.onAction(ModerationAction.CycleQueue)

        assertTrue(viewModel.uiState.value.selectedQueue != initial)
        assertTrue(viewModel.uiState.value.queuePreviewRows.isNotEmpty())
    }

    @Test
    fun `moderation reports queue viewmodel cycles priority`() {
        val viewModel = ModerationReportsQueueViewModel()

        val initial = viewModel.uiState.value.selectedPriority
        viewModel.onAction(ModerationReportsQueueAction.CyclePriority)

        assertTrue(viewModel.uiState.value.selectedPriority != initial)
        assertTrue(viewModel.uiState.value.reportRows.isNotEmpty())
    }

    @Test
    fun `moderation report detail viewmodel loads and cycles tab`() {
        val viewModel = ModerationReportDetailViewModel()

        viewModel.load("report-001")
        val initial = viewModel.uiState.value.selectedTab
        viewModel.onAction(ModerationReportDetailAction.CycleTab)

        assertEquals("report-001", viewModel.uiState.value.reportId)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedTab != initial)
    }

    @Test
    fun `ops tool viewmodel loads and cycles segment`() {
        val viewModel = OpsToolViewModel()
        val spec = OpsToolSpec(
            id = "test-ops",
            title = "Тест",
            subtitle = "Тестовый экран",
            primaryActionLabel = "Действие",
            segments = listOf("А", "Б"),
            metricRows = listOf("X" to "1"),
            primarySectionTitle = "Основное",
            primarySectionSubtitle = "Описание",
            primaryRows = listOf(ShellWireframeRow("row1", "desc", "tag")),
            secondarySectionTitle = "Вторичное",
            secondarySectionSubtitle = "Описание",
            secondaryRows = listOf(ShellWireframeRow("row2", "desc", "tag")),
            actionButtonLabel = "Переключить",
        )

        viewModel.load(spec)
        val initial = viewModel.uiState.value.selectedSegment
        viewModel.onAction(OpsToolAction.CycleSegment)

        assertEquals("test-ops", viewModel.uiState.value.toolId)
        assertTrue(viewModel.uiState.value.primaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedSegment != initial)
    }

    @Test
    fun `admin dashboard viewmodel cycles section`() {
        val viewModel = AdminDashboardViewModel()

        val initial = viewModel.uiState.value.selectedSection
        viewModel.onAction(AdminDashboardAction.CycleSection)

        assertTrue(viewModel.uiState.value.selectedSection != initial)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
    }

    @Test
    fun `role assignments viewmodel cycles selected role`() = runTest {
        val viewModel = AdminRoleAssignmentsViewModel(
            authGateway = FakeAdminAuthGateway(),
            sessionRepository = FakeSessionRepository(),
        )
        advanceUntilIdle()

        val initial = viewModel.uiState.value.selectedRole
        viewModel.onAction(AdminRoleAssignmentsAction.CycleRole)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedRole != initial)
    }

    @Test
    fun `role assignments require privileges`() = runTest {
        val authGateway = FakeAdminAuthGateway()
        val actor = ManagedAccount(
            userId = "user-1",
            callsign = "Ghost",
            email = "ghost@example.com",
            roles = setOf(AccountRole.USER),
        )
        authGateway.emitAccounts(listOf(actor), rootAdminUserId = null)
        val sessionRepository = FakeSessionRepository(
            initialState = AuthState.SignedIn(
                UserSession(
                    userId = actor.userId,
                    displayName = actor.callsign,
                    email = actor.email,
                    accountRoles = actor.roles,
                ),
            ),
        )
        val viewModel = AdminRoleAssignmentsViewModel(
            authGateway = authGateway,
            sessionRepository = sessionRepository,
        )
        advanceUntilIdle()

        viewModel.onAction(AdminRoleAssignmentsAction.GrantSelectedRole(targetUserId = actor.userId))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.message.isNullOrBlank())
    }
}

private class FakeSessionRepository(
    initialState: AuthState = AuthState.SignedOut,
) : SessionRepository {
    private val state = MutableStateFlow(initialState)

    override val authState: Flow<AuthState> = state.asStateFlow()

    override suspend fun continueAsGuest(callsign: String): AuthResult =
        AuthResult.Failure("not needed in test")

    override suspend fun registerWithEmail(
        callsign: String,
        email: String,
        password: String?,
    ): AuthResult = AuthResult.Failure("not needed in test")

    override suspend fun signInMock(callsign: String): AuthResult =
        AuthResult.Failure("not needed in test")

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun currentAuthState(): AuthState = state.value
}

private class FakeAdminAuthGateway : AuthGateway, AccountAdministrationGateway {
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.SignedOut)
    private val accountsFlow = MutableStateFlow<List<ManagedAccount>>(emptyList())
    private val rootFlow = MutableStateFlow<String?>(null)

    override val authState: Flow<AuthState> = authStateFlow.asStateFlow()
    override val managedAccounts: Flow<List<ManagedAccount>> = accountsFlow.asStateFlow()
    override val rootAdminUserId: Flow<String?> = rootFlow.asStateFlow()

    override suspend fun signIn(request: com.airsoft.social.core.auth.SignInRequest): AuthResult =
        AuthResult.Failure("not needed in test")

    override suspend fun signOut() {
        authStateFlow.value = AuthState.SignedOut
    }

    override suspend fun currentSession(): UserSession? = null

    override suspend fun grantRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> {
        val actor = accountsFlow.value.firstOrNull { it.userId == actorUserId }
            ?: return AppResult.Failure(AppError.Unauthorized)
        val target = accountsFlow.value.firstOrNull { it.userId == targetUserId }
            ?: return AppResult.Failure(AppError.Validation("not found"))
        val isRoot = rootFlow.value == actorUserId
        val canGrant = if (isRoot) {
            role != AccountRole.USER
        } else {
            AccountRole.ADMIN in actor.roles && role == AccountRole.MODERATOR
        }
        if (!canGrant) return AppResult.Failure(AppError.Unauthorized)
        accountsFlow.value = accountsFlow.value.map { account ->
            if (account.userId == target.userId) {
                account.copy(roles = account.roles + role + AccountRole.USER)
            } else {
                account
            }
        }
        return AppResult.Success(Unit)
    }

    override suspend fun revokeRole(
        actorUserId: String,
        targetUserId: String,
        role: AccountRole,
    ): AppResult<Unit> = AppResult.Success(Unit)

    fun emitAccounts(
        accounts: List<ManagedAccount>,
        rootAdminUserId: String?,
    ) {
        accountsFlow.value = accounts
        rootFlow.value = rootAdminUserId
    }
}
