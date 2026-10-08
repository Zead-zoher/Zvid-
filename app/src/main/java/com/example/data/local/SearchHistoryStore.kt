package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class SearchHistoryStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMovieHistory(): List<String> {
        val json = prefs.getString(KEY_MOVIE_HISTORY, null) ?: return emptyList()
        return parseJsonList(json)
    }

    fun saveMovieHistory(list: List<String>) {
        val array = JSONArray()
        list.take(20).forEach { array.put(it) }
        prefs.edit().putString(KEY_MOVIE_HISTORY, array.toString()).apply()
    }

    fun getTvHistory(): List<String> {
        val json = prefs.getString(KEY_TV_HISTORY, null) ?: return emptyList()
        return parseJsonList(json)
    }

    fun saveTvHistory(list: List<String>) {
        val array = JSONArray()
        list.take(20).forEach { array.put(it) }
        prefs.edit().putString(KEY_TV_HISTORY, array.toString()).apply()
    }

    fun getUniverseHistory(): List<String> {
        val json = prefs.getString(KEY_UNIVERSE_HISTORY, null) ?: return emptyList()
        return parseJsonList(json)
    }

    fun saveUniverseHistory(list: List<String>) {
        val array = JSONArray()
        list.take(20).forEach { array.put(it) }
        prefs.edit().putString(KEY_UNIVERSE_HISTORY, array.toString()).apply()
    }

    private fun parseJsonList(json: String): List<String> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.getString(i).trim()
                if (item.isNotEmpty()) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_NAME = "zvid_search_history_store"
        private const val KEY_MOVIE_HISTORY = "movie_search_history"
        private const val KEY_TV_HISTORY = "tv_search_history"
        private const val KEY_UNIVERSE_HISTORY = "universe_search_history"
    }
}
