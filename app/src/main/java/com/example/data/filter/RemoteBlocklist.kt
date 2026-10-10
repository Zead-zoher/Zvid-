package com.example.data.filter

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class RemoteBlocklistDto(
    @Json(name = "version") val version: Int = 1,
    @Json(name = "movies") val movies: List<Int> = emptyList(),
    @Json(name = "tv") val tv: List<Int> = emptyList(),
    @Json(name = "companies") val companies: List<Int> = emptyList(),
    @Json(name = "people") val people: List<Int> = emptyList(),
    @Json(name = "keywords") val keywords: List<Int> = emptyList()
) {
    fun toBlocklist(): Blocklist = Blocklist(
        movies = movies.toSet(),
        tv = tv.toSet(),
        companies = companies.toSet(),
        people = people.toSet(),
        keywords = keywords.toSet()
    )
}

class RemoteBlocklist(private val context: Context) {

    companion object {
        const val BLOCKLIST_URL = "https://raw.githubusercontent.com/Zead-zoher/Zvid-/main/blocklist.json"
        private const val PREFS_NAME = "remote_blocklist_prefs"
        private const val KEY_LAST_FETCH = "last_blocklist_fetch_ms"
        private const val CACHE_FILE_NAME = "cached_blocklist.json"
        private val SIX_HOURS_MS = TimeUnit.HOURS.toMillis(6)
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(RemoteBlocklistDto::class.java)

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        var parsedBlocklist: Blocklist? = null

        // 1. Fresh download from GitHub repository (Always attempt first to ensure latest updates)
        val remoteJson = downloadRemoteJson()
        if (!remoteJson.isNullOrBlank()) {
            val parsed = parseJson(remoteJson)
            if (parsed != null) {
                saveToDiskCache(remoteJson)
                prefs.edit().putLong(KEY_LAST_FETCH, now).apply()
                parsedBlocklist = parsed.toBlocklist()
            }
        }

        // 2. If remote download failed (e.g. offline), try disk cache
        if (parsedBlocklist == null) {
            val cachedJson = loadFromDiskCache()
            if (!cachedJson.isNullOrBlank()) {
                parsedBlocklist = parseJson(cachedJson)?.toBlocklist()
            }
        }

        // 3. If disk cache absent or invalid, try bundled assets
        if (parsedBlocklist == null) {
            val assetJson = loadFromAssets()
            if (!assetJson.isNullOrBlank()) {
                parsedBlocklist = parseJson(assetJson)?.toBlocklist()
            }
        }

        // 4. Update ContentFilter with final blocklist
        val finalBlocklist = parsedBlocklist ?: Blocklist()
        ContentFilter.updateBlocklist(finalBlocklist)
    }

    private fun downloadRemoteJson(): String? {
        return try {
            val request = Request.Builder()
                .url(BLOCKLIST_URL)
                .header("Accept", "application/json")
                .header("Cache-Control", "no-cache")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun saveToDiskCache(json: String) {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(json)
        } catch (_: Exception) {}
    }

    private fun loadFromDiskCache(): String? {
        return try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (file.exists() && file.isFile) {
                file.readText()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun loadFromAssets(): String? {
        return try {
            context.assets.open("blocklist.json").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseJson(json: String): RemoteBlocklistDto? {
        return try {
            adapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }
}
