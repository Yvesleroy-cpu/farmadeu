package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Engrais" (Fertilizer), "Semences" (Seeds), "Aliments pour animaux" (Animal Feed)
    val quantity: Double,
    val unit: String, // e.g., "kg", "tonnes", "sacs", "litres"
    val lowThreshold: Double, // threshold under which we raise warning
    val lastUpdated: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun isLowStock(): Boolean {
        return quantity <= lowThreshold
    }
}
