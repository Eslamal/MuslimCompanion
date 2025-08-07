# 🕋 Muslim Companion 

An open-source, all-in-one Islamic Android application designed to be the daily companion for every Muslim. The app provides a suite of tools and features to help users perform their daily worship with ease, featuring a modern, flexible, and interactive user interface.

## ✨ Core Features

- **Smart Dashboard:**
  - Displays accurate **Prayer Times** based on the user's location.
  - A **live countdown** for the time remaining until the next prayer.
  - Shows both Hijri and Gregorian dates.
  - **Renewable Daily Content:** A new Hadith, Aya (Verse), and Dua are randomly displayed every day.

- **The Holy Quran:**
  - Read all Surahs of the Holy Quran with a clean interface.
  - A dedicated section for the Tafseer (interpretation) of each Surah.

- **Hadith & Supplications:**
  - A complete library of Prophetic Hadiths with explanations.
  - "Hisn Al-Muslim" section for categorized daily Duas (supplications).
  - Morning (Sabah), Evening (Massa), and after-prayer Azkar.

- **Additional Tools:**
  - **Qibla Direction** compass using the device's sensors.
  - A smart electronic **Tasbeeh (Rosary)** that saves your counts.
  - **Swipe to Refresh** functionality to manually update all data.

- **Full Customization:**
  - Full support for both **Arabic and English** with automatic Right-to-Left (RTL) / Left-to-Right (LTR) layout switching.
  - Ability to switch between **Light and Dark Mode**.
  - User's selected language and theme preferences are saved and applied on every launch.
    

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
