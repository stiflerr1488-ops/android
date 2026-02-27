package com.airsoft.social.core.model

import java.util.Date

data class User(
    val id: String,
    val callsign: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val region: String? = null,
    val exitRadiusKm: Int? = null,
    val bio: String? = null,
    val roles: Set<UserRole> = emptySet(),
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isOnline: Boolean = false,
    val lastSeen: Date? = null,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val rating: Float? = null,
    val reviewsCount: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)

enum class UserRole {
    PLAYER,
    CAPTAIN,
    ORGANIZER,
    SELLER,
    TECH_MASTER,
    SHOP_PARTNER,
    MODERATOR,
    ADMIN,
}

data class PrivacySettings(
    val showPhone: Boolean = false,
    val showEmail: Boolean = false,
    val showTelegram: Boolean = false,
    val showRegion: Boolean = true,
    val showTeam: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val allowTeamInvites: Boolean = true,
    val allowEventInvites: Boolean = true,
)

data class GearItem(
    val id: String,
    val name: String,
    val category: GearCategory,
    val description: String? = null,
    val isPrimary: Boolean = false,
    val notes: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long,
)

enum class GearCategory {
    PRIMARY_WEAPONS,
    SECONDARY_WEAPONS,
    RIGS,
    HELMETS,
    ARMOR,
    RADIOS,
    FLASHLIGHTS,
    OPTICS,
    SPARE_PARTS,
    CONSUMABLES,
    OTHER,
}

data class GearCategorySummary(
    val category: GearCategory,
    val displayName: String,
    val icon: String,
    val count: Int,
)

data class GameHistoryRow(
    val id: String,
    val date: Long,
    val eventName: String,
)

data class AchievementRow(
    val id: String,
    val title: String,
    val description: String,
)

data class TrustBadgeRow(
    val id: String,
    val title: String,
    val description: String,
)

enum class ChatType {
    DIRECT,
    GROUP,
    TEAM,
    EVENT,
    GENERAL,
    SUPPORT,
}

data class Chat(
    val id: String,
    val type: ChatType,
    val name: String?,
    val lastMessage: String?,
    val unreadCount: Int,
    val updatedAt: Long,
)

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderCallsign: String,
    val senderAvatarUrl: String? = null,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val attachments: List<MessageAttachment> = emptyList(),
    val createdAt: Date,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val reportsCount: Int = 0,
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LOCATION,
    EVENT_INVITE,
    SYSTEM,
}

data class MessageAttachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val thumbnailUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LOCATION,
}

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String? = null,
    val region: String,
    val description: String? = null,
    val rules: String? = null,
    val isOpenForJoin: Boolean = true,
    val requiresApproval: Boolean = true,
    val isVerified: Boolean = false,
    val foundedDate: Date? = null,
    val memberCount: Int = 0,
    val maxMembers: Int? = null,
    val ageRestriction: Int? = null,
    val captainId: String? = null,
    val captainCallsign: String? = null,
    val contactInfo: ContactInfo? = null,
    val gameStyles: Set<GameStyle> = emptySet(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)

enum class GameStyle {
    CQB,
    FOREST,
    URBAN,
    SCENARIO,
    TACTICAL,
    RECREATIONAL,
    HARDCORE,
}

data class ContactInfo(
    val telegram: String? = null,
    val phone: String? = null,
    val email: String? = null,
)

data class GameEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val organizerId: String,
    val organizerName: String,
    val organizerType: OrganizerType,
    val startDate: Date,
    val endDate: Date? = null,
    val registrationDeadline: Date? = null,
    val location: Location,
    val fieldId: String? = null,
    val fieldName: String? = null,
    val gameFormat: GameFormat,
    val minAge: Int? = null,
    val maxPlayers: Int? = null,
    val currentPlayers: Int = 0,
    val entryFee: Double? = null,
    val currency: String = "EUR",
    val status: EventStatus = EventStatus.DRAFT,
    val registrationStatus: RegistrationStatus = RegistrationStatus.CLOSED,
    val rules: String? = null,
    val whatToBring: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)

enum class OrganizerType {
    TEAM,
    CLUB,
    FIELD,
    INDEPENDENT,
    COMMUNITY,
}

enum class GameFormat {
    CQB,
    FOREST,
    URBAN,
    MIXED,
    SCENARIO,
    DAY_NIGHT,
    TACTICAL,
    TRAINING,
}

enum class EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    POSTPONED,
    COMPLETED,
}

enum class RegistrationStatus {
    OPEN,
    CLOSED,
    WAITLIST,
    INVITATION_ONLY,
}

data class Location(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
)

data class MarketplaceListing(
    val id: String,
    val sellerId: String,
    val sellerCallsign: String,
    val sellerRating: Float? = null,
    val sellerIsVerified: Boolean = false,
    val title: String,
    val description: String,
    val category: ListingCategory,
    val subcategory: String? = null,
    val price: Double,
    val currency: String = "EUR",
    val isNegotiable: Boolean = true,
    val condition: ItemCondition,
    val brand: String? = null,
    val model: String? = null,
    val city: String,
    val region: String? = null,
    val deliveryAvailable: Boolean = false,
    val pickupOnly: Boolean = false,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val status: ListingStatus = ListingStatus.DRAFT,
    val isFeatured: Boolean = false,
    val viewsCount: Int = 0,
    val favoritesCount: Int = 0,
    val moderationStatus: ModerationStatus = ModerationStatus.PENDING,
    val moderationNote: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val expiresAt: Date? = null,
    val soldAt: Date? = null,
)

enum class ListingCategory {
    EQUIPMENT,
    CLOTHING,
    RIGGING,
    OPTICS,
    PROTECTION,
    ELECTRONICS,
    ACCESSORIES,
    SPARE_PARTS,
    CONSUMABLES,
    SERVICES,
    OTHER,
}

enum class ItemCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    SATISFACTORY,
    POOR,
    FOR_PARTS,
}

enum class ListingStatus {
    DRAFT,
    PUBLISHED,
    RESERVED,
    SOLD,
    HIDDEN,
    REMOVED,
}

enum class ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FLAGGED,
}
