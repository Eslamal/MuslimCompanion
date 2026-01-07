package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eslamdev.islamic.data.model.HadithModel
import com.eslamdev.islamic.data.repository.HadithRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HadithViewModel(private val repository: HadithRepository) : ViewModel() {

    private val _hadiths = MutableStateFlow<List<HadithModel>>(emptyList())
    val hadiths: StateFlow<List<HadithModel>> = _hadiths

    fun loadHadiths() {
        viewModelScope.launch {
            val list = repository.getAllHadiths()
            _hadiths.value = list
        }
    }
}

// Factory
class HadithViewModelFactory(private val repository: HadithRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HadithViewModel(repository) as T
    }
}