package com.example.teamcompass.data.firebase

import com.example.teamcompass.core.GeoCell
import com.example.teamcompass.core.Role
import com.example.teamcompass.domain.TeamActiveCommand
import com.example.teamcompass.domain.TeamCommandRole
import com.example.teamcompass.domain.TeamMemberRoleProfile
import com.example.teamcompass.domain.TeamViewMode
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

internal fun teamCommandToLegacyRole(commandRole: TeamCommandRole?): Role {
    return when (commandRole) {
        TeamCommandRole.SIDE_COMMANDER,
        TeamCommandRole.COMPANY_COMMANDER,
        TeamCommandRole.PLATOON_COMMANDER,
        TeamCommandRole.TEAM_COMMANDER,
        -> Role.COMMANDER

        TeamCommandRole.TEAM_DEPUTY -> Role.DEPUTY
        TeamCommandRole.FIGHTER,
        null,
        -> Role.FIGHTER
    }
}

internal fun <T> applyUpsertOrRemoveFromSnapshot(
    snapshot: DataSnapshot,
    parse: (DataSnapshot) -> T?,
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    idOf: (T) -> String,
    createdAtOf: (T) -> Long,
    onChanged: () -> Unit,
    maxItems: Int = Int.MAX_VALUE,
) {
    parse(snapshot)?.let { parsed ->
        upsertSortedByCreatedAtDescending(
            map = map,
            sorted = sorted,
            item = parsed,
            idOf = idOf,
            createdAtOf = createdAtOf,
            maxItems = maxItems,
        )
    } ?: snapshot.key?.let { key ->
        removeById(
            map = map,
            sorted = sorted,
            id = key,
            idOf = idOf,
        )
    }
    onChanged()
}

internal fun <T> applyRemoveBySnapshotKey(
    snapshot: DataSnapshot,
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    idOf: (T) -> String,
    onChanged: () -> Unit,
) {
    snapshot.key?.let { key ->
        removeById(
            map = map,
            sorted = sorted,
            id = key,
            idOf = idOf,
        )
    }
    onChanged()
}

internal fun desiredStateRefsForCell(
    viewMode: TeamViewMode,
    stateCellsReadEnabled: Boolean,
    cellId: String?,
    base: DatabaseReference,
): Map<String, DatabaseReference> {
    if (viewMode == TeamViewMode.COMMAND || !stateCellsReadEnabled) {
        return mapOf("state" to base.child("state"))
    }

    val cells = cellId
        ?.let(GeoCell::neighbors3x3)
        .orEmpty()
    if (cells.isEmpty()) {
        return mapOf("state" to base.child("state"))
    }

    val refs = linkedMapOf<String, DatabaseReference>()
    cells.forEach { neighborId ->
        refs["stateCell:$neighborId"] = base.child("stateCells").child(neighborId)
    }
    return refs
}

internal fun <T> buildSnapshotChildEventListener(
    parse: (DataSnapshot) -> T?,
    map: MutableMap<String, T>,
    sorted: MutableList<T>,
    idOf: (T) -> String,
    createdAtOf: (T) -> Long,
    onChanged: () -> Unit,
    onCancelled: (DatabaseError) -> Unit,
    beforeMutation: () -> Unit = {},
    maxItems: Int = Int.MAX_VALUE,
): ChildEventListener {
    fun onUpsert(snapshot: DataSnapshot) {
        beforeMutation()
        applyUpsertOrRemoveFromSnapshot(
            snapshot = snapshot,
            parse = parse,
            map = map,
            sorted = sorted,
            idOf = idOf,
            createdAtOf = createdAtOf,
            onChanged = onChanged,
            maxItems = maxItems,
        )
    }

    fun onRemove(snapshot: DataSnapshot) {
        beforeMutation()
        applyRemoveBySnapshotKey(
            snapshot = snapshot,
            map = map,
            sorted = sorted,
            idOf = idOf,
            onChanged = onChanged,
        )
    }

    return object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            onUpsert(snapshot)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            onUpsert(snapshot)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            onRemove(snapshot)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

        override fun onCancelled(error: DatabaseError) {
            onCancelled(error)
        }
    }
}

internal fun buildRoleProfilesChildEventListener(
    roleProfiles: MutableMap<String, TeamMemberRoleProfile>,
    roleByUid: MutableMap<String, Role>,
    onChanged: () -> Unit,
    onCancelled: (DatabaseError) -> Unit,
    onDirtyFlagsChanged: () -> Unit,
): ChildEventListener {
    fun upsertOrRemove(snapshot: DataSnapshot) {
        parseRoleProfile(snapshot)?.let { parsed ->
            roleProfiles[parsed.uid] = parsed
            roleByUid[parsed.uid] = teamCommandToLegacyRole(parsed.commandRole)
        } ?: run {
            roleProfiles.remove(snapshot.key)
            roleByUid.remove(snapshot.key)
        }
        onDirtyFlagsChanged()
        onChanged()
    }

    return object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            upsertOrRemove(snapshot)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            upsertOrRemove(snapshot)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            snapshot.key?.let {
                roleProfiles.remove(it)
                roleByUid.remove(it)
            }
            onDirtyFlagsChanged()
            onChanged()
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

        override fun onCancelled(error: DatabaseError) {
            onCancelled(error)
        }
    }
}

internal fun buildActiveCommandValueListener(
    onCommandChanged: (TeamActiveCommand?) -> Unit,
    onCancelled: (DatabaseError) -> Unit,
): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onCommandChanged(parseActiveCommand(snapshot))
        }

        override fun onCancelled(error: DatabaseError) {
            onCancelled(error)
        }
    }
}
