package com.airsoft.social.feature.directories.impl

import com.airsoft.social.core.model.EditorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectoryScreensViewModelTest {
    @Test
    fun `game calendar viewmodel toggles and cycles state`() {
        val viewModel = GameCalendarViewModel()

        val initialMode = viewModel.uiState.value.periodMode
        val initialOnlyMine = viewModel.uiState.value.onlyMyGames

        viewModel.onAction(GameCalendarAction.CyclePeriodMode)
        viewModel.onAction(GameCalendarAction.ToggleOnlyMyGames)

        assertTrue(viewModel.uiState.value.periodMode != initialMode)
        assertTrue(viewModel.uiState.value.onlyMyGames != initialOnlyMine)
    }

    @Test
    fun `game calendar detail viewmodel loads and cycles tab`() {
        val viewModel = GameCalendarDetailViewModel()

        viewModel.load("night-raid-north")
        val initial = viewModel.uiState.value.selectedTab
        viewModel.onAction(GameCalendarDetailAction.CycleTab)

        assertEquals("night-raid-north", viewModel.uiState.value.gameId)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedTab != initial)
    }

    @Test
    fun `game calendar editor viewmodel loads mode and toggles fields`() {
        val viewModel = GameCalendarEditorViewModel()

        viewModel.load(EditorMode.Edit, "game-1")
        val initialWindow = viewModel.uiState.value.selectedTimeWindow
        val initialRegistration = viewModel.uiState.value.registrationOpen
        viewModel.onAction(GameCalendarEditorAction.CycleTimeWindow)
        viewModel.onAction(GameCalendarEditorAction.ToggleRegistration)
        viewModel.onAction(GameCalendarEditorAction.SelectFormat("CQB"))

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("game-1", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.selectedTimeWindow != initialWindow)
        assertTrue(viewModel.uiState.value.registrationOpen != initialRegistration)
        assertEquals("CQB", viewModel.uiState.value.selectedFormat)
    }

    @Test
    fun `game calendar logistics viewmodel loads and cycles mode`() {
        val viewModel = GameCalendarLogisticsViewModel()

        viewModel.load("night-raid-north")
        val initial = viewModel.uiState.value.selectedMode
        viewModel.onAction(GameCalendarLogisticsAction.CycleMode)

        assertEquals("night-raid-north", viewModel.uiState.value.gameId)
        assertTrue(viewModel.uiState.value.transportRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedMode != initial)
    }

    @Test
    fun `polygons viewmodel changes region and terrain`() {
        val viewModel = PolygonsViewModel()

        val initialRegion = viewModel.uiState.value.selectedRegion
        viewModel.onAction(PolygonsAction.CycleRegion)
        viewModel.onAction(PolygonsAction.SelectTerrain("TerrainX"))

        assertTrue(viewModel.uiState.value.selectedRegion != initialRegion)
        assertEquals("TerrainX", viewModel.uiState.value.selectedTerrain)
    }

    @Test
    fun `rideshare viewmodel toggles direction and seat mode`() {
        val viewModel = RideShareViewModel()

        val initialDirection = viewModel.uiState.value.selectedDirection
        val initialRegion = viewModel.uiState.value.onlyMyRegion
        viewModel.onAction(RideShareAction.ToggleDirection)
        viewModel.onAction(RideShareAction.ToggleOnlyMyRegion)
        viewModel.onAction(RideShareAction.SelectSeatMode("SeatModeX"))

        assertTrue(viewModel.uiState.value.selectedDirection != initialDirection)
        assertTrue(viewModel.uiState.value.onlyMyRegion != initialRegion)
        assertEquals("SeatModeX", viewModel.uiState.value.selectedSeatMode)
    }

    @Test
    fun `shops viewmodel changes city and category`() {
        val viewModel = ShopsViewModel()

        val initialCity = viewModel.uiState.value.selectedCity
        viewModel.onAction(ShopsAction.CycleCity)
        viewModel.onAction(ShopsAction.SelectCategory("CategoryX"))

        assertTrue(viewModel.uiState.value.selectedCity != initialCity)
        assertEquals("CategoryX", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `services viewmodel toggles verified filter`() {
        val viewModel = ServicesViewModel()

        val initialOnlyVerified = viewModel.uiState.value.onlyVerified
        viewModel.onAction(ServicesAction.ToggleOnlyVerified)
        viewModel.onAction(ServicesAction.SelectCategory("ServiceX"))

        assertTrue(viewModel.uiState.value.onlyVerified != initialOnlyVerified)
        assertEquals("ServiceX", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `polygons search viewmodel cycles query and filter`() {
        val viewModel = PolygonsSearchViewModel()

        assertEquals("", viewModel.uiState.value.query)
        viewModel.onAction(ContextSearchAction.CycleDemoQuery)
        viewModel.onAction(ContextSearchAction.SelectFilter("FilterX"))

        assertTrue(viewModel.uiState.value.query.isNotBlank())
        assertEquals("FilterX", viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `shops search viewmodel clears query and switches scope`() {
        val viewModel = ShopsSearchViewModel()

        viewModel.onAction(ContextSearchAction.CycleDemoQuery)
        viewModel.onAction(ContextSearchAction.SelectScope("ScopeX"))
        viewModel.onAction(ContextSearchAction.ClearQuery)

        assertEquals("", viewModel.uiState.value.query)
        assertEquals("ScopeX", viewModel.uiState.value.selectedScope)
    }

    @Test
    fun `rideshare search viewmodel cycles query and scope`() {
        val viewModel = RideShareSearchViewModel()

        assertEquals("", viewModel.uiState.value.query)
        viewModel.onAction(ContextSearchAction.CycleDemoQuery)
        viewModel.onAction(ContextSearchAction.SelectScope("RideScopeX"))

        assertTrue(viewModel.uiState.value.query.isNotBlank())
        assertEquals("RideScopeX", viewModel.uiState.value.selectedScope)
    }

    @Test
    fun `rideshare trip detail viewmodel loads and toggles status`() {
        val viewModel = RideShareTripDetailViewModel()

        viewModel.load("night-raid-north-trip")
        val initialStatus = viewModel.uiState.value.status
        viewModel.onAction(RideShareTripDetailAction.ToggleStatus)

        assertEquals("night-raid-north-trip", viewModel.uiState.value.rideId)
        assertTrue(viewModel.uiState.value.routeRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.status != initialStatus)
    }

    @Test
    fun `rideshare trip editor viewmodel loads mode and changes fields`() {
        val viewModel = RideShareTripEditorViewModel()

        viewModel.load(EditorMode.Edit, "ride-1")
        val initialSeats = viewModel.uiState.value.seatCount
        viewModel.onAction(RideShareTripEditorAction.IncrementSeats)
        viewModel.onAction(RideShareTripEditorAction.ToggleFuelSplit)
        viewModel.onAction(RideShareTripEditorAction.SelectPickupZone("ZoneX"))

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("ride-1", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.seatCount >= initialSeats)
        assertEquals("ZoneX", viewModel.uiState.value.selectedPickupZone)
    }

    @Test
    fun `rideshare my route viewmodel cycles period`() {
        val viewModel = RideShareMyRouteViewModel()

        val initial = viewModel.uiState.value.selectedMode
        viewModel.onAction(RideShareMyRouteAction.ToggleMode)

        assertTrue(viewModel.uiState.value.selectedMode != initial)
        assertTrue(viewModel.uiState.value.upcomingRows.isNotEmpty())
    }

    @Test
    fun `polygon detail viewmodel loads and cycles tab`() {
        val viewModel = PolygonDetailViewModel()

        viewModel.load("polygon-severny")
        val initialTab = viewModel.uiState.value.selectedTab
        viewModel.onAction(PolygonDetailAction.CycleTab)

        assertEquals("polygon-severny", viewModel.uiState.value.polygonId)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedTab != initialTab)
    }

    @Test
    fun `polygon editor viewmodel loads mode and toggles fields`() {
        val viewModel = PolygonEditorViewModel()

        viewModel.load(EditorMode.Edit, "polygon-1")
        val initialFps = viewModel.uiState.value.fpsLimit
        val initialNight = viewModel.uiState.value.nightGamesAllowed
        viewModel.onAction(PolygonEditorAction.CycleFpsLimit)
        viewModel.onAction(PolygonEditorAction.ToggleNightGames)
        viewModel.onAction(PolygonEditorAction.SelectTerrain("CQB"))

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("polygon-1", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.fpsLimit != initialFps)
        assertTrue(viewModel.uiState.value.nightGamesAllowed != initialNight)
        assertEquals("CQB", viewModel.uiState.value.selectedTerrain)
    }

    @Test
    fun `polygon rules map viewmodel loads and cycles layer`() {
        val viewModel = PolygonRulesMapViewModel()

        viewModel.load("polygon-severny")
        val initialLayer = viewModel.uiState.value.selectedLayer
        viewModel.onAction(PolygonRulesMapAction.CycleLayer)

        assertEquals("polygon-severny", viewModel.uiState.value.polygonId)
        assertTrue(viewModel.uiState.value.mapRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedLayer != initialLayer)
    }

    @Test
    fun `shop detail viewmodel loads and cycles section`() {
        val viewModel = ShopDetailViewModel()

        viewModel.load("airsoft-hub")
        val initial = viewModel.uiState.value.selectedSection
        viewModel.onAction(ShopDetailAction.CycleSection)

        assertEquals("airsoft-hub", viewModel.uiState.value.shopId)
        assertTrue(viewModel.uiState.value.catalogRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedSection != initial)
    }

    @Test
    fun `shop editor viewmodel loads mode and toggles fields`() {
        val viewModel = ShopEditorViewModel()

        viewModel.load(EditorMode.Edit, "shop-1")
        val initialCity = viewModel.uiState.value.selectedCity
        val initialDelivery = viewModel.uiState.value.deliveryEnabled
        viewModel.onAction(ShopEditorAction.CycleCity)
        viewModel.onAction(ShopEditorAction.ToggleDelivery)
        viewModel.onAction(ShopEditorAction.SelectCategory("Оптика"))

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("shop-1", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.selectedCity != initialCity)
        assertTrue(viewModel.uiState.value.deliveryEnabled != initialDelivery)
        assertEquals("Оптика", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `service detail viewmodel loads and cycles section`() {
        val viewModel = ServiceDetailViewModel()

        viewModel.load("north-tech-tuning")
        val initial = viewModel.uiState.value.selectedSection
        viewModel.onAction(ServiceDetailAction.CycleSection)

        assertEquals("north-tech-tuning", viewModel.uiState.value.serviceId)
        assertTrue(viewModel.uiState.value.portfolioRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedSection != initial)
    }

    @Test
    fun `service editor viewmodel loads mode and toggles fields`() {
        val viewModel = ServiceEditorViewModel()

        viewModel.load(EditorMode.Edit, "service-1")
        val initialResponse = viewModel.uiState.value.responseMode
        val initialVerified = viewModel.uiState.value.onlyVerified
        viewModel.onAction(ServiceEditorAction.CycleResponseMode)
        viewModel.onAction(ServiceEditorAction.ToggleVerified)
        viewModel.onAction(ServiceEditorAction.SelectCategory("Судья"))

        assertEquals(EditorMode.Edit, viewModel.uiState.value.editorMode)
        assertEquals("service-1", viewModel.uiState.value.editorRefId)
        assertTrue(viewModel.uiState.value.responseMode != initialResponse)
        assertTrue(viewModel.uiState.value.onlyVerified != initialVerified)
        assertEquals("Судья", viewModel.uiState.value.selectedCategory)
    }
}
