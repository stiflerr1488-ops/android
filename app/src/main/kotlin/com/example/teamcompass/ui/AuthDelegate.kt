package com.example.teamcompass.ui

import com.google.firebase.auth.FirebaseAuth

internal interface AuthGateway {
    val currentUid: String?

    fun signInAnonymously(
        onSuccess: (uid: String?) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}

internal class FirebaseAuthGateway(
    private val auth: FirebaseAuth,
) : AuthGateway {
    override val currentUid: String?
        get() = auth.currentUser?.uid

    override fun signInAnonymously(
        onSuccess: (uid: String?) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                onSuccess(result.user?.uid)
            }
            .addOnFailureListener { err ->
                onFailure(err)
            }
    }
}

internal class AuthDelegate(
    private val authGateway: AuthGateway,
) {
    fun ensureAuth(
        onReady: (uid: String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val existingUid = authGateway.currentUid
        if (!existingUid.isNullOrBlank()) {
            onReady(existingUid)
            return
        }
        authGateway.signInAnonymously(
            onSuccess = { uid ->
                if (uid.isNullOrBlank()) {
                    onFailure(IllegalStateException("Firebase anonymous auth returned empty uid"))
                } else {
                    onReady(uid)
                }
            },
            onFailure = onFailure,
        )
    }
}
