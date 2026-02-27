package com.airsoft.social.infra.firebase

import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthGatewayAdapter(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthGateway {
    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser.toAuthState())
        }
        trySend(auth.currentUser.toAuthState())
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): AuthResult = runCatching {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: return AuthResult.Failure("Firebase sign-in returned null user")
        AuthResult.Success(user.toUserSession())
    }.getOrElse { throwable ->
        AuthResult.Failure(reason = throwable.message ?: "Firebase sign-in failed")
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: return AuthResult.Failure("Firebase register returned null user")
        val resolvedName = resolveDisplayName(displayName, email, user.displayName)
        if (resolvedName != user.displayName) {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(resolvedName)
                    .build(),
            ).await()
        }
        AuthResult.Success(user.toUserSession())
    }.getOrElse { throwable ->
        AuthResult.Failure(reason = throwable.message ?: "Firebase registration failed")
    }

    override suspend fun signInGuest(
        displayName: String?,
    ): AuthResult = runCatching {
        val user = auth.signInAnonymously().await().user
            ?: return AuthResult.Failure("Firebase guest sign-in returned null user")
        val resolvedName = resolveDisplayName(displayName, user.email, user.displayName)
        if (resolvedName != user.displayName) {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(resolvedName)
                    .build(),
            ).await()
        }
        AuthResult.Success(user.toUserSession())
    }.getOrElse { throwable ->
        AuthResult.Failure(reason = throwable.message ?: "Firebase guest sign-in failed")
    }

    override suspend fun upgradeGuestToEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthResult = runCatching {
        val current = auth.currentUser
            ?: return AuthResult.Failure("No authenticated user to upgrade")
        if (!current.isAnonymous) {
            return AuthResult.Failure("Current session is not guest", recoverable = true)
        }
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        val linked = current.linkWithCredential(credential).await().user
            ?: return AuthResult.Failure("Firebase guest upgrade returned null user")
        val resolvedName = resolveDisplayName(displayName, email, linked.displayName)
        if (resolvedName != linked.displayName) {
            linked.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(resolvedName)
                    .build(),
            ).await()
        }
        AuthResult.Success(linked.toUserSession())
    }.getOrElse { throwable ->
        AuthResult.Failure(reason = throwable.message ?: "Firebase guest upgrade failed")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun currentSession(): UserSession? = auth.currentUser?.toUserSession()
}

private fun FirebaseUser?.toAuthState(): AuthState = this?.let { AuthState.SignedIn(it.toUserSession()) } ?: AuthState.SignedOut

private fun FirebaseUser.toUserSession(): UserSession = UserSession(
    userId = uid,
    displayName = displayName ?: email?.substringBefore('@') ?: "Operator",
    email = email,
    isGuest = isAnonymous,
)

private fun resolveDisplayName(
    requested: String?,
    email: String?,
    existing: String?,
): String {
    val trimmed = requested?.trim().orEmpty()
    if (trimmed.isNotEmpty()) return trimmed
    val emailPrefix = email?.substringBefore('@').orEmpty().trim()
    if (emailPrefix.isNotEmpty()) return emailPrefix
    val existingTrimmed = existing?.trim().orEmpty()
    if (existingTrimmed.isNotEmpty()) return existingTrimmed
    return "Operator"
}
