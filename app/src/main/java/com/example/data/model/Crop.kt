package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crops")
data class Crop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // e.g., "Tomate", "Blé", "Maïs", "Pomme de terre"
    val variety: String,
    val plantationDate: Long = System.currentTimeMillis(),
    val area: Double,
    val areaUnit: String = "ha", // "ha" or "m²"
    val currentStage: String = "Semis", // "Semis", "Levée", "Croissance", "Floraison", "Récolte"
    val currentStageStartDate: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    // Return age in days
    fun getAgeInDays(): Long {
        val diff = System.currentTimeMillis() - plantationDate
        return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    }

    // Get stage index (0 to 4) for progress tracking
    fun getStageProgressIndex(): Int {
        return when (currentStage) {
            "Semis" -> 1
            "Levée" -> 2
            "Croissance" -> 3
            "Floraison" -> 4
            "Récolte" -> 5
            else -> 1
        }
    }
}
