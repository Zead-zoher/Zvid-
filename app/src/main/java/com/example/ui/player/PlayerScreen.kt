package com.example.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.player.*
import com.example.ui.theme.*
import java.util.Locale

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    stream: ResolvedStream?,
    isResolving: Boolean,
    resolvingTitle: String = "Zvid Media",
    embedUrl: String? = null,
    providerReferer: String = "",
    logs: List<String> = emptyList(),
    selectedProvider: StreamEmbedProvider = EmbedStreamResolver.providers.first(),
    onProviderChange: (StreamEmbedProvider) -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onPreviousEpisode: () -> Unit = {},
    onUpdateProgress: (Long, Long) -> Unit = { _, _ -> },
    onRefreshSession: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var isFullscreen by remember { mutableStateOf(false) }

    // Synchronize activity orientation and system bars with fullscreen state
    DisposableEffect(isFullscreen) {
        val window = activity?.window
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window?.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    // Intercept back button: if fullscreen -> exit fullscreen, if portrait -> close player
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    BackHandler(enabled = !isFullscreen) {
        onClose()
    }

    // Active embed URL calculation (Dedicated VidSrc)
    val currentEmbedUrl = remember(stream, embedUrl) {
        if (!embedUrl.isNullOrBlank()) {
            embedUrl
        } else if (stream != null) {
            EmbedStreamResolver.getEmbedUrl(
                provider = ProvidersConfig.standardProviders.first(),
                mediaId = stream.mediaId,
                mediaType = stream.mediaType,
                seasonNumber = stream.seasonNumber,
                episodeNumber = stream.episodeNumber
            )
        } else ""
    }

    // Subtitle Search Name (Formatted for easy copy)
    val subtitleSearchName = remember(stream, resolvingTitle) {
        if (stream != null) {
            if (stream.mediaType == "tv" && stream.seasonNumber != null && stream.episodeNumber != null) {
                String.format(
                    Locale.US,
                    "%s S%02dE%02d",
                    stream.title,
                    stream.seasonNumber,
                    stream.episodeNumber
                )
            } else {
                stream.title
            }
        } else {
            resolvingTitle
        }
    }

    var isPageLoading by remember { mutableStateOf(true) }
    var customVideoView by remember { mutableStateOf<View?>(null) }

    // Persistent WebView instance that never reloads on fullscreen or orientation change
    val persistentWebView = remember {
        createConfiguredWebView(
            context = context,
            embedUrl = currentEmbedUrl,
            referer = "https://vidsrc.win/",
            onLoadingChange = { isPageLoading = it },
            onCustomViewShow = { customVideoView = it },
            onCustomViewHide = { customVideoView = null }
        )
    }

    // Only reload when the underlying movie/episode or embed URL changes
    LaunchedEffect(currentEmbedUrl) {
        if (currentEmbedUrl.isNotBlank()) {
            isPageLoading = true
            loadStreamInWebView(persistentWebView, currentEmbedUrl, "https://vidsrc.win/")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("in_app_player_screen")
    ) {
        if (customVideoView != null) {
            // HTML5 Fullscreen View
            AndroidView(
                factory = { customVideoView!! },
                modifier = Modifier.fillMaxSize()
            )
        } else if (isFullscreen) {
            // Fullscreen Landscape Video Player (Zero reload, instant rotation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { persistentWebView },
                    modifier = Modifier.fillMaxSize()
                )

                // Exit Fullscreen Floating Button (top right)
                IconButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("exit_fullscreen_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Standard Portrait Player Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NetflixCardElevated)
                                .testTag("player_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = stream?.title ?: resolvingTitle,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (stream?.mediaType == "tv" && stream.seasonNumber != null && stream.episodeNumber != null) {
                                Text(
                                    text = "Season ${stream.seasonNumber} • Episode ${stream.episodeNumber}" +
                                            if (!stream.episodeTitle.isNullOrBlank()) " (${stream.episodeTitle})" else "",
                                    color = NetflixRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "VidSrc Player • Multi-Server Stream",
                                    color = NetflixGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Reload / Refresh Button
                    IconButton(
                        onClick = {
                            isPageLoading = true
                            if (currentEmbedUrl.isNotBlank()) {
                                loadStreamInWebView(persistentWebView, currentEmbedUrl, "https://vidsrc.win/")
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NetflixCardElevated)
                            .testTag("player_reload_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Player",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ==================== TV EPISODES NAVIGATION BAR ====================
                if (stream?.mediaType == "tv") {
                    val currentEp = stream.episodeNumber ?: 1
                    val hasPrevious = currentEp > 1

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NetflixDarkSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Episode Button
                            FilledTonalButton(
                                onClick = onPreviousEpisode,
                                enabled = hasPrevious,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF262626),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF1A1A1A),
                                    disabledContentColor = Color.Gray
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("prev_episode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous Ep", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Current Episode Label
                            Text(
                                text = "Episode $currentEp",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Next Episode Button
                            Button(
                                onClick = onNextEpisode,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetflixRed,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("next_episode_btn")
                            ) {
                                Text("Next Ep", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ==================== FULLSCREEN & ROTATE BUTTON UNDER EPISODE BAR ====================
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FilledTonalButton(
                        onClick = { isFullscreen = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("fullscreen_rotate_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fullscreen & Rotate (توسيع ولف الشاشة)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ==================== VIDEO CONTAINER ====================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(235.dp)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { persistentWebView },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isPageLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NetflixRed,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==================== RECTANGLE CARD UNDER VIDEO WITH LINK & ACTIONS ====================
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NetflixCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = NetflixRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Direct Stream Link",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "VidSrc Engine",
                                color = NetflixGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // URL Display Box
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF141414),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentEmbedUrl,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Web Video Cast & Copy Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val title = stream?.title ?: resolvingTitle
                                    if (stream != null) {
                                        WebVideoCasterHelper.castVidSrcToWebVideoCaster(
                                            context = context,
                                            tmdbId = stream.mediaId,
                                            mediaType = stream.mediaType,
                                            season = stream.seasonNumber,
                                            episode = stream.episodeNumber,
                                            title = title
                                        )
                                    } else {
                                        WebVideoCasterHelper.openInWebVideoCaster(context, currentEmbedUrl, title)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetflixRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("cast_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Web Video Cast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val title = stream?.title ?: resolvingTitle
                                    WebVideoCasterHelper.copyLinkToClipboard(context, currentEmbedUrl, title)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== SUBTITLE SEARCH NAME CARD ====================
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NetflixDarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF26A69A)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp).size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Subtitle Search Name:",
                                    color = NetflixTextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = subtitleSearchName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Copy Subtitle Title Button
                        FilledTonalButton(
                            onClick = {
                                WebVideoCasterHelper.copyLinkToClipboard(
                                    context = context,
                                    url = subtitleSearchName,
                                    label = "Subtitle Search Name"
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF262626),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("copy_subtitle_name_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Name", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info & instructions
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF141414),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Player Tips & Features:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Instant fullscreen & landscape rotation without reload.\n" +
                                    "• Press Back anytime to smoothly return to portrait.\n" +
                                    "• VidSrc contains built-in internal server choices.\n" +
                                    "• One-tap Web Video Cast casting support.",
                            color = NetflixTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun loadStreamInWebView(webView: WebView, embedUrl: String, referer: String) {
    if (embedUrl.isBlank()) return
    webView.stopLoading()

    val base = if (referer.isNotBlank()) referer else embedUrl
    val iframeHtml = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background-color: #000000;
                    overflow: hidden;
                }
                iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: 0;
                }
                ${AdBlockEngine.CUSTOM_CSS}
            </style>
        </head>
        <body>
            <iframe src="$embedUrl" allow="autoplay; fullscreen; encrypted-media; picture-in-picture" allowfullscreen></iframe>
        </body>
        </html>
    """.trimIndent()

    webView.loadDataWithBaseURL(base, iframeHtml, "text/html", "UTF-8", null)
}

private fun createConfiguredWebView(
    context: Context,
    embedUrl: String,
    referer: String,
    onLoadingChange: (Boolean) -> Unit,
    onCustomViewShow: (View) -> Unit,
    onCustomViewHide: () -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            allowFileAccess = false
            allowContentAccess = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: ""
                if (AdBlockEngine.isAdUrl(url)) {
                    return AdBlockEngine.createEmptyResourceResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val targetUrl = request?.url?.toString() ?: ""
                val isMainFrame = request?.isForMainFrame ?: false

                // If any popup or click-trap tries to navigate the main player window away, block it immediately
                if (isMainFrame && !targetUrl.contains("/watch") && !targetUrl.contains("vidsrc")) {
                    return true
                }

                if (AdBlockEngine.shouldBlockRedirect(embedUrl, targetUrl)) {
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onLoadingChange(true)
                view?.evaluateJavascript(AdBlockEngine.AD_BLOCK_JAVASCRIPT_INJECTION, null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onLoadingChange(false)
                view?.evaluateJavascript(AdBlockEngine.AD_BLOCK_JAVASCRIPT_INJECTION, null)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                view?.evaluateJavascript(AdBlockEngine.AD_BLOCK_JAVASCRIPT_INJECTION, null)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                if (view != null) onCustomViewShow(view)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                onCustomViewHide()
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                return false
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                result?.confirm()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                result?.confirm()
                return true
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                result?.confirm()
                return true
            }

            override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                result?.confirm()
                return true
            }
        }

        if (embedUrl.isNotBlank()) {
            loadStreamInWebView(this, embedUrl, referer)
        }
    }
}
