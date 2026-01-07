package com.eslamdev.islamic.presentation.viewmodel

import com.eslamdev.islamic.data.model.Month

sealed class PrayerState {
    object Idle : PrayerState() // حالة السكون (قبل طلب الموقع)
    object Loading : PrayerState()
    data class Success(val monthData: Month) : PrayerState()
    data class Error(val message: String) : PrayerState()
}