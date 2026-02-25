package com.example.teamcompass.ui

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.UUID

interface ActionTraceIdProvider {
    fun nextTraceId(action: String): String
}

internal class UuidActionTraceIdProvider : ActionTraceIdProvider {
    override fun nextTraceId(action: String): String {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        return "${action}_$suffix"
    }
}

interface StructuredLogger {
    fun logStart(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    )

    fun logSuccess(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    )

    fun logFailure(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
        throwable: Throwable? = null,
        message: String? = null,
    )
}

internal class CrashlyticsStructuredLogger(
    private val tag: String = "TeamCompassTrace",
    private val crashlyticsProvider: () -> FirebaseCrashlytics? = {
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
    },
) : StructuredLogger {
    override fun logStart(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    ) {
        log(
            level = Level.INFO,
            action = action,
            phase = "start",
            traceId = traceId,
            teamCode = teamCode,
            uid = uid,
            backendAvailable = backendAvailable,
            throwable = null,
            message = null,
        )
    }

    override fun logSuccess(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
    ) {
        log(
            level = Level.INFO,
            action = action,
            phase = "success",
            traceId = traceId,
            teamCode = teamCode,
            uid = uid,
            backendAvailable = backendAvailable,
            throwable = null,
            message = null,
        )
    }

    override fun logFailure(
        action: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
        throwable: Throwable?,
        message: String?,
    ) {
        log(
            level = Level.WARN,
            action = action,
            phase = "failure",
            traceId = traceId,
            teamCode = teamCode,
            uid = uid,
            backendAvailable = backendAvailable,
            throwable = throwable,
            message = message,
        )
    }

    private fun log(
        level: Level,
        action: String,
        phase: String,
        traceId: String,
        teamCode: String?,
        uid: String?,
        backendAvailable: Boolean,
        throwable: Throwable?,
        message: String?,
    ) {
        val safeTeamCode = teamCode.orEmpty()
        val safeUid = uid.orEmpty()
        val baseMessage = buildString {
            append("action=")
            append(action)
            append(", phase=")
            append(phase)
            append(", trace_id=")
            append(traceId)
            append(", team_code=")
            append(safeTeamCode)
            append(", uid=")
            append(safeUid)
            append(", backend_available=")
            append(backendAvailable)
            if (!message.isNullOrBlank()) {
                append(", message=")
                append(message)
            }
        }

        when (level) {
            Level.INFO -> Log.i(tag, baseMessage)
            Level.WARN -> {
                if (throwable != null) Log.w(tag, baseMessage, throwable) else Log.w(tag, baseMessage)
            }
        }

        runCatching {
            val crashlytics = crashlyticsProvider() ?: return@runCatching
            crashlytics.setCustomKey("team_code", safeTeamCode)
            crashlytics.setCustomKey("uid", safeUid)
            crashlytics.setCustomKey("backend_available", backendAvailable)
            crashlytics.setCustomKey("trace_id", traceId)
            crashlytics.log(baseMessage)
        }.onFailure { err ->
            Log.w(tag, "Failed to send structured log to Crashlytics", err)
        }
    }

    private enum class Level {
        INFO,
        WARN,
    }
}
