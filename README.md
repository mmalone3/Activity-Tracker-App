# Activity Tracker ⏱️

An Android application built with Kotlin, Jetpack Compose, Room Database, and Material 3 design patterns. **Activity Tracker** enables users to track session durations across work, play, sleep, and custom categories, providing live timer displays and cumulative history analytics.

---

## 🌟 Key Features

- **Live Activity Tracking**: Real-time timer display (`00:00:00`) tracking active sessions with automated state preservation.
- **Dynamic Category Management**:
  - Pre-configured categories (*Work*, *Play*, *Sleep*).
  - Add custom categories dynamically.
  - Interactive close **(x)** icon on custom category chips for easy removal.
- **Full Activity History Page**:
  - Dedicated History page displaying tracked entries.
  - **Category Summaries**: Aggregates total session count and cumulative duration per category.
  - **Clean Duration Formatting**: Formatted time strings (e.g. `14m 20s`, `1h 12m 5s`).
  - **Deletable Logs**: Tap trash icon to delete individual history entries.
  - Smooth back navigation (**← Back to Tracker** button & system back affordances).
- **Offline Room Persistence**: Local database storage using Room with KSP code generation and Flow real-time updates.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.1.0
- **UI Toolkit**: Material 3 & Jetpack Compose (Interop via ComposeView)
- **Database**: Room Database 2.6.1 + KSP (`com.google.devtools.ksp`)
- **Concurrency**: Kotlin Coroutines & `Flow`
- **Build System**: Android Gradle Plugin 8.9 + Gradle 9.0
- **Testing**: JUnit 4 (22+ unit tests covering formatting, tracking logic, and entity data holders)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 / JDK 21
- Android SDK 35/37 (Min SDK 24)

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ActivityTracker.git
   cd ActivityTracker
   ```

2. Build Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

3. Run Unit Tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. Build Release App Bundle (AAB):
   ```bash
   ./gradlew bundleRelease
   ```

---

## 📁 Project Structure

```text
app/src/main/java/com/amica/activitytracker/
├── MainActivity.kt               # Main dashboard with live timer & category bar
├── HistoryActivity.kt            # History page with category totals & record logs
├── ActivityTrackerLogic.kt       # Core business logic & SharedPreferences state manager
├── TrackedSession.kt             # Data model & duration formatting helper
├── SessionAdapter.kt             # RecyclerView adapter for recent sessions
├── HistoryAdapter.kt             # RecyclerView adapter for detailed history logs
└── CategorySummaryAdapter.kt    # RecyclerView adapter for category totals breakdown
```

---

## 📄 Package & Identification

- **Package Name**: `com.amica.activitytracker`
- **Application ID**: `com.amica.activitytracker`