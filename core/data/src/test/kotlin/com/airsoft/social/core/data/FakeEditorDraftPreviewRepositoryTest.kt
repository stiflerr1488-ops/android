package com.airsoft.social.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FakeEditorDraftPreviewRepositoryTest {

    private val repository = FakeEditorDraftPreviewRepository()

    @Test
    fun `returns seeded drafts used by nav`() {
        assertNotNull(repository.getTeamDraft("draft-team-ew-001"))
        assertNotNull(repository.getEventDraft("draft-event-night-raid"))
        assertNotNull(repository.getMarketplaceDraft("draft-listing-m4a1-001"))
    }

    @Test
    fun `returns expected draft defaults`() {
        val teamDraft = repository.getTeamDraft("draft-team-ew-001")
        val eventDraft = repository.getEventDraft("draft-event-night-raid")
        val listingDraft = repository.getMarketplaceDraft("draft-listing-m4a1-001")

        assertEquals("Роли", teamDraft?.suggestedStep)
        assertEquals("Правила", eventDraft?.suggestedStep)
        assertEquals("Поля", listingDraft?.suggestedStep)
    }
}
