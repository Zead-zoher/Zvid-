package com.example.ui.remote

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.player.EmbeddedHttpServer
import com.example.player.LanStreamServer
import com.example.player.SubtitleEngine
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixDarkSurface
import com.example.ui.theme.NetflixGreen
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSurfaceVariant
import com.example.ui.theme.NetflixTextMuted
import com.example.ui.theme.NetflixTextPrimary
import com.example.ui.theme.NetflixTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneRemoteControlScreen(
    onResyncLink: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val localIp = remember { LanStreamServer.getLocalIpAddress(context) }
    val serverUrl = "http://$localIp:${EmbeddedHttpServer.PORT}"

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionSeconds by remember { mutableLongStateOf(0L) }
    var totalDurationSeconds by remember { mutableLongStateOf(7200L) }
    var selectedQuality by remember { mutableStateOf("1080p FHD") }
    var selectedSubtitle by remember { mutableStateOf("English [CC]") }
    var subtitleOffsetSeconds by remember { mutableFloatStateOf(0.0f) }

    var showQualitySheet by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    val remoteState = EmbeddedHttpServer.remoteState.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("phone_remote_control_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phone Remote Control",
                        color = NetflixTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NetflixGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NetflixGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Server Active",
                            color = NetflixGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LAN Web Address Connection Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NetflixCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TV / Browser Connection Address:",
                                color = NetflixTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = serverUrl,
                                color = NetflixGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(serverUrl))
                                copiedNotice = "Server URL copied!"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NetflixRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Web Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Re-sync link button if external stream expires
                        OutlinedButton(
                            onClick = onResyncLink,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("resync_link_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = NetflixTextPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-sync Link", color = NetflixTextPrimary, fontSize = 12.sp)
                        }
                    }

                    if (copiedNotice != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = copiedNotice!!,
                            color = NetflixGreen,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Media Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NetflixCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NetflixSurfaceVariant)
                    ) {
                        if (!remoteState.posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(remoteState.posterUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = remoteState.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = remoteState.title,
                            color = NetflixTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (remoteState.mediaType == "tv" && remoteState.seasonNumber != null && remoteState.episodeNumber != null) {
                            Text(
                                text = "Season ${remoteState.seasonNumber} • Episode ${remoteState.episodeNumber}",
                                color = NetflixRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Streaming Quality: $selectedQuality",
                            color = NetflixTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BIG REMOTE CONTROLS AREA
            Text(
                text = "Remote Playback Controls",
                color = NetflixTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Playback Scrubber Slider
            Slider(
                value = currentPositionSeconds.toFloat(),
                onValueChange = { newVal ->
                    currentPositionSeconds = newVal.toLong()
                    EmbeddedHttpServer.seekTo(currentPositionSeconds)
                },
                valueRange = 0f..totalDurationSeconds.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = NetflixRed,
                    activeTrackColor = NetflixRed,
                    inactiveTrackColor = Color(0xFF333333)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remote_seek_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatSeconds(currentPositionSeconds),
                    color = NetflixTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatSeconds(totalDurationSeconds),
                    color = NetflixTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Remote Buttons (-10s, Big Play/Pause, +10s)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentPositionSeconds = (currentPositionSeconds - 10L).coerceAtLeast(0L)
                        EmbeddedHttpServer.seekTo(currentPositionSeconds)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "-10s",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(NetflixRed)
                        .clickable {
                            isPlaying = !isPlaying
                            EmbeddedHttpServer.togglePlayPause()
                        }
                        .testTag("remote_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Pause",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                IconButton(
                    onClick = {
                        currentPositionSeconds = (currentPositionSeconds + 10L).coerceAtMost(totalDurationSeconds)
                        EmbeddedHttpServer.seekTo(currentPositionSeconds)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "+10s",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quality & Subtitle Pushes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quality Push Button
                Surface(
                    onClick = { showQualitySheet = true },
                    shape = RoundedCornerShape(10.dp),
                    color = NetflixCardElevated,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Hd, contentDescription = null, tint = NetflixRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Quality", color = NetflixTextSecondary, fontSize = 11.sp)
                            Text(selectedQuality, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Subtitles Push Button
                Surface(
                    onClick = { showSubtitleSheet = true },
                    shape = RoundedCornerShape(10.dp),
                    color = NetflixCardElevated,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Subtitles, contentDescription = null, tint = NetflixRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Subtitles", color = NetflixTextSecondary, fontSize = 11.sp)
                            Text(selectedSubtitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quality Sheet
        if (showQualitySheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showQualitySheet = false },
                sheetState = sheetState,
                containerColor = NetflixDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text("Select Quality to Push to TV:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf("4K UHD HDR", "1080p FHD", "720p HD", "480p SD").forEach { quality ->
                        Surface(
                            onClick = {
                                selectedQuality = quality
                                EmbeddedHttpServer.updateQuality(quality)
                                showQualitySheet = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedQuality == quality) NetflixRed.copy(alpha = 0.2f) else NetflixCardElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = quality,
                                color = if (selectedQuality == quality) NetflixRed else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subtitle Sheet
        if (showSubtitleSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSubtitleSheet = false },
                sheetState = sheetState,
                containerColor = NetflixDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text("Push Subtitles to TV Screen:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    SubtitleEngine.availableTracks.forEach { track ->
                        Surface(
                            onClick = {
                                selectedSubtitle = track.language
                                EmbeddedHttpServer.updateSubtitle(track.language, subtitleOffsetSeconds)
                                showSubtitleSheet = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedSubtitle == track.language) NetflixRed.copy(alpha = 0.2f) else NetflixCardElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = track.language,
                                color = if (selectedSubtitle == track.language) NetflixRed else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatSeconds(totalSecs: Long): String {
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
