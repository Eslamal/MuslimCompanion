package com.example.islamic.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islamic.api.RemoteDataSource
import com.example.islamic.model.Data
import com.example.islamic.model.Day
import com.example.islamic.model.Month
import com.example.islamic.model.PrayerData
import com.example.islamic.model.Timings
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PrayerHomeViewModel  : ViewModel() {
    private val _nextPrayer = MutableLiveData<String>()
    val nextPrayer: LiveData<String> get() = _nextPrayer

    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String> get() = _timeLeft

    var prayerData = MutableLiveData<PrayerData?>()
    var apiRepository: RemoteDataSource = RemoteDataSource()
    var monthData = MutableLiveData<Month?>()

    private val arabicLocale = Locale("ar", "EG")

    val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("PrayerHomeViewModel", "CoroutineExceptionHandler caught: ${exception.message}", exception)
        _nextPrayer.postValue("خطأ: تعذر جلب الصلوات")
        _timeLeft.postValue(convertToEasternArabic("00:00"))
    }

    fun getPrayerData(lat:String,lang: String,month:String,year:String) {
        CoroutineScope(Dispatchers.IO + handler).launch {
            val response = apiRepository.getPrayerTimes(lat,lang,month,year)
            prayerData.postValue(response)

            if (response != null && response.allData.isNotEmpty()) {
                calculateNextPrayer(response)
            } else {
                _nextPrayer.postValue("لا توجد بيانات صلاة")
                _timeLeft.postValue(convertToEasternArabic("00:00"))
            }
        }
    }

    fun mapData(data : PrayerData){
        val days :MutableList<Day> = arrayListOf()
        val currentCalendar = Calendar.getInstance()

        val monthNumber = data.allData[0].date.gregorian.month.number
        val yearValue = data.allData[0].date.gregorian.year

        val monthNameEn = data.allData[0].date.gregorian.month.en
        val location = data.allData[0].meta.timezone

        val tempCalForMonth = Calendar.getInstance(arabicLocale)
        tempCalForMonth.set(Calendar.MONTH, monthNumber - 1)
        val arabicMonthName = SimpleDateFormat("MMMM", arabicLocale).format(tempCalForMonth.time)

        val name = arabicMonthName +" "+ convertToEasternArabic(yearValue.toString())

        for (item in data.allData){
            val dayNum = item.date.gregorian.day
            val monthNum = item.date.gregorian.month.number
            val yearVal = item.date.gregorian.year
            val dayOfWeekEn = item.date.gregorian.weekday.en

            val timings = item.timings

            val isToday = (dayNum == currentCalendar.get(Calendar.DAY_OF_MONTH) &&
                    monthNum == currentCalendar.get(Calendar.MONTH) + 1 &&
                    yearVal == currentCalendar.get(Calendar.YEAR))

            days.add(Day(dayNum, monthNum, yearVal, dayOfWeekEn, timings, false, isToday))
        }
        monthData.postValue(Month(name ,location,days))
    }

    private fun calculateNextPrayer(prayerData: PrayerData?) {
        prayerData?.let { data ->
            val currentTime = Calendar.getInstance(arabicLocale)
            currentTime.timeZone = TimeZone.getDefault()

            val apiTimeZoneId = data.allData[0].meta.timezone
            val apiTimeZone = TimeZone.getTimeZone(apiTimeZoneId)


            val todayTimingsData = data.allData.find { prayerDayData ->
                val day = prayerDayData.date.gregorian.day
                val month = prayerDayData.date.gregorian.month.number
                val year = prayerDayData.date.gregorian.year

                day == currentTime.get(Calendar.DAY_OF_MONTH) &&
                        month == currentTime.get(Calendar.MONTH) + 1 &&
                        year == currentTime.get(Calendar.YEAR)
            }


            val tomorrowTimingsData = data.allData.find { prayerDayData ->
                val tomorrowCalendar = Calendar.getInstance()
                tomorrowCalendar.add(Calendar.DAY_OF_MONTH, 1)

                val day = prayerDayData.date.gregorian.day
                val month = prayerDayData.date.gregorian.month.number
                val year = prayerDayData.date.gregorian.year

                day == tomorrowCalendar.get(Calendar.DAY_OF_MONTH) &&
                        month == tomorrowCalendar.get(Calendar.MONTH) + 1 &&
                        year == tomorrowCalendar.get(Calendar.YEAR)
            }

            val prayerNamesMap = mapOf(
                "Fajr" to "الفجر",
                "Dhuhr" to "الظهر",
                "Asr" to "العصر",
                "Maghrib" to "المغرب",
                "Isha" to "العشاء"
            )
            val prayerKeys = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")


            val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
            inputFormat.timeZone = apiTimeZone


            if (todayTimingsData != null) {
                for (key in prayerKeys) {
                    val prayerTimeString = when (key) {
                        "Fajr" -> todayTimingsData.timings.Fajr
                        "Dhuhr" -> todayTimingsData.timings.Dhuhr
                        "Asr" -> todayTimingsData.timings.Asr
                        "Maghrib" -> todayTimingsData.timings.Maghrib
                        "Isha" -> todayTimingsData.timings.Isha
                        else -> continue
                    }

                    try {
                        val prayerTimeDate = inputFormat.parse(prayerTimeString)
                        if (prayerTimeDate != null) {
                            val prayerCalendar = Calendar.getInstance(arabicLocale)
                            prayerCalendar.time = prayerTimeDate
                            prayerCalendar.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
                            prayerCalendar.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
                            prayerCalendar.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

                            if (prayerCalendar.after(currentTime)) {
                                val nextPrayerTranslatedName = prayerNamesMap[key] ?: key
                                val timeLeftFormatted = calculateTimeLeft(prayerCalendar.time)

                                _nextPrayer.postValue(nextPrayerTranslatedName)
                                _timeLeft.postValue(timeLeftFormatted)
                                return
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PrayerHomeViewModel", "Error parsing prayer time for $key ($prayerTimeString): ${e.message}", e)
                    }
                }
            }

            if (tomorrowTimingsData != null) {
                val fajrTomorrowTime = tomorrowTimingsData.timings.Fajr
                try {
                    val prayerTimeDate = inputFormat.parse(fajrTomorrowTime)
                    if (prayerTimeDate != null) {
                        val prayerCalendar = Calendar.getInstance(arabicLocale)
                        prayerCalendar.time = prayerTimeDate

                        val tomorrowDate = Calendar.getInstance()
                        tomorrowDate.add(Calendar.DAY_OF_MONTH, 1)
                        prayerCalendar.set(Calendar.YEAR, tomorrowDate.get(Calendar.YEAR))
                        prayerCalendar.set(Calendar.MONTH, tomorrowDate.get(Calendar.MONTH))
                        prayerCalendar.set(Calendar.DAY_OF_MONTH, tomorrowDate.get(Calendar.DAY_OF_MONTH))

                        val nextPrayerTranslatedName = prayerNamesMap["Fajr"] ?: "الفجر"
                        val timeLeftFormatted = calculateTimeLeft(prayerCalendar.time)

                        _nextPrayer.postValue(nextPrayerTranslatedName)
                        _timeLeft.postValue(timeLeftFormatted)
                        return
                    }
                } catch (e: Exception) {
                    Log.e("PrayerHomeViewModel", "Error parsing Fajr time for tomorrow ($fajrTomorrowTime): ${e.message}", e)
                }
            }


            _nextPrayer.postValue("لا توجد صلوات متاحة")
            _timeLeft.postValue(convertToEasternArabic("00:00"))

        } ?: run {
            _nextPrayer.postValue("خطأ في جلب بيانات الصلاة")
            _timeLeft.postValue(convertToEasternArabic("00:00"))
        }
    }


    private fun calculateTimeLeft(prayerTime: Date): String {
        val currentTime = Calendar.getInstance(arabicLocale).time
        val timeDiff = prayerTime.time - currentTime.time

        if (timeDiff <= 0) {
            return convertToEasternArabic("00:00")
        }

        val totalSeconds = timeDiff / 1000
        val hoursLeft = totalSeconds / (60 * 60)
        val minutesLeft = (totalSeconds % (60 * 60)) / 60

        val formattedTime = String.format("%02d:%02d", hoursLeft, minutesLeft)
        return convertToEasternArabic(formattedTime)
    }

    private fun convertToEasternArabic(numberString: String): String {
        val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (char in numberString) {
            if (char.isDigit()) {
                builder.append(arabicNumbers[char.toString().toInt()])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}