package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Crop
import com.example.data.model.Livestock
import com.example.ui.viewmodel.FarmViewModel
import com.example.util.ReminderGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FarmViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToCrops: () -> Unit,
    onNavigateToLivestock: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val crops by viewModel.allCrops.collectAsState()
    val livestock by viewModel.allLivestock.collectAsState()

    var showAddCropDialog by remember { mutableStateOf(false) }
    var showAddLivestockDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = "Farmadeu Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Farmadeu",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    val alerts by viewModel.alerts.collectAsState()
                    val count = alerts.size
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notification_bell_icon")
                    ) {
                        BadgedBox(
                            badge = {
                                if (count > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("$count", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (count > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Alertes de Soins / Biosécurité",
                                tint = if (count > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("profile_avatar_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Mon Profil",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Card with dynamic gradients
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bienvenue sur Farmadeu !",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Gérez vos cultures, suivez vos élevages étape par étape et bénéficiez de l'intelligence artificielle pour diagnostiquer les anomalies et optimiser vos rendements.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // --- Agri-météo & Localisation Card ---
            item {
                WeatherAlertsCard(viewModel = viewModel)
            }

            // --- Active Biosafety & Care Alerts Banner ---
            item {
                val alerts by viewModel.alerts.collectAsState()
                if (alerts.isNotEmpty()) {
                    val highAlerts = remember(alerts) { alerts.filter { it.priority == "HIGH" } }
                    val displayAlert = remember(alerts, highAlerts) { if (highAlerts.isNotEmpty()) highAlerts.first() else alerts.first() }
                    
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNotificationsDialog = true }
                            .testTag("active_alerts_banner"),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Soin critique",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = displayAlert.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = displayAlert.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = { showNotificationsDialog = true }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text(
                                    "Voir tout (${alerts.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Stats Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCrops() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = "Crops",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Mes Cultures", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${crops.size} active(s)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToLivestock() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = "Livestock",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text("Mon Élevage", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${livestock.sumOf { it.count }} tête(s)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Core Rapports Mensuels Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReports() }
                        .testTag("reports_dashboard_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Rapports & Rendements Mensuels",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Consultez les bilans de récoltes et la productivité sous forme de graphiques Recharts.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ouvrir les analyses",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Core Gestion d'Inventaire Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() }
                        .testTag("inventory_dashboard_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Gestion de l'Inventaire & Stocks",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Suivez les stocks d'engrais, semences et aliments avec alertes automatiques de seuil bas.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ouvrir l'inventaire",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Care Reminders Section
            item {
                val reminders = remember(crops, livestock) {
                    ReminderGenerator.generateAllReminders(crops, livestock)
                }
                var completedReminderIds by remember { mutableStateOf(setOf<String>()) }
                val activeReminders = reminders.filter { it.id !in completedReminderIds }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Rappels de soins du jour",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (activeReminders.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${activeReminders.size}", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (reminders.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Aucun rappel aujourd'hui",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Enregistrez des cultures ou bêtes pour générer automatiquement vos listes de tâches quotidiennes.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    lineHeight = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else if (activeReminders.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Toutes les tâches sont faites !",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Excellent travail ! Vos cultures et vos animaux sont en parfaite santé.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeReminders.forEach { reminder ->
                                val categoryColor = if (reminder.category == "Culture") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }

                                val priorityColor = when (reminder.priority) {
                                    com.example.util.ReminderPriority.HIGH -> MaterialTheme.colorScheme.error
                                    com.example.util.ReminderPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                    com.example.util.ReminderPriority.LOW -> MaterialTheme.colorScheme.outline
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reminder_item_${reminder.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    completedReminderIds = completedReminderIds + reminder.id
                                                }
                                            },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("reminder_checkbox_${reminder.id}")
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (reminder.category == "Culture") Icons.Default.Grass else Icons.Default.Pets,
                                                        contentDescription = null,
                                                        tint = categoryColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = reminder.category,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = categoryColor
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(priorityColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = when (reminder.priority) {
                                                            com.example.util.ReminderPriority.HIGH -> "URGENT"
                                                            com.example.util.ReminderPriority.MEDIUM -> "IMPORTANT"
                                                            com.example.util.ReminderPriority.LOW -> "CONSEILLÉ"
                                                        },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = priorityColor
                                                    )
                                                }
                                            }

                                            Text(
                                                text = reminder.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Text(
                                                text = reminder.description,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick AI Scan Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToScan() }
                        .testTag("dashboard_scan_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scanner",
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Analyse & Scan de maladie IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Scannez des symptômes de plante ou maladies d'animaux pour diagnostic.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Aller au scanner",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Registered Crops Mini View
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mes Cultures Actives",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { showAddCropDialog = true },
                        modifier = Modifier.testTag("add_crop_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enregistrer")
                    }
                }

                if (crops.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = "No crops",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "Aucune culture enregistrée",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddCropDialog = true },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Enregistrer une culture")
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(crops) { crop ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { onNavigateToCrops() },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(crop.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(crop.currentStage, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                    Text(crop.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Variété : ${crop.variety}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Superficie : ${crop.area} ${crop.areaUnit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    LinearProgressIndicator(
                                        progress = { crop.getStageProgressIndex() / 5.0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Registered Livestock Mini View
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mon Élevage",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { showAddLivestockDialog = true },
                        modifier = Modifier.testTag("add_livestock_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enregistrer")
                    }
                }

                if (livestock.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = "No animals",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "Aucun animal enregistré",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddLivestockDialog = true },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Enregistrer un élevage")
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(livestock) { live ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { onNavigateToLivestock() },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(live.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (live.healthStatus == "Excellent") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                live.healthStatus, 
                                                fontSize = 10.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = if (live.healthStatus == "Excellent") Color(0xFF2E7D32) else Color(0xFFC62828)
                                            )
                                        }
                                    }
                                    Text(live.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Race : ${live.breed}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Effectif : ${live.count} tête(s)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Autorenew, contentDescription = "Cycle", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Text("Stade : ${live.currentCycle}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // --- System & Care Alerts Modal Bottom Sheet ---
        if (showNotificationsDialog) {
            val alerts by viewModel.alerts.collectAsState()
            
            ModalBottomSheet(
                onDismissRequest = { showNotificationsDialog = false },
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("notifications_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertes de Soins & Biosécurité",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (alerts.isNotEmpty()) {
                            Text(
                                text = "${alerts.size} Active(s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    if (alerts.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Pas d'alerte",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Parfait sanitaire !",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Aucune alerte de biosécurité ou de stade de croissance critique active.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            items(alerts, key = { it.id }) { alert ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (alert.priority == "HIGH") {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (alert.priority == "HIGH") {
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val (categoryIcon, categoryColor) = when (alert.category) {
                                                    "Culture" -> Icons.Default.Grass to MaterialTheme.colorScheme.primary
                                                    "Inventaire" -> Icons.Default.Warning to MaterialTheme.colorScheme.error
                                                    "Météo" -> Icons.Default.Cloud to MaterialTheme.colorScheme.outline
                                                    "Tâche" -> Icons.Default.CalendarToday to MaterialTheme.colorScheme.tertiary
                                                    else -> Icons.Default.Pets to MaterialTheme.colorScheme.secondary
                                                }
                                                Icon(
                                                    imageVector = categoryIcon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = categoryColor
                                                )
                                                Text(
                                                    text = if (alert.category == "Inventaire" || alert.category == "Météo" || alert.category == "Tâche") "${alert.category} • ${alert.stage}" else "${alert.category} • Stade ${alert.stage}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = categoryColor
                                                )
                                            }
                                            
                                            // Action dismiss individual
                                            IconButton(
                                                onClick = { viewModel.dismissAlert(alert.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Ignorer",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = alert.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = alert.message,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showNotificationsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }

        // Add Crop Modal dialog
        if (showAddCropDialog) {
            AddCropDialog(
                onDismiss = { showAddCropDialog = false },
                onConfirm = { name, type, variety, area, unit, notes ->
                    viewModel.addCrop(name, type, variety, area, unit, notes)
                    showAddCropDialog = false
                }
            )
        }

        // Add Livestock Modal dialog
        if (showAddLivestockDialog) {
            AddLivestockDialog(
                onDismiss = { showAddLivestockDialog = false },
                onConfirm = { name, type, breed, count, health, notes ->
                    viewModel.addLivestock(name, type, breed, count, health, notes)
                    showAddLivestockDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Tomate") }
    var variety by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("ha") }
    var notes by remember { mutableStateOf("") }

    val cropTypes = listOf("Tomate", "Blé", "Maïs", "Pomme de terre", "Riz", "Laitue", "Carotte")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Enregistrer une Culture",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la parcelle / culture") },
                    modifier = Modifier.fillMaxWidth().testTag("crop_dialog_name")
                )
            }

            item {
                Text("Type de culture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(cropTypes) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variété (ex: Coeur de Boeuf)") },
                    modifier = Modifier.fillMaxWidth().testTag("crop_dialog_variety")
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text("Superficie") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("crop_dialog_area")
                    )

                    Row(modifier = Modifier.weight(1f)) {
                        RadioButton(selected = unit == "ha", onClick = { unit = "ha" })
                        Text("ha", modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = unit == "m²", onClick = { unit = "m²" })
                        Text("m²", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes optionnelles") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val area = areaText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && variety.isNotBlank()) {
                                onConfirm(name, selectedType, variety, area, unit, notes)
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("crop_dialog_save"),
                        enabled = name.isNotBlank() && variety.isNotBlank()
                    ) {
                        Text("Sauvegarder")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLivestockDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Mouton") }
    var breed by remember { mutableStateOf("") }
    var countText by remember { mutableStateOf("") }
    var health by remember { mutableStateOf("Excellent") }
    var notes by remember { mutableStateOf("") }

    val animalTypes = listOf("Mouton", "Bovin", "Volaille", "Porc", "Chèvre")
    val healthStatuses = listOf("Excellent", "Sous traitement", "À isoler")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Enregistrer un Élevage",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du groupe / enclos") },
                    modifier = Modifier.fillMaxWidth().testTag("live_dialog_name")
                )
            }

            item {
                Text("Type d'animal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(animalTypes) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Race des animaux (ex: Mérinos)") },
                    modifier = Modifier.fillMaxWidth().testTag("live_dialog_breed")
                )
            }

            item {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = { Text("Nombre de têtes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("live_dialog_count")
                )
            }

            item {
                Text("Statut sanitaire initial", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    healthStatuses.forEach { status ->
                        FilterChip(
                            selected = health == status,
                            onClick = { health = status },
                            label = { Text(status) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes importantes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val count = countText.toIntOrNull() ?: 1
                            if (name.isNotBlank() && breed.isNotBlank()) {
                                onConfirm(name, selectedType, breed, count, health, notes)
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("live_dialog_save"),
                        enabled = name.isNotBlank() && breed.isNotBlank()
                    ) {
                        Text("Sauvegarder")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsCard(
    viewModel: com.example.ui.viewmodel.FarmViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val weatherReport by viewModel.weatherReport.collectAsState()
    val weatherOverride by viewModel.weatherOverride.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val crops by viewModel.allCrops.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_alerts_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Météo et Vigilance Gel/Pluie",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alertes météo personnalisées",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isWeatherLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("weather_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualiser la météo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("weather_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Région de l'exploitation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            weatherReport?.let { report ->
                // Basic Weather Display Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Exploitation : ${report.cityName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${report.currentTemp.toInt()}°C",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Min: ${report.minTemp.toInt()}°C • Max: ${report.maxTemp.toInt()}°C",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = report.skyDescription,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (report.prepSum > 0.0) {
                            Text(
                                text = "Précipitations : ${String.format("%.1f", report.prepSum)} mm attendus",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Pas de précipitations prévues",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Weather Status Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    report.isGelAlert -> MaterialTheme.colorScheme.errorContainer
                                    report.isPluieAlert -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val weatherIcon = when {
                            report.isGelAlert -> Icons.Default.AcUnit
                            report.isPluieAlert -> Icons.Default.Thunderstorm
                            report.prepSum > 2.0 -> Icons.Default.CloudQueue
                            else -> Icons.Default.WbSunny
                        }
                        val weatherIconTint = when {
                            report.isGelAlert -> MaterialTheme.colorScheme.onErrorContainer
                            report.isPluieAlert -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                        Icon(
                            imageVector = weatherIcon,
                            contentDescription = report.skyDescription,
                            tint = weatherIconTint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Dynamic simulation mode pill
                if (report.isMock) {
                    val simLabel = when (weatherOverride) {
                        "GEL" -> "🔬 Simulation active : Risque Gel"
                        "PLUIE" -> "🔬 Simulation active : Pluie Intense"
                        "SOLEIL" -> "🔬 Simulation active : Beau Temps"
                        else -> "Données simulées (Hors-ligne / Fallback)"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = simLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Warnings section custom customized to context crops
                if (report.isGelAlert || report.isPluieAlert) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (report.isGelAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alerte active",
                                tint = if (report.isGelAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (report.isGelAlert) "Alerte de gel agricole active !" else "Précipitations torrentielles !",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (report.isGelAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        // Personalized Crop risks assessment
                        val sensitiveCrops = crops.filter { crop ->
                            if (report.isGelAlert) {
                                crop.currentStage in listOf("Semis", "Floraison")
                            } else {
                                crop.currentStage == "Semis"
                            }
                        }

                        if (sensitiveCrops.isNotEmpty()) {
                            val cropsList = sensitiveCrops.joinToString { it.name }
                            Text(
                                text = "⚠️ Parcelles sensibles : $cropsList\nAction requise d'urgence car ces cultures sont actuellement au stade critique de '${sensitiveCrops.first().currentStage}'.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (report.isGelAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "Aucune parcelle n'est enregistrée dans une phase de vulnérabilité maximale, mais surveillez les cultures de près.",
                                fontSize = 11.sp,
                                color = if (report.isGelAlert) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = report.advice,
                            fontSize = 11.sp,
                            color = if (report.isGelAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    // Regular agricultural tip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Conseil météo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = report.advice,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Config and simulation modal popup
    if (showConfigDialog) {
        Dialog(onDismissRequest = { showConfigDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("weather_config_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Localisation & Simulations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Choisissez la localisation de votre exploitation pour obtenir les prévisions réelles de l'API Open-Meteo :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Locations vertical list
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(com.example.network.WeatherClient.regions) { region ->
                            val isSelected = region.city.equals(selectedCity, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateFarmLocation(region.city)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = region.city,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = region.regionName,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Sélectionné",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Simuler des risques extrêmes (gel / fortes pluies) :",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Simulator chip buttons in a flow / row wrapping format
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Real
                        FilterChip(
                            selected = weatherOverride == null,
                            onClick = { viewModel.updateWeatherOverride(null) },
                            label = { Text("Réel (API)", fontSize = 10.sp) },
                            modifier = Modifier.testTag("weather_sim_real_chip")
                        )

                        // Frost
                        FilterChip(
                            selected = weatherOverride == "GEL",
                            onClick = { viewModel.updateWeatherOverride("GEL") },
                            label = { Text("Risque Gel", fontSize = 10.sp) },
                            modifier = Modifier.testTag("weather_sim_gel_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )

                        // Pluie
                        FilterChip(
                            selected = weatherOverride == "PLUIE",
                            onClick = { viewModel.updateWeatherOverride("PLUIE") },
                            label = { Text("Pluie forte", fontSize = 10.sp) },
                            modifier = Modifier.testTag("weather_sim_pluie_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )

                        // Soleil
                        FilterChip(
                            selected = weatherOverride == "SOLEIL",
                            onClick = { viewModel.updateWeatherOverride("SOLEIL") },
                            label = { Text("Soleil", fontSize = 10.sp) },
                            modifier = Modifier.testTag("weather_sim_sunny_chip")
                        )
                    }

                    Button(
                        onClick = { showConfigDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}
