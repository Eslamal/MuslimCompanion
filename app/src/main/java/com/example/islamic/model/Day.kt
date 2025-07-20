package com.example.islamic.model

data class Day(
    val dayNum: Int,
    val dayOfWeekEn: String,
    val times: PrayerTimingEntity,
    val isToday: Boolean
)