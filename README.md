# 🕌 Muslim Companion - Prayer Times, Quran & Azkar

**Muslim Companion** is a comprehensive Islamic application designed to be a daily assistant for Muslims worldwide. It combines accurate prayer times, Qibla direction, the Holy Quran, and daily Azkar in a modern, user-friendly interface that supports both Light and Dark modes.

## ✨ Features

* **📍 Accurate Prayer Times:**
    * Precise calculations based on user location (GPS or Manual City Selection).
    * Supports multiple calculation methods (Egyptian General Authority, Umm Al-Qura, MWL, etc.).
    * Adjustable Asr calculation (Shafi/Hanbali/Maliki vs Hanafi).

* **📢 Adhan Notifications:**
    * Customizable notifications for each prayer.
    * Beautiful Adhan sounds from Mecca, Medina, and Cairo (Sheikh Abdul Basit).
    * Ability to enable/disable specific prayer alerts.

* **🕋 Smart Qibla Compass:**
    * Real-time Qibla direction using device sensors (Accelerometer & Magnetometer).
    * **Smooth Animation** with Low-Pass Filter for stability.
    * Haptic feedback (Vibration) and visual indication when aligned with the Qibla.

* **📖 Holy Quran:**
    * Read Surahs with clear Uthmanic font.
    * Rich text formatting with Tajweed-friendly rendering.
    * Night mode support for comfortable reading.

* **📿 Daily Azkar:**
    * Morning, Evening, and Post-Prayer Azkar.
    * Counter and organized categories.

* **📅 Hijri Calendar:**
    * Display current Hijri and Gregorian dates.

* **🎨 Modern UI/UX:**
    * Clean Material Design.
    * **Dark Mode** fully supported for all screens.
    * Neumorphic touches and smooth transitions.

## 🛠 Technologies & Technical Details

This project is built using **Native Android (Kotlin)**, following the **Clean Architecture** principles and **MVVM** pattern to ensure scalability, testability, and maintainability.

### 🏗 Architecture & Design Patterns
* **MVVM (Model-View-ViewModel):** Used to separate the UI (Activity/XML) from the business logic and data operations. The `ViewModel` handles data processing and exposes it via `LiveData`/`StateFlow` to the UI.
* **Repository Pattern:** Acts as a single source of truth, mediating between local data (Database/SharedPrefs) and remote data (if added later), ensuring the ViewModel doesn't need to know where data comes from.
* **Singleton Pattern:** Used for database instances (`Room`) and Retrofit clients to ensure resource efficiency.

### 💻 Core Technologies
* **Kotlin:** The primary programming language, utilizing features like **Null Safety**, **Extension Functions**, and **Data Classes** for concise and robust code.
* **Coroutines & Flow:** Used for handling background tasks (like database queries and loading Quran HTML content) asynchronously without blocking the Main Thread, ensuring a smooth UI experience.
* **Android Architecture Components:**
    * **ViewModel:** To manage UI-related data in a lifecycle-conscious way.
    * **LiveData:** To observe data changes and update the UI automatically.

### 💾 Data & Storage
* **Room Database:** Used to persist user data and cache prayer times locally for offline access.
* **SharedPreferences:** Used to store lightweight user preferences like Selected City, Calculation Method, Madhab, and Dark Mode state.
* **JSON Parsing:** Used to parse the Quran structure and verses from local JSON assets to display Surah details.

### 📍 Location & Sensors
* **Google Play Services Location:** To fetch the user's precise Latitude and Longitude for accurate prayer time calculation (GPS).
* **Android SensorManager:**
    * **Accelerometer & Magnetometer:** Used to calculate the device's orientation relative to the magnetic north.
    * **Low-Pass Filter Algorithm:** Implemented to smooth out sensor data and prevent the compass needle from shaking/jittering.

### ⏰ Scheduling & Notifications
* **AlarmManager:** Used to schedule exact alarms for future prayer times even if the app is killed.
* **BroadcastReceiver:** Acts as the entry point for the alarm to trigger the Adhan notification and play the sound in the background.
* **NotificationChannel:** Created to handle high-priority Adhan alerts with custom sounds (Mecca, Cairo, Medina).

### 🎨 UI & UX
* **XML Layouts:** Responsive designs using `ConstraintLayout` to support various screen sizes.
* **Material Design Components:** Used for Cards, Buttons, and Ripple effects.
* **Lottie Animations:** Used in the Splash Screen for a high-quality, vector-based loading animation.
* **WebView:** Used to render the Holy Quran text with complex Arabic typography and decorative HTML/CSS styling.

### 🧮 Libraries
* **BatoulApps/Adhan:** A specialized astronomical library used to calculate accurate prayer times and Qibla direction based on the user's coordinates and date.

---

## 📸 App Screenshots

Here is a visual tour of the application features (Light & Dark Mode):

| Splash Screen | Home (Light) | Home (Dark) | Drawer/Menu | Qibla Compass |
|:---:|:---:|:---:|:---:|:---:|
| <img src="screenshots/1.png" width="160"/> | <img src="screenshots/2.png" width="160"/> | <img src="screenshots/3.png" width="160"/> | <img src="screenshots/4.png" width="160"/> | <img src="screenshots/5.png" width="160"/> |

| Quran List | Surah Detail | Quran (Dark) | Azkar Home | Morning Azkar |
|:---:|:---:|:---:|:---:|:---:|
| <img src="screenshots/6.png" width="160"/> | <img src="screenshots/7.png" width="160"/> | <img src="screenshots/8.png" width="160"/> | <img src="screenshots/9.png" width="160"/> | <img src="screenshots/10.png" width="160"/> |

| Prayer Times | Settings (Sound) | Settings (Loc) | Location (Manual) | Permission Req |
|:---:|:---:|:---:|:---:|:---:|
| <img src="screenshots/11.png" width="160"/> | <img src="screenshots/12.png" width="160"/> | <img src="screenshots/13.png" width="160"/> | <img src="screenshots/14.png" width="160"/> | <img src="screenshots/15.png" width="160"/> |

| Notification | Widget (Optional) | | | |
|:---:|:---:|:---:|:---:|:---:|
| <img src="screenshots/16.png" width="160"/> | <img src="screenshots/17.png" width="160"/> | | | |

---

## ⚙️ Configuration

The app allows users to customize:
* **Location Method:** Automatic (GPS) or Manual (State/City selection).
* **Calculation Method:** Choose from various global standards.
* **Madhab:** Standard or Hanafi.
* **Adhan Voice:** Select preferred Muezzin.
---
**Developed with ❤️ by [Eslam]**
