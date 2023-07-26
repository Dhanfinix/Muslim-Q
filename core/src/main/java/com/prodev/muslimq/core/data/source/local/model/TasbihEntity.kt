package com.prodev.muslimq.core.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prodev.muslimq.core.utils.DzikirType

@Entity(tableName = "tasbih")
data class TasbihEntity(
    @PrimaryKey
    val dzikirName: String,
    val dzikirType: DzikirType? = null,
    val arabText: String? = null,
    val translation: String? = null
)
