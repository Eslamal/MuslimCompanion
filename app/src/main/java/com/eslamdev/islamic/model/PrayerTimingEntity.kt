package com.eslamdev.islamic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_timing")
data class PrayerTimingEntity(
    @PrimaryKey val date: String, // تم جعل التاريخ هو المفتاح الأساسي لعدم التكرار
    val fajr: String,
    val sunrise: String, // <-- ### تمت إضافة هذا السطر ###
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)