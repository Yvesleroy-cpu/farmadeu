package com.example.data.local

import androidx.room.*
import com.example.data.model.Crop
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops ORDER BY plantationDate DESC")
    fun getAllCrops(): Flow<List<Crop>>

    @Query("SELECT * FROM crops WHERE id = :id")
    suspend fun getCropById(id: Long): Crop?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: Crop): Long

    @Update
    suspend fun updateCrop(crop: Crop)

    @Delete
    suspend fun deleteCrop(crop: Crop)

    @Query("DELETE FROM crops WHERE id = :id")
    suspend fun deleteCropById(id: Long)
}
