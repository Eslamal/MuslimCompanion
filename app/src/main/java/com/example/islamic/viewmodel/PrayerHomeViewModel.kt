package com.example.islamic.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.islamic.model.Day
import com.example.islamic.model.Month
import com.example.islamic.model.PrayerTimingEntity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerHomeViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData
    private val _monthData = MutableLiveData<Month>()
    val monthData: LiveData<Month> get() = _monthData

    private val _nextPrayer = MutableLiveData<String>()
    val nextPrayer: LiveData<String> get() = _nextPrayer

    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String> get() = _timeLeft

    private val _prayerProgress = MutableLiveData<Int>()
    val prayerProgress: LiveData<Int> get() = _prayerProgress

    // متغيرات لحفظ الحالة الحالية
    private var currentPrayerTimes: PrayerTimes? = null
    private var lastKnownCoordinates: Coordinates? = null
    private var lastKnownParams: com.batoulapps.adhan.CalculationParameters? = null

    // عداد لايف
    private val timerHandler = Handler(Looper.getMainLooper())
    private lateinit var timeUpdater: Runnable

    // دالة مخصصة للشاشة الرئيسية MainActivity
    fun calculateAndStartLiveTimer(latitude: Double, longitude: Double) {
        lastKnownCoordinates = Coordinates(latitude, longitude)
        lastKnownParams = CalculationMethod.EGYPTIAN.parameters.apply { madhab = Madhab.SHAFI }

        val calendar = Calendar.getInstance()
        val dateComponents = DateComponents.from(calendar.time)
        currentPrayerTimes = PrayerTimes(lastKnownCoordinates!!, dateComponents, lastKnownParams!!)
        updateNextPrayerInfo()
    }

    // دالة مخصصة لشاشة PrayerActivity
    fun getMonthlyPrayerData(year: Int, month: Int, latitude: Double, longitude: Double) {
        val coordinates = Coordinates(latitude, longitude)
        val params = CalculationMethod.EGYPTIAN.parameters.apply { madhab = Madhab.SHAFI }

        val daysInMonth = mutableListOf<Day>()
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.set(year, month - 1, 1)
        val numDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (dayOfMonth in 1..numDaysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateComponents = DateComponents.from(calendar.time)
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

            val formatter = SimpleDateFormat("hh:mm a", Locale("ar"))
            val timingEntity = PrayerTimingEntity(
                date = "$dayOfMonth-$month-$year",
                fajr = formatter.format(prayerTimes.fajr),
                sunrise = formatter.format(prayerTimes.sunrise),
                dhuhr = formatter.format(prayerTimes.dhuhr),
                asr = formatter.format(prayerTimes.asr),
                maghrib = formatter.format(prayerTimes.maghrib),
                isha = formatter.format(prayerTimes.isha)
            )

            val dayOfWeekEn = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
            val isTodayFlag = (dayOfMonth == today && month == currentMonth && year == currentYear)

            daysInMonth.add(Day(dayOfMonth, dayOfWeekEn, timingEntity, isTodayFlag))

            // *** بداية التعديل: إذا كان اليوم هو اليوم الحالي، قم بتشغيل العداد ***
            if (isTodayFlag) {
                this.currentPrayerTimes = prayerTimes
                updateNextPrayerInfo()
            }
            // *** نهاية التعديل ***
        }

        val arabicMonthName = SimpleDateFormat("MMMM yyyy", Locale("ar")).format(calendar.time)
        _monthData.postValue(Month(arabicMonthName, daysInMonth))
    }

    private fun updateNextPrayerInfo() {
        timerHandler.removeCallbacksAndMessages(null)
        startLiveTimer()
    }

    private fun startLiveTimer() {
        timeUpdater = object : Runnable {
            override fun run() {
                val prayerTimes = currentPrayerTimes ?: return

                var nextPrayerEnum = prayerTimes.nextPrayer()
                var nextPrayerTime = prayerTimes.timeForPrayer(nextPrayerEnum)
                var currentPrayerTime = prayerTimes.timeForPrayer(prayerTimes.currentPrayer())

                if (nextPrayerEnum == Prayer.NONE) {
                    val tomorrowCalendar = Calendar.getInstance()
                    tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    val tomorrowDate = DateComponents.from(tomorrowCalendar.time)
                    val tomorrowPrayerTimes = PrayerTimes(lastKnownCoordinates!!, tomorrowDate, lastKnownParams!!)
                    nextPrayerTime = tomorrowPrayerTimes.fajr
                    nextPrayerEnum = Prayer.FAJR
                    currentPrayerTime = prayerTimes.isha
                }

                val prayerNamesMap = mapOf(
                    Prayer.FAJR to "الفجر",
                    Prayer.SUNRISE to "الشروق",
                    Prayer.DHUHR to "الظهر",
                    Prayer.ASR to "العصر",
                    Prayer.MAGHRIB to "المغرب",
                    Prayer.ISHA to "العشاء"
                )
                val nextPrayerName = "صلاة ${prayerNamesMap[nextPrayerEnum] ?: "غير معروف"}"

                if (_nextPrayer.value != nextPrayerName) {
                    _nextPrayer.postValue(nextPrayerName)
                }

                val timeDiff = nextPrayerTime.time - System.currentTimeMillis()

                if (timeDiff <= 0) {
                    _timeLeft.postValue("حان الآن")
                    _prayerProgress.postValue(100)
                    timerHandler.postDelayed({ calculateAndStartLiveTimer(lastKnownCoordinates!!.latitude, lastKnownCoordinates!!.longitude) }, 2000)
                    return
                }

                val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60
                val timeLeftFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                _timeLeft.postValue("يتبقى ${convertToEasternArabic(timeLeftFormatted)}")

                if (currentPrayerTime != null) {
                    val totalTimeForPrayer = nextPrayerTime.time - currentPrayerTime.time
                    val timePassed = System.currentTimeMillis() - currentPrayerTime.time
                    if (totalTimeForPrayer > 0) {
                        val progress = ((timePassed.toDouble() / totalTimeForPrayer.toDouble()) * 100).toInt()
                        _prayerProgress.postValue(progress.coerceIn(0, 100))
                    }
                } else {
                    _prayerProgress.postValue(0)
                }

                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timeUpdater)
    }

    override fun onCleared() {
        super.onCleared()
        timerHandler.removeCallbacksAndMessages(null)
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
}