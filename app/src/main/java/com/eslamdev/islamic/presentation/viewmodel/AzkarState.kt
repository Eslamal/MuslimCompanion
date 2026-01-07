package com.eslamdev.islamic.presentation.viewmodel

import com.eslamdev.islamic.data.model.ZekrModel

sealed class AzkarState {
    object Loading : AzkarState()
    data class Success(val azkar: List<ZekrModel>) : AzkarState()
    data class Error(val message: String) : AzkarState()
}