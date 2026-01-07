package com.eslamdev.islamic.data.model

// واجهة مشتركة عشان الأدابتور يقبل النوعين
sealed interface DuaListItem

// كلاس لعنوان القسم (مثل: أدعية قرآنية)
data class DuaCategory(val title: String) : DuaListItem

// كلاس للدعاء نفسه
data class DuaItem(
    val title: String,
    val content: String,
    val category: String
) : DuaListItem