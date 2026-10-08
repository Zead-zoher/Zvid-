package com.example.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WebVideoCasterHelper {

    const val PACKAGE_NAME = "com.instantbits.cast.webvideo"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.instantbits.cast.webvideo"

    fun copyLinkToClipboard(context: Context, url: String, label: String = "Zvid Stream URL") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, url)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Casts the default VidSrc (vidsrc.win) stream directly to Web Video Caster.
     */
    fun castVidSrcToWebVideoCaster(
        context: Context,
        tmdbId: Int,
        mediaType: String,
        season: Int? = 1,
        episode: Int? = 1,
        title: String = "Zvid Video"
    ) {
        val vidsrcUrl = ProviderManager.getVidSrcCastUrl(tmdbId, mediaType, season, episode)
        openInWebVideoCaster(context, vidsrcUrl, title)
    }

    /**
     * Casts any stream/embed URL directly into Web Video Caster.
     */
    fun openInWebVideoCaster(context: Context, embedUrl: String, title: String = "Zvid Video") {
        copyLinkToClipboard(context, embedUrl, title)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(embedUrl)
            setPackage(PACKAGE_NAME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("title", title)
        }

        try {
            context.startActivity(intent)
            Toast.makeText(context, "Opening in Web Video Caster...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(embedUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                Toast.makeText(context, "Opening stream in browser...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Copied stream URL to clipboard!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
