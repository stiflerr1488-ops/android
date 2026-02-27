package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewMarketplaceEditorRepositoryTest {

    private val repository = PreviewMarketplaceEditorRepository(
        socialPreviewRepository = DemoSocialRepositoryProvider.repository,
        editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
    )

    @Test
    fun `seed returns draft values for draft mode`() {
        val seed = repository.seed(EditorMode.Draft, "draft-listing-m4a1-001")

        assertNotNull(seed)
        assertEquals(EditorMode.Draft, seed?.editorMode)
        assertEquals("draft-listing-m4a1-001", seed?.editorRefId)
        assertEquals("Поля", seed?.suggestedStep)
        assertTrue(seed?.shippingEnabled == true)
        assertTrue(seed?.negotiableEnabled == true)
    }

    @Test
    fun `seed returns listing values for edit mode`() {
        val seed = repository.seed(EditorMode.Edit, "m4a1-cyma-3mags")

        assertNotNull(seed)
        assertEquals(EditorMode.Edit, seed?.editorMode)
        assertEquals("m4a1-cyma-3mags", seed?.editorRefId)
    }
}
