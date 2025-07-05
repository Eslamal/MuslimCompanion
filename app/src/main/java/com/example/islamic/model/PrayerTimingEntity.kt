package com.example.islamic.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "prayer_timing")
data class PrayerTimingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)
