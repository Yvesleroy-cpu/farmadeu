package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Crop
import com.example.data.model.Livestock
import com.example.data.model.FarmTask
import com.example.data.repository.FarmRepository
import com.example.network.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.example.data.model.InventoryItem

class FarmViewModel(
    application: Application,
    private val repository: FarmRepository
) : AndroidViewModel(application) {

    // --- Database Flow ---
    val allCrops: StateFlow<List<Crop>> = repository.allCrops
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLivestock: StateFlow<List<Livestock>> = repository.allLivestock
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allScanResults: StateFlow<List<com.example.data.model.ScanResult>> = repository.allScanResults
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTasks: StateFlow<List<FarmTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allInventory: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Subscription State ---
    private val _currentSubscription = MutableStateFlow("HarvestPro") // Default active subscription
    val currentSubscription: StateFlow<String> = _currentSubscription.asStateFlow()

    private val _subscriptionExpiry = MutableStateFlow(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // 30 days from now
    val subscriptionExpiry: StateFlow<Long> = _subscriptionExpiry.asStateFlow()

    fun updateSubscription(planSku: String) {
        _currentSubscription.value = planSku
        _subscriptionExpiry.value = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 // renew 30 days
    }
    
    fun renewSubscription() {
        _subscriptionExpiry.value = _subscriptionExpiry.value + 30L * 24 * 60 * 60 * 1000
    }

    fun cancelSubscription() {
        _currentSubscription.value = "GermeFree"
    }

    // --- Localisation & Météo State ---
    private val sharedPrefs = application.getSharedPreferences("farm_prefs", android.content.Context.MODE_PRIVATE)

    private val _selectedCity = MutableStateFlow(sharedPrefs.getString("selected_city", "Valence") ?: "Valence")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _weatherReport = MutableStateFlow<com.example.network.WeatherClient.FarmWeatherReport?>(null)
    val weatherReport: StateFlow<com.example.network.WeatherClient.FarmWeatherReport?> = _weatherReport.asStateFlow()

    private val _weatherOverride = MutableStateFlow<String?>(sharedPrefs.getString("weather_override", null))
    val weatherOverride: StateFlow<String?> = _weatherOverride.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    fun updateFarmLocation(cityName: String) {
        viewModelScope.launch {
            _selectedCity.value = cityName
            sharedPrefs.edit().putString("selected_city", cityName).apply()
            refreshWeather()
        }
    }

    fun updateWeatherOverride(override: String?) {
        viewModelScope.launch {
            _weatherOverride.value = override
            if (override == null) {
                sharedPrefs.edit().remove("weather_override").apply()
            } else {
                sharedPrefs.edit().putString("weather_override", override).apply()
            }
            refreshWeather()
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            val region = com.example.network.WeatherClient.regions.find { it.city.equals(_selectedCity.value, ignoreCase = true) }
                ?: com.example.network.WeatherClient.regions.first()
            
            val report = com.example.network.WeatherClient.getFarmWeather(
                city = region.city,
                latitude = region.latitude,
                longitude = region.longitude,
                overrideSimulation = _weatherOverride.value
            )
            _weatherReport.value = report
            _isWeatherLoading.value = false
            
            // Re-trigger alerts analysis with updated weather
            refreshAlerts(allCrops.value, allLivestock.value, allInventory.value, allTasks.value)
        }
    }

    // --- Dynamic Notifications & Alerts System ---
    data class FarmAlert(
        val id: String,
        val title: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis(),
        val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
        var isRead: Boolean = false,
        val category: String, // "Culture", "Élevage", "Météo"
        val stage: String // e.g. "Floraison", "Semis", "Gestation", etc.
    )

    private val _alerts = MutableStateFlow<List<FarmAlert>>(emptyList())
    val alerts: StateFlow<List<FarmAlert>> = _alerts.asStateFlow()

    init {
        // Initial weather load
        refreshWeather()

        // Reactively listen to database and weather updates to generate alert notifications
        viewModelScope.launch {
            allCrops.collect { crops ->
                refreshAlerts(crops, allLivestock.value, allInventory.value, allTasks.value)
            }
        }
        viewModelScope.launch {
            allLivestock.collect { livestock ->
                refreshAlerts(allCrops.value, livestock, allInventory.value, allTasks.value)
            }
        }
        viewModelScope.launch {
            allInventory.collect { inventory ->
                refreshAlerts(allCrops.value, allLivestock.value, inventory, allTasks.value)
            }
        }
        viewModelScope.launch {
            allTasks.collect { tasks ->
                refreshAlerts(allCrops.value, allLivestock.value, allInventory.value, tasks)
            }
        }
        viewModelScope.launch {
            weatherReport.collect {
                refreshAlerts(allCrops.value, allLivestock.value, allInventory.value, allTasks.value)
            }
        }

        // Seed default inventory items if database is empty
        viewModelScope.launch {
            val items = repository.allInventoryItems.first()
            if (items.isEmpty()) {
                repository.insertInventoryItem(InventoryItem(name = "Engrais bio NPK 4-8-12", category = "Engrais", quantity = 120.0, unit = "kg", lowThreshold = 150.0, notes = "Idéal pour tomates et aubergines"))
                repository.insertInventoryItem(InventoryItem(name = "Chaux magnésienne l'Or vert", category = "Engrais", quantity = 450.0, unit = "kg", lowThreshold = 200.0, notes = "Correction de l'acidité du sol"))
                repository.insertInventoryItem(InventoryItem(name = "Semences de Tomate Cœur de Bœuf", category = "Semences", quantity = 1.2, unit = "kg", lowThreshold = 5.0, notes = "Semis de début de saison"))
                repository.insertInventoryItem(InventoryItem(name = "Semences Blé Bio l'Épi", category = "Semences", quantity = 800.0, unit = "kg", lowThreshold = 300.0, notes = "Semence certifiée d'origine France"))
                repository.insertInventoryItem(InventoryItem(name = "Granulés Pondeuses Régal", category = "Aliments pour animaux", quantity = 350.0, unit = "kg", lowThreshold = 100.0, notes = "Aliment de ponte, riche en calcium"))
                repository.insertInventoryItem(InventoryItem(name = "Aliment Croissance Porcelets", category = "Aliments pour animaux", quantity = 65.0, unit = "kg", lowThreshold = 150.0, notes = "Ration protéinée de sevrage"))
            }
        }
    }

    fun refreshAlerts(
        crops: List<Crop>,
        livestock: List<Livestock>,
        inventory: List<InventoryItem> = emptyList(),
        tasks: List<FarmTask> = emptyList()
    ) {
        val newAlerts = mutableListOf<FarmAlert>()

        // --- Imminent & Overdue Tasks Alerts ---
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000
        val twoDaysMs = 48L * 60 * 60 * 1000

        tasks.filter { !it.isCompleted }.forEach { task ->
            val diffMs = task.date - now
            if (diffMs in 0..twoDaysMs) {
                val whenText = if (diffMs < oneDayMs) "aujourd'hui" else "demain"
                newAlerts.add(
                    FarmAlert(
                        id = "alert_task_${task.id}_imminent",
                        title = "📅 TÂCHE IMMINENTE : ${task.title}",
                        message = "La tâche '${task.title}' ${if (task.targetName.isNotEmpty()) "pour ${task.targetName}" else ""} est prévue pour $whenText. N'oubliez pas de la réaliser !",
                        priority = "HIGH",
                        category = "Tâche",
                        stage = "À faire"
                    )
                )
            } else if (diffMs < 0 && (now - task.date) < 14 * oneDayMs) { // Show overdue tasks from the last 14 days
                newAlerts.add(
                    FarmAlert(
                        id = "alert_task_${task.id}_overdue",
                        title = "⏳ TÂCHE EN RETARD : ${task.title}",
                        message = "La tâche '${task.title}' ${if (task.targetName.isNotEmpty()) "pour ${task.targetName}" else ""} est en retard ! Pensez à l'accomplir dès que possible.",
                        priority = "MEDIUM",
                        category = "Tâche",
                        stage = "En retard"
                    )
                )
            }
        }

        // --- Low Stock Inventory Alerts ---
        inventory.forEach { item ->
            if (item.isLowStock()) {
                newAlerts.add(
                    FarmAlert(
                        id = "alert_inventory_${item.id}_low_stock",
                        title = "⚠️ STOCK CRITIQUE : ${item.name}",
                        message = "Le stock actuel de ${item.name} (${item.quantity} ${item.unit}) est inférieur au seuil bas requis de ${item.lowThreshold} ${item.unit}. Pensez à réapprovisionner !",
                        priority = "HIGH",
                        category = "Inventaire",
                        stage = "Seuil Bas"
                    )
                )
            }
        }
        
        crops.forEach { crop ->
            when (crop.currentStage) {
                "Semis" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_crop_${crop.id}_semis",
                            title = "Soins Préliminaires : ${crop.name}",
                            message = "Semis en cours. Températures requises : 18°C à 24°C, arrosage doux permanent par aspersion.",
                            priority = "MEDIUM",
                            category = "Culture",
                            stage = "Semis"
                        )
                    )
                }
                "Floraison" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_crop_${crop.id}_flower",
                            title = "ALERTE BIOSÉCURITÉ : Floraison ${crop.name}",
                            message = "Stade critique ! Arrosez impérativement au PIED pour éviter l'apparition du mildiou ou de maladies fongiques.",
                            priority = "HIGH",
                            category = "Culture",
                            stage = "Floraison"
                        )
                    )
                }
                "Récolte" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_crop_${crop.id}_harvest",
                            title = "Récolte Active : ${crop.name}",
                            message = "Produits majeurs mûrs ! Effectuez un ramassage régulier pour stimuler les nouvelles vagues de récolte.",
                            priority = "MEDIUM",
                            category = "Culture",
                            stage = "Récolte"
                        )
                    )
                }
            }
        }

        livestock.forEach { animal ->
            if (animal.healthStatus == "À isoler") {
                newAlerts.add(
                    FarmAlert(
                        id = "alert_live_${animal.id}_isolate",
                        title = "ALERTE VÉTÉRINAIRE CRITIQUE : ${animal.name}",
                        message = "Contagion suspectée ! Isolez immédiatement les sujets ${animal.breed} pour stopper la propagation.",
                        priority = "HIGH",
                        category = "Élevage",
                        stage = "À isoler"
                    )
                )
            } else if (animal.healthStatus == "Sous traitement") {
                newAlerts.add(
                    FarmAlert(
                        id = "alert_live_${animal.id}_treat",
                        title = "Suivi Médication : ${animal.name}",
                        message = "Traitement vétérinaire en cours sur ce groupe de ${animal.breed}. Veillez à l'administration complète des doses.",
                        priority = "HIGH",
                        category = "Élevage",
                        stage = "Sous traitement"
                    )
                )
            }

            when (animal.currentCycle) {
                "Gestation" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_live_${animal.id}_gestation",
                            title = "Alerte Gestation : ${animal.name}",
                            message = "Femelles gravides observées. Réduisez le stress et enrichissez les apports nutritifs en calcium.",
                            priority = "MEDIUM",
                            category = "Élevage",
                            stage = "Gestation"
                        )
                    )
                }
                "Lactation" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_live_${animal.id}_lactation",
                            title = "Régime Lactation : ${animal.name}",
                            message = "Suivi rigoureux d'hydratation de l'élevage de ${animal.type}. Abreuvoirs automatiques nettoyés requis 24h/24.",
                            priority = "MEDIUM",
                            category = "Élevage",
                            stage = "Lactation"
                        )
                    )
                }
                "Ponte" -> {
                    newAlerts.add(
                        FarmAlert(
                            id = "alert_live_${animal.id}_laying",
                            title = "Suivi Ponte : ${animal.name}",
                            message = "Ramassage quotidien des œufs de ${animal.breed} et complémentation calcique requise pour solidifier la coquille.",
                            priority = "MEDIUM",
                            category = "Élevage",
                            stage = "Ponte"
                        )
                    )
                }
            }
        }

        // --- Incorporate Weather Alerts ---
        weatherReport.value?.let { report ->
            if (report.isGelAlert) {
                newAlerts.add(
                    FarmAlert(
                        id = "alert_weather_gel",
                        title = "🔴 ALERTE GELÉE AU SOL : ${report.cityName}",
                        message = "Température minimale critique prévue à ${report.minTemp}°C ! Risque de gel sévère des cultures en cours. " + 
                                  "Paillez d'urgence vos parcelles (notamment celles au stade Semis/Floraison), bachez les serres et abritez le bétail jeune.",
                        priority = "HIGH",
                        category = "Météo",
                        stage = "Alerte Gel"
                    )
                )
            }
            if (report.isPluieAlert) {
                newAlerts.add(
                    FarmAlert(
                        id = "alert_weather_pluie",
                        title = "🟠 ALERTE PLUIE INTENSE : ${report.cityName}",
                        message = "Cumul d'eau de pluie prévu à ${report.prepSum} mm. " +
                                  "Attention à l'engorgement du sol, drainez les canaux et nettoyez les fossés pour éviter l'asphyxie racinaire de vos parcelles au stade Semis.",
                        priority = "HIGH",
                        category = "Météo",
                        stage = "Fortes Pluies"
                    )
                )
            }
        }

        // Trigger real lockscreen notifications for any NEW active alerts
        val existingIds = _alerts.value.map { it.id }.toSet()
        newAlerts.forEach { alert ->
            if (alert.id !in existingIds) {
                com.example.util.NotificationHelper.showNotification(
                    getApplication(),
                    alert.title,
                    alert.message
                )
            }
        }

        _alerts.value = newAlerts
    }

    fun dismissAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
    }

    // --- Crops Operations ---
    fun addCrop(name: String, type: String, variety: String, area: Double, unit: String, notes: String) {
        viewModelScope.launch {
            val crop = Crop(
                name = name,
                type = type,
                variety = variety,
                area = area,
                areaUnit = unit,
                currentStage = "Semis",
                notes = notes
            )
            repository.insertCrop(crop)
        }
    }

    fun updateCropStage(cropId: Long, nextStage: String) {
        viewModelScope.launch {
            val crops = allCrops.value
            val target = crops.find { it.id == cropId }
            if (target != null) {
                repository.updateCrop(
                    target.copy(
                        currentStage = nextStage,
                        currentStageStartDate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteCrop(cropId: Long) {
        viewModelScope.launch {
            repository.deleteCropById(cropId)
        }
    }

    // --- Livestock Operations ---
    fun addLivestock(name: String, type: String, breed: String, count: Int, healthStatus: String, notes: String) {
        viewModelScope.launch {
            val live = Livestock(
                name = name,
                type = type,
                breed = breed,
                count = count,
                healthStatus = healthStatus,
                currentCycle = when(type) {
                    "Volaille" -> "Ponte"
                    else -> "Croissance"
                },
                notes = notes
            )
            repository.insertLivestock(live)
        }
    }

    fun updateLivestockCycle(livestockId: Long, nextCycle: String) {
        viewModelScope.launch {
            val stock = allLivestock.value
            val target = stock.find { it.id == livestockId }
            if (target != null) {
                repository.updateLivestock(
                    target.copy(
                        currentCycle = nextCycle,
                        currentStageStartDate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateLivestockHealth(livestockId: Long, nextHealth: String) {
        viewModelScope.launch {
            val stock = allLivestock.value
            val target = stock.find { it.id == livestockId }
            if (target != null) {
                repository.updateLivestock(
                    target.copy(
                        healthStatus = nextHealth
                    )
                )
            }
        }
    }

    fun deleteLivestock(livestockId: Long) {
        viewModelScope.launch {
            repository.deleteLivestockById(livestockId)
        }
    }

    // --- AI Diagnose (Scanner) State ---
    data class ScanState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val pathology: String = "",
        val treatments: String = "",
        val toAvoid: String = "",
        val prevention: String = "",
        val rawResponse: String = "",
        val error: String? = null
    )

    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun resetScanState() {
        _scanState.value = ScanState()
    }

    fun performScan(type: String, symptoms: String, bitmap: Bitmap?) {
        viewModelScope.launch {
            _scanState.value = ScanState(isLoading = true)

            val base64Image = bitmap?.let { bmp ->
                try {
                    val outputStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                    Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                } catch (e: Exception) {
                    null
                }
            }

            val systemInstruction = """
                Tu es Farmadeu AI, un vétérinaire expert et agro-pathologiste de pointe.
                L'utilisateur te fournit un diagnostic sur une culture ou un animal.
                Tu dois impérativement retourner une réponse structurée avec EXACTEMENT ces 4 en-têtes en gras (Markdown) :
                1. **Pathologie Diagnostiquée :** indique le nom commun, le pathogène et une explication brève.
                2. **Produits et Traitements Recommandés :** liste les produits biologiques ou chimiques appropriés ainsi que la posologie/application.
                3. **Attitudes et Comportements à Éviter :** liste ce qu'un agriculteur ou éleveur ne doit JAMAIS faire pour ne pas empirer la situation.
                4. **Moyens de Prévention et Biosécurité :** liste les gestes de prévention indispensables pour l'avenir.
                
                Sois clair, direct, professionnel et rédige en français.
            """.trimIndent()

            val prompt = """
                Type d'analyse: $type
                Symptômes signalés: $symptoms
                
                Fournis le diagnostic complet avec la pathologie, les traitements à administrer, les attitudes à éviter absolument et les moyens de prévention.
            """.trimIndent()

            val response = GeminiClient.queryGemini(prompt, systemInstruction, base64Image)

            // Parse response sections
            if (response.startsWith("Note: Clé de l'API")) {
                val scState = ScanState(
                    isLoading = false,
                    isSuccess = true,
                    pathology = "Échec d'analyse de clé d'API",
                    treatments = "Veuillez configurer votre clé d'API dans AI Studio.",
                    toAvoid = "Aucun",
                    prevention = "Vérifiez vos secrets.",
                    rawResponse = response
                )
                _scanState.value = scState
                
                repository.insertScanResult(
                    com.example.data.model.ScanResult(
                        targetType = type,
                        symptoms = symptoms,
                        pathology = scState.pathology,
                        treatments = scState.treatments,
                        toAvoid = scState.toAvoid,
                        prevention = scState.prevention,
                        rawResponse = response
                    )
                )
            } else {
                val pathologyText = extractSection(response, "**Pathologie Diagnostiquée :**", "**Produits et Traitements Recommandés :**")
                val treatmentsText = extractSection(response, "**Produits et Traitements Recommandés :**", "**Attitudes et Comportements à Éviter :**")
                val toAvoidText = extractSection(response, "**Attitudes et Comportements à Éviter :**", "**Moyens de Prévention et Biosécurité :**")
                val preventionText = extractSection(response, "**Moyens de Prévention et Biosécurité :**", null)

                val scState = ScanState(
                    isLoading = false,
                    isSuccess = true,
                    pathology = pathologyText.ifEmpty { "Diagnostic IA Généré" },
                    treatments = treatmentsText.ifEmpty { "Se référer au rapport complet." },
                    toAvoid = toAvoidText.ifEmpty { "Éviter la propagation." },
                    prevention = preventionText.ifEmpty { "Isolement et désinfection." },
                    rawResponse = response
                )
                _scanState.value = scState

                repository.insertScanResult(
                    com.example.data.model.ScanResult(
                        targetType = type,
                        symptoms = symptoms,
                        pathology = scState.pathology,
                        treatments = scState.treatments,
                        toAvoid = scState.toAvoid,
                        prevention = scState.prevention,
                        rawResponse = response
                    )
                )
            }
        }
    }

    fun deleteScanResult(id: Long) {
        viewModelScope.launch {
            repository.deleteScanResultById(id)
        }
    }

    fun clearAllScanResults() {
        viewModelScope.launch {
            repository.clearScanHistory()
        }
    }

    private fun extractSection(text: String, startHeader: String, endHeader: String?): String {
        val startIndex = text.indexOf(startHeader)
        if (startIndex == -1) return ""
        val contentStart = startIndex + startHeader.length

        val contentEnd = if (endHeader != null) {
            val endIndex = text.indexOf(endHeader, contentStart)
            if (endIndex != -1) endIndex else text.length
        } else {
            text.length
        }

        return text.substring(contentStart, contentEnd).trim()
    }

    // --- Interactive Chat State ---
    data class ChatMessage(
        val sender: String, // "user" or "ai"
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "Bonjour ! Je suis Farmadeu AI, votre conseiller agronome et vétérinaire. Comment puis-je vous aider aujourd'hui ?")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _chatLoading.value = true

            val systemInstruction = """
                Tu es l'agronome et vétérinaire virtuel de Farmadeu. Ton rôle est de conseiller les agriculteurs et éleveurs dans la gestion au quotidien de leurs animaux et cultures.
                Donne des conseils avisés, professionnels, écologiques et économiquement viables.
                Réponds toujours de manière polie, enthousiaste et réconfortante en français.
            """.trimIndent()

            // Build historical context from last 6 messages
            val contextBuilder = StringBuilder()
            val history = _chatMessages.value.takeLast(6)
            for (msg in history) {
                val prefix = if (msg.sender == "user") "Utilisateur" else "Conseiller Farmadeu"
                contextBuilder.append("$prefix: ${msg.message}\n")
            }
            contextBuilder.append("Conseiller Farmadeu:")

            val response = GeminiClient.queryGemini(contextBuilder.toString(), systemInstruction)
            val cleanResponse = if (response.contains("Note: Clé de l'API")) {
                "D'accord ! Pour que je puisse vous guider parfaitement sur votre élevage ou culture de manière personnalisée par IA, s'il vous plaît renseignez votre Clé d'API Gemini. Autrement, veillez à bien respecter les cycles de soins, d'arrosage et les vaccins réglementaires de vos animaux."
            } else {
                response
            }

            _chatMessages.value = _chatMessages.value + ChatMessage("ai", cleanResponse)
            _chatLoading.value = false
        }
    }

    fun requestCycleAdvice(name: String, type: String, currentStage: String) {
        val requestText = "Donne-moi des conseils précis d'expert et un calendrier d'action pour ma culture de $type ($name) actuellement au stade de : $currentStage."
        sendMessage(requestText)
    }

    fun requestLivestockAdvice(name: String, type: String, currentCycle: String) {
        val requestText = "Donne-moi des conseils d'alimentation et soins sanitaires pratiques pour mon élevage de $type ($name) au cycle de : $currentCycle."
        sendMessage(requestText)
    }

    // --- Task Operations ---
    fun addTask(title: String, description: String, type: String, date: Long, targetName: String = "") {
        viewModelScope.launch {
            val task = FarmTask(
                title = title,
                description = description,
                type = type,
                date = date,
                targetName = targetName,
                isCompleted = false
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: FarmTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTaskById(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    // --- Inventory Operations ---
    fun addInventoryItem(name: String, category: String, quantity: Double, unit: String, lowThreshold: Double, notes: String = "") {
        viewModelScope.launch {
            val item = InventoryItem(
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                lowThreshold = lowThreshold,
                notes = notes,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertInventoryItem(item)
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    fun deleteInventoryItemById(id: Long) {
        viewModelScope.launch {
            repository.deleteInventoryItemById(id)
        }
    }
}

class FarmViewModelFactory(
    private val application: Application,
    private val repository: FarmRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FarmViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
