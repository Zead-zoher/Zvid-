package com.example.player

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockEngine {

    // =========================================================================
    // 1. CUSTOM CSS (Cosmetic Filter - Brave & uBlock Origin Style Rules)
    // =========================================================================
    const val CUSTOM_CSS = """
        /* 1. Hide Download button & Back button */
        i.fa-download,
        .fa-download,
        [aria-label="Open download page"],
        i[class*="fa-download"],
        i.fa-arrow-left,
        .fa-arrow-left,
        [aria-label="Go back"],
        i[class*="fa-arrow-left"] {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
            width: 0 !important;
            height: 0 !important;
            margin: 0 !important;
            padding: 0 !important;
        }

        /* 2. Hide all in-video ad banners, floating overlays, and sponsored popups */
        [class*="ad-"]:not([class*="load"]):not([class*="head"]),
        [class*="ads-"],
        [class*="banner-"],
        [class*="sponsor-"],
        [id*="ad-"]:not([id*="load"]):not([id*="head"]),
        [id*="ads-"],
        [id*="banner-"],
        [id*="sponsor-"],
        [class*="popunder"],
        [id*="popunder"],
        [class*="clickjack"],
        [id*="clickjack"],
        [class*="ad_wrapper"],
        [id*="ad_wrapper"],
        [class*="adbox"],
        [id*="adbox"],
        [class*="vast-"],
        [id*="vast-"],
        [class*="vpaid-"],
        [id*="vpaid-"],
        [class*="preroll"],
        [id*="preroll"],
        .ad-container,
        .ads-container,
        .ad-banner,
        .advertisement,
        .banner-ad,
        .popup-overlay,
        .ad-overlay,
        #ad-overlay,
        #ads-holder,
        #player-ad,
        .video-ad,
        .preroll-ad,
        .interstitial-ad,
        .overlay-banner,
        .banner-overlay,
        .float-banner,
        .sticky-banner,
        .close-ad,
        .ad-close,
        .ad-btn {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            pointer-events: none !important;
            width: 0 !important;
            height: 0 !important;
            position: absolute !important;
            left: -99999px !important;
            top: -99999px !important;
            z-index: -99999 !important;
        }

        /* Ensure Subtitles and Video Controls Stay 100% Visible & Crisp */
        .vjs-text-track-display,
        .jw-captions,
        .plyr__captions,
        .dplayer-subtitle,
        [class*="caption"],
        [class*="subtitle"],
        [class*="subtitles"],
        ::cue {
            display: block !important;
            visibility: visible !important;
            opacity: 1 !important;
            z-index: 2147483640 !important;
        }

        .vjs-control-bar,
        .jw-controls,
        .plyr__controls,
        .art-controls,
        .dplayer-controller,
        [class*="control-bar"],
        [class*="controls-container"] {
            visibility: visible !important;
            opacity: 1 !important;
            z-index: 2147483645 !important;
        }
    """

    // =========================================================================
    // 2. EXPLICIT AD & POPUP DOMAINS (Comprehensive Blacklist)
    // =========================================================================
    private val externalAdDomains = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleads.g.doubleclick.net",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "securepubads.g.doubleclick.net",
        "popcash.net",
        "popads.net",
        "adsterra.com",
        "propellerads.com",
        "propellerclick.com",
        "exoclick.com",
        "trafficjunky.com",
        "clickadu.com",
        "hilltopads.net",
        "hilltopads.com",
        "richpush.co",
        "monetag.com",
        "admaven.com",
        "yllix.com",
        "evadav.com",
        "clickaine.com",
        "rollerads.com",
        "galaksion.com",
        "onclickpredictiv.com",
        "stripcdn.com",
        "etahub.com",
        "creativecdn.com",
        "adlightning.com",
        "adcash.com",
        "popmyads.com",
        "adsupply.com",
        "bidvertiser.com",
        "zeropark.com",
        "mgid.com",
        "taboola.com",
        "outbrain.com",
        "revcontent.com",
        "infolinks.com",
        "bet365.com",
        "1xbet.com",
        "melbet.com",
        "mostbet.com",
        "1win.pro",
        "1win.com",
        "histats.com",
        "hotjar.com",
        "trackjs.com",
        "deloplen.com",
        "deloton.com",
        "gogcontent.com",
        "cootlogu.com",
        "wistand.com",
        "highcpmrevenuenetwork.com",
        "onclick.net",
        "onclickads.net",
        "pangle.io",
        "vdo.ai",
        "inmobi.com",
        "unityads.unity3d.com",
        "applovin.com",
        "admob.com",
        "vungle.com",
        "chartboost.com",
        "vpaid",
        "vast"
    )

    private val adUrlSignatures = listOf(
        "/ads/",
        "/banner/",
        "/popunder",
        "/pop/",
        "/clickjack",
        "/interstitial",
        "/vast",
        "/vpaid",
        "/monetag",
        "/syndication",
        "popunder.js",
        "advert.js",
        "adservice",
        "ads.js"
    )

    /**
     * Checks if a URL strictly belongs to a known external ad network or signature.
     */
    fun isAdUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return false
        }

        val scheme = uri.scheme?.lowercase() ?: ""
        if (scheme != "http" && scheme != "https") {
            return true // Block non-http schemes (intent:, market:, tel:, etc.)
        }

        val host = uri.host?.lowercase() ?: return false

        // Check against known ad domains
        if (externalAdDomains.any { host == it || host.endsWith(".$it") }) {
            return true
        }

        val path = (uri.path?.lowercase() ?: "") + (uri.query?.lowercase() ?: "")
        if (adUrlSignatures.any { path.contains(it) }) {
            return true
        }

        return false
    }

    /**
     * Blocks popup/tab redirection to external ad domains,
     * while allowing all VidSrc internal player frames, CDNs, and media streams to load freely.
     */
    fun shouldBlockRedirect(currentEmbedUrl: String, targetUrl: String): Boolean {
        if (targetUrl.isBlank()) return true
        val targetUri = try {
            Uri.parse(targetUrl)
        } catch (_: Exception) {
            return true
        }

        val targetScheme = targetUri.scheme?.lowercase() ?: ""
        if (targetScheme != "http" && targetScheme != "https") {
            return true // Block non-http schemes (intent:, market:, etc.)
        }

        // Always block known ad networks
        if (isAdUrl(targetUrl)) return true

        val targetHost = targetUri.host?.lowercase() ?: ""
        val targetPath = targetUri.path?.lowercase() ?: ""

        // 1. Allow the root embed url and its /watch/ endpoints
        if (targetUrl == currentEmbedUrl || targetPath.contains("/watch")) {
            return false
        }

        // 2. Allow VidSrc domains and internal endpoints (rcp, prorcp, player, embed)
        if (targetHost.contains("vidsrc") ||
            targetHost.contains("2embed") ||
            targetHost.contains("vidplay") ||
            targetHost.contains("cloudstream") ||
            targetHost.contains("filemoon") ||
            targetHost.contains("streamtape")
        ) {
            return false
        }

        // 3. Allow streaming video CDNs and subtitles
        val isVideoResource = targetHost.contains("stream") ||
                targetHost.contains("cdn") ||
                targetHost.contains("video") ||
                targetHost.contains("cloud") ||
                targetHost.contains("embed") ||
                targetHost.contains("hls") ||
                targetPath.endsWith(".m3u8") ||
                targetPath.endsWith(".mp4") ||
                targetPath.contains("/hls/") ||
                targetPath.contains("/sub/")

        if (isVideoResource) {
            return false
        }

        // Block any other external navigation
        return true
    }

    fun createEmptyResourceResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    /**
     * Advanced In-Video Ad Banner & Overlay Purger (uBlock/Brave Engine style):
     * 1. Continuously scans the DOM and nested iframes to remove all banner overlays, ads, and click-traps.
     * 2. Specifically preserves video element, controls, subtitles/captions, and quality/server settings.
     * 3. Completely disables popup tabs and synthetic anchor triggers.
     */
    const val AD_BLOCK_JAVASCRIPT_INJECTION = """
        (function() {
            try {
                // 1. Disable window.open popups
                window.open = function() { return null; };
                window.alert = function() {};
                window.confirm = function() { return true; };
                window.prompt = function() { return null; };

                // 2. Override programmatic anchor clicks targeting _blank or external URLs
                var originalAnchorClick = HTMLAnchorElement.prototype.click;
                HTMLAnchorElement.prototype.click = function() {
                    var href = this.getAttribute('href') || '';
                    var target = this.getAttribute('target') || '';
                    if (target === '_blank' || (href && !href.includes('vidsrc') && !href.startsWith('#') && !href.startsWith('javascript:'))) {
                        return; // Block programmatic ad click
                    }
                    return originalAnchorClick.apply(this, arguments);
                };

                // 3. Intercept click-jacking and new tab links in capturing phase
                function handleEventCapture(e) {
                    var target = e.target;
                    while (target && target !== document) {
                        if (target.tagName === 'A') {
                            var href = target.getAttribute('href') || '';
                            var trg = target.getAttribute('target') || '';
                            if (trg === '_blank' || (href && !href.includes('vidsrc') && !href.startsWith('#') && !href.startsWith('javascript:'))) {
                                e.preventDefault();
                                e.stopPropagation();
                                e.stopImmediatePropagation();
                                return false;
                            }
                        }
                        target = target.parentElement;
                    }
                }

                document.addEventListener('click', handleEventCapture, true);
                document.addEventListener('auxclick', handleEventCapture, true);

                // Helper: Check if element is an essential player component
                function isEssentialPlayerComponent(el) {
                    if (!el) return false;
                    var tag = el.tagName;
                    if (tag === 'VIDEO' || tag === 'TRACK' || tag === 'AUDIO' || tag === 'CANVAS') return true;
                    
                    var cls = (el.className && typeof el.className === 'string') ? el.className.toLowerCase() : '';
                    var id = (el.id && typeof el.id === 'string') ? el.id.toLowerCase() : '';

                    // Preserved: Subtitles, Controls, Quality/Audio/Server menus
                    if (cls.includes('caption') || cls.includes('subtitle') || cls.includes('text-track') ||
                        cls.includes('control') || cls.includes('player-controls') || cls.includes('vjs-') ||
                        cls.includes('jw-') || cls.includes('plyr') || cls.includes('dplayer') ||
                        cls.includes('menu') || cls.includes('settings') || cls.includes('quality') ||
                        cls.includes('server') || cls.includes('audio') || cls.includes('speed') ||
                        cls.includes('progress') || cls.includes('volume') || cls.includes('play') ||
                        id.includes('subtitle') || id.includes('caption') || id.includes('control')) {
                        return true;
                    }

                    // Check parents
                    if (el.closest('.vjs-control-bar') || el.closest('.jw-controls') || 
                        el.closest('.vjs-text-track-display') || el.closest('.jw-captions') ||
                        el.closest('.plyr__controls') || el.closest('.dplayer-controller')) {
                        return true;
                    }

                    return false;
                }

                // 4. Purge in-video ad overlays, floating banners, and click traps
                function purgeInVideoAds(doc) {
                    if (!doc) return;
                    try {
                        // A) Remove ad iframes inside player
                        var iframes = doc.querySelectorAll('iframe');
                        for (var f = 0; f < iframes.length; f++) {
                            var ifr = iframes[f];
                            var src = (ifr.src || '').toLowerCase();
                            if (src.includes('ad') || src.includes('banner') || src.includes('pop') || 
                                src.includes('vast') || src.includes('monetag') || src.includes('syndication')) {
                                ifr.remove();
                            }
                        }

                        // B) Remove banner overlay divs & elements appearing over video
                        var overlays = doc.querySelectorAll('div, a, span, section, ins');
                        for (var i = 0; i < overlays.length; i++) {
                            var el = overlays[i];
                            if (isEssentialPlayerComponent(el)) continue;

                            var cls = (el.className && typeof el.className === 'string') ? el.className.toLowerCase() : '';
                            var id = (el.id && typeof el.id === 'string') ? el.id.toLowerCase() : '';

                            // Match ad keywords in class or ID
                            if (cls.includes('ad-') || cls.includes('ads-') || cls.includes('banner') || 
                                cls.includes('popunder') || cls.includes('clickjack') || cls.includes('vast') ||
                                cls.includes('preroll') || cls.includes('sponsor') || cls.includes('overlay-banner') ||
                                id.includes('ad-') || id.includes('ads-') || id.includes('banner') || 
                                id.includes('sponsor') || id.includes('preroll') || id.includes('vast')) {
                                el.remove();
                                continue;
                            }

                            // Match floating banners by computed style
                            var style = window.getComputedStyle ? window.getComputedStyle(el) : null;
                            if (style && (style.position === 'fixed' || style.position === 'absolute')) {
                                var zIndex = parseInt(style.zIndex, 10);
                                var w = el.offsetWidth;
                                var h = el.offsetHeight;

                                // Large transparent click trap
                                if (zIndex >= 50 && w > (window.innerWidth * 0.4) && h > (window.innerHeight * 0.4) && !el.querySelector('video') && !el.querySelector('iframe')) {
                                    el.remove();
                                    continue;
                                }

                                // In-video overlay ad banners (bottom banner, floating popup)
                                if (zIndex >= 10 && (cls.includes('overlay') || cls.includes('popup') || id.includes('overlay') || id.includes('popup'))) {
                                    el.remove();
                                }
                            }
                        }
                    } catch(err) {}
                }

                // 5. Injects custom CSS rules into document and nested iframes
                function applyCustomStyles(doc) {
                    if (!doc || !doc.head) return;
                    var styleId = 'zvid-browser-custom-css';
                    if (!doc.getElementById(styleId)) {
                        var styleEl = doc.createElement('style');
                        styleEl.id = styleId;
                        styleEl.type = 'text/css';
                        styleEl.innerHTML = `
                            i.fa-download,
                            .fa-download,
                            [aria-label="Open download page"],
                            i[class*="fa-download"],
                            i.fa-arrow-left,
                            .fa-arrow-left,
                            [aria-label="Go back"],
                            i[class*="fa-arrow-left"],
                            [class*="ad-"]:not([class*="load"]):not([class*="head"]),
                            [class*="ads-"],
                            [class*="banner-"],
                            [class*="sponsor-"],
                            [id*="ad-"]:not([id*="load"]):not([id*="head"]),
                            [id*="ads-"],
                            [id*="banner-"],
                            [id*="sponsor-"],
                            .ad-container,
                            .ads-container,
                            .ad-banner,
                            .popup-overlay,
                            .ad-overlay {
                                display: none !important;
                                visibility: hidden !important;
                                opacity: 0 !important;
                                pointer-events: none !important;
                                width: 0 !important;
                                height: 0 !important;
                                margin: 0 !important;
                                padding: 0 !important;
                            }
                            .vjs-text-track-display, .jw-captions, .plyr__captions, [class*="subtitle"], [class*="caption"] {
                                display: block !important;
                                visibility: visible !important;
                                opacity: 1 !important;
                            }
                        `;
                        doc.head.appendChild(styleEl);
                    }

                    purgeInVideoAds(doc);

                    // Apply to any nested player iframes
                    var iframes = doc.querySelectorAll('iframe');
                    for (var k = 0; k < iframes.length; k++) {
                        try {
                            if (iframes[k].contentDocument) {
                                applyCustomStyles(iframes[k].contentDocument);
                            }
                        } catch(e) {}
                    }
                }

                applyCustomStyles(document);
                
                // Continuous protection loop (every 300ms) to destroy in-video banner ads immediately
                setInterval(function() {
                    applyCustomStyles(document);
                }, 300);

                if (window.MutationObserver) {
                    var observer = new MutationObserver(function() {
                        applyCustomStyles(document);
                    });
                    observer.observe(document.body || document.documentElement, {
                        childList: true,
                        subtree: true
                    });
                }
            } catch(e) {}
        })();
    """
}
