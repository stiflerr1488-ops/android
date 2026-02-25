package com.example.teamcompass.data.firebase

import com.example.teamcompass.BuildConfig
import com.example.teamcompass.core.PlayerState
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal fun observeTeamCellsFlow(
    backendClient: RealtimeBackendClient,
    teamCode: String,
    cellIds: Set<String>,
    snapshotDebounceMs: Long,
): Flow<List<PlayerState>> = callbackFlow {
    if (!BuildConfig.STATE_CELLS_V1_ENABLED) {
        trySend(emptyList())
        close()
        return@callbackFlow
    }
    val code = validatedTeamCodeOrNull(teamCode)
    if (code == null) {
        close(IllegalArgumentException("Invalid team code format"))
        return@callbackFlow
    }
    val normalizedCellIds = cellIds
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .toSet()

    if (normalizedCellIds.isEmpty()) {
        trySend(emptyList())
        close()
        return@callbackFlow
    }

    val playersByUid = linkedMapOf<String, PlayerState>()
    val refs = mutableListOf<DatabaseReference>()
    val listeners = mutableListOf<ChildEventListener>()
    var pendingEmitJob: Job? = null

    fun emitNow() {
        trySend(playersByUid.values.toList())
    }

    fun scheduleEmit() {
        if (pendingEmitJob?.isActive == true) return
        pendingEmitJob = launch {
            delay(snapshotDebounceMs)
            emitNow()
        }
    }

    normalizedCellIds.forEach { cellId ->
        val ref = backendClient.child("teams/$code/stateCells/$cellId")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                parsePlayer(snapshot, selfUid = "")?.let { playersByUid[it.uid] = it }
                scheduleEmit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                parsePlayer(snapshot, selfUid = "")?.let { playersByUid[it.uid] = it }
                scheduleEmit()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot.key?.let { playersByUid.remove(it) }
                scheduleEmit()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                close()
            }
        }
        ref.addChildEventListener(listener)
        refs += ref
        listeners += listener
    }

    emitNow()
    awaitClose {
        pendingEmitJob?.cancel()
        refs.zip(listeners).forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
    }
}
