package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eslamdev.islamic.domain.repository.IAzkarRepository

class AzkarViewModelFactory(private val repository: IAzkarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AzkarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AzkarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}