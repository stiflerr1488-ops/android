package com.airsoft.social.core.auth

import com.airsoft.social.core.model.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryAuthGatewayTest {
    @Test
    fun `signIn updates auth state to signed in`() = runTest {
        val gateway = InMemoryAuthGateway()
        gateway.registerWithEmail(
            email = "ghost@example.com",
            password = "password123",
            displayName = "Ghost",
        )
        gateway.signOut()

        val result = gateway.signInWithEmail(
            email = "ghost@example.com",
            password = "password123",
        )

        assertTrue(result is AuthResult.Success)
        assertTrue(gateway.authState.first() is AuthState.SignedIn)
    }

    @Test
    fun `signOut returns signed out state`() = runTest {
        val gateway = InMemoryAuthGateway()
        gateway.signInGuest(displayName = "Ghost")

        gateway.signOut()

        assertTrue(gateway.authState.first() is AuthState.SignedOut)
    }
}

