package com.airsoft.social.core.model

enum class AccountRole {
    USER,
    COMMERCIAL,
    MODERATOR,
    ADMIN,
}

enum class AccountTier {
    GUEST,
    REGISTERED,
}

data class AccountAccess(
    val tier: AccountTier = AccountTier.GUEST,
    val roles: Set<AccountRole> = emptySet(),
) {
    val isGuest: Boolean = tier == AccountTier.GUEST
    val isRegistered: Boolean = tier == AccountTier.REGISTERED
    val isCommercial: Boolean = AccountRole.COMMERCIAL in roles || AccountRole.ADMIN in roles
    val isModerator: Boolean = AccountRole.MODERATOR in roles || AccountRole.ADMIN in roles
    val isAdmin: Boolean = AccountRole.ADMIN in roles

    val canEditProfile: Boolean = isRegistered
    val canSendChatMessages: Boolean = isRegistered
    val canCreateMarketplaceListings: Boolean = isRegistered
    val canCreateRideShareListings: Boolean = isRegistered
    val canCreateGameEvents: Boolean = isCommercial
    val canCreateShopListings: Boolean = isCommercial
}

fun UserSession.toAccountAccess(): AccountAccess {
    val isRegistered = !email.isNullOrBlank()
    val normalizedRoles = if (!isRegistered) {
        emptySet()
    } else if (accountRoles.isEmpty()) {
        setOf(AccountRole.USER)
    } else {
        accountRoles
    }
    return AccountAccess(
        tier = if (isRegistered) AccountTier.REGISTERED else AccountTier.GUEST,
        roles = normalizedRoles,
    )
}

fun AuthState.toAccountAccess(): AccountAccess = when (this) {
    is AuthState.SignedIn -> session.toAccountAccess()
    AuthState.SignedOut,
    AuthState.Unknown,
    -> AccountAccess()
}

