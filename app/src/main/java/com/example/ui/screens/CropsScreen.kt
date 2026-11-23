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
import com.example.data.model.Crop
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsScreen(
    viewModel: FarmViewModel
) {
    val crops by viewModel.allCrops.collectAsState()
    var selectedCrop by remember { mutableStateOf<Crop?>(null) }

    // Sync selected crop if it updates in list
    val currentSelected = crops.find { it.id == selectedCrop?.id } ?: crops.firstOrNull()

    val stages = listOf("Semis", "Levée", "Croissance", "Floraison", "Récolte")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi de mes Cultures", fontWeight = FontWeight.Bold) },
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
            // Left Panel: List of Crops (takes 40% on wide screen / behaves responsively)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    "Mes parcelles",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                if (crops.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucune culture",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(crops) { crop ->
                            val isSelected = crop.id == currentSelected?.id
                            Card(
                                onClick = { selectedCrop = crop },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .testTag("crop_item_${crop.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(crop.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(crop.type, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(crop.currentStage, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Panel: Selected Crop Details (60%)
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
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Sélectionnez une culture pour débuter le suivi de cycle", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(currentSelected.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("${currentSelected.type} • ${currentSelected.variety}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(currentSelected.currentStage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Superficie", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentSelected.area} ${currentSelected.areaUnit}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Âge de culture", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentSelected.getAgeInDays()} jours", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            if (currentSelected.notes.isNotBlank()) {
                                Text("Notes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentSelected.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteCrop(currentSelected.id) },
                                modifier = Modifier.align(Alignment.End).testTag("delete_crop_${currentSelected.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Stepper Progression Indicator
                    Text("Progression de la Culture", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        stages.forEachIndexed { index, stage ->
                            val isCompleted = stages.indexOf(currentSelected.currentStage) >= index
                            val isActive = currentSelected.currentStage == stage

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) {
                                        MaterialTheme.colorScheme.primary
                                    } else if (isCompleted) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        stage,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (index < stages.size - 1) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }

                    // Cycle trigger button
                    val currentIndex = stages.indexOf(currentSelected.currentStage)
                    if (currentIndex < stages.size - 1) {
                        val nextStageName = stages[currentIndex + 1]
                        Button(
                            onClick = { viewModel.updateCropStage(currentSelected.id, nextStageName) },
                            modifier = Modifier.fillMaxWidth().testTag("next_stage_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Passer à l'étape : $nextStageName")
                        }
                    }

                    // Contextual Stage Checklist Tasks
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
                                Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Check-list d'entretien du cycle : ${currentSelected.currentStage}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            val tasks = when (currentSelected.currentStage) {
                                "Semis" -> listOf(
                                    "Préparer un lit de semence meuble et aéré",
                                    "Semer à la profondeur adéquate (e.g. 2-3x la taille de graine)",
                                    "Arrosage régulier en pluie fine pour maintenir le sol humide"
                                )
                                "Levée" -> listOf(
                                    "Éliminer les plantules en surnombre si trop denses",
                                    "Surveiller l'apparition des limaces et des nuisibles de sol",
                                    "Désherbage manuel minutieux pour ne pas déranger les racines"
                                )
                                "Croissance" -> listOf(
                                    "Apporter un engrais azoté organique liquide",
                                    "Mettre en place les tuteurs si besoin d'érection",
                                    "Arroser en profondeur deux à trois fois par semaine"
                                )
                                "Floraison" -> listOf(
                                    "Réduire l'apport en azote et augmenter le potassium",
                                    "Arroser rigoureusement au pied (ne pas mouiller les fleurs)",
                                    "Surveiller la présence des pollinisateurs et abeilles"
                                )
                                "Récolte" -> listOf(
                                    "Procéder à la cueillette tôt le matin pour garder la fermeté",
                                    "Utiliser des outils désinfectés pour éviter les blessures du plant",
                                    "Stocker dans un endroit sec, propre et ombragé"
                                )
                                else -> emptyList()
                            }

                            tasks.forEach { task ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(task, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
