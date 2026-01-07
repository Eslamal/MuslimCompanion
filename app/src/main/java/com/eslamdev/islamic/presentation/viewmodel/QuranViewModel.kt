package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eslamdev.islamic.data.model.SurahModel
import com.eslamdev.islamic.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _surahList = MutableStateFlow<List<SurahModel>>(emptyList())
    val surahList: StateFlow<List<SurahModel>> = _surahList

    fun loadSurahs() {
        viewModelScope.launch {
            _surahList.value = repository.getSurahList()
        }
    }
}

class QuranViewModelFactory(private val repository: QuranRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuranViewModel(repository) as T
    }
}