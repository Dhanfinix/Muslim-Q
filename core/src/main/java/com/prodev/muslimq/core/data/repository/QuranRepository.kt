package com.prodev.muslimq.core.data.repository

import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.data.source.remote.model.TafsirDetailItem
import com.prodev.muslimq.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getQuran(): Flow<Resource<List<QuranEntity>>>
    fun getQuranDetail(id: Int): Flow<Resource<QuranDetailEntity>>
    fun getQuranTafsir(surahId: Int, ayahNumber: Int): Flow<Resource<TafsirDetailItem>>
    fun getBookmark(): Flow<List<QuranDetailEntity>>
    suspend fun insertToBookmark(quran: QuranDetailEntity, isBookmarked: Boolean)
    suspend fun deleteAllBookmark()
    suspend fun deleteBookmark(surahId: Int)
}