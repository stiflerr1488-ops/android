package com.airsoft.social.feature.marketplace.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airsoft.social.core.data.MarketplaceRepository
import com.airsoft.social.core.model.ListingCategory
import com.airsoft.social.core.model.MarketplaceListing
import com.airsoft.social.core.ui.WireframeChipRow
import com.airsoft.social.core.ui.WireframeItemRow
import com.airsoft.social.core.ui.WireframeMetricRow
import com.airsoft.social.core.ui.WireframePage
import com.airsoft.social.core.ui.WireframeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarketplaceSearchRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val trailing: String? = null,
)

data class MarketplaceSearchUiState(
    val query: String = "",
    val scopes: List<String> = listOf("Лента", "Мои объявления"),
    val selectedScope: String = "Лента",
    val filters: List<String> = listOf("Все", "Приводы", "Снаряга", "Оптика", "Расходники"),
    val selectedFilter: String = "Все",
    val feedRows: List<MarketplaceSearchRow> = emptyList(),
    val myRows: List<MarketplaceSearchRow> = emptyList(),
)

sealed interface MarketplaceSearchAction {
    data object CycleDemoQuery : MarketplaceSearchAction
    data object ToggleScope : MarketplaceSearchAction
    data object ToggleFilter : MarketplaceSearchAction
    data object ClearQuery : MarketplaceSearchAction
    data object OpenFirstListingClicked : MarketplaceSearchAction
}

@HiltViewModel
class MarketplaceSearchViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceSearchUiState())
    val uiState: StateFlow<MarketplaceSearchUiState> = _uiState.asStateFlow()

    private var feed: List<MarketplaceListing> = emptyList()
    private var myListings: List<MarketplaceListing> = emptyList()
    private var feedJob: Job? = null
    private var myJob: Job? = null

    init {
        feedJob = viewModelScope.launch {
            marketplaceRepository.observeMarketplaceFeed().collect {
                feed = it
                rebuildUiState()
            }
        }
        myJob = viewModelScope.launch {
            marketplaceRepository.observeMyListings().collect {
                myListings = it
                rebuildUiState()
            }
        }
    }

    fun onAction(action: MarketplaceSearchAction) {
        when (action) {
            MarketplaceSearchAction.CycleDemoQuery -> {
                val next = when (_uiState.value.query) {
                    "" -> "m4"
                    "m4" -> "аккум"
                    else -> ""
                }
                _uiState.value = _uiState.value.copy(query = next)
                rebuildUiState()
            }
            MarketplaceSearchAction.ToggleScope -> {
                _uiState.value = _uiState.value.copy(
                    selectedScope = if (_uiState.value.selectedScope == "Лента") "Мои объявления" else "Лента",
                )
            }
            MarketplaceSearchAction.ToggleFilter -> {
                val filters = _uiState.value.filters
                val idx = filters.indexOf(_uiState.value.selectedFilter)
                _uiState.value = _uiState.value.copy(
                    selectedFilter = filters[(if (idx < 0) 0 else idx + 1) % filters.size],
                )
                rebuildUiState()
            }
            MarketplaceSearchAction.ClearQuery -> {
                _uiState.value = _uiState.value.copy(query = "")
                rebuildUiState()
            }
            MarketplaceSearchAction.OpenFirstListingClicked -> Unit
        }
    }

    private fun rebuildUiState() {
        val query = _uiState.value.query.trim().lowercase(Locale.getDefault())
        val filter = _uiState.value.selectedFilter
        _uiState.value = _uiState.value.copy(
            feedRows = filterListings(feed, query, filter),
            myRows = filterListings(myListings, query, filter),
        )
    }

    private fun filterListings(
        listings: List<MarketplaceListing>,
        query: String,
        filter: String,
    ): List<MarketplaceSearchRow> = listings
        .asSequence()
        .filter { matchesFilter(it, filter) }
        .filter { listing ->
            query.isBlank() || listOf(
                listing.title,
                listing.description,
                listing.brand.orEmpty(),
                listing.model.orEmpty(),
                listing.city,
            ).any { it.lowercase(Locale.getDefault()).contains(query) }
        }
        .sortedByDescending { it.updatedAt.time }
        .map { listing ->
            MarketplaceSearchRow(
                id = listing.id,
                title = listing.title,
                subtitle = listOf(
                    listing.city,
                    listing.category.name,
                    listing.sellerCallsign,
                ).joinToString(" | "),
                trailing = listing.price.toInt().toString(),
            )
        }
        .toList()
}

