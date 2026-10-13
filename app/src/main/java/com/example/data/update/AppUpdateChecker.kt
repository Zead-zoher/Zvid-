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
     * Fetches version.json from GitHub and checks for update.
     * Returns a Pair:
     * - first: Boolean indicating whether network fetch succeeded (true if version.json was downloaded)
     * - second: AppUpdateInfo? non-null ONLY if remote semantic version is strictly greater than current version (>).
     */
    suspend fun fetchUpdateFile(): Pair<Boolean, AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val primaryUrl = "$VERSION_CHECK_URL?t=$timestamp"
            val fallbackUrl = "$FALLBACK_VERSION_URL?t=$timestamp"

            val body = fetchUrl(primaryUrl) ?: fetchUrl(fallbackUrl)
            if (body.isNullOrBlank()) {
                return@withContext Pair(false, null)
            }

            val updateInfo = adapter.fromJson(body) ?: return@withContext Pair(false, null)

            val remoteVersionStr = updateInfo.version?.takeIf { it.isNotBlank() }
                ?: updateInfo.versionName?.takeIf { it.isNotBlank() }
                ?: updateInfo.effectiveVersionString
            val currentVersionStr = BuildConfig.VERSION_NAME

            // User requirement: Compare ONLY the version string (e.g. 1.1.0), NOT version_code.
            // If remote <= current, do not show update (return null).
            // If remote > current, return updateInfo.
            val isNewer = isNewerVersion(remoteVersionStr, currentVersionStr)
            val infoToShow = if (isNewer) updateInfo else null

            Pair(true, infoToShow)
        } catch (_: Exception) {
            Pair(false, null)
        }
    }

    suspend fun checkForUpdate(): AppUpdateInfo? {
        val result = fetchUpdateFile()
        return result.second
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
     * Compares remote and current versions strictly using semantic versioning.
     * Returns true ONLY if remote is strictly greater than current (>).
     * If remote <= current (same or older), returns false.
     */
    fun isNewerVersion(
        remoteVersionStr: String,
        currentVersionStr: String
    ): Boolean {
        if (remoteVersionStr.isNotBlank()) {
            val cmp = compareSemVer(remoteVersionStr, currentVersionStr)
            if (cmp != null) {
                return cmp > 0
            }
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
