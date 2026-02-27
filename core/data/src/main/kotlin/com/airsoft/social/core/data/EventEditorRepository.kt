package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.model.GameEvent
import com.airsoft.social.core.model.RegistrationStatus

data class EventEditorSeed(
    val editorMode: EditorMode,
    val editorRefId: String,
    val suggestedStep: String,
    val manualApprovalEnabled: Boolean,
)

interface EventEditorRepository {
    fun seed(editorMode: EditorMode, editorRefId: String): EventEditorSeed?
}

class PreviewEventEditorRepository(
    private val socialPreviewRepository: SocialPreviewRepository,
    private val editorDraftPreviewRepository: EditorDraftPreviewRepository,
) : EventEditorRepository {
    override fun seed(editorMode: EditorMode, editorRefId: String): EventEditorSeed? {
        return when (editorMode) {
            EditorMode.Create, EditorMode.Draft -> {
                editorDraftPreviewRepository.getEventDraft(editorRefId)?.toSeed(editorMode)
            }

            EditorMode.Edit -> {
                socialPreviewRepository.getEvent(editorRefId)?.toSeed(editorMode)
            }
        }
    }

    private fun EventDraftPreview.toSeed(editorMode: EditorMode): EventEditorSeed =
        EventEditorSeed(
            editorMode = editorMode,
            editorRefId = refId,
            suggestedStep = suggestedStep,
            manualApprovalEnabled = manualApprovalEnabled,
        )

    private fun GameEvent.toSeed(editorMode: EditorMode): EventEditorSeed =
        EventEditorSeed(
            editorMode = editorMode,
            editorRefId = id,
            suggestedStep = if (rules.isNullOrBlank()) "Настройка" else "Правила",
            manualApprovalEnabled = registrationStatus != RegistrationStatus.OPEN,
        )
}
