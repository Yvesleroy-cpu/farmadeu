package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "livestock")
data class Livestock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., "Moutons Bergerie Nord"
    val type: String, // e.g., "Mouton", "Bovin", "Volaille", "Porc"
    val breed: String, // e.g., "Mérinos", "Charolais"
    val count: Int,
    val entryDate: Long = System.currentTimeMillis(),
    val healthStatus: String = "Excellent", // "Excellent", "Sous traitement", "À isoler"
    val currentCycle: String = "Croissance", // "Gestation", "Lactation", "Croissance", "Engraissement", "Ponte"
    val currentStageStartDate: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    // Return days in herd
    fun getDaysInHerd(): Long {
        val diff = System.currentTimeMillis() - entryDate
        return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    }

    // Get cycle indicator
    fun getCycleProgressIndex(): Int {
        return when (currentCycle) {
            "Gestation" -> 1
            "Lactation" -> 2
            "Croissance" -> 3
            "Engraissement" -> 4
            "Ponte" -> 5
            else -> 1
        }
    }
}
