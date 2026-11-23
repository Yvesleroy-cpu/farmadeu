package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val targetType: String, // "Plante / Culture" or "Animal / Élevage"
    val symptoms: String,
    val pathology: String,
    val treatments: String,
    val toAvoid: String,
    val prevention: String,
    val rawResponse: String = ""
)
