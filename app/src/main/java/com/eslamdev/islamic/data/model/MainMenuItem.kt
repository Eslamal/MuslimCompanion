package com.eslamdev.islamic.data.model

data class MainMenuItem(
    val title: String,
    val iconRes: Int,
    val activity: Class<*>? = null // ده المتغير اللي كان ناقص عشان نعرف نفتح أي صفحة
)