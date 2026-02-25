package com.example.teamcompass.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthDelegateTest {

    @Test
    fun ensureAuth_existingUid_callsOnReady_withoutSignIn() {
        val gateway = FakeAuthGateway(currentUid = "uid-existing")
        val delegate = AuthDelegate(authGateway = gateway)
        var readyUid: String? = null
        var failure: Throwable? = null

        delegate.ensureAuth(
            onReady = { uid -> readyUid = uid },
            onFailure = { err -> failure = err },
        )

        assertEquals("uid-existing", readyUid)
        assertNull(failure)
        assertEquals(0, gateway.signInCalls)
    }

    @Test
    fun ensureAuth_noUid_signInSuccess_callsOnReady() {
        val gateway = FakeAuthGateway(currentUid = null).apply {
            signInResultUid = "uid-new"
        }
        val delegate = AuthDelegate(authGateway = gateway)
        var readyUid: String? = null
        var failure: Throwable? = null

        delegate.ensureAuth(
            onReady = { uid -> readyUid = uid },
            onFailure = { err -> failure = err },
        )

        assertEquals("uid-new", readyUid)
        assertNull(failure)
        assertEquals(1, gateway.signInCalls)
    }

    @Test
    fun ensureAuth_noUid_signInFailure_callsOnFailure() {
        val expected = IllegalStateException("auth failed")
        val gateway = FakeAuthGateway(currentUid = null).apply {
            signInError = expected
        }
        val delegate = AuthDelegate(authGateway = gateway)
        var readyUid: String? = null
        var failure: Throwable? = null

        delegate.ensureAuth(
            onReady = { uid -> readyUid = uid },
            onFailure = { err -> failure = err },
        )

        assertNull(readyUid)
        assertEquals(expected, failure)
        assertEquals(1, gateway.signInCalls)
    }
}

private class FakeAuthGateway(
    override val currentUid: String?,
) : AuthGateway {
    var signInCalls: Int = 0
    var signInResultUid: String? = null
    var signInError: Throwable? = null

    override fun signInAnonymously(
        onSuccess: (uid: String?) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        signInCalls += 1
        val error = signInError
        if (error != null) {
            onFailure(error)
            return
        }
        onSuccess(signInResultUid)
    }
}
