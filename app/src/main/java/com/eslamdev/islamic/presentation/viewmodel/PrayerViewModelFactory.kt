package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eslamdev.islamic.data.model.PrayerRepository

class PrayerViewModelFactory(private val repository: PrayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrayerHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}