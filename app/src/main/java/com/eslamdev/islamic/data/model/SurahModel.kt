package com.eslamdev.islamic.data.model

import java.io.Serializable

data class SurahModel(
    val id: Int,       // رقم السورة (index)
    val name: String   // اسم السورة
) : Serializable