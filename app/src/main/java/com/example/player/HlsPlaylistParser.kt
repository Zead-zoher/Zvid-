package com.example.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class HlsVariantQuality(
    val resolution: String, // e.g. "1080p", "720p", "480p", "360p"
    val bandwidth: Long,    // e.g. 8500000
    val url: String,        // Variant .m3u8 URL
    val label: String       // e.g. "1080p FHD (8.5 Mbps)"
)

object HlsPlaylistParser {

    /**
     * Parses an HLS Master Playlist URL or raw string content to extract variant resolution streams.
     */
    suspend fun parseMasterPlaylist(
        masterUrl: String,
        rawContent: String? = null
    ): List<HlsVariantQuality> = withContext(Dispatchers.IO) {
        val variants = mutableListOf<HlsVariantQuality>()

        try {
            val content = rawContent ?: try {
                URL(masterUrl).readText()
            } catch (_: Exception) {
                null
            }

            if (!content.isNullOrBlank() && content.contains("#EXT-X-STREAM-INF")) {
                val lines = content.lines()
                var lastResolution = "1080p"
                var lastBandwidth = 5000000L

                for (i in lines.indices) {
                    val line = lines[i].trim()
                    if (line.startsWith("#EXT-X-STREAM-INF")) {
                        // Extract BANDWIDTH and RESOLUTION
                        val bandwidthMatch = Regex("BANDWIDTH=(\\d+)").find(line)
                        if (bandwidthMatch != null) {
                            lastBandwidth = bandwidthMatch.groupValues[1].toLongOrNull() ?: 5000000L
                        }

                        val resMatch = Regex("RESOLUTION=(\\d+)x(\\d+)").find(line)
                        if (resMatch != null) {
                            val height = resMatch.groupValues[2]
                            lastResolution = "${height}p"
                        }

                        // Next non-empty line is the variant URL
                        if (i + 1 < lines.size) {
                            val variantUri = lines[i + 1].trim()
                            if (variantUri.isNotEmpty() && !variantUri.startsWith("#")) {
                                val fullVariantUrl = if (variantUri.startsWith("http")) {
                                    variantUri
                                } else {
                                    resolveRelativeUrl(masterUrl, variantUri)
                                }

                                val mbps = String.format("%.1f", lastBandwidth / 1_000_000.0)
                                variants.add(
                                    HlsVariantQuality(
                                        resolution = lastResolution,
                                        bandwidth = lastBandwidth,
                                        url = fullVariantUrl,
                                        label = "$lastResolution (${mbps} Mbps)"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (variants.isEmpty()) {
            // Default fallback quality variants for HLS streams
            variants.addAll(
                listOf(
                    HlsVariantQuality("1080p", 8500000L, masterUrl, "1080p FHD (8.5 Mbps)"),
                    HlsVariantQuality("720p", 4200000L, masterUrl, "720p HD (4.2 Mbps)"),
                    HlsVariantQuality("480p", 1800000L, masterUrl, "480p SD (1.8 Mbps)"),
                    HlsVariantQuality("360p", 800000L, masterUrl, "360p Mobile (0.8 Mbps)")
                )
            )
        }

        return@withContext variants.sortedByDescending { it.bandwidth }
    }

    private fun resolveRelativeUrl(baseUrl: String, relative: String): String {
        return try {
            val base = URL(baseUrl)
            URL(base, relative).toString()
        } catch (_: Exception) {
            relative
        }
    }
}
