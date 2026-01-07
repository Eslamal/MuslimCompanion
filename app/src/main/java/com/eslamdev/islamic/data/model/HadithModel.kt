package com.eslamdev.islamic.data.model

import java.io.Serializable

data class HadithModel(
    val id: Int,
    val title: String,
    val content: String, // كان اسمها hadithText
    val description: String = ""
) : Serializable