package com.example.data.report

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TelegramReportSender(
    private val reportStore: ReportStore
) : ReportSender {

    // Clean client without any logging interceptor to strictly prevent logging secrets or URLs
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun sendReport(item: ReportItem, appVersion: String): ReportResult = withContext(Dispatchers.IO) {
        // Abuse protection check
        val (canReport, abuseReason) = reportStore.canSubmitReport(item.id)
        if (!canReport) {
            return@withContext if (reportStore.isReported(item.id)) {
                ReportResult.AlreadyReported(abuseReason ?: "Already reported")
            } else {
                ReportResult.RateLimited(abuseReason ?: "Report rate limit reached")
            }
        }

        val token = reportStore.getTelegramBotToken()
        val chatId = reportStore.getTelegramChatId()

        if (token.isBlank() || chatId.isBlank()) {
            return@withContext ReportResult.Error("Reporting is not configured (Bot Token or Chat ID is missing)")
        }

        val reasonText = if (item.reason.trim().isBlank()) "none" else item.reason.trim().take(300)
        val linkUrl = if (item.type == ReportTargetType.SearchKeyword) {
            if (item.id > 0) "https://www.themoviedb.org/keyword/${item.id}"
            else "https://www.themoviedb.org/search/keyword?query=${java.net.URLEncoder.encode(item.title, "UTF-8")}"
        } else {
            "https://www.themoviedb.org/${item.type.tmdbPath}/${item.id}"
        }

        val messageText = buildString {
            appendLine("New report")
            appendLine("Type: ${item.type.key}")
            if (item.type == ReportTargetType.SearchKeyword) {
                appendLine("Search Word: ${item.title}")
                if (item.id > 0) {
                    appendLine("Keyword ID: ${item.id}")
                } else {
                    appendLine("Keyword ID: None / Not found on TMDB")
                }
            } else {
                appendLine("ID: ${item.id}")
                appendLine("Title: ${item.title}")
            }
            appendLine("Link: $linkUrl")
            appendLine("Reason: $reasonText")
            append("App version: $appVersion")
        }

        try {
            val endpoint = "https://api.telegram.org/bot$token/sendMessage"
            val formBody = FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", messageText)
                .build()

            val request = Request.Builder()
                .url(endpoint)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            response.use {
                if (it.isSuccessful) {
                    reportStore.recordSuccessfulReport(item)
                    ReportResult.Success
                } else {
                    val errorDetail = it.body?.string()?.take(150) ?: ""
                    ReportResult.Error("Telegram error (HTTP ${it.code}): ${if (errorDetail.isNotBlank()) errorDetail else "check Bot Token & Chat ID"}")
                }
            }
        } catch (e: Exception) {
            ReportResult.Error("Connection error while sending report: ${e.message ?: "network failure"}")
        }
    }
}
