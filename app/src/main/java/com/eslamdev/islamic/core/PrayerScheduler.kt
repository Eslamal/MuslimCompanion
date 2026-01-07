package com.eslamdev.islamic.core

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.*

object PrayerScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNextPrayer(context: Context) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("LATITUDE", 30.0444f).toDouble()
        val lon = prefs.getFloat("LONGITUDE", 31.2357f).toDouble()

        // إعدادات الحساب (نفس اللي في Repository)
        val params = CalculationMethod.EGYPTIAN.parameters
        params.madhab = if (prefs.getInt("MADHAB_INDEX", 0) == 1) Madhab.HANAFI else Madhab.SHAFI

        val coordinates = Coordinates(lat, lon)
        val now = Date()
        val dateComponents = DateComponents.from(now)

        // حساب مواقيت اليوم
        var prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        var nextPrayer = prayerTimes.nextPrayer()

        // لو صلوات اليوم خلصت (بعد العشاء)، نحسب لأول صلاة بكرة (الفجر)
        if (nextPrayer == Prayer.NONE) {
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowComponents = DateComponents.from(tomorrow.time)
            prayerTimes = PrayerTimes(coordinates, tomorrowComponents, params)
            nextPrayer = Prayer.FAJR
        }

        val nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer) ?: return
        val prayerName = getArabicName(nextPrayer)

        // جدولة المنبه
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // إلغاء أي منبه قديم وجدولة الجديد
        alarmManager.cancel(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextPrayerTime.time, pendingIntent)
            } else {
                // ممكن تطلب من المستخدم يدي صلاحية المنبهات الدقيقة هنا
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextPrayerTime.time, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextPrayerTime.time, pendingIntent)
        }
    }

    private fun getArabicName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "صلاة الفجر"
            Prayer.DHUHR -> "صلاة الظهر"
            Prayer.ASR -> "صلاة العصر"
            Prayer.MAGHRIB -> "صلاة المغرب"
            Prayer.ISHA -> "صلاة العشاء"
            else -> "الصلاة"
        }
    }
}