package com.airsoft.social.core.data

import com.airsoft.social.core.model.Chat
import com.airsoft.social.core.model.ChatMessage
import com.airsoft.social.core.model.ChatType
import com.airsoft.social.core.model.EventStatus
import com.airsoft.social.core.model.GameEvent
import com.airsoft.social.core.model.GameFormat
import com.airsoft.social.core.model.GameStyle
import com.airsoft.social.core.model.ListingCategory
import com.airsoft.social.core.model.ListingStatus
import com.airsoft.social.core.model.Location
import com.airsoft.social.core.model.MarketplaceListing
import com.airsoft.social.core.model.ModerationStatus
import com.airsoft.social.core.model.ItemCondition
import com.airsoft.social.core.model.OrganizerType
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.RegistrationStatus
import com.airsoft.social.core.model.Team
import com.airsoft.social.core.model.User
import com.airsoft.social.core.model.UserRole
import java.util.Date

interface SocialPreviewRepository {
    fun listUsers(): List<User>
    fun listChats(): List<Chat>
    fun listChatMessages(chatId: String): List<ChatMessage>
    fun listTeams(): List<Team>
    fun listEvents(): List<GameEvent>
    fun listMarketplaceListings(): List<MarketplaceListing>
    fun getUser(id: String): User?
    fun getChat(id: String): Chat?
    fun getTeam(id: String): Team?
    fun getEvent(id: String): GameEvent?
    fun getMarketplaceListing(id: String): MarketplaceListing?
}

object DemoSocialRepositoryProvider {
    val repository: SocialPreviewRepository = FakeSocialPreviewRepository()
}

class FakeSocialPreviewRepository : SocialPreviewRepository {
    private val users = listOf(
        User(
            id = "self",
            callsign = "Teiwaz_",
            firstName = "Artem",
            lastName = "Volkov",
            teamId = "ew-easy-winner",
            teamName = "[EW] EASY WINNER",
            region = "Москва",
            bio = "Командир, CQB и лесные игры по выходным.",
            roles = setOf(UserRole.PLAYER, UserRole.CAPTAIN, UserRole.SELLER),
            privacySettings = PrivacySettings(),
            isOnline = true,
            isVerified = true,
            rating = 4.9f,
            reviewsCount = 27,
        ),
        User(
            id = "ghost",
            callsign = "Ghost",
            firstName = "Ivan",
            lastName = "Sokolov",
            region = "Москва",
            bio = "Штурм, ищу команду, выезды до 120 км.",
            roles = setOf(UserRole.PLAYER),
            isOnline = false,
            isVerified = false,
            rating = 4.8f,
            reviewsCount = 12,
        ),
        User(
            id = "raven",
            callsign = "Raven",
            firstName = "Maksim",
            lastName = "Orlov",
            teamId = "ew-easy-winner",
            teamName = "[EW] EASY WINNER",
            region = "Москва",
            bio = "Штурм и связь.",
            roles = setOf(UserRole.PLAYER),
            isOnline = true,
            rating = 4.7f,
            reviewsCount = 9,
        ),
        User(
            id = "medic-fox",
            callsign = "MedicFox",
            firstName = "Anna",
            lastName = "Lina",
            teamId = "ew-easy-winner",
            teamName = "[EW] EASY WINNER",
            region = "Казань",
            bio = "Медик, полевые сценарные игры.",
            roles = setOf(UserRole.PLAYER),
            isOnline = true,
            rating = 4.6f,
            reviewsCount = 7,
        ),
    )

    private val teams = listOf(
        Team(
            id = "ew-easy-winner",
            name = "EASY WINNER",
            shortName = "EW",
            region = "Москва",
            description = "Штурмовой состав для тренировок и турниров.",
            isOpenForJoin = true,
            requiresApproval = true,
            isVerified = true,
            memberCount = 14,
            maxMembers = 20,
            captainId = "self",
            captainCallsign = "Teiwaz_",
            gameStyles = setOf(GameStyle.CQB, GameStyle.FOREST, GameStyle.TACTICAL),
        ),
    )

