package com.eslamdev.islamic.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.eslamdev.islamic.data.model.Day
import com.eslamdev.islamic.data.model.Month
import com.eslamdev.islamic.data.model.PrayerRepository
import com.eslamdev.islamic.data.model.PrayerTimingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerHomeViewModel(private val repository: PrayerRepository) : ViewModel() {

    private val _prayerState = MutableStateFlow<PrayerState>(PrayerState.Idle)
    val prayerState: StateFlow<PrayerState> = _prayerState

    private val _nextPrayer = MutableStateFlow("")
    val nextPrayer: StateFlow<String> = _nextPrayer

    private val _timeLeft = MutableStateFlow("")
    val timeLeft: StateFlow<String> = _timeLeft

    private val _prayerProgress = MutableStateFlow(0)
    val prayerProgress: StateFlow<Int> = _prayerProgress

    private var timerJob: Job? = null
    private var currentPrayerTimes: PrayerTimes? = null

    private var savedCoordinates: Coordinates? = null
    private var savedParams: CalculationParameters? = null

    fun getMonthlyPrayerData(year: Int, month: Int, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _prayerState.value = PrayerState.Loading
            try {
                savedCoordinates = Coordinates(latitude, longitude)
                val params = CalculationMethod.EGYPTIAN.parameters
                params.madhab = Madhab.SHAFI
                savedParams = params

                val daysList = mutableListOf<Day>()
                val calendar = Calendar.getInstance()
                val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale("ar"))

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                val monthName = monthFormatter.format(calendar.time)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                for (day in 1..daysInMonth) {
                    val dateComponents = DateComponents(year, month, day)
                    val prayerTimes = PrayerTimes(savedCoordinates, dateComponents, params)

                    val today = Calendar.getInstance()
                    if (day == today.get(Calendar.DAY_OF_MONTH) && month == (today.get(Calendar.MONTH) + 1)) {
                        currentPrayerTimes = prayerTimes
                        startTimer()
                    }

                    val timingEntity = PrayerTimingEntity(
                        date = "$day-$month-$year",
                        fajr = formatTime(prayerTimes.fajr),
                        sunrise = formatTime(prayerTimes.sunrise),
                        dhuhr = formatTime(prayerTimes.dhuhr),
                        asr = formatTime(prayerTimes.asr),
                        maghrib = formatTime(prayerTimes.maghrib),
                        isha = formatTime(prayerTimes.isha)
                    )

                    val isToday = (day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    daysList.add(Day(day, getDayName(day, month, year), timingEntity, isToday))
                }

                _prayerState.value = PrayerState.Success(Month(monthName, daysList))

            } catch (e: Exception) {
                _prayerState.value = PrayerState.Error(e.message ?: "حدث خطأ غير متوقع")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && currentPrayerTimes != null) {
                updateNextPrayerInfo()
                delay(1000)
            }
        }
    }

    private fun updateNextPrayerInfo() {
        val prayerTimes = currentPrayerTimes ?: return
        var nextPrayerType = prayerTimes.nextPrayer()
        var nextPrayerTime = prayerTimes.timeForPrayer(nextPrayerType)

        var currentPrayerTimeForProgress: Date? = prayerTimes.timeForPrayer(prayerTimes.currentPrayer())

        if (nextPrayerType == Prayer.NONE || nextPrayerTime == null) {
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowDate = DateComponents.from(tomorrow.time)

            if (savedCoordinates != null && savedParams != null) {
                val tomorrowPrayerTimes = PrayerTimes(savedCoordinates, tomorrowDate, savedParams)
                nextPrayerTime = tomorrowPrayerTimes.fajr
                nextPrayerType = Prayer.FAJR

                currentPrayerTimeForProgress = prayerTimes.isha
            }
        }

        if (nextPrayerTime != null) {
            val now = System.currentTimeMillis()
            val diff = nextPrayerTime.time - now

            if (diff > 0) {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

                val timeLeftFormatted = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)

                _timeLeft.value = convertToEasternArabic(timeLeftFormatted)
                _nextPrayer.value = getArabicPrayerName(nextPrayerType)

                if (currentPrayerTimeForProgress != null) {
                    val totalTime = nextPrayerTime.time - currentPrayerTimeForProgress.time
                    val timePassed = now - currentPrayerTimeForProgress.time
                    val progress = ((timePassed.toDouble() / totalTime.toDouble()) * 100).toInt()
                    _prayerProgress.value = progress.coerceIn(0, 100)
                }
            } else {
                _timeLeft.value = "00:00:00"
            }
        }
    }

    private fun formatTime(date: Date): String {
        return SimpleDateFormat("hh:mm a", Locale("ar")).format(date)
    }

    private fun getDayName(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("ar")) ?: ""
    }

    private fun getArabicPrayerName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "صلاة الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "صلاة الظهر"
            Prayer.ASR -> "صلاة العصر"
            Prayer.MAGHRIB -> "صلاة المغرب"
            Prayer.ISHA -> "صلاة العشاء"
            else -> "صلاة الفجر" // في حالة الليل المتأخر
        }
    }

    private fun convertToEasternArabic(numberString: String): String {
        val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (char in numberString) {
            if (char.isDigit()) {
                builder.append(arabicNumbers[Character.getNumericValue(char)])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    fun getPrayerTimingForSpecificDay(day: Int, month: Int, year: Int, context: Context, onResult: (PrayerTimingEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val lat = prefs.getFloat("LATITUDE", 30.0444f).toDouble()
            val lon = prefs.getFloat("LONGITUDE", 31.2357f).toDouble()

            val monthTimings = repository.getPrayerTimingsForMonth(month, year, lat, lon)
            val timing = if (day <= monthTimings.size) monthTimings[day - 1] else null

            withContext(Dispatchers.Main) {
                onResult(timing)
            }
        }
    }
}