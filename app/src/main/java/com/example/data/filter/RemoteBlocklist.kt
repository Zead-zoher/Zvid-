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
        const val CDN_BLOCKLIST_URL = "https://cdn.jsdelivr.net/gh/Zead-zoher/Zvid-@main/blocklist.json"
        const val GITHUB_RAW_URL = "https://github.com/Zead-zoher/Zvid-/raw/main/blocklist.json"
        const val GITHUB_BLOB_RAW_URL = "https://github.com/Zead-zoher/Zvid-/blob/main/blocklist.json?raw=true"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Downloads the blocklist live from remote sources on every startup / refresh.
     * No blocklist is stored inside the app files or internal disk cache.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        // Clean up any old cached file so no blocklist exists locally in app storage
        try {
            val oldCache = File(context.filesDir, "cached_blocklist.json")
            if (oldCache.exists()) {
                oldCache.delete()
            }
        } catch (_: Exception) {}

        var parsedBlocklist: Blocklist? = null

        val timestamp = System.currentTimeMillis()
        val urls = listOf(
            "$BLOCKLIST_URL?t=$timestamp",
            "$CDN_BLOCKLIST_URL?t=$timestamp",
            "$GITHUB_RAW_URL?t=$timestamp",
            "$GITHUB_BLOB_RAW_URL&t=$timestamp"
        )

        for (url in urls) {
            val remoteJson = downloadRemoteJson(url)
            if (!remoteJson.isNullOrBlank()) {
                val parsed = parseJson(remoteJson)
                if (parsed != null) {
                    parsedBlocklist = parsed
                    break
                }
            }
        }

        if (parsedBlocklist != null) {
            ContentFilter.updateBlocklist(parsedBlocklist)
            true
        } else {
            false
        }
    }

    private fun downloadRemoteJson(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
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

            // 1. Process "keywords" array (handles both numeric IDs and text words)
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

            // 2. Process "words" array
            root.optJSONArray("words")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val str = arr.optString(i)
                    if (!str.isNullOrBlank()) {
                        words.add(str.trim())
                    }
                }
            }

            // 3. Process "blocked_words" array
            root.optJSONArray("blocked_words")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val str = arr.optString(i)
                    if (!str.isNullOrBlank()) {
                        words.add(str.trim())
                    }
                }
            }

            // 4. Process "blocked_keywords" array
            root.optJSONArray("blocked_keywords")?.let { arr ->
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
