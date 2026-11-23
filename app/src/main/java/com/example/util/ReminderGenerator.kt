package com.example.util

import com.example.data.model.Crop
import com.example.data.model.Livestock

enum class ReminderPriority {
    HIGH, MEDIUM, LOW
}

data class CareReminder(
    val id: String,
    val title: String,
    val description: String,
    val priority: ReminderPriority,
    val category: String, // "Culture" or "Élevage"
    val itemName: String
)

object ReminderGenerator {

    /**
     * Generates daily care reminders based on the growth cycle stage of a registered crop.
     */
    fun generateCropReminders(crop: Crop): List<CareReminder> {
        val reminders = mutableListOf<CareReminder>()
        val name = crop.name
        val type = crop.type

        when (crop.currentStage) {
            "Semis" -> {
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_semis_water",
                        title = "Arrosage délicat ($name)",
                        description = "Humidifier finement le lit de semence par aspersion douce ou micro-aspersion. Le sol doit rester humide mais pas détrempé.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_semis_temp",
                        title = "Contrôle de température ($name)",
                        description = "S'assurer que la température ambiante de la parcelle/serre est située entre 18°C et 24°C pour favoriser la germination.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Culture",
                        itemName = name
                    )
                )
            }
            "Levée" -> {
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_levee_pest",
                        title = "Protection des jeunes pousses ($name)",
                        description = "Inspecter les feuilles émergentes de $type pour détecter les attaques précoces de limaces, altises ou pucerons.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_levee_weed",
                        title = "Désherbage manuel ($name)",
                        description = "Retirer manuellement les mauvaises herbes naissantes à proximité immédiate des plants sans endommager le système racinaire superficiel.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Culture",
                        itemName = name
                    )
                )
            }
            "Croissance" -> {
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_growth_water",
                        title = "Arrosage en profondeur ($name)",
                        description = "Irriguer copieusement le sol au pied des plants de $type. Privilégier un arrosage le matin pour limiter l'évaporation et éviter le mildiou.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_growth_fertilize",
                        title = "Apport en nutriments azotés ($name)",
                        description = "Appliquer un engrais biologique riche en azote (purin d'ortie dilué ou guano) pour soutenir le développement du feuillage de $type.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_growth_maintenance",
                        title = "Tuteurage et taille ($name)",
                        description = "Vérifier la bonne tenue des tuteurs ou filets. Éliminer les gourmands si nécessaire (surtout pour les tomates).",
                        priority = ReminderPriority.LOW,
                        category = "Culture",
                        itemName = name
                    )
                )
            }
            "Floraison" -> {
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_flower_potash",
                        title = "Changement de fertilisation ($name)",
                        description = "Réduire les apports azotés. Privilégier un engrais organique riche en potassium et phosphore (le purin de consoude est idéal) pour stimuler les fleurs de $type.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_flower_pollination",
                        title = "Suivi des pollinisateurs ($name)",
                        description = "S'assurer de la présence d'abeilles ou d'insectes auxiliaires. Aérer les serres pour faciliter la pollinisation naturelle.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_flower_nodamp",
                        title = "Arrosage strict au pied ($name)",
                        description = "Éviter impérativement de mouiller les fleurs lors de l'arrosage de $type pour réduire les risques de pourriture grise ou d'avortement floral.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
            }
            "Récolte" -> {
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_harvest_check",
                        title = "Évaluation de maturité ($name)",
                        description = "Récolter quotidiennement les fruits/légumes de $type mûrs à point pour encourager les nouvelles vagues de maturation.",
                        priority = ReminderPriority.HIGH,
                        category = "Culture",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "crop_${crop.id}_harvest_handle",
                        title = "Cueillette soignée ($name)",
                        description = "Utiliser des sécateurs propres pour couper proprement la tige de $type sans arracher le rameau.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Culture",
                        itemName = name
                    )
                )
            }
        }
        return reminders
    }

    /**
     * Generates daily care reminders based on the cycle stage of a registered livestock group.
     */
    fun generateLivestockReminders(livestock: Livestock): List<CareReminder> {
        val reminders = mutableListOf<CareReminder>()
        val name = livestock.name
        val type = livestock.type

        // Add reminders based on health status first
        when (livestock.healthStatus) {
            "Sous traitement" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_health_treat",
                        title = "Administration du traitement ($name)",
                        description = "Administrer scrupuleusement la médication / le soin prescrit par le vétérinaire pour le troupeau de ${livestock.breed}.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
            "À isoler" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_health_isolate",
                        title = "Quarantaine et isolement ($name)",
                        description = "Isoler immédiatement le ou les sujets malades de $name dans un box de quarantaine désinfecté pour éviter la contagion.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
        }

        // Add reminders based on current cycle stage
        when (livestock.currentCycle) {
            "Gestation" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_gestation_diet",
                        title = "Suivi nutritionnel gestation ($name)",
                        description = "Distribuer un aliment concentré riche en protéines, calcium et minéraux pour soutenir le développement fœtal des femelles gravides.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_gestation_stress",
                        title = "Réduction du stress ($name)",
                        description = "Éviter les bruits soudains, les regroupements violents et limiter les déplacements de ce groupe de ${livestock.breed}.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
            "Lactation" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_lactation_water",
                        title = "Hydratation maximale ($name)",
                        description = "La lactation exige d'énormes volumes d'eau. S'assurer que les abreuvoirs automatiques de $name fournissent de l'eau claire et fraîche en permanence.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_lactation_hygiene",
                        title = "Hygiène mammaire et traite ($name)",
                        description = "Nettoyer soigneusement les mamelles avant la traite pour prévenir les mammites et veiller au bien-être des jeunes allaités.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
            "Croissance" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_growth_decouple",
                        title = "Suivi du sevrage et ration d'élevage ($name)",
                        description = "Fournir une ration riche en azote protéique assimilable et s'assurer de l'accès aisé aux blocs de sel de léchage mineral.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_growth_parasites",
                        title = "Contrôle parasitaire ($name)",
                        description = "Vérifier la date du dernier traitement anti-parasitaire (vermifugation). Inspecter l'enclos pour repérer d'éventuels signes de diarrhées.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
            "Engraissement" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_fattening_feed",
                        title = "Ration énergétique ($name)",
                        description = "Vérifier la consommation de la ration d'engraissement (riche en amidon/céréales). Ajuster l'indice de consommation.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_fattening_weigh",
                        title = "Suivi de croissance pondérale ($name)",
                        description = "Peser un échantillon d'animaux de $name pour contrôler le gain moyen quotidien (GMQ). Recalibrer les doses si nécessaire.",
                        priority = ReminderPriority.LOW,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
            "Ponte" -> {
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_laying_calcium",
                        title = "Apport en coquilles de calcium ($name)",
                        description = "Distribuer des coquilles d'huîtres broyées ou compléments calciques pour renforcer la qualité et dureté des coquilles d'œufs.",
                        priority = ReminderPriority.HIGH,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_laying_eggs",
                        title = "Ramassage des œufs ($name)",
                        description = "Récolter les œufs manuellement 2 à 3 fois par jour pour éviter les bris, les salissures et limiter les comportements de picage.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Élevage",
                        itemName = name
                    )
                )
                reminders.add(
                    CareReminder(
                        id = "live_${livestock.id}_laying_light",
                        title = "Contrôle du minutage lumineux ($name)",
                        description = "S'assurer que la minuterie d'éclairage fournit exactement entre 14h et 16h de lumière quotidiennement pour soutenir le stimulus de ponte.",
                        priority = ReminderPriority.MEDIUM,
                        category = "Élevage",
                        itemName = name
                    )
                )
            }
        }
        return reminders
    }

    /**
     * Generates a combined sorted list of daily care reminders for all farm assets.
     */
    fun generateAllReminders(crops: List<Crop>, livestock: List<Livestock>): List<CareReminder> {
        val all = mutableListOf<CareReminder>()
        crops.forEach { all.addAll(generateCropReminders(it)) }
        livestock.forEach { all.addAll(generateLivestockReminders(it)) }
        // Sort: HIGH priority first, then MEDIUM, then LOW
        return all.sortedBy {
            when (it.priority) {
                ReminderPriority.HIGH -> 0
                ReminderPriority.MEDIUM -> 1
                ReminderPriority.LOW -> 2
            }
        }
    }
}
