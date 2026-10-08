package com.example.viewmodel

enum class ContentRegion(
    val id: String,
    val label: String,
    val languageCode: String,
    val originalLanguages: String?
) {
    GLOBAL("global", "Global", "en-US", null),
    ARABIC("arabic", "Arabic", "ar-SA", "ar"),
    HOLLYWOOD("hollywood", "Hollywood", "en-US", "en"),
    ASIAN("asian", "Asian", "en-US", "ko|ja|hi|zh|th"),
    EURO_LATIN("euro_latin", "EU & Lat", "en-US", "es|tr|fr|de|it|pt");

    companion object {
        fun fromId(id: String?): ContentRegion {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GLOBAL
        }
    }
}
