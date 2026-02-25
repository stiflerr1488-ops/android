package com.airsoft.social.infra.firebase

import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.AuthResult
import com.airsoft.social.core.auth.SignInRequest
import com.airsoft.social.core.model.AuthState
import com.airsoft.social.core.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    override suspend fun signIn(request: SignInRequest): AuthResult = runCatching {
        val email = request.email
        val password = request.password
        val user = if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            auth.signInWithEmailAndPassword(email, password).await().user
        } else {
            auth.signInAnonymously().await().user
        } ?: return AuthResult.Failure("Firebase sign-in returned null user")
        AuthResult.Success(user.toUserSession())
    }.getOrElse { throwable ->
        AuthResult.Failure(reason = throwable.message ?: "Firebase sign-in failed")
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
)
