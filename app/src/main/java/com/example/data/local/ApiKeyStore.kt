package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiKeyStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    fun getApiKey(): String {
        val stored = prefs.getString(KEY_TMDB_API_KEY, "") ?: ""
        return if (stored.isNotBlank()) stored else DEFAULT_DEMO_KEY
    }

    fun hasCustomKey(): Boolean {
        val stored = prefs.getString(KEY_TMDB_API_KEY, "") ?: ""
        return stored.isNotBlank()
    }

    fun saveApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        prefs.edit().putString(KEY_TMDB_API_KEY, trimmed).apply()
        _apiKeyFlow.value = if (trimmed.isNotBlank()) trimmed else DEFAULT_DEMO_KEY
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_TMDB_API_KEY).apply()
        _apiKeyFlow.value = DEFAULT_DEMO_KEY
    }

    fun setDemoKey() {
        // Sets or resets to working demo key
        saveApiKey(DEFAULT_DEMO_KEY)
    }

    companion object {
        private const val PREFS_NAME = "zvid_settings"
        private const val KEY_TMDB_API_KEY = "tmdb_api_key"
        // Standard public educational/demo TMDB key to provide instant out-of-the-box readiness
        const val DEFAULT_DEMO_KEY = "b3c88bb71239274da7eef41b6352c3c6"
    }
}
