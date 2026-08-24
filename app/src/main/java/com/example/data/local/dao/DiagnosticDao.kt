package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DiagnosticLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticDao {
    @Query("SELECT * FROM diagnostic_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogsFlow(): Flow<List<DiagnosticLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DiagnosticLogEntity): Long

    @Query("DELETE FROM diagnostic_logs")
    suspend fun clearLogs()
}
