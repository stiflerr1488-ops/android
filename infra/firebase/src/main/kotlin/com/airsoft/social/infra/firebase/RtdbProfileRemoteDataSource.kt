package com.airsoft.social.infra.firebase

import com.airsoft.social.core.common.AppError
import com.airsoft.social.core.common.AppResult
import com.airsoft.social.core.data.ProfileRemoteDataSource
import com.airsoft.social.core.data.ProfileUpdatePayload
import com.airsoft.social.core.model.AchievementRow
import com.airsoft.social.core.model.GameHistoryRow
import com.airsoft.social.core.model.GearCategory
import com.airsoft.social.core.model.GearItem
import com.airsoft.social.core.model.PrivacySettings
import com.airsoft.social.core.model.TrustBadgeRow
import com.airsoft.social.core.model.User
import com.airsoft.social.core.model.UserRole
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class RtdbProfileRemoteDataSource(
    private val root: DatabaseReference?,
) : ProfileRemoteDataSource {
    override fun observeUser(userId: String): Flow<User?> {
        val reference = root?.child("users")?.child(userId)
            ?: return flowOf(null)
        return callbackFlow {
            val listener = reference.listen(
                onData = { snapshot -> trySend(snapshot.toUser(userId)) },
                onError = { close(it.toException()) },
            )
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    override fun observeGearItems(userId: String): Flow<List<GearItem>> {
        val reference = root?.child("userGear")?.child(userId)
            ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = reference.listen(
                onData = { snapshot ->
                    val items = snapshot.children.mapNotNull { child ->
                        child.toGearItem(child.key ?: return@mapNotNull null)
                    }
                    trySend(items.sortedByDescending { it.createdAt })
                },
                onError = { close(it.toException()) },
            )
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    override fun observeGameHistory(userId: String): Flow<List<GameHistoryRow>> {
        val reference = root?.child("userGameHistory")?.child(userId)
            ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = reference.listen(
                onData = { snapshot ->
                    val rows = snapshot.children.mapNotNull { child ->
                        child.toGameHistoryRow(child.key ?: return@mapNotNull null)
                    }
                    trySend(rows.sortedByDescending { it.date })
                },
                onError = { close(it.toException()) },
            )
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    override fun observeAchievements(userId: String): Flow<List<AchievementRow>> {
        val reference = root?.child("userAchievements")?.child(userId)
            ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = reference.listen(
                onData = { snapshot ->
                    val rows = snapshot.children.mapNotNull { child ->
                        child.toAchievementRow(child.key ?: return@mapNotNull null)
                    }
                    trySend(rows)
                },
                onError = { close(it.toException()) },
            )
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    override fun observeTrustBadges(userId: String): Flow<List<TrustBadgeRow>> {
        val reference = root?.child("userTrustBadges")?.child(userId)
            ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = reference.listen(
                onData = { snapshot ->
                    val rows = snapshot.children.mapNotNull { child ->
                        child.toTrustBadgeRow(child.key ?: return@mapNotNull null)
                    }
                    trySend(rows)
                },
                onError = { close(it.toException()) },
            )
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        payload: ProfileUpdatePayload,
    ): AppResult<Unit> {
        val reference = root?.child("users")?.child(userId)
            ?: return AppResult.Failure(AppError.Unsupported)
        val updates = mutableMapOf<String, Any?>(
            "callsign" to payload.callsign,
            "callsignLower" to payload.callsign.lowercase(),
            "firstName" to payload.firstName,
            "lastName" to payload.lastName,
            "bio" to payload.bio,
            "region" to payload.region,
            "exitRadiusKm" to payload.exitRadiusKm,
            "avatarUrl" to payload.avatarUrl,
            "bannerUrl" to payload.bannerUrl,
            "updatedAtMs" to ServerValue.TIMESTAMP,
            "privacy/showPhone" to payload.privacySettings.showPhone,
            "privacy/showEmail" to payload.privacySettings.showEmail,
            "privacy/showTelegram" to payload.privacySettings.showTelegram,
            "privacy/showRegion" to payload.privacySettings.showRegion,
            "privacy/showTeam" to payload.privacySettings.showTeam,
            "privacy/allowDirectMessages" to payload.privacySettings.allowDirectMessages,
            "privacy/allowTeamInvites" to payload.privacySettings.allowTeamInvites,
            "privacy/allowEventInvites" to payload.privacySettings.allowEventInvites,
        )

        return runCatching {
            reference.updateChildren(updates).await()
            AppResult.Success(Unit)
        }.getOrElse { throwable ->
            AppResult.Failure(AppError.Network(throwable.message))
        }
    }
}

private fun DatabaseReference.listen(
    onData: (DataSnapshot) -> Unit,
    onError: (com.google.firebase.database.DatabaseError) -> Unit,
): ValueEventListener {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) = onData(snapshot)
        override fun onCancelled(error: com.google.firebase.database.DatabaseError) = onError(error)
    }
    addValueEventListener(listener)
    return listener
}

private fun DataSnapshot.toUser(userId: String): User? {
    if (!exists()) return null
    val callsign = child("callsign").getValue(String::class.java).orEmpty()
    if (callsign.isBlank()) return null
    val firstName = child("firstName").getValue(String::class.java).orEmpty()
    val lastName = child("lastName").getValue(String::class.java).orEmpty()
    val roles = parseRoles(child("roles"))
    return User(
        id = userId,
        callsign = callsign,
        firstName = firstName,
        lastName = lastName,
        avatarUrl = child("avatarUrl").getValue(String::class.java),
        bannerUrl = child("bannerUrl").getValue(String::class.java),
        teamId = child("teamId").getValue(String::class.java),
        teamName = child("teamName").getValue(String::class.java),
        region = child("region").getValue(String::class.java),
        exitRadiusKm = child("exitRadiusKm").getValue(Int::class.java),
        bio = child("bio").getValue(String::class.java),
        roles = roles,
        privacySettings = child("privacy").toPrivacySettings(),
        isOnline = child("isOnline").getValue(Boolean::class.java) ?: false,
        lastSeen = child("lastSeenMs").getValue(Long::class.java)?.let(::Date),
        isVerified = child("isVerified").getValue(Boolean::class.java) ?: false,
        isBanned = child("isBanned").getValue(Boolean::class.java) ?: false,
        rating = child("rating").getValue(Double::class.java)?.toFloat(),
        reviewsCount = child("reviewsCount").getValue(Int::class.java) ?: 0,
        createdAt = child("createdAtMs").getValue(Long::class.java)?.let(::Date) ?: Date(),
        updatedAt = child("updatedAtMs").getValue(Long::class.java)?.let(::Date) ?: Date(),
    )
}

private fun DataSnapshot.toPrivacySettings(): PrivacySettings = PrivacySettings(
    showPhone = child("showPhone").getValue(Boolean::class.java) ?: false,
    showEmail = child("showEmail").getValue(Boolean::class.java) ?: false,
    showTelegram = child("showTelegram").getValue(Boolean::class.java) ?: false,
    showRegion = child("showRegion").getValue(Boolean::class.java) ?: true,
    showTeam = child("showTeam").getValue(Boolean::class.java) ?: true,
    allowDirectMessages = child("allowDirectMessages").getValue(Boolean::class.java) ?: true,
    allowTeamInvites = child("allowTeamInvites").getValue(Boolean::class.java) ?: true,
    allowEventInvites = child("allowEventInvites").getValue(Boolean::class.java) ?: true,
)

private fun parseRoles(snapshot: DataSnapshot): Set<UserRole> {
    if (!snapshot.exists()) return emptySet()
    val result = mutableSetOf<UserRole>()
    snapshot.children.forEach { child ->
        val key = child.key ?: return@forEach
        val enabled = child.getValue(Boolean::class.java)
        if (enabled == false) return@forEach
        runCatching { UserRole.valueOf(key.uppercase()) }.getOrNull()?.let(result::add)
    }
    return result
}

private fun DataSnapshot.toGearItem(id: String): GearItem? {
    val name = child("name").getValue(String::class.java).orEmpty()
    if (name.isBlank()) return null
    val categoryRaw = child("category").getValue(String::class.java)
    val category = runCatching { GearCategory.valueOf(categoryRaw.orEmpty()) }.getOrDefault(GearCategory.OTHER)
    return GearItem(
        id = id,
        name = name,
        category = category,
        description = child("description").getValue(String::class.java),
        isPrimary = child("isPrimary").getValue(Boolean::class.java) ?: false,
        notes = child("notes").getValue(String::class.java),
        imageUrl = child("imageUrl").getValue(String::class.java),
        createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
    )
}

private fun DataSnapshot.toGameHistoryRow(id: String): GameHistoryRow? {
    val eventName = child("eventName").getValue(String::class.java).orEmpty()
    val date = child("date").getValue(Long::class.java) ?: return null
    if (eventName.isBlank()) return null
    return GameHistoryRow(
        id = id,
        date = date,
        eventName = eventName,
    )
}

private fun DataSnapshot.toAchievementRow(id: String): AchievementRow? {
    val title = child("title").getValue(String::class.java).orEmpty()
    if (title.isBlank()) return null
    return AchievementRow(
        id = id,
        title = title,
        description = child("description").getValue(String::class.java).orEmpty(),
    )
}

private fun DataSnapshot.toTrustBadgeRow(id: String): TrustBadgeRow? {
    val title = child("title").getValue(String::class.java).orEmpty()
    if (title.isBlank()) return null
    return TrustBadgeRow(
        id = id,
        title = title,
        description = child("description").getValue(String::class.java).orEmpty(),
    )
}
