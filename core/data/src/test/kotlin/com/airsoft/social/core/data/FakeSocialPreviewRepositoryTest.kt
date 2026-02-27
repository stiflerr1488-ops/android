package com.airsoft.social.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSocialPreviewRepositoryTest {

    private val repository = FakeSocialPreviewRepository()

    @Test
    fun `contains seeded ids used by nav demos`() {
        assertNotNull(repository.getUser("self"))
        assertNotNull(repository.getUser("ghost"))
        assertNotNull(repository.getChat("team-ew-general"))
        assertNotNull(repository.getTeam("ew-easy-winner"))
        assertNotNull(repository.getEvent("night-raid-north"))
        assertNotNull(repository.getMarketplaceListing("m4a1-cyma-3mags"))
    }

    @Test
    fun `seeded event and listing are open and published`() {
        val event = repository.getEvent("night-raid-north")
        val listing = repository.getMarketplaceListing("m4a1-cyma-3mags")

        assertEquals("Night Raid North", event?.title)
        assertTrue(event?.currentPlayers ?: 0 > 0)
        assertEquals("M4A1 Cyma + 3 магазина", listing?.title)
        assertTrue(listing?.deliveryAvailable == true)
    }

    @Test
    fun `seeded team chat has messages`() {
        val messages = repository.listChatMessages("team-ew-general")

        assertTrue(messages.isNotEmpty())
        assertEquals("team-ew-general", messages.first().chatId)
    }
}
