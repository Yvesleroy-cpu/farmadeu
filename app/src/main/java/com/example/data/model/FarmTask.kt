package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farm_tasks")
data class FarmTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val type: String, // "ENTRETIEN", "RECOLTE", "VACCINATION", "AUTRE"
    val date: Long, // timestamp for day
    val targetName: String = "", // e.g. Name of crop or animal group
    val isCompleted: Boolean = false
)
