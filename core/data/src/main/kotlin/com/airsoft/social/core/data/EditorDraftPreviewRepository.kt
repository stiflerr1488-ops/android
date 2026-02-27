package com.airsoft.social.core.data

data class TeamDraftPreview(
    val refId: String,
    val suggestedStep: String,
    val autoApproveEnabled: Boolean,
    val templateName: String? = null,
)

data class EventDraftPreview(
    val refId: String,
    val suggestedStep: String,
    val manualApprovalEnabled: Boolean,
    val title: String? = null,
)

data class MarketplaceDraftPreview(
    val refId: String,
    val suggestedStep: String,
    val shippingEnabled: Boolean,
    val negotiableEnabled: Boolean,
    val title: String? = null,
)

interface EditorDraftPreviewRepository {
    fun getTeamDraft(refId: String): TeamDraftPreview?
    fun getEventDraft(refId: String): EventDraftPreview?
    fun getMarketplaceDraft(refId: String): MarketplaceDraftPreview?
}

object DemoEditorDraftPreviewRepositoryProvider {
    val repository: EditorDraftPreviewRepository = FakeEditorDraftPreviewRepository()
}

class FakeEditorDraftPreviewRepository : EditorDraftPreviewRepository {
    private val teamDrafts = mapOf(
        "draft-team-ew-001" to TeamDraftPreview(
            refId = "draft-team-ew-001",
            suggestedStep = "Роли",
            autoApproveEnabled = false,
            templateName = "Штурмовой состав",
        ),
    )

    private val eventDrafts = mapOf(
        "draft-event-night-raid" to EventDraftPreview(
            refId = "draft-event-night-raid",
            suggestedStep = "Правила",
            manualApprovalEnabled = true,
            title = "Night Raid North",
        ),
    )

    private val marketplaceDrafts = mapOf(
        "draft-listing-m4a1-001" to MarketplaceDraftPreview(
            refId = "draft-listing-m4a1-001",
            suggestedStep = "Поля",
            shippingEnabled = true,
            negotiableEnabled = true,
            title = "M4A1 Cyma + 3 магазина",
        ),
    )

    override fun getTeamDraft(refId: String): TeamDraftPreview? = teamDrafts[refId]

    override fun getEventDraft(refId: String): EventDraftPreview? = eventDrafts[refId]

    override fun getMarketplaceDraft(refId: String): MarketplaceDraftPreview? = marketplaceDrafts[refId]
}
