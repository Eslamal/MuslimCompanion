package com.eslamdev.islamic.data.model

data class MainMenuItem(
    val title: String,
    val iconRes: Int,
    val activity: Class<*>? = null
)