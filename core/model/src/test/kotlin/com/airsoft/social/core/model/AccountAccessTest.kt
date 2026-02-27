package com.airsoft.social.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountAccessTest {

    @Test
    fun `guest session has read only access`() {
        val access = UserSession(
            userId = "guest",
            displayName = "Nomad",
            email = null,
        ).toAccountAccess()

        assertTrue(access.isGuest)
        assertFalse(access.canSendChatMessages)
        assertFalse(access.canEditProfile)
        assertFalse(access.canCreateMarketplaceListings)
        assertFalse(access.canCreateRideShareListings)
        assertFalse(access.canCreateGameEvents)
        assertFalse(access.canCreateShopListings)
    }

    @Test
    fun `registered user can write social data but not commercial tools`() {
        val access = UserSession(
            userId = "user",
            displayName = "Raven",
            email = "raven@example.com",
        ).toAccountAccess()

        assertTrue(access.isRegistered)
        assertTrue(access.canSendChatMessages)
        assertTrue(access.canEditProfile)
        assertTrue(access.canCreateMarketplaceListings)
        assertTrue(access.canCreateRideShareListings)
        assertFalse(access.canCreateGameEvents)
        assertFalse(access.canCreateShopListings)
    }

    @Test
    fun `commercial role opens game and shop creation`() {
        val access = UserSession(
            userId = "commercial",
            displayName = "ShopOwner",
            email = "shop@example.com",
            accountRoles = setOf(AccountRole.USER, AccountRole.COMMERCIAL),
        ).toAccountAccess()

        assertTrue(access.canCreateGameEvents)
        assertTrue(access.canCreateShopListings)
    }

    @Test
    fun `admin role includes moderation and commercial capabilities`() {
        val access = UserSession(
            userId = "admin",
            displayName = "Admin",
            email = "admin@example.com",
            accountRoles = setOf(AccountRole.ADMIN),
        ).toAccountAccess()

        assertTrue(access.isAdmin)
        assertTrue(access.isModerator)
        assertTrue(access.isCommercial)
        assertTrue(access.canCreateGameEvents)
        assertTrue(access.canCreateShopListings)
    }
}

