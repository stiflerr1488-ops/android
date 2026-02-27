package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewEventEditorRepositoryTest {

    private val repository = PreviewEventEditorRepository(
        socialPreviewRepository = DemoSocialRepositoryProvider.repository,
        editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
    )

    @Test
    fun `returns seeded create draft context`() {
        val seed = repository.seed(EditorMode.Create, "draft-event-night-raid")

        assertNotNull(seed)
        assertEquals(EditorMode.Create, seed?.editorMode)
        assertEquals("draft-event-night-raid", seed?.editorRefId)
        assertTrue(seed?.manualApprovalEnabled ?: false)
    }

    @Test
    fun `returns seeded edit context from event preview`() {
        val seed = repository.seed(EditorMode.Edit, "night-raid-north")

        assertNotNull(seed)
        assertEquals(EditorMode.Edit, seed?.editorMode)
        assertEquals("night-raid-north", seed?.editorRefId)
    }
}