    private val events = listOf(
        GameEvent(
            id = "night-raid-north",
            title = "Night Raid North",
            description = "Ночная игра с фазами и брифингом.",
            organizerId = "ew-easy-winner",
            organizerName = "[EW] EASY WINNER",
            organizerType = OrganizerType.TEAM,
            startDate = Date(1_740_787_200_000L), // Mar 1, 2025 UTC
            endDate = Date(1_740_816_000_000L),
            location = Location(
                name = "Лесной полигон Север",
                address = "МО, северное направление",
                description = "Три игровые зоны + парковка у ворот C",
            ),
            gameFormat = GameFormat.DAY_NIGHT,
            maxPlayers = 60,
            currentPlayers = 32,
            entryFee = 1200.0,
            currency = "RUB",
            status = EventStatus.PUBLISHED,
            registrationStatus = RegistrationStatus.OPEN,
            rules = "Обязателен хронограф и защита глаз.",
            whatToBring = listOf("Радиосвязь", "Фонарь", "Красная тряпка"),
        ),
    )

    private val listings = listOf(
        MarketplaceListing(
            id = "m4a1-cyma-3mags",
            sellerId = "self",
            sellerCallsign = "Teiwaz_",
            sellerRating = 4.9f,
            sellerIsVerified = true,
            title = "M4A1 Cyma + 3 магазина",
            description = "Рабочий привод для тренировок, комплект магазинов и ремень.",
            category = ListingCategory.EQUIPMENT,
            subcategory = "AEG",
            price = 18500.0,
            currency = "RUB",
            isNegotiable = true,
            condition = ItemCondition.GOOD,
            brand = "CYMA",
            model = "M4A1",
            city = "Москва",
            deliveryAvailable = true,
            pickupOnly = false,
            status = ListingStatus.PUBLISHED,
            viewsCount = 14,
            favoritesCount = 2,
            moderationStatus = ModerationStatus.APPROVED,
        ),
    )

    private val chats = listOf(
        Chat(
            id = "team-ew-general",
            type = ChatType.TEAM,
            name = "Команда [EW] EASY WINNER",
            lastMessage = "Суббота 09:00 сбор, подтвердите участие",
            unreadCount = 12,
            updatedAt = System.currentTimeMillis(),
        ),
        Chat(
            id = "ghost-direct",
            type = ChatType.DIRECT,
            name = "Ghost",
            lastMessage = "Ищу команду на выходные",
            unreadCount = 1,
            updatedAt = System.currentTimeMillis() - 60_000L,
        ),
    )

    private val chatMessages = listOf(
        ChatMessage(
            id = "msg-1",
            chatId = "team-ew-general",
            senderId = "self",
            senderCallsign = "Teiwaz_",
            content = "Точку сбора перенесли к воротам C. Смотрите карту.",
            createdAt = Date(System.currentTimeMillis() - 15 * 60_000L),
            isRead = true,
        ),
        ChatMessage(
            id = "msg-2",
            chatId = "team-ew-general",
            senderId = "raven",
            senderCallsign = "Raven",
            content = "Принял. Беру дополнительный аккумулятор для связи.",
            createdAt = Date(System.currentTimeMillis() - 12 * 60_000L),
            isRead = true,
        ),
        ChatMessage(
            id = "msg-3",
            chatId = "team-ew-general",
            senderId = "medic-fox",
            senderCallsign = "MedicFox",
            content = "Нужно подтвердить состав до 22:00.",
            createdAt = Date(System.currentTimeMillis() - 10 * 60_000L),
            isRead = false,
        ),
        ChatMessage(
            id = "msg-4",
            chatId = "ghost-direct",
            senderId = "ghost",
            senderCallsign = "Ghost",
            content = "Ищу команду на выходные, играю штурмом.",
            createdAt = Date(System.currentTimeMillis() - 20 * 60_000L),
            isRead = false,
        ),
    )

    private val usersById = users.associateBy(User::id)
    private val chatsById = chats.associateBy(Chat::id)
    private val teamsById = teams.associateBy(Team::id)
    private val eventsById = events.associateBy(GameEvent::id)
    private val listingsById = listings.associateBy(MarketplaceListing::id)

    override fun listUsers(): List<User> = users

    override fun listChats(): List<Chat> = chats

    override fun listChatMessages(chatId: String): List<ChatMessage> =
        chatMessages.filter { it.chatId == chatId }.sortedBy { it.createdAt.time }

    override fun listTeams(): List<Team> = teams

    override fun listEvents(): List<GameEvent> = events

    override fun listMarketplaceListings(): List<MarketplaceListing> = listings

    override fun getUser(id: String): User? = usersById[id]

    override fun getChat(id: String): Chat? = chatsById[id]

    override fun getTeam(id: String): Team? = teamsById[id]

    override fun getEvent(id: String): GameEvent? = eventsById[id]

    override fun getMarketplaceListing(id: String): MarketplaceListing? = listingsById[id]
}
