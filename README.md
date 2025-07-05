About The Project
The "Muslim Companion" project aims to provide a comprehensive, reliable, and user-friendly mobile application. The app is built to be a daily companion for Muslims, combining essential functionalities like Quran reading, prayer times, and Azkar in one place. It makes most of its content available offline to ensure accessibility anytime, anywhere.

✨ Key Features
📖 Holy Quran (Offline)

Full Surah index for easy navigation.

Clear, high-quality Quranic text using a custom Uthmanic font.

Dark mode support for a comfortable reading experience at night.

📿 Azkar & Tasbeeh (Offline)

Morning, Evening, and Post-Prayer remembrances.

Repetition counter for each Zikr to help you keep track.

📚 Hadith (Offline)

A library of Hadith from authentic sources (Bukhari, Muslim, etc.).

🕌 Prayer Times & Adhan

Accurate prayer time calculations based on your location.

Notification for the upcoming prayer and the time remaining.

Monthly calendar view for prayer timings.

🧭 Qibla Direction

An accurate compass to determine the Qibla direction using device sensors.


Tech Stack & Architecture
The app is built using modern, Google-recommended best practices to ensure high performance and a maintainable codebase.

Programming Languages: Kotlin (primary) and Java.

Architecture: MVVM (Model-View-ViewModel).

Jetpack Libraries:

ViewModel: To manage UI-related data in a lifecycle-conscious way.

LiveData: For building observable, data-driven UIs.

RecyclerView: For displaying large lists efficiently.

ViewBinding: For safer interaction between code and XML layouts.

Asynchronous Programming: Kotlin Coroutines for managing background tasks smoothly.

Networking: Retrofit & OkHttp for fetching data from the internet (e.g., prayer times).

Data Source:

Local JSON Files: For storing Quran, Hadith, and Azkar data (works offline).

Remote API: For fetching dynamic data (e.g., prayer times from api.aladhan.com).
