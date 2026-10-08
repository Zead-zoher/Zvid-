package com.example.data.remote

import com.example.data.local.ApiKeyStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object TmdbApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private var currentApi: TmdbApi? = null
    private var lastApiKey: String = ""

    fun getApi(apiKeyStore: ApiKeyStore): TmdbApi {
        val currentKey = apiKeyStore.getApiKey()
        if (currentApi != null && lastApiKey == currentKey) {
            return currentApi!!
        }

        lastApiKey = currentKey

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val originalHttpUrl = original.url

            val activeKey = apiKeyStore.getApiKey()

            val requestBuilder = original.newBuilder()

            // Support both v3 API key (32-char hex string) and v4 Bearer tokens (JWTs)
            if (activeKey.startsWith("eyJ")) {
                requestBuilder.header("Authorization", "Bearer $activeKey")
                val url = originalHttpUrl.newBuilder().build()
                requestBuilder.url(url)
            } else {
                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("api_key", activeKey)
                    .build()
                requestBuilder.url(url)
            }

            requestBuilder.header("Accept", "application/json")
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(TmdbApi::class.java)
        currentApi = api
        return api
    }
}
