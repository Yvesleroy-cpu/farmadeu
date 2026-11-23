package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Livestock
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockScreen(
    viewModel: FarmViewModel
) {
    val livestock by viewModel.allLivestock.collectAsState()
    var selectedLive by remember { mutableStateOf<Livestock?>(null) }

    val currentSelected = livestock.find { it.id == selectedLive?.id } ?: livestock.firstOrNull()

    val cycles = listOf("Gestation", "Lactation", "Croissance", "Engraissement", "Ponte")
    val healthStatuses = listOf("Excellent", "Sous traitement", "À isoler")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion de mon Élevage", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Left Panel: Herd list (40%)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    "Mes Groupes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )

                if (livestock.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucun élevage",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(livestock) { live ->
                            val isSelected = live.id == currentSelected?.id
                            Card(
                                onClick = { selectedLive = live },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .testTag("live_item_${live.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(live.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${live.count} ${live.type}(s)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(live.currentCycle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Panel: Livestock details (60%)
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentSelected == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text("Sélectionnez un élevage pour suivre son statut sanitaire ou cycle.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(currentSelected.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("${currentSelected.type} • Race : ${currentSelected.breed}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(currentSelected.currentCycle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Effectif du troupeau", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentSelected.count} têtes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Temps d'élevage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentSelected.getDaysInHerd()} jours", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            // Interactive Health Status Segmented Control
                            Text("Statut Sanitaire Actuel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                healthStatuses.forEach { status ->
                                    val isSelected = currentSelected.healthStatus == status
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateLivestockHealth(currentSelected.id, status) },
                                        label = { Text(status, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (status == "Excellent") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            selectedLabelColor = if (status == "Excellent") Color(0xFF2E7D32) else Color(0xFFC62828)
                                        ),
                                        modifier = Modifier.testTag("health_status_${status}")
                                    )
                                }
                            }

                            if (currentSelected.notes.isNotBlank()) {
                                Text("Notes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentSelected.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteLivestock(currentSelected.id) },
                                modifier = Modifier.align(Alignment.End).testTag("delete_live_${currentSelected.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Stepper Cycle Progression
                    Text("Phase de Cycle", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        cycles.forEachIndexed { index, cycle ->
                            val isActive = currentSelected.currentCycle == cycle

                            Card(
                                onClick = { viewModel.updateLivestockCycle(currentSelected.id, cycle) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    cycle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            if (index < cycles.size - 1) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }

                    // Contextual Care Timelines
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text("Soins & Alimentation : ${currentSelected.currentCycle}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            val adviceList = when (currentSelected.currentCycle) {
                                "Gestation" -> listOf(
                                    "Isoler les femelles gravides des mâles et du reste du groupe",
                                    "Augmenter l'aliment riche en minéraux (calcium et phosphore)",
                                    "Planifier la vaccination pré-part sous recommandation"
                                )
                                "Lactation" -> listOf(
                                    "Assurer de l'eau propre illimitée (la lactation consomme énormément)",
                                    "Fournir un fourrage d'excellente qualité et repas concentrés",
                                    "Traire régulièrement et surveiller l'absence de mammites"
                                )
                                "Croissance" -> listOf(
                                    "Aliment de sevrage équilibré et protéines digestes",
                                    "Traiter préventivement contre les parasites internes (vermifuge)",
                                    "Garantir un enclos propre avec une litière sèche"
                                )
                                "Engraissement" -> listOf(
                                    "Ratio énergétique élevé avec céréales de qualité",
                                    "Limiter les grands déplacements inutiles de l'animal",
                                    "Vérifier régulièrement l'indice de consommation"
                                )
                                "Ponte" -> listOf(
                                    "Ajouter du calcaire broyé/coquilles d'huitres pour la solidité de coquille",
                                    "Maintenir une durée d'éclairage de 14 à 16 heures par jour",
                                    "Ramasser régulièrement les oeufs (2 à 3 fois par jour)"
                                )
                                else -> emptyList()
                            }

                            adviceList.forEach { advice ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.OfflineBolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Text(advice, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