@Composable
fun MarketplaceSearchRoute(
    onOpenListingDetail: (String) -> Unit,
    viewModel: MarketplaceSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    MarketplaceSearchScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                MarketplaceSearchAction.OpenFirstListingClicked -> {
                    (uiState.feedRows.firstOrNull() ?: uiState.myRows.firstOrNull())?.id?.let(onOpenListingDetail)
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
private fun MarketplaceSearchScreen(
    uiState: MarketplaceSearchUiState,
    onAction: (MarketplaceSearchAction) -> Unit,
) {
    WireframePage(
        title = "Поиск товаров",
        subtitle = "Контекстный поиск барахолки: ищет только объявления/товары, без команд и событий.",
        primaryActionLabel = "Открыть первое объявление",
        onPrimaryAction = { onAction(MarketplaceSearchAction.OpenFirstListingClicked) },
    ) {
        WireframeSection(title = "Запрос", subtitle = "Поиск по названию, бренду, модели, городу и описанию.") {
            WireframeMetricRow(
                items = listOf(
                    "Запрос" to if (uiState.query.isBlank()) "(пусто)" else uiState.query,
                    "Область" to uiState.selectedScope,
                    "Фильтр" to uiState.selectedFilter,
                ),
            )
            WireframeChipRow(listOf("m4", "аккум", "оптика", "бронежилет", "шары"))
        }
        WireframeSection(title = "Область и фильтры", subtitle = "Лента marketplace или мои объявления.") {
            WireframeChipRow(uiState.scopes.map { if (it == uiState.selectedScope) "[$it]" else it })
            WireframeChipRow(uiState.filters.map { if (it == uiState.selectedFilter) "[$it]" else it })
        }
        WireframeSection(title = "Лента объявлений", subtitle = "Контекстная выдача по барахолке.") {
            if (uiState.feedRows.isEmpty()) {
                WireframeItemRow("Ничего не найдено", "Измените запрос или фильтр")
            } else {
                uiState.feedRows.forEach { WireframeItemRow(it.title, it.subtitle, it.trailing) }
            }
        }
        WireframeSection(title = "Мои объявления", subtitle = "Быстрый поиск по своим карточкам.") {
            if (uiState.myRows.isEmpty()) {
                WireframeItemRow("Моих объявлений нет", "Или запрос их не нашёл")
            } else {
                uiState.myRows.forEach { WireframeItemRow(it.title, it.subtitle, it.trailing) }
            }
        }
        WireframeSection(title = "Действия", subtitle = "Проверка контекстного поиска барахолки.") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(MarketplaceSearchAction.CycleDemoQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Подставить demo-запрос")
                }
                OutlinedButton(onClick = { onAction(MarketplaceSearchAction.ToggleScope) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Переключить область")
                }
                OutlinedButton(onClick = { onAction(MarketplaceSearchAction.ToggleFilter) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Переключить фильтр")
                }
                OutlinedButton(onClick = { onAction(MarketplaceSearchAction.ClearQuery) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text("Очистить запрос")
                }
            }
        }
    }
}

private fun matchesFilter(listing: MarketplaceListing, filter: String): Boolean = when (filter) {
    "Приводы" -> listing.category == ListingCategory.EQUIPMENT
    "Снаряга" -> listing.category == ListingCategory.CLOTHING || listing.category == ListingCategory.RIGGING
    "Оптика" -> listing.category == ListingCategory.OPTICS
    "Расходники" -> listing.category == ListingCategory.CONSUMABLES
    else -> true
}
