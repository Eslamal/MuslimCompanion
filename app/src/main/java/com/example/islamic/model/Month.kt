package com.example.islamic.model


data class Month(
    val name: String,
    var location :String,
    val days: List<Day>
)
data class Day(
val day: Int,
val month: Int,
val year: Int,
val day_of_week_en: String,
val times: Timings,
var selected: Boolean,
var today: Boolean
)