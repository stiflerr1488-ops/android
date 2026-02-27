package com.airsoft.social.core.data

import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.model.MarketplaceListing

data class MarketplaceEditorSeed(
    val editorMode: EditorMode,
    val editorRefId: String,
    val suggestedStep: String,
    val shippingEnabled: Boolean,
    val negotiableEnabled: Boolean,
)

interface MarketplaceEditorRepository {
    fun seed(editorMode: EditorMode, editorRefId: String): MarketplaceEditorSeed?
}

class PreviewMarketplaceEditorRepository(
    private val socialPreviewRepository: SocialPreviewRepository,
    private val editorDraftPreviewRepository: EditorDraftPreviewRepository,
) : MarketplaceEditorRepository {
    override fun seed(editorMode: EditorMode, editorRefId: String): MarketplaceEditorSeed? {
        return when (editorMode) {
            EditorMode.Create, EditorMode.Draft -> {
                editorDraftPreviewRepository.getMarketplaceDraft(editorRefId)?.toSeed(editorMode)
            }

            EditorMode.Edit -> {
                socialPreviewRepository.getMarketplaceListing(editorRefId)?.toSeed(editorMode)
            }
        }
    }

    private fun MarketplaceDraftPreview.toSeed(editorMode: EditorMode): MarketplaceEditorSeed =
        MarketplaceEditorSeed(
            editorMode = editorMode,
            editorRefId = refId,
            suggestedStep = suggestedStep,
            shippingEnabled = shippingEnabled,
            negotiableEnabled = negotiableEnabled,
        )

    private fun MarketplaceListing.toSeed(editorMode: EditorMode): MarketplaceEditorSeed =
        MarketplaceEditorSeed(
            editorMode = editorMode,
            editorRefId = id,
            suggestedStep = if (images.isNotEmpty()) "Публикация" else "Медиа",
            shippingEnabled = deliveryAvailable,
            negotiableEnabled = isNegotiable,
        )
}

