package com.example.teamcompass.ui

import com.example.teamcompass.auth.IdentityLinkingService

/**
 * Handles identity-linking eligibility checks and dedupes prompts per UID.
 */
internal class IdentityLinkingCoordinator(
    private val identityLinkingService: IdentityLinkingService,
    private val nextTraceId: () -> String,
    private val onStart: (traceId: String, teamCode: String, uid: String) -> Unit,
    private val onSuccess: (traceId: String, teamCode: String, uid: String) -> Unit,
    private val onFailure: (traceId: String, teamCode: String, uid: String, err: Throwable) -> Unit,
    private val onEligiblePrompt: (uid: String, teamCode: String, reason: String) -> Unit,
) {
    private var promptTrackedUid: String? = null

    fun evaluate(teamCode: String, uid: String?) {
        val localUid = uid?.trim().orEmpty()
        if (localUid.isEmpty()) return
        if (promptTrackedUid == localUid) return

        val traceId = nextTraceId()
        onStart(traceId, teamCode, localUid)
        runCatching { identityLinkingService.evaluateEligibility() }
            .onSuccess { eligibility ->
                if (eligibility.shouldPrompt) {
                    promptTrackedUid = localUid
                    onEligiblePrompt(localUid, teamCode, eligibility.reason)
                }
                onSuccess(traceId, teamCode, localUid)
            }
            .onFailure { err ->
                onFailure(traceId, teamCode, localUid, err)
            }
    }
}
