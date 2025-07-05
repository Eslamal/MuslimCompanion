package com.example.islamic.model

import com.google.gson.annotations.SerializedName

data class PrayerData(
    val status: String,
    @SerializedName("data") val allData : List<Data>
)
data class Data(
    val date: Date,
    val timings: Timings,
    val meta: Meta
)
data class Meta(
        val timezone: String
)
data class Timings(
    val date: String,
    val Asr: String,
    val Dhuhr: String,
    val Fajr: String,
    val Isha: String,
    val Maghrib: String,
    val Midnight: String,
    val Sunrise: String,
    val Sunset: String
)