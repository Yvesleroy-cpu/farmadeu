package com.example.data.repository

import com.example.data.local.CropDao
import com.example.data.local.LivestockDao
import com.example.data.local.ScanResultDao
import com.example.data.local.FarmTaskDao
import com.example.data.local.InventoryDao
import com.example.data.model.Crop
import com.example.data.model.Livestock
import com.example.data.model.ScanResult
import com.example.data.model.FarmTask
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

class FarmRepository(
    private val cropDao: CropDao,
    private val livestockDao: LivestockDao,
    private val scanResultDao: ScanResultDao,
    private val farmTaskDao: FarmTaskDao,
    private val inventoryDao: InventoryDao
) {
    val allCrops: Flow<List<Crop>> = cropDao.getAllCrops()
    val allLivestock: Flow<List<Livestock>> = livestockDao.getAllLivestock()
    val allScanResults: Flow<List<ScanResult>> = scanResultDao.getAllScanResults()
    val allTasks: Flow<List<FarmTask>> = farmTaskDao.getAllTasks()
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()

    suspend fun getCropById(id: Long): Crop? = cropDao.getCropById(id)
    suspend fun insertCrop(crop: Crop): Long = cropDao.insertCrop(crop)
    suspend fun updateCrop(crop: Crop) = cropDao.updateCrop(crop)
    suspend fun deleteCropById(id: Long) = cropDao.deleteCropById(id)

    suspend fun getLivestockById(id: Long): Livestock? = livestockDao.getLivestockById(id)
    suspend fun insertLivestock(livestock: Livestock): Long = livestockDao.insertLivestock(livestock)
    suspend fun updateLivestock(livestock: Livestock) = livestockDao.updateLivestock(livestock)
    suspend fun deleteLivestockById(id: Long) = livestockDao.deleteLivestockById(id)

    suspend fun insertScanResult(scanResult: ScanResult): Long = scanResultDao.insertScanResult(scanResult)
    suspend fun deleteScanResultById(id: Long) = scanResultDao.deleteScanResult(id)
    suspend fun clearScanHistory() = scanResultDao.clearHistory()

    // Task Operations
    suspend fun insertTask(task: FarmTask): Long = farmTaskDao.insertTask(task)
    suspend fun updateTask(task: FarmTask) = farmTaskDao.updateTask(task)
    suspend fun deleteTask(task: FarmTask) = farmTaskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = farmTaskDao.deleteTaskById(id)

    // Inventory Operations
    suspend fun getInventoryItemById(id: Long): InventoryItem? = inventoryDao.getInventoryItemById(id)
    suspend fun insertInventoryItem(item: InventoryItem): Long = inventoryDao.insertInventoryItem(item)
    suspend fun updateInventoryItem(item: InventoryItem) = inventoryDao.updateInventoryItem(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = inventoryDao.deleteInventoryItem(item)
    suspend fun deleteInventoryItemById(id: Long) = inventoryDao.deleteInventoryItemById(id)
}
