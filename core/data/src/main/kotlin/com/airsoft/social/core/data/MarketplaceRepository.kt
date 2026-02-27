package com.airsoft.social.core.data

import com.airsoft.social.core.model.MarketplaceListing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface MarketplaceRepository {
    fun observeMarketplaceFeed(): Flow<List<MarketplaceListing>>
    fun observeMyListings(sellerId: String = DEFAULT_SELLER_ID): Flow<List<MarketplaceListing>>
    fun observeListing(listingId: String): Flow<MarketplaceListing?>

    companion object {
        const val DEFAULT_SELLER_ID: String = "self"
    }
}

class PreviewMarketplaceRepository(
    previewRepository: SocialPreviewRepository,
) : MarketplaceRepository {
    private val listings = MutableStateFlow(previewRepository.listMarketplaceListings())

    override fun observeMarketplaceFeed(): Flow<List<MarketplaceListing>> = listings

    override fun observeMyListings(sellerId: String): Flow<List<MarketplaceListing>> =
        listings.map { list -> list.filter { it.sellerId == sellerId } }

    override fun observeListing(listingId: String): Flow<MarketplaceListing?> =
        listings.map { list -> list.firstOrNull { it.id == listingId } }
}

