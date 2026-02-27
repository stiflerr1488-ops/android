package com.airsoft.social.feature.marketplace.impl

import com.airsoft.social.core.data.DemoEditorDraftPreviewRepositoryProvider
import com.airsoft.social.core.data.DemoSocialRepositoryProvider
import com.airsoft.social.core.data.PreviewMarketplaceEditorRepository
import com.airsoft.social.core.data.PreviewMarketplaceRepository
import com.airsoft.social.core.model.EditorMode
import com.airsoft.social.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MarketplaceSecondaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `listing detail viewmodel loads id and toggles flags`() = runTest {
        val viewModel = MarketplaceListingDetailViewModel(
            PreviewMarketplaceRepository(DemoSocialRepositoryProvider.repository),
        )

        viewModel.load("listing-42")
        advanceUntilIdle()
        viewModel.onAction(MarketplaceListingDetailAction.SelectSection("Продавец"))
        viewModel.onAction(MarketplaceListingDetailAction.ToggleNegotiable)
        viewModel.onAction(MarketplaceListingDetailAction.ToggleSellerVerified)

        assertEquals("listing-42", viewModel.uiState.value.listingId)
        assertEquals("Продавец", viewModel.uiState.value.selectedSection)
        assertFalse(viewModel.uiState.value.canNegotiate)
        assertFalse(viewModel.uiState.value.sellerVerified)
    }

    @Test
    fun `marketplace editor viewmodel loads context and toggles controls`() = runTest {
        val viewModel = MarketplaceEditorViewModel(
            PreviewMarketplaceEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )

        viewModel.load(editorMode = EditorMode.Draft, editorRefId = "draft-listing-1")
        viewModel.onAction(MarketplaceEditorAction.SelectStep("Поля"))
        viewModel.onAction(MarketplaceEditorAction.ToggleShipping)
        viewModel.onAction(MarketplaceEditorAction.ToggleNegotiable)

        assertEquals(EditorMode.Draft, viewModel.uiState.value.editorMode)
        assertEquals("draft-listing-1", viewModel.uiState.value.editorRefId)
        assertEquals("Поля", viewModel.uiState.value.selectedStep)
        assertFalse(viewModel.uiState.value.shippingEnabled)
        assertTrue(viewModel.uiState.value.negotiableEnabled)
    }

    @Test
    fun `marketplace editor loads seeded draft context`() = runTest {
        val viewModel = MarketplaceEditorViewModel(
            PreviewMarketplaceEditorRepository(
                socialPreviewRepository = DemoSocialRepositoryProvider.repository,
                editorDraftPreviewRepository = DemoEditorDraftPreviewRepositoryProvider.repository,
            ),
        )
        val seededDraft = DemoEditorDraftPreviewRepositoryProvider.repository
            .getMarketplaceDraft("draft-listing-m4a1-001")
            ?: error("Seed draft not found")

        viewModel.load(editorMode = EditorMode.Create, editorRefId = "draft-listing-m4a1-001")

        assertEquals("draft-listing-m4a1-001", viewModel.uiState.value.editorRefId)
        assertEquals(seededDraft.suggestedStep, viewModel.uiState.value.selectedStep)
        assertTrue(viewModel.uiState.value.shippingEnabled)
        assertTrue(viewModel.uiState.value.negotiableEnabled)
    }
}
