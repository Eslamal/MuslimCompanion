package com.eslamdev.islamic.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.*

class PrayerRepository(private val prayerTimingDao: PrayerTimingDao) {

    // دالة جديدة للحساب المحلي
    fun calculateAndGetPrayerTimes(latitude: Double, longitude: Double): PrayerTimingEntity {
        // 1. الحصول على الإحداثيات
        val coordinates = Coordinates(latitude, longitude)

        // 2. الحصول على تاريخ اليوم
        val date = DateComponents.from(Date())

        // 3. تحديد طريقة الحساب (الهيئة المصرية العامة للمساحة)
        // ### بداية التعديل النهائي ###
        val params = CalculationMethod.EGYPTIAN.parameters
        params.madhab = Madhab.SHAFI // نقوم بتعديل المذهب مباشرة هكذا
        // ### نهاية التعديل النهائي ###

        // 4. حساب أوقات الصلاة
        val prayerTimes = PrayerTimes(coordinates, date, params)

        // 5. تنسيق الوقت
        val formatter = SimpleDateFormat("hh:mm a", Locale("ar"))

        // 6. إنشاء كائن لتخزينه أو عرضه
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

    // دوال قاعدة البيانات
    suspend fun insertPrayerTimings(prayerTiming: PrayerTimingEntity) {
        prayerTimingDao.insertPrayerTiming(prayerTiming)
    }

    suspend fun getPrayerTimingForDate(date: String): PrayerTimingEntity? {
        return prayerTimingDao.getPrayerTimingByDate(date)
    }
}