# 🎬 Zvid - Modern Movies & TV Series Android Application

**Zvid** is a feature-rich, modern Android media streaming and exploration application built with **Kotlin** and **Jetpack Compose**. It leverages the TMDB API to provide seamless browsing of movies, TV series, actor filmographies, production studio catalogs, and embedded streaming playback.

---

## 🌟 Features

- 🎬 **Movies & TV Shows Catalog**: Explore popular, top-rated, and trending movies and TV series with rich category filtering (including regional collections like EU & Lat).
- 🏢 **Production Companies Catalog**: Browse full media libraries from major production studios and companies without limit restrictions.
- 🎭 **Actor & Cast Profiles**: Deep dive into actors' bios, full filmographies, and media appearances with smooth backstack navigation.
- ⏯️ **Integrated Media Player**: Custom AndroidX Media3 / ExoPlayer integration supporting custom stream selection, quality overlays, and playback control interface.
- 🔖 **Saved Watchlist & Playback History**: Keep track of bookmarked movies/shows and pick up where you left off with recent history.
- 📺 **Web Video Caster Support**: Stream video links directly to your Smart TV or casting devices with seamless Web Video Caster integration.
- 🎨 **Material 3 Dark UI**: Modern, sleek dark theme with fluid animations, adaptive layouts, and responsive components.

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

## 🚀 How to Build & Run

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: JDK 17 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 34/35

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/zvid.cmd.git
   cd zvid
   ```

2. **Build the Debug APK**:
   ```bash
   gradle assembleDebug
   ```

3. **Configure TMDB API Key / إعداد مفتاح API الخاص بـ TMDB**:
   The app runs instantly using a pre-configured demo key. For unlimited personal usage, obtain your own TMDB API Key:
   - **How to get the API Key (كيفية الحصول على المفتاح)**:
     1. Create a free account on [The Movie Database (TMDB)](https://www.themoviedb.org/).
     2. Go to your **Account Settings** (اضغط على صورة حسابك ثم Settings).
     3. Select the **API** tab from the left sidebar.
     4. Click on **Create** under the API Key request section and choose **Developer** (مطور).
     5. Accept terms and fill in a brief application description (e.g., "Zvid Streaming Android App").
     6. Copy your **API Key (v3 auth)**.
   - **How to use it in Zvid (كيفية استخدام المفتاح في التطبيق)**:
     - Open Zvid app on your device.
     - Tap on the **Settings Gear Icon** (أيقونة الترس/الإعدادات) in the top-right header to open the settings drawer.
     - Paste your key in the TMDB API Key field and tap **Save Key / حفظ المفتاح**.
     - Alternatively, you can edit `com.example.data.local.ApiKeyStore.kt` and replace `DEFAULT_DEMO_KEY` with your key for a permanent default value.

---

## 📄 Arabic Summary / ملخص المشروع

**Zvid** هو تطبيق أندرويد حديث لمشاهدة وتصفح الأفلام والمسلسلات مع واجهة أنيقة باللون الداكن (Dark Mode) مبني باستخدام **Kotlin** و **Jetpack Compose**.

### أهم المميزات:
- 🎬 **تصفح شامل للأفلام والمسلسلات**: تصنيفات متنوعة مع دعم فلترة المناطق (مثل EU & Lat).
- 🏢 **عرض أعمال شركات الإنتاج**: تصفح كل أفلام ومسلسلات الشركات بدون قيود.
- 🎭 **صفحات الممثلين وفريق العمل**: استعراض الأعمال الكاملة للممثلين والتنقل السلس بين الصفحات.
- ⏯️ **مشغل فيديو متكامل**: مشغل مدمج تدعم تغيير الجودة والسيرفرات والتحكم في التشغيل.
- 📺 **دعم Web Video Caster**: إمكانية إرسال وبث روابط الفيديو مباشرة إلى التلفزيون الذكي عبر تطبيق Web Video Caster بسلاسة.
- 🔖 **قائمة الحفظ والسجل**: حفظ الأعمال المفضلة وسجل المشاهدات الأخيرة.

---

## 📜 License

This project is open source and available under the [MIT License](https://opensource.org/licenses/MIT).
