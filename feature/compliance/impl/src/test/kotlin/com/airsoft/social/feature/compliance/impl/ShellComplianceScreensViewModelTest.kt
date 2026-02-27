package com.airsoft.social.feature.compliance.impl

import org.junit.Assert.assertTrue
import org.junit.Test

class ShellComplianceScreensViewModelTest {
    @Test
    fun `rules compliance viewmodel cycles group`() {
        val viewModel = RulesComplianceViewModel()

        val initial = viewModel.uiState.value.selectedGroup
        viewModel.onAction(RulesComplianceAction.CycleGroup)

        assertTrue(viewModel.uiState.value.selectedGroup != initial)
        assertTrue(viewModel.uiState.value.summaryRows.isNotEmpty())
    }

    @Test
    fun `compliance tool viewmodel loads and cycles tab`() {
        val viewModel = ComplianceToolViewModel()
        val spec = ComplianceToolSpec(
            id = "compliance-test",
            title = "Тест",
            subtitle = "Тестовый compliance экран",
            primaryActionLabel = "Действие",
            tabs = listOf("А", "Б"),
            metricRows = listOf("X" to "1"),
            mainSectionTitle = "Основное",
            mainSectionSubtitle = "Описание",
            mainRows = listOf(ShellWireframeRow("row1", "desc", "tag")),
            policySectionTitle = "Политики",
            policySectionSubtitle = "Описание",
            policyRows = listOf(ShellWireframeRow("row2", "desc", "tag")),
            cycleButtonLabel = "Переключить",
        )

        viewModel.load(spec)
        val initial = viewModel.uiState.value.selectedTab
        viewModel.onAction(ComplianceToolAction.CycleTab)

        assertTrue(viewModel.uiState.value.toolId == "compliance-test")
        assertTrue(viewModel.uiState.value.mainRows.isNotEmpty())
        assertTrue(viewModel.uiState.value.selectedTab != initial)
    }
}
