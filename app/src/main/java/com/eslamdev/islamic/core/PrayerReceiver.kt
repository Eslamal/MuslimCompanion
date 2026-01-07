package com.eslamdev.islamic.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eslamdev.islamic.R
import com.eslamdev.islamic.presentation.ui.activity.PrayerActivity

class PrayerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        // تشغيل الأذان وعرض الإشعار بناءً على الإعدادات المحفوظة
        showAdhanNotification(context, prayerName)

        // جدولة الصلاة القادمة فوراً عشان السلسلة متقفش
        PrayerScheduler.scheduleNextPrayer(context)
    }

    private fun showAdhanNotification(context: Context, prayerName: String) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // 1. التحقق هل المستخدم مفعل صوت الأذان؟
        val isSoundEnabled = prefs.getBoolean("IS_ADHAN_ENABLED", true)

        // 2. معرفة الصوت المختار (0: مكة، 1: مصر، 2: المدينة)
        val soundIndex = prefs.getInt("ADHAN_SOUND_INDEX", 0)

        // مصفوفة ملفات الصوت (لازم تكون الأسماء دي موجودة في res/raw)
        // تأكد إنك غيرت أسماء الملفات عندك لـ adhan_mecca, adhan_cairo, adhan_medina
        val adhanResIds = arrayOf(
            R.raw.adhan_mecca,
            R.raw.adhan_cairo,
            R.raw.adhan_medina
        )

        // اختيار الملف بأمان (عشان لو الاندكس غلط ميعملش كراش ويختار الأول)
        val selectedResId = if (soundIndex in adhanResIds.indices) adhanResIds[soundIndex] else adhanResIds[0]

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // بنعمل ID للقناة مرتبط برقم الصوت، عشان لو غيرت الصوت الأندرويد يعمل قناة جديدة بالإعدادات الجديدة
        val channelId = "prayer_channel_sound_$soundIndex"

        val soundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$selectedResId")

        // إنشاء قناة الإشعارات (للأندرويد 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (isSoundEnabled) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, "تنبيهات الصلاة", importance).apply {
                description = "تنبيهات مواقيت الصلاة والأذان"

                if (isSoundEnabled) {
                    // لو الصوت مفعل، بنربط الصوت بالقناة
                    setSound(soundUri, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    enableVibration(true)
                } else {
                    // لو الصوت مقفول، بنخلي الصوت null
                    setSound(null, null)
                    enableVibration(true)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        // عند الضغط على الإشعار يفتح صفحة المواقيت
        val contentIntent = Intent(context, PrayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // بناء الإشعار
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.prayer) // تأكد إن الأيقونة موجودة
            .setContentTitle("حان الآن موعد $prayerName")
            .setContentText(if (isSoundEnabled) "حي على الصلاة.. حي على الفلاح" else "تم كتم صوت الأذان")
            .setPriority(if (isSoundEnabled) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true) // الإشعار يختفي لما تضغط عليه
            .setContentIntent(pendingIntent)

        // للأجهزة القديمة (قبل أندرويد 8)
        if (isSoundEnabled) {
            builder.setSound(soundUri)
        }

        notificationManager.notify(1, builder.build())
    }
}