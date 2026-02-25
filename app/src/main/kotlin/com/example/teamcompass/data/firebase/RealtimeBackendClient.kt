package com.example.teamcompass.data.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

internal fun splitRealtimePath(path: String): List<String> {
    return path
        .trim()
        .split('/')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

internal interface RealtimeBackendClient {
    fun child(path: String): DatabaseReference
    fun push(path: String): DatabaseReference
    suspend fun get(path: String): DataSnapshot
    suspend fun setValue(path: String, value: Any?)
    suspend fun updateChildren(path: String, updates: Map<String, Any?>)
    suspend fun removeValue(path: String)
}

internal class FirebaseRealtimeBackendClient(
    private val reference: DatabaseReference,
) : RealtimeBackendClient {
    override fun child(path: String): DatabaseReference {
        val segments = splitRealtimePath(path)
        if (segments.isEmpty()) return reference
        return segments.fold(reference) { current, segment ->
            current.child(segment)
        }
    }

    override fun push(path: String): DatabaseReference = child(path).push()

    override suspend fun get(path: String): DataSnapshot = child(path).get().await()

    override suspend fun setValue(path: String, value: Any?) {
        child(path).setValue(value).await()
    }

    override suspend fun updateChildren(path: String, updates: Map<String, Any?>) {
        child(path).updateChildren(updates).await()
    }

    override suspend fun removeValue(path: String) {
        child(path).removeValue().await()
    }
}
