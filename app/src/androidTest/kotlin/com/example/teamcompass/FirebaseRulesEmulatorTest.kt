package com.example.teamcompass

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.core.TeamCodeSecurity
import com.example.teamcompass.data.firebase.FirebaseTeamRepository
import com.example.teamcompass.domain.TeamActionError
import com.example.teamcompass.domain.TeamActionResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.os.Build
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class FirebaseRulesEmulatorTest {
    private val emulatorHost: String by lazy { resolveEmulatorHost() }

    private val auth: FirebaseAuth by lazy {
        configureAuthEmulator()
    }

    private val db: FirebaseDatabase by lazy {
        configureDatabaseEmulator()
    }

    private val repository: FirebaseTeamRepository by lazy {
        FirebaseTeamRepository(db.reference)
    }

    @Before
    fun checkEmulator() {
        assumeTrue(isPortOpen(emulatorHost, DB_PORT) && isPortOpen(emulatorHost, AUTH_PORT))
        auth.signOut()
    }

    @Test
    fun nonMember_cannotReadTeamState() = runBlocking {
        val teamCode = randomCode()

        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        signIn()

        val denied = runCatching {
            db.reference.child("teams").child(teamCode).child("state").get().await()
        }.exceptionOrNull()

        assertTrue(denied != null)
    }

    @Test
    fun member_canJoinAndReadTeam() = runBlocking {
        val teamCode = randomCode()

        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joiner = signIn()
        db.reference.child("teams").child(teamCode).child("members").child(joiner)
            .setValue(mapOf("callsign" to "joiner", "joinedAtMs" to System.currentTimeMillis()))
            .await()

        val meta = db.reference.child("teams").child(teamCode).child("meta").get().await()
        assertTrue(meta.exists())
        assertTrue(meta.child("joinSalt").value is String)
        assertTrue(meta.child("joinHash").value is String)
    }

    @Test
    fun member_cannotEditForeignTeamPoint() = runBlocking {
        val teamCode = randomCode()

        val ownerUid = signIn()
        createTeam(teamCode, "owner")
        db.reference.child("teams").child(teamCode).child("points").child("p1")
            .setValue(
                mapOf(
                    "lat" to 55.0,
                    "lon" to 37.0,
                    "label" to "A",
                    "icon" to "flag",
                    "createdAtMs" to System.currentTimeMillis(),
                    "createdBy" to ownerUid,
                )
            )
            .await()

        auth.signOut()
        val joinerUid = signIn()
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(mapOf("callsign" to "joiner", "joinedAtMs" to System.currentTimeMillis()))
            .await()

        val denied = runCatching {
            db.reference.child("teams").child(teamCode).child("points").child("p1")
                .updateChildren(mapOf("label" to "tampered"))
                .await()
        }.exceptionOrNull()

        assertTrue(denied != null)
    }

    @Test
    fun member_canWriteAndReadOwnMemberPrefs() = runBlocking {
        val teamCode = randomCode()

        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joinerUid = signIn()
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(mapOf("callsign" to "joiner", "joinedAtMs" to System.currentTimeMillis()))
            .await()

        val now = System.currentTimeMillis()
        db.reference.child("teams").child(teamCode).child("memberPrefs").child(joinerUid)
            .setValue(
                mapOf(
                    "preset" to "NEAR",
                    "nearRadiusM" to 180,
                    "showDead" to true,
                    "showStale" to false,
                    "focusMode" to true,
                    "updatedAtMs" to now,
                )
            )
            .await()

        val snapshot = db.reference.child("teams").child(teamCode).child("memberPrefs").child(joinerUid).get().await()
        assertTrue(snapshot.exists())
        assertTrue(snapshot.child("preset").value == "NEAR")
    }

    @Test
    fun member_cannotReadOrWriteForeignMemberPrefs() = runBlocking {
        val teamCode = randomCode()

        val ownerUid = signIn()
        createTeam(teamCode, "owner")
        db.reference.child("teams").child(teamCode).child("memberPrefs").child(ownerUid)
            .setValue(
                mapOf(
                    "preset" to "ALL",
                    "nearRadiusM" to 150,
                    "showDead" to true,
                    "showStale" to true,
                    "focusMode" to false,
                    "updatedAtMs" to System.currentTimeMillis(),
                )
            )
            .await()

        auth.signOut()
        val joinerUid = signIn()
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(mapOf("callsign" to "joiner", "joinedAtMs" to System.currentTimeMillis()))
            .await()

        val deniedRead = runCatching {
            db.reference.child("teams").child(teamCode).child("memberPrefs").child(ownerUid).get().await()
        }.exceptionOrNull()
        assertTrue(deniedRead != null)

        val deniedWrite = runCatching {
            db.reference.child("teams").child(teamCode).child("memberPrefs").child(ownerUid)
                .setValue(
                    mapOf(
                        "preset" to "SOS",
                        "nearRadiusM" to 200,
                        "showDead" to false,
                        "showStale" to false,
                        "focusMode" to true,
                        "updatedAtMs" to System.currentTimeMillis(),
                    )
                )
                .await()
        }.exceptionOrNull()
        assertTrue(deniedWrite != null)
    }

    @Test
    fun meta_join_fields_are_immutable_after_create() = runBlocking {
        val teamCode = randomCode()

        signIn()
        createTeam(teamCode, "owner")

        val deniedSaltRewrite = runCatching {
            db.reference.child("teams").child(teamCode).child("meta").child("joinSalt")
                .setValue("a".repeat(16))
                .await()
        }.exceptionOrNull()
        assertTrue(deniedSaltRewrite != null)

        val deniedHashRewrite = runCatching {
            db.reference.child("teams").child(teamCode).child("meta").child("joinHash")
                .setValue("b".repeat(64))
                .await()
        }.exceptionOrNull()
        assertTrue(deniedHashRewrite != null)
    }

    @Test
    fun create_team_missing_join_security_fields_is_denied() = runBlocking {
        val teamCode = randomCode()
        val uid = signIn()
        val now = System.currentTimeMillis()

        val denied = runCatching {
            db.reference.child("teams").child(teamCode)
                .setValue(
                    mapOf(
                        "meta" to mapOf(
                            "createdAtMs" to now,
                            "createdBy" to uid,
                            "isLocked" to false,
                            "expiresAtMs" to (now + 60_000L),
                        ),
                        "members" to mapOf(
                            uid to mapOf(
                                "callsign" to "owner",
                                "joinedAtMs" to now,
                            )
                        ),
                    )
                )
                .await()
        }.exceptionOrNull()

        assertTrue(denied != null)
    }

    @Test
    fun repository_join_rejects_non_six_digit_code() = runBlocking {
        val uid = signIn()
        val shortCodeResult = repository.joinTeam(
            teamCode = "123",
            uid = uid,
            callsign = "joiner",
            nowMs = System.currentTimeMillis(),
        )
        assertFailureError(shortCodeResult, TeamActionError.INVALID_INPUT)

        val alphaCodeResult = repository.joinTeam(
            teamCode = "12A456",
            uid = uid,
            callsign = "joiner",
            nowMs = System.currentTimeMillis(),
        )
        assertFailureError(alphaCodeResult, TeamActionError.INVALID_INPUT)
    }

    @Test
    fun repository_join_returns_not_found_when_meta_hash_is_inconsistent() = runBlocking {
        val teamCode = randomCode()
        signIn()
        val joinSalt = TeamCodeSecurity.generateSaltHex()
        val mismatchedHash = TeamCodeSecurity.hashJoinCode(differentCode(teamCode), joinSalt)
        createTeam(
            teamCode = teamCode,
            callsign = "owner",
            joinSaltOverride = joinSalt,
            joinHashOverride = mismatchedHash,
        )

        auth.signOut()
        val joinerUid = signIn()
        val result = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "joiner",
            nowMs = System.currentTimeMillis(),
        )

        assertFailureError(result, TeamActionError.NOT_FOUND)
    }

    @Test
    fun repository_join_returns_locked_when_meta_isLocked_true() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(
            teamCode = teamCode,
            callsign = "owner",
            isLockedOverride = true,
        )

        auth.signOut()
        val joinerUid = signIn()
        val result = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "joiner",
            nowMs = System.currentTimeMillis(),
        )

        assertFailureError(result, TeamActionError.LOCKED)
    }

    @Test
    fun repository_join_returns_expired_when_meta_expiresAtMs_in_past() = runBlocking {
        val teamCode = randomCode()
        signIn()
        val now = System.currentTimeMillis()
        createTeam(
            teamCode = teamCode,
            callsign = "owner",
            createdAtMsOverride = now - 120_000L,
            expiresAtMsOverride = now - 60_000L,
        )

        auth.signOut()
        val joinerUid = signIn()
        val result = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "joiner",
            nowMs = now,
        )

        assertFailureError(result, TeamActionError.EXPIRED)
    }

    @Test
    fun member_cannot_mutate_point_createdBy_or_createdAtMs() = runBlocking {
        val teamCode = randomCode()
        val ownerUid = signIn()
        createTeam(teamCode, "owner")
        val pointRef = db.reference.child("teams").child(teamCode).child("points").child("p1")
        val createdAtMs = System.currentTimeMillis()
        pointRef.setValue(
            mapOf(
                "lat" to 55.0,
                "lon" to 37.0,
                "label" to "A",
                "icon" to "flag",
                "createdAtMs" to createdAtMs,
                "createdBy" to ownerUid,
            )
        ).await()

        val deniedCreatedByMutation = runCatching {
            pointRef.updateChildren(mapOf("createdBy" to "intruder"))
                .await()
        }.exceptionOrNull()
        assertPermissionDenied(deniedCreatedByMutation)

        val deniedCreatedAtMutation = runCatching {
            pointRef.updateChildren(mapOf("createdAtMs" to (createdAtMs + 1_000L)))
                .await()
        }.exceptionOrNull()
        assertPermissionDenied(deniedCreatedAtMutation)
    }

    @Test
    fun member_can_write_enemy_ping_with_bluetooth_type() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joinerUid = signIn()
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(mapOf("callsign" to "joiner", "joinedAtMs" to System.currentTimeMillis()))
            .await()

        val now = System.currentTimeMillis()
        val pingId = "bt_ping_1"
        val payload = mapOf(
            "lat" to 55.0,
            "lon" to 37.0,
            "createdAtMs" to now,
            "createdBy" to joinerUid,
            "expiresAtMs" to (now + 30_000L),
            "type" to "BLUETOOTH",
        )

        db.reference.child("teams").child(teamCode)
            .updateChildren(
                mapOf(
                    "enemyPings/$pingId" to payload,
                    "rateLimits/enemyPing/$joinerUid/lastAtMs" to now,
                )
            )
            .await()

        val snapshot = db.reference.child("teams").child(teamCode).child("enemyPings").child(pingId).get().await()
        assertTrue(snapshot.exists())
        assertTrue(snapshot.child("type").value == "BLUETOOTH")
    }

    @Test
    fun create_team_with_invalid_joinSalt_format_is_denied() = runBlocking {
        val teamCode = randomCode()
        val uid = signIn()
        val now = System.currentTimeMillis()
        val denied = runCatching {
            db.reference.child("teams").child(teamCode)
                .setValue(
                    mapOf(
                        "meta" to mapOf(
                            "createdAtMs" to now,
                            "createdBy" to uid,
                            "isLocked" to false,
                            "expiresAtMs" to (now + 60_000L),
                            "joinSalt" to "salt-with-invalid-format",
                            "joinHash" to TeamCodeSecurity.hashJoinCode(teamCode, TeamCodeSecurity.generateSaltHex()),
                        ),
                        "members" to mapOf(
                            uid to mapOf(
                                "callsign" to "owner",
                                "joinedAtMs" to now,
                            )
                        ),
                    )
                )
                .await()
        }.exceptionOrNull()

        assertPermissionDenied(denied)
    }

    @Test
    fun create_team_with_invalid_joinHash_length_is_denied() = runBlocking {
        val teamCode = randomCode()
        val uid = signIn()
        val now = System.currentTimeMillis()
        val denied = runCatching {
            db.reference.child("teams").child(teamCode)
                .setValue(
                    mapOf(
                        "meta" to mapOf(
                            "createdAtMs" to now,
                            "createdBy" to uid,
                            "isLocked" to false,
                            "expiresAtMs" to (now + 60_000L),
                            "joinSalt" to TeamCodeSecurity.generateSaltHex(),
                            "joinHash" to "deadbeef",
                        ),
                        "members" to mapOf(
                            uid to mapOf(
                                "callsign" to "owner",
                                "joinedAtMs" to now,
                            )
                        ),
                    )
                )
                .await()
        }.exceptionOrNull()

        assertPermissionDenied(denied)
    }

    @Test
    fun repository_join_adds_member_when_meta_is_valid() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joinerUid = signIn()
        val joinResult = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "joiner",
            nowMs = System.currentTimeMillis(),
        )
        assertSuccess(joinResult)

        val memberSnapshot = db.reference.child("teams").child(teamCode).child("members").child(joinerUid).get().await()
        assertTrue(memberSnapshot.exists())
        assertTrue(memberSnapshot.child("callsign").value == "joiner")
    }

    @Test
    fun joinTeam_existingMember_preserves_joinedAtMs_with_atomicWrite() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joinerUid = signIn()
        val existingJoinedAtMs = System.currentTimeMillis() - 120_000L
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(
                mapOf(
                    "callsign" to "before",
                    "joinedAtMs" to existingJoinedAtMs,
                )
            )
            .await()
        db.reference.child("teams").child(teamCode).child("memberRoles").child(joinerUid)
            .setValue(
                mapOf(
                    "commandRole" to "FIGHTER",
                    "combatRole" to "NONE",
                    "vehicleRole" to "NONE",
                    "sideId" to "SIDE-1",
                    "callsign" to "before",
                    "updatedAtMs" to existingJoinedAtMs,
                    "updatedBy" to joinerUid,
                )
            )
            .await()

        val joinResult = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "after",
            nowMs = System.currentTimeMillis(),
        )
        assertSuccess(joinResult)

        val memberSnapshot = db.reference.child("teams").child(teamCode).child("members").child(joinerUid).get().await()
        assertTrue(memberSnapshot.exists())
        assertTrue(memberSnapshot.child("callsign").value == "after")
        assertTrue(memberSnapshot.child("joinedAtMs").value == existingJoinedAtMs)

        val roleSnapshot = db.reference.child("teams").child(teamCode).child("memberRoles").child(joinerUid).get().await()
        assertTrue(roleSnapshot.exists())
        assertTrue(roleSnapshot.child("callsign").value == "after")
    }

    @Test
    fun joinTeam_atomicMultiPathWrite_noPartialState_onFailure() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val joinerUid = signIn()
        val existingJoinedAtMs = System.currentTimeMillis() - 180_000L
        val roleUpdatedAtMs = System.currentTimeMillis() + 60_000L
        db.reference.child("teams").child(teamCode).child("members").child(joinerUid)
            .setValue(
                mapOf(
                    "callsign" to "before",
                    "joinedAtMs" to existingJoinedAtMs,
                )
            )
            .await()
        db.reference.child("teams").child(teamCode).child("memberRoles").child(joinerUid)
            .setValue(
                mapOf(
                    "commandRole" to "FIGHTER",
                    "combatRole" to "NONE",
                    "vehicleRole" to "NONE",
                    "sideId" to "SIDE-1",
                    "callsign" to "before",
                    "updatedAtMs" to roleUpdatedAtMs,
                    "updatedBy" to joinerUid,
                )
            )
            .await()

        val joinResult = repository.joinTeam(
            teamCode = teamCode,
            uid = joinerUid,
            callsign = "after",
            nowMs = roleUpdatedAtMs - 30_000L,
        )
        assertFailureError(joinResult, TeamActionError.PERMISSION_DENIED)

        val memberSnapshot = db.reference.child("teams").child(teamCode).child("members").child(joinerUid).get().await()
        val roleSnapshot = db.reference.child("teams").child(teamCode).child("memberRoles").child(joinerUid).get().await()
        assertTrue(memberSnapshot.exists())
        assertTrue(roleSnapshot.exists())
        assertTrue(memberSnapshot.child("callsign").value == "before")
        assertTrue(memberSnapshot.child("joinedAtMs").value == existingJoinedAtMs)
        assertTrue(roleSnapshot.child("callsign").value == "before")
        assertTrue(roleSnapshot.child("updatedAtMs").value == roleUpdatedAtMs)
    }

    private suspend fun signIn(): String {
        return auth.signInAnonymously().await().user?.uid.orEmpty()
    }

    private suspend fun createTeam(
        teamCode: String,
        callsign: String,
        joinSaltOverride: String? = null,
        joinHashOverride: String? = null,
        isLockedOverride: Boolean = false,
        createdAtMsOverride: Long? = null,
        expiresAtMsOverride: Long? = null,
    ) {
        val uid = auth.currentUser?.uid ?: error("No auth uid")
        val now = createdAtMsOverride ?: System.currentTimeMillis()
        val expiresAtMs = expiresAtMsOverride ?: (now + 60_000L)
        val joinSalt = joinSaltOverride ?: TeamCodeSecurity.generateSaltHex()
        val joinHash = joinHashOverride ?: TeamCodeSecurity.hashJoinCode(teamCode, joinSalt)
        db.reference.child("teams").child(teamCode)
            .setValue(
                mapOf(
                    "meta" to mapOf(
                        "createdAtMs" to now,
                        "createdBy" to uid,
                        "isLocked" to isLockedOverride,
                        "expiresAtMs" to expiresAtMs,
                        "joinSalt" to joinSalt,
                        "joinHash" to joinHash,
                    ),
                    "members" to mapOf(
                        uid to mapOf(
                            "callsign" to callsign,
                            "joinedAtMs" to now,
                        )
                    ),
                )
            )
            .await()
    }

    private fun randomCode(): String {
        return Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
    }

    private fun differentCode(base: String): String {
        var candidate = randomCode()
        while (candidate == base) {
            candidate = randomCode()
        }
        return candidate
    }

    private fun assertFailureError(result: TeamActionResult<*>, expectedError: TeamActionError) {
        when (result) {
            is TeamActionResult.Success -> throw AssertionError("Expected failure with $expectedError, got success")
            is TeamActionResult.Failure -> {
                if (result.details.error != expectedError) {
                    throw AssertionError(
                        "Expected failure with $expectedError, got ${result.details.error} " +
                            "(message=${result.details.message})"
                    )
                }
            }
        }
    }

    private fun assertSuccess(result: TeamActionResult<*>) {
        when (result) {
            is TeamActionResult.Success -> Unit
            is TeamActionResult.Failure -> {
                throw AssertionError(
                    "Expected success, got failure ${result.details.error} " +
                        "(message=${result.details.message})"
                )
            }
        }
    }

    private fun assertPermissionDenied(error: Throwable?) {
        if (error == null) {
            throw AssertionError("Expected permission denied failure, got success")
        }
        val message = error.message.orEmpty()
        if (!message.contains("Permission denied", ignoreCase = true)) {
            throw AssertionError("Expected permission denied failure, got: $message")
        }
    }

    private fun isPortOpen(host: String, port: Int): Boolean {
        return runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 800)
            }
            true
        }.getOrDefault(false)
    }

    private fun configureAuthEmulator(): FirebaseAuth {
        val instance = FirebaseAuth.getInstance()
        runCatching { instance.useEmulator(emulatorHost, AUTH_PORT) }
            .onFailure { error ->
                if (error !is IllegalStateException ||
                    !error.message.orEmpty().contains("already been initialized")
                ) {
                    throw error
                }
            }
        return instance
    }

    private fun configureDatabaseEmulator(): FirebaseDatabase {
        val instance = FirebaseDatabase.getInstance(testDbUrl())
        runCatching { instance.useEmulator(emulatorHost, DB_PORT) }
            .onFailure { error ->
                if (error !is IllegalStateException ||
                    !error.message.orEmpty().contains("already been initialized")
                ) {
                    throw error
                }
            }
        return instance
    }

    private fun isAndroidEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            product.contains("sdk") ||
            product.contains("emulator")
    }

    private fun resolveEmulatorHost(): String {
        return if (isAndroidEmulator()) ANDROID_EMULATOR_HOST else LOCALHOST
    }

    private fun testDbUrl(): String {
        return "http://$emulatorHost:$DB_PORT?ns=$TEST_DB_NAMESPACE"
    }

    companion object {
        private const val ANDROID_EMULATOR_HOST = "10.0.2.2"
        private const val LOCALHOST = "127.0.0.1"
        private const val AUTH_PORT = 9099
        private const val DB_PORT = 9000
        private const val TEST_DB_NAMESPACE = "demo-teamcompass-default-rtdb"
    }
}
