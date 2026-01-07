package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eslamdev.islamic.domain.repository.AzkarType
import com.eslamdev.islamic.domain.repository.IAzkarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AzkarViewModel(private val repository: IAzkarRepository) : ViewModel() {

    private val _azkarState = MutableStateFlow<AzkarState>(AzkarState.Loading)
    val azkarState: StateFlow<AzkarState> = _azkarState

    fun loadAzkar(type: AzkarType) {
        viewModelScope.launch {
            _azkarState.value = AzkarState.Loading
            try {
                val result = repository.getAzkar(type)
                if (result.isNotEmpty()) {
                    _azkarState.value = AzkarState.Success(result)
                } else {
                    _azkarState.value = AzkarState.Error("لا توجد أذكار متاحة")
                }
            } catch (e: Exception) {
                _azkarState.value = AzkarState.Error(e.message ?: "حدث خطأ غير متوقع")
            }
        }
    }
}