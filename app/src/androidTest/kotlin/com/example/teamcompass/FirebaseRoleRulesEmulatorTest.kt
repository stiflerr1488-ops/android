package com.example.teamcompass

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.teamcompass.core.TeamCodeSecurity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class FirebaseRoleRulesEmulatorTest {
    private val emulatorHost: String by lazy { resolveEmulatorHost() }

    private val auth: FirebaseAuth by lazy {
        configureAuthEmulator()
    }

    private val db: FirebaseDatabase by lazy {
        configureDatabaseEmulator()
    }

    @Before
    fun checkEmulator() {
        assumeTrue(isPortOpen(emulatorHost, DB_PORT) && isPortOpen(emulatorHost, AUTH_PORT))
        auth.signOut()
    }

    @Test
    fun nonMember_cannotReadTeamMeta() = runBlocking {
        val teamCode = randomCode()
        signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        signIn()
        val denied = runCatching {
            db.reference.child("teams").child(teamCode).child("meta").get().await()
        }.exceptionOrNull()
        assertPermissionDenied(denied)
    }

    @Test
    fun fighter_cannotSelfPromoteToSideCommander() = runBlocking {
        val teamCode = randomCode()
        val ownerUid = signIn()
        createTeam(teamCode, "owner")

        auth.signOut()
        val fighterUid = signIn()
        addMember(teamCode, fighterUid, "fighter")

        val denied = runCatching {
            db.reference.child("teams").child(teamCode).child("memberRoles").child(fighterUid)
                .setValue(
                    roleMap(
                        commandRole = "SIDE_COMMANDER",
                        sideId = "SIDE-1",
                        updatedBy = fighterUid,
                    )
                )
                .await()
        }.exceptionOrNull()
        assertPermissionDenied(denied)

        // owner role remains valid and untouched
        val ownerRole = db.reference.child("teams").child(teamCode).child("memberRoles").child(ownerUid).get().await()
        assertTrue(ownerRole.exists())
        assertTrue(ownerRole.child("commandRole").value == "SIDE_COMMANDER")
    }

    @Test
    fun sideCommander_canAssignCompanyCommander_inSameSide() = runBlocking {
        val teamCode = randomCode()
        val ownerUid = signIn()
        createTeam(teamCode, "owner")
        val targetUid = "target-uid-0"
        val result = runCatching {
            db.reference.child("teams").child(teamCode).child("memberRoles").child(targetUid)
                .setValue(
                    roleMap(
                        commandRole = "COMPANY_COMMANDER",
                        sideId = "SIDE-1",
                        companyId = "C1",
                        updatedBy = ownerUid,
                    )
                )
                .await()
        }.exceptionOrNull()
        assertTrue(result == null)
    }

    @Test
    fun companyCommander_cannotPromoteTargetAboveOwnRank_andCannotWriteOutOfScope() = runBlocking {
        val teamCode = randomCode()
        val companyCommanderUid = signIn()
        createTeam(
            teamCode = teamCode,
            callsign = "company",
            ownerCommandRole = "COMPANY_COMMANDER",
            ownerCompanyId = "C1",
        )
        val targetUid = "target-uid-1"

        // Company commander tries to assign SIDE_COMMANDER (rank-up) -> denied.
        val deniedRankUp = runCatching {
            db.reference.child("teams").child(teamCode).child("memberRoles").child(targetUid)
                .setValue(
                    roleMap(
                        commandRole = "SIDE_COMMANDER",
                        sideId = "SIDE-1",
                        updatedBy = companyCommanderUid,
                    )
                )
                .await()
        }.exceptionOrNull()
        assertPermissionDenied(deniedRankUp)

        // Company commander tries to assign fighter in foreign company C2 -> denied.
        val deniedOutOfScope = runCatching {
            db.reference.child("teams").child(teamCode).child("memberRoles").child(targetUid)
                .setValue(
                    roleMap(
                        commandRole = "FIGHTER",
                        sideId = "SIDE-1",
                        companyId = "C2",
                        platoonId = "P2",
                        teamId = "T2",
                        updatedBy = companyCommanderUid,
                    )
                )
                .await()
        }.exceptionOrNull()
        assertPermissionDenied(deniedOutOfScope)
    }

    private suspend fun signIn(): String {
        return auth.signInAnonymously().await().user?.uid.orEmpty()
    }

    private suspend fun addMember(teamCode: String, uid: String, callsign: String) {
        db.reference.child("teams").child(teamCode).child("members").child(uid)
            .setValue(
                mapOf(
                    "callsign" to callsign,
                    "joinedAtMs" to System.currentTimeMillis(),
                )
            )
            .await()
    }

    private suspend fun createTeam(
        teamCode: String,
        callsign: String,
        ownerCommandRole: String = "SIDE_COMMANDER",
        ownerCompanyId: String? = null,
        ownerPlatoonId: String? = null,
        ownerTeamId: String? = null,
    ) {
        val uid = auth.currentUser?.uid ?: error("No auth uid")
        val now = System.currentTimeMillis()
        val joinSalt = TeamCodeSecurity.generateSaltHex()
        val joinHash = TeamCodeSecurity.hashJoinCode(teamCode, joinSalt)
        db.reference.child("teams").child(teamCode)
            .setValue(
                mapOf(
                    "meta" to mapOf(
                        "createdAtMs" to now,
                        "createdBy" to uid,
                        "isLocked" to false,
                        "expiresAtMs" to (now + 60_000L),
                        "joinSalt" to joinSalt,
                        "joinHash" to joinHash,
                    ),
                    "members" to mapOf(
                        uid to mapOf(
                            "callsign" to callsign,
                            "joinedAtMs" to now,
                        )
                    ),
                    "memberRoles" to mapOf(
                        uid to roleMap(
                            commandRole = ownerCommandRole,
                            sideId = "SIDE-1",
                            companyId = ownerCompanyId,
                            platoonId = ownerPlatoonId,
                            teamId = ownerTeamId,
                            updatedBy = uid,
                        )
                    )
                )
            )
            .await()
    }

    private fun roleMap(
        commandRole: String,
        sideId: String,
        companyId: String? = null,
        platoonId: String? = null,
        teamId: String? = null,
        vehicleId: String? = null,
        combatRole: String = "NONE",
        vehicleRole: String = "NONE",
        updatedBy: String,
    ): Map<String, Any> {
        val out = linkedMapOf<String, Any>(
            "commandRole" to commandRole,
            "combatRole" to combatRole,
            "vehicleRole" to vehicleRole,
            "sideId" to sideId,
            "updatedAtMs" to System.currentTimeMillis(),
            "updatedBy" to updatedBy,
        )
        if (!companyId.isNullOrBlank()) out["companyId"] = companyId
        if (!platoonId.isNullOrBlank()) out["platoonId"] = platoonId
        if (!teamId.isNullOrBlank()) out["teamId"] = teamId
        if (!vehicleId.isNullOrBlank()) out["vehicleId"] = vehicleId
        return out
    }

    private fun randomCode(): String {
        return Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
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
