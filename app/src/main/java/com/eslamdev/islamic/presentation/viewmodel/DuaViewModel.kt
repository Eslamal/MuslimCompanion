package com.eslamdev.islamic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eslamdev.islamic.data.model.DuaListItem
import com.eslamdev.islamic.data.repository.DuaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DuaViewModel(private val repository: DuaRepository) : ViewModel() {

    private val _duaList = MutableStateFlow<List<DuaListItem>>(emptyList())
    val duaList: StateFlow<List<DuaListItem>> = _duaList

    fun loadDuas() {
        viewModelScope.launch {
            _duaList.value = repository.getDuas()
        }
    }
}

class DuaViewModelFactory(private val repository: DuaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DuaViewModel(repository) as T
    }
}