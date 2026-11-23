package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ScanResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAllScanResults(): Flow<List<ScanResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResult): Long

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScanResult(id: Long)

    @Query("DELETE FROM scan_results")
    suspend fun clearHistory()
}
