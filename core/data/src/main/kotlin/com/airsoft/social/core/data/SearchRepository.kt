package com.airsoft.social.core.data

import com.airsoft.social.core.model.GameEvent
import com.airsoft.social.core.model.MarketplaceListing
import com.airsoft.social.core.model.Team
import com.airsoft.social.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class SearchRecentQuery(
    val id: String,
    val text: String,
    val categoryKey: String,
)

data class SavedSearchPreview(
    val id: String,
    val title: String,
    val queryText: String,
    val categoryKey: String,
    val filtersSummary: String,
)

interface SearchRepository {
    fun observeUsers(): Flow<List<User>>
    fun observeTeams(): Flow<List<Team>>
    fun observeEvents(): Flow<List<GameEvent>>
    fun observeMarketplaceListings(): Flow<List<MarketplaceListing>>
    fun observeRecentQueries(): Flow<List<SearchRecentQuery>>
    fun observeSavedSearches(): Flow<List<SavedSearchPreview>>
}

class PreviewSearchRepository(
    previewRepository: SocialPreviewRepository,
) : SearchRepository {
    private val users = MutableStateFlow(previewRepository.listUsers())
    private val teams = MutableStateFlow(previewRepository.listTeams())
    private val events = MutableStateFlow(previewRepository.listEvents())
    private val listings = MutableStateFlow(previewRepository.listMarketplaceListings())
    private val recentQueries = MutableStateFlow(
        listOf(
            SearchRecentQuery("recent-players-nearby", "игроки рядом", "players"),
            SearchRecentQuery("recent-team-cqb", "команда cqb", "teams"),
            SearchRecentQuery("recent-raid-weekend", "игра выходные москва", "events"),
            SearchRecentQuery("recent-market-m4", "m4a1 cyma", "marketplace"),
        ),
    )
    private val savedSearches = MutableStateFlow(
        listOf(
            SavedSearchPreview(
                id = "saved-medics-msk",
                title = "Медики в Москве",
                queryText = "медик",
                categoryKey = "players",
                filtersSummary = "Москва · Онлайн · Верифицированные",
            ),
            SavedSearchPreview(
                id = "saved-events-night",
                title = "Ночные игры",
                queryText = "night raid",
                categoryKey = "events",
                filtersSummary = "100 км · Выходные",
            ),
            SavedSearchPreview(
                id = "saved-market-aeg",
                title = "AEG до 20к",
                queryText = "aeg",
                categoryKey = "marketplace",
                filtersSummary = "До 20 000 ₽ · Доставка",
            ),
        ),
    )

    override fun observeUsers(): Flow<List<User>> = users
    override fun observeTeams(): Flow<List<Team>> = teams
    override fun observeEvents(): Flow<List<GameEvent>> = events
    override fun observeMarketplaceListings(): Flow<List<MarketplaceListing>> = listings
    override fun observeRecentQueries(): Flow<List<SearchRecentQuery>> = recentQueries
    override fun observeSavedSearches(): Flow<List<SavedSearchPreview>> = savedSearches
}

