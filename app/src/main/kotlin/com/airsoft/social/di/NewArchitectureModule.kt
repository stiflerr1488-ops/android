package com.airsoft.social.di

import android.app.Application
import com.airsoft.social.app.LegacyTacticalOverviewBridge
import com.airsoft.social.core.auth.AuthGateway
import com.airsoft.social.core.auth.InMemoryAuthGateway
import com.airsoft.social.core.data.BootstrapRepository
import com.airsoft.social.core.data.DefaultFeatureFlagsRepository
import com.airsoft.social.core.data.DefaultOnboardingRepository
import com.airsoft.social.core.data.DefaultSessionRepository
import com.airsoft.social.core.data.FakeBootstrapRepository
import com.airsoft.social.core.data.FeatureFlagsRepository
import com.airsoft.social.core.data.OnboardingRepository
import com.airsoft.social.core.data.SessionRepository
import com.airsoft.social.core.database.AirsoftDatabase
import com.airsoft.social.core.database.AppMetaDao
import com.airsoft.social.core.datastore.LocalFeatureFlagsDataSource
import com.airsoft.social.core.datastore.OnboardingLocalDataSource
import com.airsoft.social.core.datastore.PreferencesLocalFeatureFlagsDataSource
import com.airsoft.social.core.datastore.PreferencesOnboardingLocalDataSource
import com.airsoft.social.core.datastore.PreferencesSessionLocalDataSource
import com.airsoft.social.core.datastore.SessionLocalDataSource
import com.airsoft.social.core.network.AuthTokenProvider
import com.airsoft.social.core.network.NetworkStatusMonitor
import com.airsoft.social.core.network.NoopNetworkStatusMonitor
import com.airsoft.social.core.network.RetrofitSocialApiFactory
import com.airsoft.social.core.network.SocialApiFactory
import com.airsoft.social.core.network.StaticAuthTokenProvider
import com.airsoft.social.core.network.buildDefaultOkHttpClient
import com.airsoft.social.core.realtime.NoopRealtimeTacticalGateway
import com.airsoft.social.core.realtime.RealtimeTacticalGateway
import com.airsoft.social.core.telemetry.CrashReporter
import com.airsoft.social.core.telemetry.NoopCrashReporter
import com.airsoft.social.core.telemetry.NoopTelemetryReporter
import com.airsoft.social.core.telemetry.TelemetryReporter
import com.airsoft.social.core.tactical.TacticalOverviewPort
import com.airsoft.social.infra.firebase.FirebaseAuthGatewayAdapter
import com.airsoft.social.infra.firebase.FirebaseRealtimeTacticalGatewayAdapter
import com.airsoft.social.infra.firebase.FirebaseTelemetryAdapter
import com.example.teamcompass.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object NewArchitectureModule {
    @Provides
    @Singleton
    fun provideSessionLocalDataSource(app: Application): SessionLocalDataSource =
        PreferencesSessionLocalDataSource(app)

    @Provides
    @Singleton
    fun provideOnboardingLocalDataSource(app: Application): OnboardingLocalDataSource =
        PreferencesOnboardingLocalDataSource(app)

    @Provides
    @Singleton
    fun provideLocalFeatureFlagsDataSource(app: Application): LocalFeatureFlagsDataSource =
        PreferencesLocalFeatureFlagsDataSource(app)

    @Provides
    @Singleton
    fun provideAuthGateway(): AuthGateway =
        if (BuildConfig.NEW_APP_USE_FIREBASE_ADAPTERS) {
            FirebaseAuthGatewayAdapter()
        } else {
            InMemoryAuthGateway()
        }

    @Provides
    @Singleton
    fun provideTelemetryReporter(app: Application): TelemetryReporter =
        if (BuildConfig.NEW_APP_USE_FIREBASE_ADAPTERS) {
            FirebaseTelemetryAdapter(app)
        } else {
            NoopTelemetryReporter()
        }

    @Provides
    @Singleton
    fun provideCrashReporter(app: Application): CrashReporter =
        if (BuildConfig.NEW_APP_USE_FIREBASE_ADAPTERS) {
            FirebaseTelemetryAdapter(app)
        } else {
            NoopCrashReporter()
        }

    @Provides
    @Singleton
    fun provideRealtimeTacticalGateway(): RealtimeTacticalGateway =
        if (BuildConfig.NEW_APP_USE_FIREBASE_ADAPTERS) {
            FirebaseRealtimeTacticalGatewayAdapter()
        } else {
            NoopRealtimeTacticalGateway
        }

    @Provides
    @Singleton
    fun provideLegacyTacticalOverviewBridge(): LegacyTacticalOverviewBridge = LegacyTacticalOverviewBridge()

    @Provides
    @Singleton
    fun provideTacticalOverviewPort(
        bridge: LegacyTacticalOverviewBridge,
    ): TacticalOverviewPort = bridge

    @Provides
    @Singleton
    fun provideAuthTokenProvider(): AuthTokenProvider = StaticAuthTokenProvider()

    @Provides
    @Singleton
    fun provideNetworkStatusMonitor(): NetworkStatusMonitor = NoopNetworkStatusMonitor

    @Provides
    @Singleton
    fun provideSocialApiFactory(): SocialApiFactory =
        RetrofitSocialApiFactory(
            okHttpClient = buildDefaultOkHttpClient(enableLogging = BuildConfig.DEBUG),
        )

    @Provides
    @Singleton
    fun provideAirsoftDatabase(app: Application): AirsoftDatabase = AirsoftDatabase.build(app)

    @Provides
    fun provideAppMetaDao(database: AirsoftDatabase): AppMetaDao = database.appMetaDao()

    @Provides
    @Singleton
    fun provideSessionRepository(
        authGateway: AuthGateway,
        sessionLocalDataSource: SessionLocalDataSource,
        telemetryReporter: TelemetryReporter,
    ): SessionRepository = DefaultSessionRepository(
        authGateway = authGateway,
        localDataSource = sessionLocalDataSource,
        telemetryReporter = telemetryReporter,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        localDataSource: OnboardingLocalDataSource,
    ): OnboardingRepository = DefaultOnboardingRepository(localDataSource)

    @Provides
    @Singleton
    fun provideBootstrapRepository(): BootstrapRepository = FakeBootstrapRepository()

    @Provides
    @Singleton
    fun provideFeatureFlagsRepository(
        localDataSource: LocalFeatureFlagsDataSource,
    ): FeatureFlagsRepository = DefaultFeatureFlagsRepository(localDataSource)
}
