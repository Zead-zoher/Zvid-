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

    fun submitReport(onReportSubmittedLocally: (id: Int) -> Unit = {}) {
        val currentItem = _activeReportItem.value ?: return
        val itemToSend = currentItem.copy(reason = _reportReason.value)

        viewModelScope.launch {
            _isSending.value = true
            val appVersion = BuildConfig.VERSION_NAME
            when (val result = reportSender.sendReport(itemToSend, appVersion)) {
                is ReportResult.Success -> {
                    closeReportDialog()
                    onReportSubmittedLocally(itemToSend.id)
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
}
