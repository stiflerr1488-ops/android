package com.airsoft.social.feature.marketplace.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import com.airsoft.social.feature.marketplace.api.MarketplaceFeatureApi

data class MarketplaceListRow(
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class MarketplaceUiState(
    val categoryFilters: List<String> = listOf("All", "AEGs", "Gear", "Uniform", "Optics", "Radios", "Trade"),
    val selectedCategoryFilter: String = "All",
    val quickFilters: List<String> = listOf("Moscow", "SPB", "Up to 10k", "Shipping", "New"),
    val selectedQuickFilter: String = "Moscow",
    val listingsFeed: List<MarketplaceListRow> = listOf(
        MarketplaceListRow("M4A1 Cyma + 3 magazines", "Moscow | Great condition | Pickup/Shipping", "18 500 RUB"),
        MarketplaceListRow("Plate carrier + pouches", "Kazan | Negotiable | Size M/L", "9 000 RUB"),
        MarketplaceListRow("EOTech replica 552", "SPB | CDEK/Post | Tested", "4 200 RUB"),
        MarketplaceListRow("Baofeng UV-5R pair", "Ekaterinburg | New batteries", "5 500 RUB"),
    ),
    val myListings: List<MarketplaceListRow> = listOf(
        MarketplaceListRow("FAST helmet (draft)", "Photos uploaded | awaiting publish", "Draft"),
        MarketplaceListRow("Mechanix gloves", "Published | 14 views | 2 messages", "Active"),
    ),
)

sealed interface MarketplaceAction {
    data class SelectCategoryFilter(val filter: String) : MarketplaceAction
    data class SelectQuickFilter(val filter: String) : MarketplaceAction
    data object OpenListingDetailDemoClicked : MarketplaceAction
    data object OpenCreateListingDemoClicked : MarketplaceAction
}

class MarketplaceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    fun onAction(action: MarketplaceAction) {
        when (action) {
            is MarketplaceAction.SelectCategoryFilter -> {
                _uiState.value = _uiState.value.copy(selectedCategoryFilter = action.filter)
            }
            is MarketplaceAction.SelectQuickFilter -> {
                _uiState.value = _uiState.value.copy(selectedQuickFilter = action.filter)
            }
            MarketplaceAction.OpenListingDetailDemoClicked -> Unit
            MarketplaceAction.OpenCreateListingDemoClicked -> Unit
        }
    }
}

