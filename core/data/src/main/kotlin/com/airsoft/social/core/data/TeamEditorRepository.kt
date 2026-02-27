package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.model.Team

data class TeamEditorSeed(
    val editorMode: EditorMode,
    val editorRefId: String,
    val suggestedStep: String,
    val autoApproveEnabled: Boolean,
)

interface TeamEditorRepository {
    fun seed(editorMode: EditorMode, editorRefId: String): TeamEditorSeed?
}

class PreviewTeamEditorRepository(
    private val socialPreviewRepository: SocialPreviewRepository,
    private val editorDraftPreviewRepository: EditorDraftPreviewRepository,
) : TeamEditorRepository {
    override fun seed(editorMode: EditorMode, editorRefId: String): TeamEditorSeed? {
        return when (editorMode) {
            EditorMode.Create, EditorMode.Draft -> {
                editorDraftPreviewRepository.getTeamDraft(editorRefId)?.toSeed(editorMode)
            }

            EditorMode.Edit -> {
                socialPreviewRepository.getTeam(editorRefId)?.toSeed(editorMode)
            }
        }
    }

    private fun TeamDraftPreview.toSeed(editorMode: EditorMode): TeamEditorSeed =
        TeamEditorSeed(
            editorMode = editorMode,
            editorRefId = refId,
            suggestedStep = suggestedStep,
            autoApproveEnabled = autoApproveEnabled,
        )

    private fun Team.toSeed(editorMode: EditorMode): TeamEditorSeed =
        TeamEditorSeed(
            editorMode = editorMode,
            editorRefId = id,
            suggestedStep = if (gameStyles.isEmpty()) "База" else "Роли",
            autoApproveEnabled = !requiresApproval,
        )
}
