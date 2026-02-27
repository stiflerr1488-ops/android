package com.airsoft.social.core.data

import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.model.AchievementRow
import com.airsoft.social.core.model.GameHistoryRow
import com.airsoft.social.core.model.GearCategory
import com.airsoft.social.core.model.GearCategorySummary
import com.airsoft.social.core.model.GearItem
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.TrustBadgeRow
import com.airsoft.social.core.model.User
import com.airsoft.social.core.model.AuthState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest

const val SELF_USER_ID: String = "self"

data class ProfileUpdatePayload(
    val callsign: String,
    val firstName: String,
    val lastName: String,
    val bio: String?,
    val region: String?,
    val exitRadiusKm: Int?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val privacySettings: PrivacySettings,
)

interface ProfileRemoteDataSource {
    fun observeUser(userId: String): Flow<User?>
    fun observeGearItems(userId: String): Flow<List<GearItem>>
    fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>>
    fun observeAchievements(userId: String): Flow<List<AchievementRow>>
    fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>>

    suspend fun updateUserProfile(
        userId: String,
        payload: ProfileUpdatePayload,
    ): AppResult<Unit>
}

interface ProfileRepository {
    fun observeCurrentUser(): Flow<User?>
    fun observeUser(userId: String): Flow<User?>
    fun observeGearCategories(userId: String): Flow<List<GearCategorySummary>>
    fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>>
    fun observeAchievements(userId: String): Flow<List<AchievementRow>>
    fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>>

    suspend fun updateUserProfile(
        userId: String,
        callsign: String,
        firstName: String,
        lastName: String,
        bio: String?,
        region: String?,
        exitRadiusKm: Int?,
        avatarUrl: String?,
        bannerUrl: String?,
        privacySettings: PrivacySettings,
    ): AppResult<Unit>
}

class RealtimeProfileRepository(
    private val authGateway: AuthGateway,
    private val remoteDataSource: ProfileRemoteDataSource,
) : ProfileRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> =
        authGateway.authState.transformLatest { state ->
            when (state) {
                is AuthState.SignedIn -> emitAll(remoteDataSource.observeUser(state.session.userId))
                else -> emit(null)
            }
        }

    override fun observeUser(userId: String): Flow<User?> {
        if (userId != SELF_USER_ID) return remoteDataSource.observeUser(userId)
        return observeCurrentUser()
    }

    override fun observeGearCategories(userId: String): Flow<List<GearCategorySummary>> =
        scopedUserFlow(
            userId = userId,
            observe = remoteDataSource::observeGearItems,
        ).map(::toCategorySummaries)

    override fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>> =
        scopedUserFlow(
            userId = userId,
            observe = remoteDataSource::observeGameHistory,
        ).map { list -> list.sortedByDescending { it.date } }

    override fun observeAchievements(userId: String): Flow<List<AchievementRow>> =
        scopedUserFlow(
            userId = userId,
            observe = remoteDataSource::observeAchievements,
        )

    override fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>> =
        scopedUserFlow(
            userId = userId,
            observe = remoteDataSource::observeTrustBadges,
        )

    override suspend fun updateUserProfile(
        userId: String,
        callsign: String,
        firstName: String,
        lastName: String,
        bio: String?,
        region: String?,
        exitRadiusKm: Int?,
        avatarUrl: String?,
        bannerUrl: String?,
        privacySettings: PrivacySettings,
    ): AppResult<Unit> {
        val session = authGateway.currentSession()
            ?: return AppResult.Failure(AppError.Unauthorized)
        if (session.isGuest) {
            return AppResult.Failure(AppError.Unauthorized)
        }
        val targetUserId = if (userId == SELF_USER_ID) session.userId else userId
        val payload = ProfileUpdatePayload(
            callsign = callsign.trim(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            bio = bio?.trim().takeUnless { it.isNullOrBlank() },
            region = region?.trim().takeUnless { it.isNullOrBlank() },
            exitRadiusKm = exitRadiusKm,
            avatarUrl = avatarUrl?.trim().takeUnless { it.isNullOrBlank() },
            bannerUrl = bannerUrl?.trim().takeUnless { it.isNullOrBlank() },
            privacySettings = privacySettings,
        )
        if (payload.callsign.isBlank()) {
            return AppResult.Failure(AppError.Validation("Callsign is required"))
        }
        return remoteDataSource.updateUserProfile(
            userId = targetUserId,
            payload = payload,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> scopedUserFlow(
        userId: String,
        observe: (String) -> Flow<T>,
    ): Flow<T> {
        if (userId != SELF_USER_ID) return observe(userId)
        return authGateway.authState.transformLatest { state ->
            when (state) {
                is AuthState.SignedIn -> emitAll(observe(state.session.userId))
                else -> emitAll(emptyFlow())
            }
        }
    }

    private fun toCategorySummaries(items: List<GearItem>): List<GearCategorySummary> {
        if (items.isEmpty()) return emptyList()
        val grouped = items.groupingBy { it.category }.eachCount()
        return GearCategory.entries.mapNotNull { category ->
            val count = grouped[category] ?: 0
            if (count <= 0) return@mapNotNull null
            GearCategorySummary(
                category = category,
                displayName = categoryDisplayName(category),
                icon = category.name.lowercase(),
                count = count,
            )
        }
    }

    private fun categoryDisplayName(category: GearCategory): String = when (category) {
        GearCategory.PRIMARY_WEAPONS -> "Приводы"
        GearCategory.SECONDARY_WEAPONS -> "Пистолеты"
        GearCategory.RIGS -> "Разгрузки"
        GearCategory.HELMETS -> "Шлемы"
        GearCategory.ARMOR -> "Броня"
        GearCategory.RADIOS -> "Рации"
        GearCategory.FLASHLIGHTS -> "Фонари"
        GearCategory.OPTICS -> "Прицелы"
        GearCategory.SPARE_PARTS -> "Запчасти"
        GearCategory.CONSUMABLES -> "Расходники"
        GearCategory.OTHER -> "Разное"
    }
}

class NoopProfileRemoteDataSource : ProfileRemoteDataSource {
    override fun observeUser(userId: String): Flow<User?> = flow { emit(null) }

    override fun observeGearItems(userId: String): Flow<List<GearItem>> = flow { emit(emptyList()) }

    override fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>> = flow { emit(emptyList()) }

    override fun observeAchievements(userId: String): Flow<List<AchievementRow>> = flow { emit(emptyList()) }

    override fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>> = flow { emit(emptyList()) }

    override suspend fun updateUserProfile(
        userId: String,
        payload: ProfileUpdatePayload,
    ): AppResult<Unit> = AppResult.Failure(AppError.Unsupported)
}
