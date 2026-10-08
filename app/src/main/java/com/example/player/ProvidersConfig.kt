package com.example.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

data class StreamEmbedProvider(
    val id: String,
    val name: String,
    val movieUrlTemplate: String,
    val tvUrlTemplate: String,
    val defaultReferer: String = "",
    val isPrimary: Boolean = true
)

object ProvidersConfig {

    // VidSrc is the sole dedicated provider (internally contains multiple servers)
    val standardProviders = listOf(
        StreamEmbedProvider(
            id = "vidsrc_win",
            name = "VidSrc (vidsrc.win)",
            movieUrlTemplate = "https://vidsrc.win/watch/{tmdb_id}",
            tvUrlTemplate = "https://vidsrc.win/watch/{tmdb_id}?s={season}&e={episode}",
            defaultReferer = "https://vidsrc.win/",
            isPrimary = true
        )
    )

    const val VIDSRC_WIN_MOVIE_TEMPLATE = "https://vidsrc.win/watch/{tmdb_id}"
    const val VIDSRC_WIN_SERIES_TEMPLATE = "https://vidsrc.win/watch/{tmdb_id}?s={season}&e={episode}"
}

object ProviderManager {

    /**
     * Returns the dedicated VidSrc provider.
     */
    fun getInAppProviders(): List<StreamEmbedProvider> = ProvidersConfig.standardProviders

    /**
     * Resolves the in-app embed URL for VidSrc.
     */
    fun getInAppEmbedUrl(
        provider: StreamEmbedProvider = ProvidersConfig.standardProviders.first(),
        tmdbId: Int,
        mediaType: String,
        season: Int? = 1,
        episode: Int? = 1
    ): String {
        return if (mediaType == "tv") {
            provider.tvUrlTemplate
                .replace("{tmdb_id}", tmdbId.toString())
                .replace("{season}", (season ?: 1).toString())
                .replace("{episode}", (episode ?: 1).toString())
        } else {
            provider.movieUrlTemplate
                .replace("{tmdb_id}", tmdbId.toString())
        }
    }

    /**
     * Resolves the default VidSrc URL for Web Video Caster.
     */
    fun getVidSrcCastUrl(
        tmdbId: Int,
        mediaType: String,
        season: Int? = 1,
        episode: Int? = 1
    ): String {
        return if (mediaType == "tv") {
            ProvidersConfig.VIDSRC_WIN_SERIES_TEMPLATE
                .replace("{tmdb_id}", tmdbId.toString())
                .replace("{season}", (season ?: 1).toString())
                .replace("{episode}", (episode ?: 1).toString())
        } else {
            ProvidersConfig.VIDSRC_WIN_MOVIE_TEMPLATE
                .replace("{tmdb_id}", tmdbId.toString())
        }
    }

    /**
     * Launches Web Video Caster app directly with the provided URL.
     * Falls back to opening in browser if Web Video Caster is not installed.
     */
    fun castToWebVideoCaster(
        context: Context,
        url: String,
        title: String = "Zvid Media"
    ) {
        val wvcIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            setPackage("com.instantbits.cast.webvideo")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("title", title)
        }

        try {
            context.startActivity(wvcIntent)
            Toast.makeText(context, "Opening in Web Video Caster...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            // Fallback to system browser
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                Toast.makeText(context, "Opening stream in browser...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
