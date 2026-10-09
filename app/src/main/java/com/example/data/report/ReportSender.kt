package com.example.data.report

interface ReportSender {
    suspend fun sendReport(item: ReportItem, appVersion: String): ReportResult
}
