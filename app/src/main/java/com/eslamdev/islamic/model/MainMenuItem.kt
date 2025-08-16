package com.eslamdev.islamic.model

import androidx.annotation.DrawableRes

data class MainMenuItem(
    val title: String,
    @DrawableRes val iconResId: Int,
    val destinationActivity: Class<*>
)