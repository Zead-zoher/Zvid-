package com.example.data.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppUpdateInfo(
    @Json(name = "version_code") val versionCode: Int = 1,
    @Json(name = "version_name") val versionName: String = "1.0",
    @Json(name = "download_url") val downloadUrl: String = "https://github.com/Zead-zoher/Zvid-/releases/latest"
)
