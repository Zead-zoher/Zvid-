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
        /**
         * CURRENT_APP_VERSION:
         * مكان الإصدار الحالي في الكود (1.1.0) لمقارنته مع ملف version.json الخارجي.
         * قم بتعديل هذا المتغير عند إطلاق أي إصدار جديد.
         */
        const val CURRENT_APP_VERSION = "1.1.0"

        const val VERSION_CHECK_URL = "https://raw.githubusercontent.com/Zead-zoher/Zvid-/main/version.json"
        const val CDN_VERSION_URL = "https://cdn.jsdelivr.net/gh/Zead-zoher/Zvid-@main/version.json"
        const val GITHUB_RAW_URL = "https://github.com/Zead-zoher/Zvid-/raw/main/version.json"
        const val GITHUB_BLOB_RAW_URL = "https://github.com/Zead-zoher/Zvid-/blob/main/version.json?raw=true"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AppUpdateInfo::class.java)

    /**
     * Fetches version.json live from remote sources on every startup / check.
     * No version.json file is stored locally in the app files.
     * Returns a Pair:
     * - first: Boolean indicating whether network fetch succeeded
     * - second: AppUpdateInfo? non-null ONLY if remote semantic version is strictly greater than CURRENT_APP_VERSION (1.1.0).
     */
    suspend fun fetchUpdateFile(): Pair<Boolean, AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val urls = listOf(
                "$VERSION_CHECK_URL?t=$timestamp",
                "$CDN_VERSION_URL?t=$timestamp",
                "$GITHUB_RAW_URL?t=$timestamp",
                "$GITHUB_BLOB_RAW_URL&t=$timestamp"
            )

            var downloadedBody: String? = null
            for (url in urls) {
                val body = fetchUrl(url)
                if (!body.isNullOrBlank()) {
                    downloadedBody = body
                    break
                }
            }

            if (downloadedBody.isNullOrBlank()) {
                return@withContext Pair(false, null)
            }

            val updateInfo = adapter.fromJson(downloadedBody) ?: return@withContext Pair(false, null)

            val remoteVersionStr = updateInfo.version?.takeIf { it.isNotBlank() }
                ?: updateInfo.versionName?.takeIf { it.isNotBlank() }
                ?: updateInfo.effectiveVersionString
            val currentVersionStr = CURRENT_APP_VERSION

            // Compare ONLY the version string (e.g. 1.1.0), NOT version_code.
            // If remote <= current (e.g. remote is "1.1.0" and current is "1.1.0"), do not show update.
            // If remote > current (e.g. remote is "1.1.1"), return updateInfo.
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
                .header("Accept", "*/*")
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
