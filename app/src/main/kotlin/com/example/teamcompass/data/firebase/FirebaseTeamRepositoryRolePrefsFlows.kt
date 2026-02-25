package com.example.teamcompass.data.firebase

import com.example.teamcompass.domain.TeamMemberPrefs
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun observeMemberPrefsFlow(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    uid: String,
): Flow<TeamMemberPrefs?> = callbackFlow {
    val code = validatedTeamCodeOrNull(teamCode)
    if (code == null) {
        close(IllegalArgumentException("Invalid team code format"))
        return@callbackFlow
    }
    val ref = backendClient.child("teams/$code/memberPrefs/$uid")
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            trySend(parseMemberPrefs(snapshot))
        }

        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    ref.addValueEventListener(listener)
    awaitClose { ref.removeEventListener(listener) }
}

internal fun observeTeamRoleProfilesFlow(
    backendClient: RealtimeBackendClient,
    teamCode: String,
): Flow<List<TeamMemberRoleProfile>> = callbackFlow {
    val code = validatedTeamCodeOrNull(teamCode)
    if (code == null) {
        close(IllegalArgumentException("Invalid team code format"))
        return@callbackFlow
    }
    val ref = backendClient.child("teams/$code/memberRoles")
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val profiles = snapshot.children
                .mapNotNull(::parseRoleProfile)
                .sortedWith(
                    compareBy<TeamMemberRoleProfile> { it.commandRole.rank }
                        .thenBy { it.callsign.orEmpty().lowercase() }
                        .thenBy { it.uid }
                )
            trySend(profiles)
        }

        override fun onCancelled(error: DatabaseError) {
            close()
        }
    }
    ref.addValueEventListener(listener)
    awaitClose { ref.removeEventListener(listener) }
}
