package com.example.islamic.model


data class Date(
    val gregorian: GregorianInfo,
    val readable: String,
    val timestamp: String
)
data class GregorianInfo(
    val date: String,
    val day: Int,
    val month: MonthInfo,
    val year: Int,
    val weekday: WeekdayInfo
)

data class MonthInfo(
    val number: Int,
    val en: String
)
data class WeekdayInfo(
    val en: String
)