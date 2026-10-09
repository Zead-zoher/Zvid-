# Zvid - Movies & TV Series Explorer

🌐 **Language / اللغة:** [العربية (Arabic)](README.ar.md) | **[Download APK](https://github.com/Zead-zoher/Zvid-/releases/download/v1.0.0/Zvid_v1.0.0.apk)**

**License:** MIT | **Min SDK:** Android 7.0 (API 24) | **Language:** Kotlin | **Ads:** None | **Streaming Server:** vidsrc.win

Zvid is an elegant, ad-free Android media exploration and streaming client built with Kotlin and Jetpack Compose. It allows users to browse movies, TV shows, production studio catalogs, and actors, with seamless embedded playback powered by AndroidX Media3/ExoPlayer and external cast support.

---

## Table of Contents
1. [Features](#features)
2. [Architecture & Data Flow](#architecture--data-flow)
3. [Directory Structure](#directory-structure)
4. [Prerequisites & Build](#prerequisites--build)
5. [TMDB API Key Setup](#tmdb-api-key-setup)
6. [Roadmap](#roadmap)
7. [Contributing](#contributing)
8. [Known Issues](#known-issues)
9. [Disclaimer](#disclaimer)
10. [License](#license)

---

## Features

Zvid offers a high-performance experience with real-time metadata exploration and integrated playback features. Below is the implementation status of key features:

| Feature | Description | Status |
| :--- | :--- | :--- |
| **Discover / Browse Screen** | Detailed browsing of trending, popular, and top-rated movies & TV shows with advanced filters (Genre, Release Year, Language). | Completed |
| **Media Detail Screen** | Comprehensive views of media containing synopses, user ratings, release dates, runtime, studio portfolios, cast member list, and season/episode selectors. | Completed |
| **Actor Profiles** | In-depth actor biographies, personal details (birthplace, birthday, popularity), and scrollable filmographies with smooth backstack navigation. | Completed |
| **Studio Catalogs** | Exploration of works belonging to major production companies (e.g., Marvel, Disney) with release-year filters. | Completed |
| **Embedded ExoPlayer Playback** | Dynamic stream resolution selector (4K, 1080p, 720p, 480p) playing public web streams via an integrated AndroidX Media3/ExoPlayer with speed control. | Completed |
| **Web Video Caster** | Direct casting of active stream links to external Smart TVs and casting devices using the Web Video Caster app. | Completed |
| **Watchlist & History** | Local data persistence for personal bookmarked watchlists and recent playback history. | Completed |
| **Search Functionality** | Direct real-time search of movies, TV shows, and production companies across the TMDB network. | Completed |
| **Subtitles Engine** | Automated subtitle search and local subtitle rendering. | Under Development |
| **Offline Download Manager** | Local caching and offline video downloads. | Under Development |

---

## Architecture & Data Flow

Zvid is built using modern Android development practices, adhering to the MVVM (Model-View-ViewModel) architectural pattern coupled with Unidirectional Data Flow (UDF). This architecture decouples data fetching and business logic from the UI layer, facilitating easy maintainability and testing.

### Data Flow Process
1. **Repository & Remote Sources**: Raw network data is fetched from the TMDB API using Retrofit. The Repository acts as a single source of truth, converting network data Transfer Objects (DTOs) into clean, presentation-ready Domain Models.
2. **ViewModel & StateFlow**: The ViewModel manages state by making asynchronous calls to the Repository within Kotlin Coroutines. The results are transformed and exposed as read-only, lifecycle-aware `StateFlow` states to prevent unnecessary state mutations.
3. **Compose UI**: Jetpack Compose UI screens collect the exposed `StateFlow` states safely using `collectAsStateWithLifecycle()`. Any UI interaction is emitted back to the ViewModel as an event, keeping data flow completely unidirectional.

---

## Directory Structure

```
app/src/main/java/com/example/
├── data/              # Remote API services, local repositories, & API Key management
├── player/            # ExoPlayer / Media3 setup, controllers, and caster integrations
├── ui/                # UI Layer
│   ├── components/    # Reusable widgets (cards, grids, sliders)
│   ├── modals/        # Resolution selectors, cast info bottom sheets
│   ├── screens/       # Views (Movies, Series, Companies, Saved, History)
│   └── theme/         # Material 3 typography, colors, and shapes
└── viewmodel/         # ViewModels managing state & reactive StateFlows
```

---

## Prerequisites & Build

**Prerequisites:** JDK 17 | Android SDK API 24+ (Min SDK 24, Target SDK 34/35) | Android Studio (Ladybug or newer)

To build the project locally, run the following commands in your terminal:

```bash
# Clone the repository
git clone https://github.com/Zead-zoher/Zvid-.git
cd Zvid-

# Build the Debug APK using Gradle wrapper
./gradlew assembleDebug # macOS/Linux
gradlew.bat assembleDebug # Windows
```

---

## TMDB API Key Setup

Zvid runs immediately with a temporary key. To use your own TMDB API key:

1. Register for a free account at [The Movie Database (TMDB)](https://www.themoviedb.org/).
2. Navigate to your **Account Settings** -> **API** in the left sidebar menu.
3. Request an API Key as a "Developer" and accept terms.
4. Copy the **API Key (v3 auth)**.
5. In the Zvid app, tap the **Settings Gear Icon** (top-right), paste your key, and click **Save Key**.

---

## Configuration (Telegram Reporting)

The app includes an in-app report feature that dispatches user reports directly to a configured Telegram bot. To configure the Telegram integration:

- **Local Builds**: Add the following to your untracked `local.properties`:
  ```properties
  TELEGRAM_BOT_TOKEN=your_bot_token_here
  TELEGRAM_CHAT_ID=your_chat_id_here
  ```
- **GitHub Actions**: Add the repository secrets under **Settings > Secrets and variables > Actions**:
  - `TELEGRAM_BOT_TOKEN`
  - `TELEGRAM_CHAT_ID`

If not configured, the app builds normally and shows a friendly notice if a report is submitted.

---

## Roadmap

Upcoming features and improvements planned for Zvid:
- [ ] Add support for multiple subtitle tracks (SRT/VTT).
- [ ] Optimize Picture-in-Picture (PiP) mode for TV layouts.
- [ ] Implement custom user-created folders and custom media lists.
- [ ] Enable offline media downloads.

---

## Contributing

Contributions are welcome! If you'd like to improve Zvid, feel free to fork the repository, make your changes on a separate feature branch, and submit a Pull Request.

---

## Known Issues

- Stream load speed and playback stability depend directly on the external streaming server's bandwidth and uptime.

---

## Disclaimer

- **Third-Party Providers**: This application does not host, upload, or store any video files on its servers. All video streams are resolved dynamically from third-party public web servers, specifically [vidsrc.win](https://vidsrc.win/). The end-user is solely responsible for any content accessed through this client.
- **Metadata & TMDB API**: All metadata, poster images, actor bio details, and studio information are fetched from the official TMDB API. This client is not officially endorsed or certified by TMDB.
- **No Ads**: Zvid contains no advertisements, trackers, or monetization features. It is built as a non-commercial, open-source educational project.

---

## License

This project is licensed under the MIT License - see the [LICENSE](https://opensource.org/licenses/MIT) file for details.
