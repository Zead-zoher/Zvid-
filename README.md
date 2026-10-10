# 🎬 Zvid - Modern Movies & TV Series Android Application

🌐 **English** | [العربية](README.ar.md)

**Zvid** is a feature-rich, modern Android media streaming and exploration application built with **Kotlin** and **Jetpack Compose**. It leverages the TMDB API to provide seamless browsing of movies, TV series, actor filmographies, production studio catalogs, and embedded streaming playback, with a built-in always-on filter that keeps pornographic content out of the app in both **Arabic and English**.

📥 **[Download Zvid_v1.0.0.apk](https://github.com/Zead-zoher/Zvid-/releases/download/v1.0.0/Zvid_v1.0.0.apk)**

---

## 🌟 Features

- 🎬 **Movies & TV Shows Catalog**: Explore popular, top-rated, and trending movies and TV series with rich category filtering (including regional collections like EU & Lat).
- 🛡️ **Adult Content Protection (Arabic & English)**: A built-in, always-on filtering system that blocks pornographic and sexual content (including hentai) in both Arabic and English. See [Content Protection](#-content-protection) below.
- 🏢 **Production Companies Catalog**: Browse full media libraries from major production studios and companies without limit restrictions.
- 🎭 **Actor & Cast Profiles**: Deep dive into actors' bios, full filmographies, and media appearances with smooth backstack navigation.
- ⏯️ **Integrated Media Player**: Custom AndroidX Media3 / ExoPlayer integration supporting custom stream selection, quality overlays, and playback control interface.
- 🔖 **Saved Watchlist & Playback History**: Keep track of bookmarked movies/shows and pick up where you left off with recent history.
- 📺 **Web Video Caster Support**: Stream video links directly to your Smart TV or casting devices with seamless Web Video Caster integration.
- 🎨 **Material 3 Dark UI**: Modern, sleek dark theme with fluid animations, adaptive layouts, and responsive components.

---

## 🛡️ Content Protection

Zvid includes a built-in filtering system that protects users from pornographic and sexual content in **both Arabic and English**.

- **Always on**: The filter is part of the app itself. There is no toggle in the settings, so it cannot be switched off by mistake.
- **Arabic & English coverage**: Titles, descriptions, and keywords are checked in both languages, so adult content is caught whether it is written in Arabic or English.
- **Targeted, not an age filter**: The filter blocks sexual content only. Regular action and violent 18+ movies are **not** removed from the catalog.
- **Blocklist**: Specific movie, TV, and production company IDs can be blocked through a JSON blocklist file hosted in this repository, which the app reads to hide those entries.
- **Report button**: If something inappropriate slips through, users can report it directly from the app so it can be reviewed and added to the blocklist.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (StateFlow)
- **Networking**: Retrofit 2 & OkHttp 3 with JSON parsing
- **Media Playback**: AndroidX Media3 / ExoPlayer
- **Image Loading**: Coil Compose
- **Async & Reactive**: Kotlin Coroutines & Flow

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── data/              # Data models, local storage, remote API services, repositories
├── player/            # Video player implementation & controls
├── ui/                # Jetpack Compose UI layer
│   ├── components/    # Reusable UI widgets & cards
│   ├── modals/        # Details, cast profiles, resolution selectors
│   ├── player/        # Custom player composables
│   ├── remote/        # Remote control interface
│   ├── screens/       # Main app screens (Movies, Series, Companies, Saved, Recent)
│   └── theme/         # Color palettes, Typography, and Shapes
├── util/              # Helper utilities and extensions
├── viewmodel/         # Screen ViewModels managing state & business logic
└── MainActivity.kt    # Root activity & navigation entry point
```

---

## 🚀 How to Install & Run

### 📥 Direct Download

If you just want to install and use the app immediately, you can download the latest pre-compiled debug APK from the link below:

👉 **[Download Zvid_v1.0.0.apk](https://github.com/Zead-zoher/Zvid-/releases/download/v1.0.0/Zvid_v1.0.0.apk)**

---

### Building from Source

#### Prerequisites

- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: JDK 17 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 34/35

1. **Clone the repository**:

```
git clone https://github.com/Zead-zoher/Zvid-.git
cd Zvid-
```

2. **Build the Debug APK**:

```
gradle assembleDebug
```

3. **Configure TMDB API Key**:
The app runs instantly using a pre-configured demo key. For unlimited personal usage, obtain your own TMDB API Key:

  - **How to get the API Key**:
    1. Create a free account on [The Movie Database (TMDB)](https://www.themoviedb.org/).
    2. Click your profile picture, then go to **Settings**.
    3. Select the **API** tab from the left sidebar.
    4. Click on **Create** under the API Key request section and choose **Developer**.
    5. Accept terms and fill in a brief application description (e.g., "Zvid Streaming Android App").
    6. Copy your **API Key (v3 auth)**.
  - **How to use it in Zvid**:
    * Open the Zvid app on your device.
    * Tap on the **Settings Gear Icon** in the top-right header to open the settings drawer.
    * Paste your key in the TMDB API Key field and tap **Save Key**.
    * Alternatively, you can edit `com.example.data.local.ApiKeyStore.kt` and replace `DEFAULT_DEMO_KEY` with your key for a permanent default value.

---

## ⚖️ Disclaimer

- **No Advertisements**: This application is 100% free, open-source, and does not contain any advertisements, tracking, or premium paywalls.
- **Media Information**: All movie, TV show, and cast metadata (including titles, descriptions, and posters) are retrieved dynamically using the official TMDB (The Movie Database) API via the API key provided by the user.
- **Video Streams**: This application does not host, upload, or store any media/video files on its servers. It only provides a client interface to play embed links from external streaming providers (specifically [VidSrc Win](https://vidsrc.win/)). Any copyright inquiries or complaints regarding video files should be directed to the third-party providers hosting the actual content.
- **Content Filter**: The content filter greatly reduces exposure to sexual content, but no automated filter is perfect. Please use the in-app Report button to flag anything that gets through.

---

## 📜 License

This project is open source and available under the [MIT License](https://opensource.org/licenses/MIT).
