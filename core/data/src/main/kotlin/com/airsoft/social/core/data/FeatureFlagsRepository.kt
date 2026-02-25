package com.airsoft.social.core.data

import com.airsoft.social.core.datastore.LocalFeatureFlags
import com.airsoft.social.core.datastore.LocalFeatureFlagsDataSource
import kotlinx.coroutines.flow.Flow

interface FeatureFlagsRepository {
    fun observeFlags(): Flow<LocalFeatureFlags>
    suspend fun setUseFirebaseAdapters(enabled: Boolean)
}

class DefaultFeatureFlagsRepository(
    private val localDataSource: LocalFeatureFlagsDataSource,
) : FeatureFlagsRepository {
    override fun observeFlags(): Flow<LocalFeatureFlags> = localDataSource.observeFlags()

    override suspend fun setUseFirebaseAdapters(enabled: Boolean) {
        localDataSource.setUseFirebaseAdapters(enabled)
    }
}

