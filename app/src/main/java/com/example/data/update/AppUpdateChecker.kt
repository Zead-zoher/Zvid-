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
        const val FALLBACK_VERSION_URL = "https://github.com/Zead-zoher/Zvid-/raw/main/version.json"
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
     * Returns AppUpdateInfo ONLY if remote version is strictly greater than current version (>),
     * or null if up to date, smaller, equal, or upon network failure.
     */
    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val primaryUrl = "$VERSION_CHECK_URL?t=$timestamp"
            val fallbackUrl = "$FALLBACK_VERSION_URL?t=$timestamp"

            val body = fetchUrl(primaryUrl) ?: fetchUrl(fallbackUrl) ?: return@withContext null
            val updateInfo = adapter.fromJson(body) ?: return@withContext null

            val remoteVersionStr = updateInfo.effectiveVersionString
            val currentVersionStr = BuildConfig.VERSION_NAME
            val remoteVersionCode = updateInfo.versionCode
            val currentVersionCode = BuildConfig.VERSION_CODE

            // Strict comparison: show update ONLY if remote > current.
            // If remote <= current, return null (do not show anything).
            if (isNewerVersion(remoteVersionStr, currentVersionStr, remoteVersionCode, currentVersionCode)) {
                return@withContext updateInfo
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchUrl(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Compares remote and current versions.
     * Returns true ONLY if remote is strictly greater than current.
     */
    fun isNewerVersion(
        remoteVersionStr: String,
        currentVersionStr: String,
        remoteVersionCode: Int?,
        currentVersionCode: Int
    ): Boolean {
        if (remoteVersionStr.isNotBlank()) {
            val cmp = compareSemVer(remoteVersionStr, currentVersionStr)
            if (cmp != null) {
                return cmp > 0
            }
        }
        if (remoteVersionCode != null && remoteVersionCode > 0) {
            return remoteVersionCode > currentVersionCode
        }
        return false
    }

    private fun compareSemVer(v1: String, v2: String): Int? {
        return try {
            val clean1 = v1.trim().removePrefix("v").removePrefix("V")
            val clean2 = v2.trim().removePrefix("v").removePrefix("V")
            val parts1 = clean1.split(".").mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }
            val parts2 = clean2.split(".").mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }
            if (parts1.isEmpty() || parts2.isEmpty()) return null
            val maxLen = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLen) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 > p2) return 1
                if (p1 < p2) return -1
            }
            0
        } catch (_: Exception) {
            null
        }
    }
}
