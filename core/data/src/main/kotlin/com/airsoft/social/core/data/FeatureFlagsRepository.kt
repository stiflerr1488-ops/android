package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.LocalFeatureFlags
import com.airsoft.social.core.datastore.LocalFeatureFlagsDataSource
import kotlinx.coroutines.flow.Flow

interface FeatureFlagsRepository {
    fun observeFlags(): Flow<LocalFeatureFlags>
    suspend fun setUseFirebaseAdapters(enabled: Boolean)
    suspend fun setCoreSocialOnly(enabled: Boolean)
    suspend fun setRealProfileChats(enabled: Boolean)
    suspend fun setRealSocialAll(enabled: Boolean)
}

class DefaultFeatureFlagsRepository(
    private val localDataSource: LocalFeatureFlagsDataSource,
) : FeatureFlagsRepository {
    override fun observeFlags(): Flow<LocalFeatureFlags> = localDataSource.observeFlags()

    override suspend fun setUseFirebaseAdapters(enabled: Boolean) {
        localDataSource.setUseFirebaseAdapters(enabled)
    }

    override suspend fun setCoreSocialOnly(enabled: Boolean) {
        localDataSource.setCoreSocialOnly(enabled)
    }

    override suspend fun setRealProfileChats(enabled: Boolean) {
        localDataSource.setRealProfileChats(enabled)
    }

    override suspend fun setRealSocialAll(enabled: Boolean) {
        localDataSource.setRealSocialAll(enabled)
    }
}

