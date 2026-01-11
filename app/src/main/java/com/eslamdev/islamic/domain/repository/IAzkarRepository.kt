package com.eslamdev.islamic.domain.repository

import com.eslamdev.islamic.data.model.ZekrModel
import kotlinx.coroutines.flow.Flow

interface IAzkarRepository {

    suspend fun getAzkar(type: AzkarType): List<ZekrModel>
}