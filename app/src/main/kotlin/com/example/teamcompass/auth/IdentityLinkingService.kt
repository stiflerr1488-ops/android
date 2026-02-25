package com.example.teamcompass.auth

import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionFailure
import com.example.teamcompass.domain.TeamActionResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

internal data class IdentityLinkingEligibility(
    val shouldPrompt: Boolean,
    val reason: String,
)

internal interface IdentityLinkingService {
    fun evaluateEligibility(): IdentityLinkingEligibility

    suspend fun linkWithEmail(
        email: String,
        password: String,
    ): TeamActionResult<Unit>
}

internal object NoOpIdentityLinkingService : IdentityLinkingService {
    override fun evaluateEligibility(): IdentityLinkingEligibility {
        return IdentityLinkingEligibility(
            shouldPrompt = false,
            reason = "disabled",
        )
    }

    override suspend fun linkWithEmail(
        email: String,
        password: String,
    ): TeamActionResult<Unit> {
        return TeamActionResult.Failure(
            TeamActionFailure(
                error = TeamActionError.INVALID_INPUT,
                message = "Identity linking is disabled",
            )
        )
    }
}

internal class FirebaseIdentityLinkingService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : IdentityLinkingService {
    override fun evaluateEligibility(): IdentityLinkingEligibility {
        val user = auth.currentUser
        return when {
            user == null -> IdentityLinkingEligibility(shouldPrompt = false, reason = "missing_user")
            !user.isAnonymous -> IdentityLinkingEligibility(shouldPrompt = false, reason = "already_linked")
            else -> IdentityLinkingEligibility(shouldPrompt = true, reason = "anonymous_user")
        }
    }

    override suspend fun linkWithEmail(
        email: String,
        password: String,
    ): TeamActionResult<Unit> {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            return TeamActionResult.Failure(
                TeamActionFailure(
                    error = TeamActionError.INVALID_INPUT,
                    message = "email must not be blank",
                )
            )
        }
        if (password.length < 6) {
            return TeamActionResult.Failure(
                TeamActionFailure(
                    error = TeamActionError.INVALID_INPUT,
                    message = "password must be at least 6 characters",
                )
            )
        }

        val user = auth.currentUser
        if (user == null) {
            return TeamActionResult.Failure(
                TeamActionFailure(
                    error = TeamActionError.NOT_FOUND,
                    message = "No authenticated user",
                )
            )
        }
        if (!user.isAnonymous) {
            return TeamActionResult.Success(Unit)
        }

        return runCatching {
            user.linkWithCredential(
                EmailAuthProvider.getCredential(normalizedEmail, password)
            ).await()
        }.fold(
            onSuccess = { TeamActionResult.Success(Unit) },
            onFailure = { TeamActionResult.Failure(it.toIdentityFailure()) },
        )
    }
}

private fun Throwable.toIdentityFailure(): TeamActionFailure {
    return when (this) {
        is FirebaseNetworkException -> TeamActionFailure(
            error = TeamActionError.NETWORK,
            message = message,
            cause = this,
        )
        is FirebaseAuthUserCollisionException -> TeamActionFailure(
            error = TeamActionError.COLLISION,
            message = message,
            cause = this,
        )
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthWeakPasswordException,
        -> TeamActionFailure(
            error = TeamActionError.INVALID_INPUT,
            message = message,
            cause = this,
        )
        else -> TeamActionFailure(
            error = TeamActionError.UNKNOWN,
            message = message,
            cause = this,
        )
    }
}
