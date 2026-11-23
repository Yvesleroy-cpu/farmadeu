package com.example

import com.example.data.model.Crop
import com.example.data.model.Livestock
import com.example.util.ReminderGenerator
import com.example.util.ReminderPriority
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testGenerateCropReminders_SemisStage() {
        val crop = Crop(
            id = 1,
            name = "Parcelle Tomates A",
            type = "Tomate",
            variety = "Marmande",
            area = 0.5,
            currentStage = "Semis"
        )
        val reminders = ReminderGenerator.generateCropReminders(crop)
        
        assertNotNull(reminders)
        assertTrue(reminders.any { it.title.contains("Arrosage délicat") })
        assertTrue(reminders.any { it.title.contains("Contrôle de température") })
        assertEquals(2, reminders.size)
        assertEquals(ReminderPriority.HIGH, reminders.first().priority)
    }

    @Test
    fun testGenerateCropReminders_GrowthStage() {
        val crop = Crop(
            id = 2,
            name = "Parcelle Blé B",
            type = "Blé",
            variety = "Triticum",
            area = 10.0,
            currentStage = "Croissance"
        )
        val reminders = ReminderGenerator.generateCropReminders(crop)
        
        assertNotNull(reminders)
        assertTrue(reminders.any { it.title.contains("Arrosage en profondeur") })
        assertTrue(reminders.any { it.title.contains("Apport en nutriments") })
        assertTrue(reminders.any { it.title.contains("Tuteurage") })
        assertEquals(3, reminders.size)
    }

    @Test
    fun testGenerateLivestockReminders_GestationAndTreatment() {
        val livestock = Livestock(
            id = 1,
            name = "Brebis Bergerie Sud",
            type = "Mouton",
            breed = "Mérinos",
            count = 45,
            healthStatus = "Sous traitement",
            currentCycle = "Gestation"
        )
        val reminders = ReminderGenerator.generateLivestockReminders(livestock)
        
        assertNotNull(reminders)
        // Should contain health treatment reminder and gestation dietary/stress reminders
        assertTrue(reminders.any { it.title.contains("Administration du traitement") })
        assertTrue(reminders.any { it.title.contains("Suivi nutritionnel") })
        assertTrue(reminders.any { it.title.contains("Réduction du stress") })
        assertEquals(3, reminders.size)
    }

    @Test
    fun testGenerateAllReminders_PrioritySorting() {
        val crop = Crop(
            id = 1,
            name = "Carottes",
            type = "Carotte",
            variety = "Nantaise",
            area = 0.2,
            currentStage = "Semis" // Creates 1 HIGH, 1 MEDIUM
        )

        val livestock = Livestock(
            id = 2,
            name = "Moutons",
            type = "Mouton",
            breed = "Mérinos",
            count = 10,
            healthStatus = "À isoler", // Creates 1 HIGH
            currentCycle = "Engraissement" // Creates 1 HIGH, 1 LOW
        )

        val allReminders = ReminderGenerator.generateAllReminders(listOf(crop), listOf(livestock))

        assertEquals(5, allReminders.size)
        // Verified high priority should be at the front
        assertEquals(ReminderPriority.HIGH, allReminders[0].priority)
        assertEquals(ReminderPriority.HIGH, allReminders[1].priority)
        assertEquals(ReminderPriority.HIGH, allReminders[2].priority)
        assertEquals(ReminderPriority.MEDIUM, allReminders[3].priority)
        assertEquals(ReminderPriority.LOW, allReminders[4].priority)
    }
}
