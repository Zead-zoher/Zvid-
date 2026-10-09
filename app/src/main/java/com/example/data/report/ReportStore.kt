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
        private const val KEY_REPORT_TIMESTAMPS = "report_timestamps_csv"
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

    fun isReported(id: Int): Boolean {
        return _reportedIdsFlow.value.contains(id)
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
