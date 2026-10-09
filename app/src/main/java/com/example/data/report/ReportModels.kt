package com.example.data.report

sealed class ReportTargetType(val key: String, val tmdbPath: String) {
    object Movie : ReportTargetType("movie", "movie")
    object Tv : ReportTargetType("tv", "tv")
    object Company : ReportTargetType("company", "company")
    object Person : ReportTargetType("person", "person")
}

data class ReportItem(
    val type: ReportTargetType,
    val id: Int,
    val title: String,
    val reason: String = ""
)

sealed class ReportResult {
    object Success : ReportResult()
    data class Error(val message: String) : ReportResult()
    data class RateLimited(val message: String) : ReportResult()
    data class AlreadyReported(val message: String) : ReportResult()
}
