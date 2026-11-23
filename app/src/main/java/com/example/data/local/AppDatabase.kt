package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Crop
import com.example.data.model.Livestock
import com.example.data.model.ScanResult
import com.example.data.model.FarmTask
import com.example.data.model.InventoryItem

@Database(entities = [Crop::class, Livestock::class, ScanResult::class, FarmTask::class, InventoryItem::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cropDao(): CropDao
    abstract fun livestockDao(): LivestockDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun farmTaskDao(): FarmTaskDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farmadeu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
