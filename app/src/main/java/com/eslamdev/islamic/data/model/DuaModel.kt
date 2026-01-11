package com.eslamdev.islamic.data.model

sealed interface DuaListItem


data class DuaCategory(val title: String) : DuaListItem


data class DuaItem(
    val title: String,
    val content: String,
    val category: String
) : DuaListItem