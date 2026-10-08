package com.example.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewStreamDetector(
    embedUrl: String,
    providerReferer: String,
    onStreamDetected: (streamUrl: String, headers: Map<String, String>) -> Unit,
    onLogEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val customUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = customUserAgent
                    allowFileAccess = false
                    allowContentAccess = false
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.message()?.let { msg ->
                            if (msg.contains(".m3u8") || msg.contains(".mp4")) {
                                onLogEvent("Console: $msg")
                            }
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                webViewClient = object : WebViewClient() {

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

                        if (isMediaStreamUrl(url)) {
                            val headers = mutableMapOf<String, String>()
                            request.requestHeaders?.forEach { (key, value) ->
                                headers[key] = value
                            }

                            if (!headers.containsKey("Referer") && providerReferer.isNotEmpty()) {
                                headers["Referer"] = providerReferer
                            }
                            if (!headers.containsKey("User-Agent")) {
                                headers["User-Agent"] = customUserAgent
                            }

                            onLogEvent("Media Stream Detected: ${url.take(60)}...")
                            onStreamDetected(url, headers)
                        }

                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        val uri = Uri.parse(url)

                        // Prevent ad popups or intent:// or market:// links from leaving the app
                        val scheme = uri.scheme
                        if (scheme != "http" && scheme != "https") {
                            onLogEvent("Blocked non-http redirect: $scheme")
                            return true
                        }

                        // Block common ad networks / popups
                        if (isAdDomain(url)) {
                            onLogEvent("Blocked pop-under ad domain")
                            return true
                        }

                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLogEvent("Loading embed URL: ${url?.take(50)}...")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLogEvent("Page loaded. Sniffing video requests...")

                        // Auto-play trigger injection for embedded players
                        view?.evaluateJavascript(
                            """
                            (function() {
                                try {
                                    var v = document.querySelector('video');
                                    if (v) { v.play(); }
                                    var playBtn = document.querySelector('.play-btn, .jw-icon-play, #player');
                                    if (playBtn) { playBtn.click(); }
                                } catch(e){}
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }

                // Load with custom referer header
                val extraHeaders = mapOf("Referer" to providerReferer)
                loadUrl(embedUrl, extraHeaders)
            }
        },
        update = { webView ->
            if (webView.url != embedUrl) {
                val extraHeaders = mapOf("Referer" to providerReferer)
                webView.loadUrl(embedUrl, extraHeaders)
            }
        },
        modifier = modifier
    )
}

private fun isMediaStreamUrl(url: String): Boolean {
    val lower = url.lowercase()
    return (lower.contains(".m3u8") ||
            lower.contains(".mp4") ||
            lower.contains(".mkv") ||
            lower.contains("index.m3u8") ||
            lower.contains("master.m3u8") ||
            lower.contains("/hls/") ||
            lower.contains("/stream/")) &&
            !lower.contains(".jpg") &&
            !lower.contains(".png") &&
            !lower.contains(".css") &&
            !lower.contains(".js")
}

private fun isAdDomain(url: String): Boolean {
    val lower = url.lowercase()
    return lower.contains("bet") ||
           lower.contains("casino") ||
           lower.contains("popads") ||
           lower.contains("doubleclick") ||
           lower.contains("adsterra") ||
           lower.contains("syndication")
}
