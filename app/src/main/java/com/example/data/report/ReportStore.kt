package com.example.data.report

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class ReportStore(context: Context) {

    companion object {
        private const val PREFS_NAME = "zvid_report_store"
        private const val KEY_REPORTED_IDS = "reported_item_ids"
        private const val KEY_REPORTED_PEOPLE = "reported_people_ids"
        private const val KEY_REPORTED_COMPANIES = "reported_companies_ids"
        private const val KEY_REPORT_TIMESTAMPS = "report_timestamps_csv"
        private const val KEY_CUSTOM_BOT_TOKEN = "custom_telegram_bot_token"
        private const val KEY_CUSTOM_CHAT_ID = "custom_telegram_chat_id"
        private val ONE_HOUR_MS = TimeUnit.HOURS.toMillis(1)
        private const val MAX_REPORTS_PER_HOUR = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _reportedIdsFlow = MutableStateFlow<Set<Int>>(loadReportedIds())
    val reportedIdsFlow: StateFlow<Set<Int>> = _reportedIdsFlow.asStateFlow()

    private fun loadReportedIds(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_REPORTED_IDS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun getReportedPeople(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_REPORTED_PEOPLE, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun getReportedCompanies(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_REPORTED_COMPANIES, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun isReported(id: Int): Boolean {
        return _reportedIdsFlow.value.contains(id)
    }

    fun isReportedPerson(id: Int): Boolean {
        return getReportedPeople().contains(id) || isReported(id)
    }

    fun isReportedCompany(id: Int): Boolean {
        return getReportedCompanies().contains(id) || isReported(id)
    }

    fun getTelegramBotToken(): String {
        val custom = prefs.getString(KEY_CUSTOM_BOT_TOKEN, null)?.trim()
        if (!custom.isNullOrBlank()) return custom
        return com.example.BuildConfig.TELEGRAM_BOT_TOKEN.trim()
    }

    fun getTelegramChatId(): String {
        val custom = prefs.getString(KEY_CUSTOM_CHAT_ID, null)?.trim()
        if (!custom.isNullOrBlank()) return custom
        return com.example.BuildConfig.TELEGRAM_CHAT_ID.trim()
    }

    fun isTelegramConfigured(): Boolean {
        return getTelegramBotToken().isNotBlank() && getTelegramChatId().isNotBlank()
    }

    fun saveTelegramConfig(botToken: String, chatId: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_BOT_TOKEN, botToken.trim())
            .putString(KEY_CUSTOM_CHAT_ID, chatId.trim())
            .apply()
    }

    fun canSubmitReport(id: Int): Pair<Boolean, String?> {
        if (isReported(id)) {
            return Pair(false, "You have already reported this item.")
        }

        val now = System.currentTimeMillis()
        val timestamps = getRecentTimestamps(now)

        if (timestamps.size >= MAX_REPORTS_PER_HOUR) {
            return Pair(false, "Report limit reached (maximum 5 reports per hour). Please try again later.")
        }

        return Pair(true, null)
    }

    fun recordSuccessfulReport(item: ReportItem) {
        recordSuccessfulReport(item.id)
        when (item.type) {
            ReportTargetType.Person -> {
                val current = getReportedPeople().toMutableSet()
                current.add(item.id)
                prefs.edit().putStringSet(KEY_REPORTED_PEOPLE, current.map { it.toString() }.toSet()).apply()
            }
            ReportTargetType.Company -> {
                val current = getReportedCompanies().toMutableSet()
                current.add(item.id)
                prefs.edit().putStringSet(KEY_REPORTED_COMPANIES, current.map { it.toString() }.toSet()).apply()
            }
            else -> {}
        }
    }

    fun recordSuccessfulReport(id: Int) {
        val now = System.currentTimeMillis()
        val currentIds = _reportedIdsFlow.value.toMutableSet()
        currentIds.add(id)
        _reportedIdsFlow.value = currentIds

        prefs.edit().putStringSet(KEY_REPORTED_IDS, currentIds.map { it.toString() }.toSet()).apply()

        val recentTimestamps = getRecentTimestamps(now).toMutableList()
        recentTimestamps.add(now)
        val csv = recentTimestamps.joinToString(",")
        prefs.edit().putString(KEY_REPORT_TIMESTAMPS, csv).apply()
    }

    private fun getRecentTimestamps(now: Long): List<Long> {
        val raw = prefs.getString(KEY_REPORT_TIMESTAMPS, "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it <= ONE_HOUR_MS }
    }
}
