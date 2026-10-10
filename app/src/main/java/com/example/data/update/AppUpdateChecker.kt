package com.example.data.update

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class AppUpdateChecker(private val context: Context) {

    companion object {
        const val VERSION_CHECK_URL = "https://raw.githubusercontent.com/Zead-zoher/Zvid-/main/version.json"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AppUpdateInfo::class.java)

    /**
     * Checks if a newer version exists on GitHub.
     * Returns AppUpdateInfo if an update is available, or null if up to date or network error.
     */
    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(VERSION_CHECK_URL)
                .header("Cache-Control", "no-cache")
                .build()

            val response = httpClient.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) return@withContext null
                val body = it.body?.string() ?: return@withContext null
                val updateInfo = adapter.fromJson(body) ?: return@withContext null

                val currentVersionCode = BuildConfig.VERSION_CODE
                // An update is available if GitHub versionCode is strictly greater than current app's versionCode
                if (updateInfo.versionCode > currentVersionCode) {
                    return@withContext updateInfo
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
