package com.example.data.filter

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class RemoteBlocklist(private val context: Context) {

    companion object {
        const val BLOCKLIST_URL = "https://raw.githubusercontent.com/Zead-zoher/Zvid-/main/blocklist.json"
        const val FALLBACK_BLOCKLIST_URL = "https://github.com/Zead-zoher/Zvid-/raw/main/blocklist.json"
        private const val PREFS_NAME = "remote_blocklist_prefs"
        private const val KEY_LAST_FETCH = "last_blocklist_fetch_ms"
        private const val CACHE_FILE_NAME = "cached_blocklist.json"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        var parsedBlocklist: Blocklist? = null

        // 1. Fresh download from GitHub repository (Always attempt first to ensure latest updates)
        val remoteJson = downloadRemoteJson(BLOCKLIST_URL) ?: downloadRemoteJson(FALLBACK_BLOCKLIST_URL)
        if (!remoteJson.isNullOrBlank()) {
            val parsed = parseJson(remoteJson)
            if (parsed != null) {
                saveToDiskCache(remoteJson)
                prefs.edit().putLong(KEY_LAST_FETCH, now).apply()
                parsedBlocklist = parsed
            }
        }

        // 2. If remote download failed (e.g. offline), try disk cache
        if (parsedBlocklist == null) {
            val cachedJson = loadFromDiskCache()
            if (!cachedJson.isNullOrBlank()) {
                parsedBlocklist = parseJson(cachedJson)
            }
        }

        // 3. If disk cache absent or invalid, try bundled assets
        if (parsedBlocklist == null) {
            val assetJson = loadFromAssets()
            if (!assetJson.isNullOrBlank()) {
                parsedBlocklist = parseJson(assetJson)
            }
        }

        // 4. Update ContentFilter with final blocklist
        val finalBlocklist = parsedBlocklist ?: Blocklist()
        ContentFilter.updateBlocklist(finalBlocklist)
    }

    private fun downloadRemoteJson(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
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

    private fun parseJson(json: String): Blocklist? {
        return try {
            val root = JSONObject(json)
            val movies = mutableSetOf<Int>()
            root.optJSONArray("movies")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optInt(i, -1)
                    if (id > 0) movies.add(id)
                }
            }

            val tv = mutableSetOf<Int>()
            root.optJSONArray("tv")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optInt(i, -1)
                    if (id > 0) tv.add(id)
                }
            }

            val companies = mutableSetOf<Int>()
            root.optJSONArray("companies")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optInt(i, -1)
                    if (id > 0) companies.add(id)
                }
            }

            val people = mutableSetOf<Int>()
            root.optJSONArray("people")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optInt(i, -1)
                    if (id > 0) people.add(id)
                }
            }

            val keywords = mutableSetOf<Int>()
            val words = mutableSetOf<String>()

            root.optJSONArray("keywords")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.opt(i)
                    when (item) {
                        is Number -> {
                            val id = item.toInt()
                            if (id > 0) keywords.add(id)
                        }
                        is String -> {
                            val intVal = item.toIntOrNull()
                            if (intVal != null && intVal > 0) {
                                keywords.add(intVal)
                            } else if (item.isNotBlank()) {
                                words.add(item.trim())
                            }
                        }
                    }
                }
            }

            // Also check for optional explicit blocked_words or words list
            val wordsArr = root.optJSONArray("blocked_words") ?: root.optJSONArray("words")
            wordsArr?.let { arr ->
                for (i in 0 until arr.length()) {
                    val str = arr.optString(i)
                    if (!str.isNullOrBlank()) {
                        words.add(str.trim())
                    }
                }
            }

            Blocklist(
                movies = movies,
                tv = tv,
                companies = companies,
                people = people,
                keywords = keywords,
                words = words
            )
        } catch (_: Exception) {
            null
        }
    }
}

