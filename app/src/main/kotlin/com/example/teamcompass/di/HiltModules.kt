package com.example.teamcompass.di

import android.app.Application
import android.util.Log
import com.example.teamcompass.BuildConfig
import com.example.teamcompass.data.firebase.FirebaseRealtimeBackendClient
import com.example.teamcompass.data.firebase.FirebaseTeamRepository
import com.example.teamcompass.domain.TeamRepository
import com.example.teamcompass.domain.TrackingController
import com.example.teamcompass.p2p.P2PTransportManager
import com.example.teamcompass.p2p.P2PTransportRegistry
import com.example.teamcompass.tracking.TrackingControllerImpl
import com.example.teamcompass.ui.UserPrefs
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModules {
    private const val TAG = "HiltModules"
    private var persistenceConfigured = false

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference {
        val explicitRtdbUrl = BuildConfig.RTDB_URL.trim().takeIf { it.isNotBlank() }
        val googleServicesRtdbUrl = runCatching {
            FirebaseApp.getInstance().options.databaseUrl?.trim().orEmpty()
        }.onFailure { err ->
            Log.w(TAG, "Failed to read Firebase databaseUrl from default app options", err)
        }.getOrNull()?.takeIf { it.isNotBlank() }

        val database = when {
            explicitRtdbUrl != null -> FirebaseDatabase.getInstance(explicitRtdbUrl)
            googleServicesRtdbUrl != null -> FirebaseDatabase.getInstance(googleServicesRtdbUrl)
            else -> FirebaseDatabase.getInstance()
        }

        synchronized(this) {
            if (!persistenceConfigured) {
                runCatching {
                    database.setPersistenceEnabled(true)
                    persistenceConfigured = true
                    Log.i(TAG, "Firebase RTDB offline persistence enabled")
                }.onFailure { err ->
                    Log.w(TAG, "Failed to enable Firebase RTDB persistence", err)
                }
            }
        }

        return database.reference
    }

    @Provides
    @Singleton
    fun provideTeamRepository(reference: DatabaseReference): TeamRepository {
        return FirebaseTeamRepository(FirebaseRealtimeBackendClient(reference))
    }

    @Provides
    @Singleton
    fun provideTrackingControllerImpl(
        app: Application,
        teamRepository: TeamRepository,
        coroutineExceptionHandler: CoroutineExceptionHandler,
    ): TrackingControllerImpl {
        return TrackingControllerImpl(
            app = app,
            repository = teamRepository,
            coroutineExceptionHandler = coroutineExceptionHandler,
        )
    }

    @Provides
    @Singleton
    fun provideTrackingController(controllerImpl: TrackingControllerImpl): TrackingController {
        return controllerImpl
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideUserPrefs(app: Application): UserPrefs = UserPrefs(app)

    @Provides
    @Singleton
    fun provideP2PTransportRegistry(): P2PTransportRegistry {
        if (!BuildConfig.P2P_BRIDGES_ENABLED) {
            Log.i(TAG, "P2P bridge transports are disabled; registry will be empty")
            return P2PTransportRegistry()
        }
        // Defensive default: do not expose stub transports as if they were operational bridges.
        // Real bridge clients must be wired explicitly before enabling runtime P2P bridges.
        Log.w(
            TAG,
            "P2P_BRIDGES_ENABLED is true but concrete bridge clients are not wired; registry will stay empty",
        )
        return P2PTransportRegistry()
    }

    @Provides
    @Singleton
    fun provideP2PTransportManager(registry: P2PTransportRegistry): P2PTransportManager {
        return P2PTransportManager(registry)
    }

    @Provides
    @Singleton
    fun provideGlobalCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Unhandled coroutine exception", throwable)
            runCatching {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            }.onFailure { err ->
                Log.w(TAG, "Failed to report coroutine exception to Crashlytics", err)
            }
        }
    }
}
