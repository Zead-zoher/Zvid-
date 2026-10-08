package com.example.player

typealias EmbedProvider = StreamEmbedProvider

data class DetectedStreamMedia(
    val mediaId: Int,
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val streamUrl: String,
    val embedUrl: String = "",
    val providerName: String,
    val headers: Map<String, String> = emptyMap(),
    val qualityLabel: String = "1080p FHD HLS",
    val initialPositionSeconds: Long = 0L,
    val totalDurationSeconds: Long = 7200L
)

object EmbedStreamResolver {

    val providers: List<StreamEmbedProvider> get() = ProvidersConfig.standardProviders

    fun getEmbedUrl(
        provider: StreamEmbedProvider,
        mediaId: Int,
        mediaType: String,
        seasonNumber: Int? = 1,
        episodeNumber: Int? = 1
    ): String {
        return ProviderManager.getInAppEmbedUrl(
            provider = provider,
            tmdbId = mediaId,
            mediaType = mediaType,
            season = seasonNumber,
            episode = episodeNumber
        )
    }

    fun getVidSrcCastUrl(
        mediaId: Int,
        mediaType: String,
        seasonNumber: Int? = 1,
        episodeNumber: Int? = 1
    ): String {
        return ProviderManager.getVidSrcCastUrl(
            tmdbId = mediaId,
            mediaType = mediaType,
            season = seasonNumber,
            episode = episodeNumber
        )
    }
}
