package com.example.teamcompass.ui

import com.example.teamcompass.auth.IdentityLinkingEligibility
import com.example.teamcompass.auth.IdentityLinkingService
import com.example.teamcompass.domain.TeamActionResult
import org.junit.Assert.assertEquals
import org.junit.Test

class IdentityLinkingCoordinatorTest {

    @Test
    fun evaluate_skips_when_uid_blank() {
        val service = FakeIdentityLinkingService(
            eligibility = IdentityLinkingEligibility(shouldPrompt = true, reason = "anonymous_user")
        )
        var started = 0

        val coordinator = IdentityLinkingCoordinator(
            identityLinkingService = service,
            nextTraceId = { "trace" },
            onStart = { _, _, _ -> started++ },
            onSuccess = { _, _, _ -> Unit },
            onFailure = { _, _, _, _ -> Unit },
            onEligiblePrompt = { _, _, _ -> Unit },
        )

        coordinator.evaluate(teamCode = "123456", uid = "  ")

        assertEquals(0, service.evaluateCalls)
        assertEquals(0, started)
    }

    @Test
    fun evaluate_prompts_only_once_per_uid_when_eligible() {
        val service = FakeIdentityLinkingService(
            eligibility = IdentityLinkingEligibility(shouldPrompt = true, reason = "anonymous_user")
        )
        val prompts = mutableListOf<String>()

        val coordinator = IdentityLinkingCoordinator(
            identityLinkingService = service,
            nextTraceId = { "trace" },
            onStart = { _, _, _ -> Unit },
            onSuccess = { _, _, _ -> Unit },
            onFailure = { _, _, _, _ -> Unit },
            onEligiblePrompt = { uid, _, _ -> prompts += uid },
        )

        coordinator.evaluate(teamCode = "123456", uid = "u1")
        coordinator.evaluate(teamCode = "123456", uid = "u1")

        assertEquals(1, service.evaluateCalls)
        assertEquals(listOf("u1"), prompts)
    }

    @Test
    fun evaluate_calls_failure_callback_and_retries_for_same_uid_after_failure() {
        val service = FakeIdentityLinkingService(
            eligibility = IdentityLinkingEligibility(shouldPrompt = false, reason = "n/a"),
            error = IllegalStateException("boom"),
        )
        var failureCalls = 0
        val traceIds = mutableListOf<String>()

        val coordinator = IdentityLinkingCoordinator(
            identityLinkingService = service,
            nextTraceId = {
                "trace-${traceIds.size + 1}".also(traceIds::add)
            },
            onStart = { _, _, _ -> Unit },
            onSuccess = { _, _, _ -> Unit },
            onFailure = { _, _, _, _ -> failureCalls++ },
            onEligiblePrompt = { _, _, _ -> Unit },
        )

        coordinator.evaluate(teamCode = "123456", uid = "u1")
        coordinator.evaluate(teamCode = "123456", uid = "u1")

        assertEquals(2, service.evaluateCalls)
        assertEquals(2, failureCalls)
        assertEquals(listOf("trace-1", "trace-2"), traceIds)
    }

    private class FakeIdentityLinkingService(
        private val eligibility: IdentityLinkingEligibility,
        private val error: Throwable? = null,
    ) : IdentityLinkingService {
        var evaluateCalls: Int = 0

        override fun evaluateEligibility(): IdentityLinkingEligibility {
            evaluateCalls++
            if (error != null) throw error
            return eligibility
        }

        override suspend fun linkWithEmail(
            email: String,
            password: String,
        ): TeamActionResult<Unit> = TeamActionResult.Success(Unit)
    }
}
