package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.report.ReportItem
import com.example.data.report.ReportResult
import com.example.data.report.ReportSender
import com.example.data.report.ReportStore
import com.example.data.report.ReportTargetType
import com.example.data.report.TelegramReportSender
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    val reportStore = ReportStore(application)
    private val reportSender: ReportSender = TelegramReportSender(reportStore)

    private val _activeReportItem = MutableStateFlow<ReportItem?>(null)
    val activeReportItem: StateFlow<ReportItem?> = _activeReportItem.asStateFlow()

    private val _reportReason = MutableStateFlow("")
    val reportReason: StateFlow<String> = _reportReason.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun openReportDialog(type: ReportTargetType, id: Int, title: String) {
        val (canReport, abuseReason) = reportStore.canSubmitReport(id)
        if (!canReport) {
            viewModelScope.launch {
                _snackbarEvent.emit(abuseReason ?: "Cannot report this item")
            }
            return
        }
        _reportReason.value = ""
        _activeReportItem.value = ReportItem(type = type, id = id, title = title)
    }

    fun closeReportDialog() {
        _activeReportItem.value = null
        _reportReason.value = ""
        _isSending.value = false
    }

    fun onReasonChanged(text: String) {
        if (text.length <= 300) {
            _reportReason.value = text
        }
    }

    fun submitReport(onReportSubmittedLocally: (item: ReportItem) -> Unit = {}) {
        val currentItem = _activeReportItem.value ?: return
        val itemToSend = currentItem.copy(reason = _reportReason.value)

        viewModelScope.launch {
            _isSending.value = true
            val appVersion = BuildConfig.VERSION_NAME
            when (val result = reportSender.sendReport(itemToSend, appVersion)) {
                is ReportResult.Success -> {
                    closeReportDialog()
                    onReportSubmittedLocally(itemToSend)
                    _snackbarEvent.emit("Thanks, report sent")
                }
                is ReportResult.Error -> {
                    _isSending.value = false
                    _snackbarEvent.emit(result.message)
                }
                is ReportResult.RateLimited -> {
                    closeReportDialog()
                    _snackbarEvent.emit(result.message)
                }
                is ReportResult.AlreadyReported -> {
                    closeReportDialog()
                    _snackbarEvent.emit(result.message)
                }
            }
        }
    }

    fun testTelegramConfig(token: String, chatId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val okHttpClient = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val endpoint = "https://api.telegram.org/bot${token.trim()}/sendMessage"
                val form = okhttp3.FormBody.Builder()
                    .add("chat_id", chatId.trim())
                    .add("text", "✅ Zvid: Telegram report integration is successfully connected and working!")
                    .build()
                val req = okhttp3.Request.Builder().url(endpoint).post(form).build()
                val resp = okHttpClient.newCall(req).execute()
                resp.use {
                    if (it.isSuccessful) {
                        onResult(true, "Test message delivered to Telegram successfully!")
                    } else {
                        val body = it.body?.string()?.take(150) ?: ""
                        onResult(false, "Failed (HTTP ${it.code}): $body")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
