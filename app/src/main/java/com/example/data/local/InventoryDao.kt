package com.example.data.local

import androidx.room.*
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY lastUpdated DESC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getInventoryItemById(id: Long): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItemById(id: Long)
}
