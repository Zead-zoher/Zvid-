package com.example.player

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.math.BigInteger
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

data class TvRemoteState(
    val title: String = "No Media Loaded",
    val mediaType: String = "movie",
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val posterUrl: String? = null,
    val streamUrl: String = "",
    val refererHeader: String = "https://vidsrc.to/",
    val isPlaying: Boolean = true,
    val currentTimeSeconds: Long = 0L,
    val totalDurationSeconds: Long = 7200L,
    val qualityLabel: String = "1080p FHD",
    val subtitleLanguage: String = "English",
    val subtitleOffsetSeconds: Float = 0f,
    val activeSubtitleText: String? = null,
    val isConnected: Boolean = false,
    val serverIpAddress: String = "0.0.0.0"
)

object EmbeddedHttpServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    const val PORT = 8080

    private val _remoteState = MutableStateFlow(TvRemoteState())
    val remoteState: StateFlow<TvRemoteState> = _remoteState.asStateFlow()

    fun startServer(context: Context) {
        if (serverSocket != null && !serverSocket!!.isClosed) return

        val localIp = getLocalWifiIpAddress(context)

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(PORT)
                _remoteState.value = _remoteState.value.copy(
                    isConnected = true,
                    serverIpAddress = localIp
                )

                while (serverSocket != null && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    launch { handleClientSocket(clientSocket) }
                }
            } catch (_: Exception) {
                _remoteState.value = _remoteState.value.copy(isConnected = false)
            }
        }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
        } catch (_: Exception) {}
        _remoteState.value = _remoteState.value.copy(isConnected = false)
    }

    fun updateMediaInfo(
        title: String,
        mediaType: String,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeTitle: String? = null,
        posterUrl: String? = null,
        streamUrl: String,
        referer: String = "https://vidsrc.to/",
        initialPositionSeconds: Long = 0L,
        totalDurationSeconds: Long = 7200L,
        qualityLabel: String = "1080p FHD"
    ) {
        val currentIp = _remoteState.value.serverIpAddress
        
        val formattedStreamUrl = if (streamUrl.startsWith("http") && !streamUrl.contains(".m3u8")) {
            "http://$currentIp:$PORT/proxy?url=${Uri.encode(streamUrl)}"
        } else {
            streamUrl
        }

        _remoteState.value = _remoteState.value.copy(
            title = title,
            mediaType = mediaType,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            posterUrl = posterUrl,
            streamUrl = formattedStreamUrl,
            refererHeader = referer,
            isPlaying = true,
            currentTimeSeconds = initialPositionSeconds,
            totalDurationSeconds = totalDurationSeconds,
            qualityLabel = qualityLabel
        )
    }

    fun togglePlayPause() {
        val current = _remoteState.value
        _remoteState.value = current.copy(isPlaying = !current.isPlaying)
    }

    fun seekTo(seconds: Long) {
        _remoteState.value = _remoteState.value.copy(currentTimeSeconds = seconds)
    }

    fun updateQuality(quality: String) {
        _remoteState.value = _remoteState.value.copy(qualityLabel = quality)
    }

    fun updateSubtitle(trackName: String, offsetSeconds: Float = 0f) {
        _remoteState.value = _remoteState.value.copy(
            subtitleLanguage = trackName,
            subtitleOffsetSeconds = offsetSeconds
        )
    }

    private fun handleClientSocket(socket: Socket) {
        try {
            socket.soTimeout = 8000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return

            val pathWithQuery = parts[1]
            val uri = Uri.parse(pathWithQuery)
            val path = uri.path ?: "/"

            when {
                path == "/" || path == "/tv" -> serveHtmlPlayerPage(output)
                path == "/api/status" -> serveStatusJson(output)
                path == "/proxy" -> {
                    val targetUrl = uri.getQueryParameter("url")
                    if (!targetUrl.isNullOrBlank()) {
                        proxyStreamChunk(targetUrl, _remoteState.value.refererHeader, output)
                    } else {
                        sendResponse(output, "400 Bad Request", "text/plain", "Missing url parameter")
                    }
                }
                else -> sendResponse(output, "404 Not Found", "text/plain", "404 Not Found")
            }
        } catch (_: Exception) {
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun serveStatusJson(output: OutputStream) {
        val state = _remoteState.value
        val json = """
            {
                "title": "${escapeJson(state.title)}",
                "mediaType": "${state.mediaType}",
                "season": ${state.seasonNumber ?: "null"},
                "episode": ${state.episodeNumber ?: "null"},
                "streamUrl": "${escapeJson(state.streamUrl)}",
                "isPlaying": ${state.isPlaying},
                "currentTime": ${state.currentTimeSeconds},
                "totalDuration": ${state.totalDurationSeconds},
                "quality": "${state.qualityLabel}",
                "subtitle": "${state.subtitleLanguage}",
                "subtitleOffset": ${state.subtitleOffsetSeconds}
            }
        """.trimIndent()

        sendResponse(output, "200 OK", "application/json", json)
    }

    private fun proxyStreamChunk(targetUrl: String, referer: String, output: OutputStream) {
        try {
            val req = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0.0.0")
                .header("Referer", referer)
                .build()

            httpClient.newCall(req).execute().use { response ->
                val responseCode = response.code
                val contentType = response.header("Content-Type") ?: "video/mp4"

                val headersStr = "HTTP/1.1 $responseCode OK\r\n" +
                        "Content-Type: $contentType\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"

                output.write(headersStr.toByteArray())

                response.body?.byteStream()?.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (_: Exception) {
            sendResponse(output, "500 Internal Error", "text/plain", "Proxy error")
        }
    }

    private fun serveHtmlPlayerPage(output: OutputStream) {
        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Zvid Smart TV Web Player</title>
                <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000; color: #fff; font-family: sans-serif; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                    #video-container { position: relative; width: 100vw; height: 100vh; }
                    video { width: 100%; height: 100%; object-fit: contain; }
                    .tv-overlay { position: absolute; top: 20px; left: 20px; right: 20px; display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.75); padding: 12px 24px; border-radius: 8px; z-index: 10; }
                    .brand { color: #E50914; font-weight: bold; font-size: 20px; }
                    .title { color: #fff; font-size: 18px; margin-left: 12px; }
                </style>
            </head>
            <body>
                <div id="video-container">
                    <div class="tv-overlay">
                        <div><span class="brand">Zvid TV</span><span class="title" id="media-title">Connecting to App...</span></div>
                        <div id="quality-badge" style="color: #46D369; font-weight: bold;">1080p FHD</div>
                    </div>
                    <video id="player" autoplay controls></video>
                </div>
                <script>
                    const player = document.getElementById('player');
                    const titleEl = document.getElementById('media-title');
                    const badgeEl = document.getElementById('quality-badge');
                    let currentStreamUrl = '';
                    let hls = null;

                    async function pollStatus() {
                        try {
                            const res = await fetch('/api/status');
                            const data = await res.json();
                            titleEl.innerText = data.title;
                            badgeEl.innerText = data.quality;

                            if (data.streamUrl && data.streamUrl !== currentStreamUrl) {
                                currentStreamUrl = data.streamUrl;
                                loadStream(currentStreamUrl, data.currentTime);
                            }
                        } catch(e) {}
                    }

                    function loadStream(url, startSecs) {
                        if (Hls.isSupported()) {
                            if (hls) hls.destroy();
                            hls = new Hls();
                            hls.loadSource(url);
                            hls.attachMedia(player);
                            hls.on(Hls.Events.MANIFEST_PARSED, function() {
                                player.currentTime = startSecs;
                                player.play();
                            });
                        } else if (player.canPlayType('application/vnd.apple.mpegurl')) {
                            player.src = url;
                            player.currentTime = startSecs;
                            player.play();
                        }
                    }
                    setInterval(pollStatus, 1000);
                </script>
            </body>
            </html>
        """.trimIndent()

        sendResponse(output, "200 OK", "text/html", html)
    }

    private fun sendResponse(output: OutputStream, status: String, contentType: String, content: String) {
        try {
            val bytes = content.toByteArray()
            val header = "HTTP/1.1 $status\r\n" +
                    "Content-Type: $contentType; charset=UTF-8\r\n" +
                    "Content-Length: ${bytes.size}\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(header.toByteArray())
            output.write(bytes)
            output.flush()
        } catch (_: Exception) {}
    }

    private fun escapeJson(str: String): String {
        return str.replace("\"", "\\\"").replace("\n", " ")
    }

    private fun getLocalWifiIpAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            val ipInt = if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) Integer.reverseBytes(ipAddress) else ipAddress
            val ipBytes = BigInteger.valueOf(ipInt.toLong()).toByteArray()
            InetAddress.getByAddress(ipBytes).hostAddress ?: "127.0.0.1"
        } catch (_: Exception) {
            "127.0.0.1"
        }
    }
}
