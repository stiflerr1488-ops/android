package com.airsoft.social.feature.settings.impl

import android.Manifest
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellSettingsAdvancedScreensViewModelTest {
    @Test
    fun `settings tool viewmodel does not reset state when same spec loaded twice`() {
        val viewModel = SettingsToolViewModel()
        val spec = testSpec(id = "tool-a")

        viewModel.load(spec)
        viewModel.onAction(SettingsToolAction.CycleTab)
        val selectedAfterCycle = viewModel.uiState.value.selectedTab

        viewModel.load(spec)

        assertEquals("tool-a", viewModel.uiState.value.toolId)
        assertEquals(selectedAfterCycle, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `settings tool viewmodel cycles tabs and wraps around`() {
        val viewModel = SettingsToolViewModel()
        viewModel.load(
            testSpec(
                id = "tool-b",
                tabs = listOf("A", "B", "C"),
            ),
        )

        assertEquals("A", viewModel.uiState.value.selectedTab)
        viewModel.onAction(SettingsToolAction.CycleTab)
        assertEquals("B", viewModel.uiState.value.selectedTab)
        viewModel.onAction(SettingsToolAction.CycleTab)
        assertEquals("C", viewModel.uiState.value.selectedTab)
        viewModel.onAction(SettingsToolAction.CycleTab)
        assertEquals("A", viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `settings screen specs expose expected tabs and rows`() {
        val privacy = settingsPrivacyToolSpecRes()
        val permissions = settingsPermissionsToolSpecRes()
        val security = settingsSecurityToolSpecRes()
        val battery = settingsBatteryToolSpecRes()

        assertEquals("settings-privacy", privacy.id)
        assertEquals(4, privacy.tabResIds.size)
        assertTrue(privacy.mainRows.isNotEmpty())
        assertTrue(privacy.scenarioRows.isNotEmpty())

        assertEquals("settings-permissions", permissions.id)
        assertEquals(4, permissions.tabResIds.size)
        assertTrue(permissions.extraActions.size >= 3)
        assertTrue(permissions.extraActions.any { it.id == "open_app_permissions" })

        assertEquals("settings-security", security.id)
        assertEquals(4, security.tabResIds.size)
        assertTrue(security.metricRows.isNotEmpty())

        assertEquals("settings-battery", battery.id)
        assertEquals(4, battery.tabResIds.size)
        assertTrue(battery.extraActions.size >= 2)
    }

    @Test
    fun `required runtime permissions include modern permissions on Android 13`() {
        val permissions = requiredRuntimePermissionsForSettings(Build.VERSION_CODES.TIRAMISU)

        assertTrue(Manifest.permission.POST_NOTIFICATIONS in permissions)
        assertTrue(Manifest.permission.READ_MEDIA_IMAGES in permissions)
        assertFalse(Manifest.permission.READ_EXTERNAL_STORAGE in permissions)
    }

    @Test
    fun `required runtime permissions include legacy media permission on Android 12`() {
        val permissions = requiredRuntimePermissionsForSettings(Build.VERSION_CODES.S)

        assertTrue(Manifest.permission.READ_EXTERNAL_STORAGE in permissions)
        assertFalse(Manifest.permission.POST_NOTIFICATIONS in permissions)
    }

    private fun testSpec(
        id: String,
        tabs: List<String> = listOf("Overview", "Params"),
    ): SettingsToolSpec = SettingsToolSpec(
        id = id,
        title = "Title",
        subtitle = "Subtitle",
        primaryActionLabel = "Primary",
        tabs = tabs,
        metricRows = listOf("M1" to "1"),
        mainSectionTitle = "Main",
        mainSectionSubtitle = "Main description",
        mainRows = listOf(ShellWireframeRow("A", "B", "C")),
        scenarioSectionTitle = "Scenarios",
        scenarioSectionSubtitle = "Scenarios description",
        scenarioRows = listOf(ShellWireframeRow("X", "Y", "Z")),
        cycleButtonLabel = "Cycle",
    )
}