@Composable
fun MarketplacePlaceholderScreen(
    onOpenListingDetailDemo: () -> Unit = {},
    onOpenCreateListingDemo: () -> Unit = {},
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
) {
    val uiState by marketplaceViewModel.uiState.collectAsState()

    MarketplaceScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                MarketplaceAction.OpenListingDetailDemoClicked -> onOpenListingDetailDemo()
                MarketplaceAction.OpenCreateListingDemoClicked -> onOpenCreateListingDemo()
                is MarketplaceAction.SelectCategoryFilter -> marketplaceViewModel.onAction(action)
                is MarketplaceAction.SelectQuickFilter -> marketplaceViewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun MarketplaceScreen(
    uiState: MarketplaceUiState,
    onAction: (MarketplaceAction) -> Unit,
) {
    WireframePage(
        title = MarketplaceFeatureApi.contract.title,
        subtitle = "Marketplace skeleton (Avito-like): categories, filters, listings, and publish flow.",
        primaryActionLabel = "Post Listing",
    ) {
        WireframeSection(
            title = "Categories and Filters",
            subtitle = "Future filters by price, city, condition, and shipping.",
        ) {
            WireframeChipRow(
                labels = uiState.categoryFilters.map { filter ->
                    if (filter == uiState.selectedCategoryFilter) "[$filter]" else filter
                },
            )
            WireframeChipRow(
                labels = uiState.quickFilters.map { filter ->
                    if (filter == uiState.selectedQuickFilter) "[$filter]" else filter
                },
            )
        }
        WireframeSection(
            title = "Listings Feed",
            subtitle = "Future feed, cards, saved items, and favorites.",
        ) {
            uiState.listingsFeed.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "My Listings",
            subtitle = "Drafts, published, sold, archived.",
        ) {
            uiState.myListings.forEach { row ->
                WireframeItemRow(row.title, row.subtitle, row.trailing)
            }
        }
        WireframeSection(
            title = "Navigation Targets",
            subtitle = "Secondary pages for listing detail and create listing flows.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(MarketplaceAction.OpenListingDetailDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Listing Detail Page")
                }
                OutlinedButton(
                    onClick = { onAction(MarketplaceAction.OpenCreateListingDemoClicked) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Open Create Listing Page")
                }
                OutlinedButton(
                    onClick = {
                        val nextCategory = if (uiState.selectedCategoryFilter == "All") "AEGs" else "All"
                        onAction(MarketplaceAction.SelectCategoryFilter(nextCategory))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Category Filter")
                }
                OutlinedButton(
                    onClick = {
                        val nextQuick = if (uiState.selectedQuickFilter == "Moscow") "Shipping" else "Moscow"
                        onAction(MarketplaceAction.SelectQuickFilter(nextQuick))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Text("Toggle Quick Filter")
                }
            }
        }
    }
}

@Composable
fun MarketplaceListingDetailSkeletonScreen() {
    WireframePage(
        title = "Listing Detail",
        subtitle = "Skeleton for listing page: gallery, specs, seller profile, delivery/payment options.",
        primaryActionLabel = "Message Seller",
    ) {
        WireframeSection(
            title = "Listing Header",
            subtitle = "Title, price, city, condition, publish date, views.",
        ) {
            WireframeItemRow("M4A1 Cyma + 3 magazines", "Moscow | Great condition | 14 views", "18 500 RUB")
            WireframeChipRow(listOf("Pickup", "Shipping", "Negotiable", "Video check"))
        }
        WireframeSection(
            title = "Specs and Completeness",
            subtitle = "Tech specs, included items, defects, upgrade notes.",
        ) {
            WireframeItemRow("Configuration", "AEG | M110 | metal hop-up chamber")
            WireframeItemRow("Included", "3 mags, battery, charger, sling")
            WireframeItemRow("Condition", "Minor body scratches, no functional issues")
        }
        WireframeSection(
            title = "Seller and Safety",
            subtitle = "Seller card, rating, verified signals, meeting rules.",
        ) {
            WireframeItemRow("Seller", "Teiwaz_ | 4.9 rating | 27 completed deals")
            WireframeItemRow("Meetup", "Public place / field meetup only")
            WireframeItemRow("Warranty", "No warranty, functional demo on handoff")
        }
    }
}

@Composable
fun MarketplaceCreateListingSkeletonScreen() {
    WireframePage(
        title = "Create Listing",
        subtitle = "Skeleton for posting flow: photos, category, fields, pricing, moderation.",
        primaryActionLabel = "Submit for Review",
    ) {
        WireframeSection(
            title = "Media and Category",
            subtitle = "Photos/video, category, condition, tags.",
        ) {
            WireframeItemRow("Photos", "Upload area placeholder (up to 10)")
            WireframeItemRow("Category", "AEG / Gear / Uniform / Optics ...")
            WireframeItemRow("Condition", "New / Used / Parts")
        }
        WireframeSection(
            title = "Listing Fields",
            subtitle = "Title, description, price, city, shipping methods.",
        ) {
            WireframeItemRow("Title", "Text field placeholder")
            WireframeItemRow("Description", "Rich text / bullet hints placeholder")
            WireframeItemRow("Price", "Number + currency + negotiable toggle")
            WireframeItemRow("Delivery", "Pickup / CDEK / Post / courier")
        }
        WireframeSection(
            title = "Publishing Controls",
            subtitle = "Preview, moderation notes, draft saving, bump options.",
        ) {
            WireframeChipRow(listOf("Preview", "Save draft", "Moderation note", "Promote later"))
        }
    }
}
