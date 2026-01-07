package com.eslamdev.islamic.data.model

import android.content.Context
import android.location.Geocoder
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.eslamdev.islamic.data.local.PrayerTimingDao
import java.text.SimpleDateFormat
import java.util.*

// أضفنا context في الـ Constructor عشان نقرأ الـ SharedPreferences
class PrayerRepository(
    private val prayerTimingDao: PrayerTimingDao,
    private val context: Context
) {

    // دالة مساعدة لقراءة الإعدادات
    private fun getCalculationParameters(): com.batoulapps.adhan.CalculationParameters {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // قراءة طريقة الحساب (الافتراضي: مصر)
        val methodIndex = prefs.getInt("CALC_METHOD_INDEX", 4) // 4 = EGYPTIAN
        val method = when(methodIndex) {
            0 -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            1 -> CalculationMethod.KARACHI
            2 -> CalculationMethod.NORTH_AMERICA
            3 -> CalculationMethod.DUBAI
            4 -> CalculationMethod.EGYPTIAN
            5 -> CalculationMethod.UMM_AL_QURA
            else -> CalculationMethod.EGYPTIAN
        }

        val params = method.parameters

        // قراءة المذهب (الافتراضي: شافعي/جمهور)
        val madhabIndex = prefs.getInt("MADHAB_INDEX", 0) // 0 = SHAFI, 1 = HANAFI
        params.madhab = if (madhabIndex == 1) Madhab.HANAFI else Madhab.SHAFI

        return params
    }

    // دالة الحساب لليوم الحالي (للشاشة الرئيسية)
    fun calculateAndGetPrayerTimes(latitude: Double, longitude: Double): PrayerTimingEntity {
        val coordinates = Coordinates(latitude, longitude)
        val date = DateComponents.from(Date())
        val params = getCalculationParameters() // استخدام الإعدادات الديناميكية

        val prayerTimes = PrayerTimes(coordinates, date, params)
        val formatter = SimpleDateFormat("hh:mm a", Locale("ar"))

        return PrayerTimingEntity(
            date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
            fajr = formatter.format(prayerTimes.fajr),
            sunrise = formatter.format(prayerTimes.sunrise),
            dhuhr = formatter.format(prayerTimes.dhuhr),
            asr = formatter.format(prayerTimes.asr),
            maghrib = formatter.format(prayerTimes.maghrib),
            isha = formatter.format(prayerTimes.isha)
        )
    }

    // دالة الحساب للشهر (لشاشة المواقيت)
    fun getPrayerTimingsForMonth(month: Int, year: Int, latitude: Double, longitude: Double): List<PrayerTimingEntity> {
        val list = mutableListOf<PrayerTimingEntity>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val params = getCalculationParameters() // استخدام الإعدادات الديناميكية

        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateComponents = DateComponents.from(calendar.time)
            val coordinates = Coordinates(latitude, longitude)
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
            val formatter = SimpleDateFormat("hh:mm a", Locale("ar"))

            list.add(PrayerTimingEntity(
                date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time),
                fajr = formatter.format(prayerTimes.fajr),
                sunrise = formatter.format(prayerTimes.sunrise),
                dhuhr = formatter.format(prayerTimes.dhuhr),
                asr = formatter.format(prayerTimes.asr),
                maghrib = formatter.format(prayerTimes.maghrib),
                isha = formatter.format(prayerTimes.isha)
            ))
        }
        return list
    }

    // دالة لجلب اسم المدينة (أونلاين، ولو فشل يرجع إحداثيات)
    fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality // المدينة
                val adminArea = addresses[0].adminArea // المحافظة
                "$city، $adminArea"
            } else {
                "موقع غير معروف"
            }
        } catch (e: Exception) {
            // في حالة الأوفلاين التام
            "خط العرض: ${String.format("%.2f", lat)}"
        }
    }
}