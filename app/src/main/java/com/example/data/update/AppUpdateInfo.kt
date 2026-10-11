package com.example.data.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppUpdateInfo(
    @Json(name = "version") val version: String? = null,
    @Json(name = "version_code") val versionCode: Int? = null,
    @Json(name = "version_name") val versionName: String? = null,
    @Json(name = "download_url") val downloadUrl: String = "https://github.com/Zead-zoher/Zvid-/releases/latest",
    @Json(name = "url") val url: String? = null
) {
    val effectiveVersionString: String
        get() = version?.takeIf { it.isNotBlank() }
            ?: versionName?.takeIf { it.isNotBlank() }
            ?: versionCode?.toString()
            ?: "1.0.0"

    val effectiveDownloadUrl: String
        get() = downloadUrl.takeIf { it.isNotBlank() }
            ?: url?.takeIf { it.isNotBlank() }
            ?: "https://github.com/Zead-zoher/Zvid-/releases/latest"
}
