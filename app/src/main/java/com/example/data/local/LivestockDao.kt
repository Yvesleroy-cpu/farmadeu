package com.example.data.local

import androidx.room.*
import com.example.data.model.Livestock
import kotlinx.coroutines.flow.Flow

@Dao
interface LivestockDao {
    @Query("SELECT * FROM livestock ORDER BY entryDate DESC")
    fun getAllLivestock(): Flow<List<Livestock>>

    @Query("SELECT * FROM livestock WHERE id = :id")
    suspend fun getLivestockById(id: Long): Livestock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestock(livestock: Livestock): Long

    @Update
    suspend fun updateLivestock(livestock: Livestock)

    @Delete
    suspend fun deleteLivestock(livestock: Livestock)

    @Query("DELETE FROM livestock WHERE id = :id")
    suspend fun deleteLivestockById(id: Long)
}
