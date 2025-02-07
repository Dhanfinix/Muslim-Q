package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import kotlinx.coroutines.flow.Flow

interface TasbihRepository {

    suspend fun insertDzikir(tasbih: TasbihEntity)

    fun getAllDzikir(): Flow<List<TasbihEntity>>

    suspend fun deleteDzikir(dzikirName: String)
}