🕌 Muslim Companion App
Muslim Companion is a comprehensive, open-source Islamic application for Android, designed to be the daily assistant for every Muslim. The app provides an integrated suite of tools and features for daily worship and life, with a focus on ease of use and offline support for many core functionalities.

✨ Key Features
Holy Quran (Offline):

Complete index of all Surahs in the Holy Quran.

Read Surahs with high-quality text rendered using the authentic Uthmanic Hafs font for the best reading experience.

A dedicated reader screen that supports both light and dark modes for eye comfort.

Data is loaded from local JSON files (Quran.json, QuranDetails.json) packaged with the app.

Azkar & Hisn Al-Muslim (Offline):

A complete collection of daily remembrances (Azkar), including:

Morning Azkar (Remembrances).

Evening Azkar (Remembrances).

Azkar after prayer.

Displays the repetition count for each Zikr to help the user keep track.

Hadith (Offline):

A library of Prophetic Hadiths from major books like Sahih al-Bukhari and Sahih Muslim.

Hadith data is loaded from a local JSON file (file.json).

Prayer Times:

Accurate display of the five daily prayer times based on the user's geographic location.

Identifies the next prayer and displays the time remaining for it.

A calendar interface allows users to browse prayer times for different days of the month.

Data is fetched online from the api.aladhan.com API.

Qibla Direction:

An accurate compass to determine the direction of the Qibla using the device's sensors and location.

A graphical interface that shows the Qibla direction relative to the device's current orientation.

🏛️ App Architecture & Technical Details
The application is built following the MVVM (Model-View-ViewModel) architecture to ensure organized code, separation of concerns, and to facilitate future maintenance and development.

Programming Languages: The app is developed using a mix of Kotlin for modern features and Java for some legacy components.

Data Source: The app uses a hybrid model:

Local Data: Quran, Azkar, and Hadith data are stored in JSON files within the app's assets folder. This ensures that these core features work perfectly offline.

Remote Data: Prayer times are fetched dynamically from an external API (api.aladhan.com) using the Retrofit library.

State & Data Management: ViewModel and LiveData are used to efficiently manage UI-related data and its state.

Location Services:

For Prayer Times: FusedLocationProviderClient is used to get the user's precise location.

For Qibla: LocationManager is used in conjunction with device sensors (ACCELEROMETER, MAGNETIC_FIELD).

🛠️ Technologies & Libraries Used
Languages: Kotlin & Java

Architecture: MVVM (ViewModel, LiveData, Repository)

Android Jetpack:

ViewModel, LiveData

RecyclerView, CardView

ViewBinding

Asynchronous Programming: Kotlin Coroutines

Networking: Retrofit2 & OkHttp3

Database: Room (for caching or future storage of prayer data).

UI:

WebView for rendering Quran content with HTML/CSS.

Dark Mode Support.
