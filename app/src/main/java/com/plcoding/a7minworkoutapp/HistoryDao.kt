package com.plcoding.a7minworkoutapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(historyEntity:HistoryEntity)

    @Query("SELECT * FROM `history-table`")
    fun fetchAllDated(): Flow<List<HistoryEntity>>
}