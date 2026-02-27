package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class PreviewTeamEditorRepositoryTest {

    private val repository = PreviewTeamEditorRepository(
        socialPreviewRepository = DemoSocialRepositoryProvider.repository,
        editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
    )

    @Test
    fun `returns seeded draft context for create mode`() {
        val seed = repository.seed(
            editorMode = EditorMode.Create,
            editorRefId = "draft-team-ew-001",
        )

        assertNotNull(seed)
        assertEquals(EditorMode.Create, seed?.editorMode)
        assertEquals("draft-team-ew-001", seed?.editorRefId)
        assertFalse(seed?.autoApproveEnabled ?: true)
    }

    @Test
    fun `returns seeded edit context from team preview`() {
        val seed = repository.seed(
            editorMode = EditorMode.Edit,
            editorRefId = "ew-easy-winner",
        )

        assertNotNull(seed)
        assertEquals(EditorMode.Edit, seed?.editorMode)
        assertEquals("ew-easy-winner", seed?.editorRefId)
    }
}
