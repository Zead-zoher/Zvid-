package com.example.player

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

object LanStreamServer {

    /**
     * Retrieves the device's local IPv4 address on the Wi-Fi or LAN network.
     */
    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        // Fallback network interface inspection
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress
                        if (hostAddress != null && hostAddress.indexOf(':') < 0) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return "192.168.1.100"
    }

    /**
     * Formats a LAN streaming URL that can be copied or casted to external Smart TVs or VLC on the local network.
     */
    fun buildLanStreamUrl(
        localIp: String,
        port: Int = 8080,
        streamUrl: String
    ): String {
        val encodedUrl = Uri.encode(streamUrl)
        return "http://$localIp:$port/stream?url=$encodedUrl"
    }

    /**
     * Generates a standard .m3u playlist string for external player compatibility (VLC, MX Player, Smart TV).
     */
    fun buildM3uPlaylist(
        title: String,
        streamUrl: String,
        headers: Map<String, String>
    ): String {
        val referer = headers["Referer"] ?: ""
        val userAgent = headers["User-Agent"] ?: ""
        return """
            #EXTM3U
            #EXTINF:-1 tvg-name="$title" group-title="Zvid Streaming", $title
            #EXTVLCOPT:http-user-agent=$userAgent
            #EXTVLCOPT:http-referrer=$referer
            $streamUrl
        """.trimIndent()
    }
}
