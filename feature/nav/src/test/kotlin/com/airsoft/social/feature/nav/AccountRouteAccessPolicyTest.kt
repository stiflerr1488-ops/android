package com.airsoft.social.feature.nav

import com.airsoft.social.core.model.AccountAccess
import com.airsoft.social.core.model.AccountRole
import com.airsoft.social.core.model.AccountTier
import com.airsoft.social.core.model.EditorMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRouteAccessPolicyTest {

    @Test
    fun `guest account is read-only for protected routes`() {
        val guest = AccountAccess()

        assertFalse(guest.canOpenRoute(AirsoftRoutes.Moderation))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.AdminDashboard))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.profileEditRoute("self")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.teamEditorRoute(EditorMode.Create, "draft")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.eventEditorRoute(EditorMode.Create, "draft")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.marketplaceEditorRoute(EditorMode.Create, "draft")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.rideShareTripEditorRoute(EditorMode.Create, "draft")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.shopEditorRoute(EditorMode.Create, "draft")))
        assertFalse(guest.canOpenRoute(AirsoftRoutes.serviceEditorRoute(EditorMode.Create, "draft")))
        assertTrue(guest.canOpenRoute(AirsoftRoutes.ChatRoomDemo))
    }

    @Test
    fun `registered user can access basic writer routes but not commercial routes`() {
        val user = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER),
        )

        assertTrue(user.canOpenRoute(AirsoftRoutes.profileEditRoute("self")))
        assertTrue(user.canOpenRoute(AirsoftRoutes.teamEditorRoute(EditorMode.Create, "draft")))
        assertTrue(user.canOpenRoute(AirsoftRoutes.marketplaceEditorRoute(EditorMode.Create, "draft")))
        assertTrue(user.canOpenRoute(AirsoftRoutes.rideShareTripEditorRoute(EditorMode.Create, "draft")))

        assertFalse(user.canOpenRoute(AirsoftRoutes.eventEditorRoute(EditorMode.Create, "draft")))
        assertFalse(user.canOpenRoute(AirsoftRoutes.gameCalendarEditorRoute(EditorMode.Create, "draft")))
        assertFalse(user.canOpenRoute(AirsoftRoutes.shopEditorRoute(EditorMode.Create, "draft")))
        assertFalse(user.canOpenRoute(AirsoftRoutes.serviceEditorRoute(EditorMode.Create, "draft")))
        assertFalse(user.canOpenRoute(AirsoftRoutes.Moderation))
    }

    @Test
    fun `commercial role can access game and shop editors`() {
        val commercial = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER, AccountRole.COMMERCIAL),
        )

        assertTrue(commercial.canOpenRoute(AirsoftRoutes.eventEditorRoute(EditorMode.Create, "draft")))
        assertTrue(commercial.canOpenRoute(AirsoftRoutes.gameCalendarEditorRoute(EditorMode.Create, "draft")))
        assertTrue(commercial.canOpenRoute(AirsoftRoutes.shopEditorRoute(EditorMode.Create, "draft")))
        assertTrue(commercial.canOpenRoute(AirsoftRoutes.serviceEditorRoute(EditorMode.Create, "draft")))
    }

    @Test
    fun `moderator can access moderation but not admin routes`() {
        val moderator = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER, AccountRole.MODERATOR),
        )

        assertTrue(moderator.canOpenRoute(AirsoftRoutes.Moderation))
        assertTrue(moderator.canOpenRoute(AirsoftRoutes.ModerationReportsQueue))
        assertFalse(moderator.canOpenRoute(AirsoftRoutes.AdminDashboard))
    }

    @Test
    fun `admin can access moderation and admin routes`() {
        val admin = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER, AccountRole.ADMIN),
        )

        assertTrue(admin.canOpenRoute(AirsoftRoutes.Moderation))
        assertTrue(admin.canOpenRoute(AirsoftRoutes.AdminDashboard))
        assertTrue(admin.canOpenRoute(AirsoftRoutes.AdminAuditLog))
    }

    @Test
    fun `route decision returns registration requirement for guest writer routes`() {
        val guest = AccountAccess()

        val decision = guest.routeAccessDecision(AirsoftRoutes.marketplaceEditorRoute(EditorMode.Create, "draft"))

        assertFalse(decision.allowed)
        assertEquals(RouteAccessRequirement.Registration, decision.requirement)
    }

    @Test
    fun `route decision returns commercial requirement for registered user without commercial role`() {
        val user = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER),
        )

        val decision = user.routeAccessDecision(AirsoftRoutes.eventEditorRoute(EditorMode.Create, "draft"))

        assertFalse(decision.allowed)
        assertEquals(RouteAccessRequirement.CommercialRole, decision.requirement)
    }

    @Test
    fun `route decision returns moderator and admin requirements for protected backoffice routes`() {
        val user = AccountAccess(
            tier = AccountTier.REGISTERED,
            roles = setOf(AccountRole.USER),
        )

        val moderationDecision = user.routeAccessDecision(AirsoftRoutes.Moderation)
        val adminDecision = user.routeAccessDecision(AirsoftRoutes.AdminDashboard)

        assertFalse(moderationDecision.allowed)
        assertEquals(RouteAccessRequirement.ModeratorRole, moderationDecision.requirement)
        assertFalse(adminDecision.allowed)
        assertEquals(RouteAccessRequirement.AdminRole, adminDecision.requirement)
    }
}
