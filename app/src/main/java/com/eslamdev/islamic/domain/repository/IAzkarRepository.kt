package com.eslamdev.islamic.domain.repository

import com.eslamdev.islamic.data.model.ZekrModel
import kotlinx.coroutines.flow.Flow

interface IAzkarRepository {
    // دالة بترجع قائمة الأذكار حسب النوع
    suspend fun getAzkar(type: AzkarType): List<ZekrModel>
}