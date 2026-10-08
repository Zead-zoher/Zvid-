package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.EmbedProvider
import com.example.player.EmbedStreamResolver
import com.example.player.WebViewStreamDetector
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixDarkSurface
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixTextMuted
import com.example.ui.theme.NetflixTextPrimary
import com.example.ui.theme.NetflixTextSecondary

@Composable
fun ResolutionOverlayModal(
    isResolving: Boolean,
    title: String,
    mediaId: Int,
    mediaType: String,
    seasonNumber: Int? = null,
    episodeNumber: Int? = null,
    selectedProvider: EmbedProvider,
    logs: List<String>,
    onProviderSelected: (EmbedProvider) -> Unit,
    onStreamDetected: (streamUrl: String, headers: Map<String, String>) -> Unit,
    onLogEvent: (String) -> Unit,
    onCancel: () -> Unit
) {
    if (!isResolving) return

    val embedUrl = EmbedStreamResolver.getEmbedUrl(
        provider = selectedProvider,
        mediaId = mediaId,
        mediaType = mediaType,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack.copy(alpha = 0.95f))
            .padding(16.dp)
            .testTag("resolution_overlay_modal"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NetflixDarkSurface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Resolving Stream Source...",
                        color = NetflixTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = NetflixTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Episode Info
            Text(
                text = title,
                color = NetflixTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (mediaType == "tv" && seasonNumber != null && episodeNumber != null) {
                Text(
                    text = "Season $seasonNumber • Episode $episodeNumber",
                    color = NetflixRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Loading Spinner
            CircularProgressIndicator(
                color = NetflixRed,
                modifier = Modifier.size(44.dp),
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Provider Switcher Row
            Text(
                text = "Embed Stream Sources:",
                color = NetflixTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(EmbedStreamResolver.providers) { provider ->
                    val isSelected = provider.id == selectedProvider.id
                    Surface(
                        onClick = { onProviderSelected(provider) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NetflixRed else NetflixCardElevated
                    ) {
                        Text(
                            text = provider.name,
                            color = if (isSelected) Color.White else NetflixTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Log Feed
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NetflixBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    logs.takeLast(6).forEach { log ->
                        Text(
                            text = "> $log",
                            color = if (log.contains("Detected")) Color(0xFF46D369) else NetflixTextMuted,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integrated Headless / Stream Sniffing WebView
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                WebViewStreamDetector(
                    embedUrl = embedUrl,
                    providerReferer = selectedProvider.defaultReferer,
                    onStreamDetected = onStreamDetected,
                    onLogEvent = onLogEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("cancel_resolution_btn")
            ) {
                Text("Cancel Resolution", color = NetflixTextSecondary, fontSize = 13.sp)
            }
        }
    }
}
