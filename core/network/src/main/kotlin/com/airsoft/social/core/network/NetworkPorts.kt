package com.airsoft.social.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface AuthTokenProvider {
    suspend fun getAccessToken(): String?
}

interface SocialApiFactory {
    fun createRetrofit(baseUrl: String): Retrofit
}

interface NetworkStatusMonitor {
    fun isOnline(): kotlinx.coroutines.flow.Flow<Boolean>
}

class StaticAuthTokenProvider(
    private val token: String? = null,
) : AuthTokenProvider {
    override suspend fun getAccessToken(): String? = token
}

class RetrofitSocialApiFactory(
    private val okHttpClient: OkHttpClient,
) : SocialApiFactory {
    override fun createRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
}

object NoopNetworkStatusMonitor : NetworkStatusMonitor {
    override fun isOnline(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)
}

