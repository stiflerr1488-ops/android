package com.airsoft.social.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditorModeTest {

    @Test
    fun `from route value parses supported modes case insensitively`() {
        assertEquals(EditorMode.Create, EditorMode.fromRouteValue("create"))
        assertEquals(EditorMode.Edit, EditorMode.fromRouteValue("EDIT"))
        assertEquals(EditorMode.Draft, EditorMode.fromRouteValue(" Draft "))
    }

    @Test
    fun `from route value returns null for unsupported modes`() {
        assertNull(EditorMode.fromRouteValue("clone"))
        assertNull(EditorMode.fromRouteValue(""))
    }
}
