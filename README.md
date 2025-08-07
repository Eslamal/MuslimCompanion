# 🕋 Muslim Companion 

An open-source, all-in-one Islamic Android application designed to be the daily companion for every Muslim. The app provides a suite of tools and features to help users perform their daily worship with ease, featuring a modern, flexible, and interactive user interface.

## ✨ Key Features

-   📖 **Full Holy Quran:**
    -   Crystal-clear Uthmanic Hafs font.
    -   **Reading Modes:** Switch between Light, Sepia (for eye comfort), and Dark themes.
    -   **Bookmarks:** Save your reading position to return to it later.

-   🕌 **Accurate Prayer Times:**
    -   Calculates prayer times precisely based on your location.
    -   Works completely **offline** after the initial location setup.

-   🧭 **Qibla Compass:**
    -   A simple and accurate compass to find the direction of the Kaaba.

-   📿 **Azkar & Duas:**
    -   A comprehensive collection of Azkar (morning, evening, after-prayer).
    -   A curated list of essential Duas (supplications) for various occasions.

-   📜 **Hadith Collection:**
    -   A browsable collection of authentic Hadith.

-   🔢 **Digital Tasbeeh:**
    -   A simple counter to help you keep track of your dhikr.

-   **Core Principle: 100% Offline Functionality**
    -   All content (Quran, Hadith, Azkar) is bundled within the app. Prayer times are calculated locally. No internet required for core features.

    

## 🛠️ Tech Stack

- **Languages:** Kotlin & Java.
- **Architecture:** MVVM (Model-View-ViewModel).
- **UI:** Android XML, `ConstraintLayout`, `RecyclerView`, `CardView`, `SwipeRefreshLayout`.
- **Jetpack Libraries:**
  - `ViewModel`: To manage UI-related data in a lifecycle-conscious way.
  - `LiveData`: To observe data changes and automatically update the UI.
- **Other Libraries:**
  - **Adhan for Android:** For accurate prayer time calculations.
  - **Google Play Services (Location):** To determine the user's location.
