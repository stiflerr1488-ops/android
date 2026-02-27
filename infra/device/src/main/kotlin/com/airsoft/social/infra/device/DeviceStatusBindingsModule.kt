package com.airsoft.social.infra.device

import android.app.Application
import com.airsoft.social.core.data.DeviceDiagnosticsPort
import com.airsoft.social.core.data.PermissionStatusPort
import com.airsoft.social.core.data.SecurityStatusPort
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceStatusBindingsModule {

    @Provides
    @Singleton
    fun providePermissionStatusPort(app: Application): PermissionStatusPort =
        AndroidPermissionStatusPort(app)

    @Provides
    @Singleton
    fun provideSecurityStatusPort(app: Application): SecurityStatusPort =
        AndroidSecurityStatusPort(app)

    @Provides
    @Singleton
    fun provideDeviceDiagnosticsPort(app: Application): DeviceDiagnosticsPort =
        AndroidDeviceDiagnosticsPort(app)
}
