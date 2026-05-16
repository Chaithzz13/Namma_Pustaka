# Namma-Pustaka (ನಮ್ಮ ಪುಸ್ತಕ) — Smart Rural Library Assistant

An intelligent, offline-first native Android application designed to digitalize school library ecosystems and improve literacy rates across rural educational landscapes. **Namma-Pustaka** bridges socioeconomic constraints by addressing two key regional barriers: **intermittent network access** and **linguistic divides**.

---

## 🚀 Core Features

### 👤 Role-Based Portals (RBAC)
- **Librarian Portal:** A secure dashboard enabling administrators to register students, manage catalog inventory, execute real-time issuance/returns, and analyze borrowing trends.
- **Student Portal:** A personalized digital bookshelf showing actively borrowed books, return thresholds, and historic readings.

### 🔍 Computer Vision QR Circulation
- Integrates an on-device **Google ML Kit Barcode Scanning API** coupled with an Android **CameraX** view preview.
- Replaces legacy hand-written ledger record keeping, trimming down a multiple-minute task into a quick **3-second camera scan**.

### 🤖 Generative AI & Localized Summaries
- Connects with **Google Gemini API** to output context-driven, structural overviews of academic resources.
- Leverages an on-device **Google ML Kit Translation Model** to seamlessly interpret summaries from English into **Kannada**, ensuring high information accessibility for language-medium students.

### 🕒 Proactive Return Watchdog
- Powered by an asynchronous background **Android WorkManager Engine** executing routine checks every 24 hours.
- Handles automated status alerts ("Due Soon", "Due Tomorrow", "Overdue Alert!") utilizing Android's lifecycle-aware `NotificationManager` system.

### 📈 Gamified Leaderboards & Analytics
- Implements aggregate SQL data mapping to highlight the **"Village Top Readers"** based on successful on-time check-ins.
- Generates beautiful native Jetpack Compose charts to keep track of operational library insights like peak hour distributions and high-demand genres.

---

## 🛠️ Technical Architecture & Stack

The system relies strictly on a standardized separation of concerns, avoiding restrictive backend frameworks in favor of native mobile system durability:
+-------------------------------------------------------+
|                Jetpack Compose (UI)                   |
|           Material Design 3 Components                |
+--------------------------+----------------------------+
|  (State & Event Streams)
v
+-------------------------------------------------------+
|                MVVM - ViewModel Layer                 |
|           Kotlin Coroutines / StateFlow Flow          |
+--------------------------+----------------------------+
|  (Clean Access API)
v
+--------------------------+----------------------------+
|                    Repository Layer                   |
+----+---------------------+-----------------------+----+
|                                             |
v                                             v
+----+---------------------+                 +-----+----+----------------+
|  Local Data Layer        |                 | Intelligence Services      |
|  Room DB (SQLite) Cache  |                 | Google Gemini SDK / LLM    |
|  100% Offline Autonomy   |                 | Google ML Kit (Translate)  |
+--------------------------+                 +----------------------------+


- **Language:** 100% Kotlin (utilizing Type-Safety, Coroutines, and Context-Aware Scopes).
- **UI Framework:** Jetpack Compose (Declarative UI Toolkit) + Material Design 3.
- **Local Database:** Room Abstraction Object-Relational Layer over structured SQLite Tables (`books`, `students`, `transactions`, `reviews`).
- **Background Pipeline:** WorkManager API (Persistent Task Scheduling constraints).

---

## 📦 Dependency Breakdown

Key dependencies specified in `app/build.gradle.kts`:
- **Jetpack Room Room Database (`androidx.room`)** — For local schema mappings and persistent offline transactions.
- **Navigation Compose (`androidx.navigation.compose`)** — Safe state-aware view switching between screens.
- **Google ML Kit Barcode Engine (`google.mlkit.barcode.scanning`)** — Native computer-vision item scanning.
- **Google ML Kit Translate Component (`com.google.mlkit:translate`)** — Pre-downloaded neural language packs for absolute offline Kannada localization.
- **WorkManager Runtime (`androidx.work:work-runtime-ktx`)** — Background thread orchestration scheduling.

---

## 🔧 Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/chaithzz13/namma_pustaka.git](https://github.com/chaithzz13/namma_pustaka.git)
   cd namma_pustaka
Open in Android Studio:

Launch Android Studio (Ladybug/Koala or later recommended).

Select Open an Existing Project and navigate to the project directory.

Sync Gradle & Precompile:

Allow the IDE to process build.gradle.kts layout dependencies through Gradle.

Project targets Compile SDK 34 with a minimal deployment constraint of API 26 (Android 8.0).

Run the Project:

Connect an Android device with camera hardware or boot up an Emulator.

Select app target configuration and click Run (Shift + F10).

📂 Code Directory Layout
Plaintext
app/src/main/java/com/example/manoj/
 ├── data/
 │    ├── Entities.kt            # Room DB Tables (Book, Student, Transactions, Reviews)
 │    ├── LibraryDao.kt          # Structured SQL Data Access Queries
 │    └── LibraryDatabase.kt     # SQLite Database Setup & Migration Handling
 ├── ui/
 │    ├── screens/
 │    │    ├── HomeScreen.kt     # Search catalog view & borrowed items status counters
 │    │    ├── ScannerScreen.kt  # Camera view capturing QR check-outs
 │    │    ├── BookDetail.kt     # Book specific specifications & Gemini localization translations
 │    │    ├── Leaderboard.kt    # Village top readers gamification scoring view
 │    │    └── InsightsScreen.kt # High-fidelity librarian analytics trends bar graphs
 │    └── theme/                 # Modern Material 3 style colors & configurations
 ├── utils/
 │    └── TranslatorUtils.kt     # Download management for ML Kit Translation Engines
 ├── worker/
 │    └── DeadlineWorker.kt      # Automated background time computation alerts logic
 └── MainActivity.kt             # Application Navigation Architecture & Manifest Entry Hook
📄 License & Affiliations
Developed as a credit-bearing engineering project under the structured training programs of MindMatrix, Bangalore, in coordination with Visvesvaraya Technological University (VTU) guidelines.


---

### 💡 To apply this file via Git:
Once you have created the file and pasted the above content, you can commit it to your project history using the standard Git commands we reviewed earlier:
```bash
git add README.md
git commit -m "docs: add comprehensive, professional project README"
git push origin main
