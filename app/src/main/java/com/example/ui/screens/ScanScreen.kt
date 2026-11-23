package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(viewModel: FarmViewModel) {
    val scanState by viewModel.scanState.collectAsState()
    val history by viewModel.allScanResults.collectAsState()
    val context = LocalContext.current

    var isPlantScan by remember { mutableStateOf(true) }
    var symptomsText by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoSourceBottomSheet by remember { mutableStateOf(false) }
    var showHistoryTab by remember { mutableStateOf(false) }

    // System Image Picker Launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedBitmap = null
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                selectedBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedUri = null
        }
    }

    // Default Symptoms Presets
    val plantPresets = listOf(
        "Mildiou" to "Taches brunes concentriques avec halo jaune sur feuilles de tomate",
        "Oïdium" to "Feutrage blanc poudreux sur les feuilles supérieures de mes courgettes",
        "Rouille" to "Pustules orangées à brunes sur feuilles et tiges de blé",
        "Flétrissement" to "Feuilles jaunies et flétrissement rapide du plant de pomme de terre"
    )

    val animalPresets = listOf(
        "Piétin" to "Mouton boiteux avec l'espace interdigital rouge, gonflé et malodorant",
        "Fièvre Aphteuse" to "Bovin avec fièvre élevée, salivante excessive et aphtes sur la langue",
        "Grippe Aviaire" to "Poules léthargiques, crête bleuie, baisse soudaine de ponte",
        "Clavelée" to "Grave éruption cutanée nodulaire sur le pis et les lèvres de mes brebis"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner & Diagnostic IA", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val tabActiveBg = if (!showHistoryTab) MaterialTheme.colorScheme.primary else Color.Transparent
                    val tabActiveContent = if (!showHistoryTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabActiveBg)
                            .clickable { showHistoryTab = false }
                            .padding(horizontal = 4.dp)
                            .testTag("scan_tab_new"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tabActiveContent
                            )
                            Text(
                                text = "Nouveau diagnostic",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = tabActiveContent,
                                maxLines = 1
                            )
                        }
                    }

                    val tabInactiveBg = if (showHistoryTab) MaterialTheme.colorScheme.primary else Color.Transparent
                    val tabInactiveContent = if (showHistoryTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabInactiveBg)
                            .clickable { showHistoryTab = true }
                            .padding(horizontal = 4.dp)
                            .testTag("scan_tab_history"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tabInactiveContent
                            )
                            Text(
                                text = "Historique (${history.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = tabInactiveContent,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (showHistoryTab) {
                if (history.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Troubleshoot,
                                    contentDescription = "Aucun historique",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Aucun diagnostic enregistré",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Faites une analyse d'une anomalie de plante ou d'animal pour enregistrer automatiquement son diagnostic complet dans l'historique de votre ferme.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analyses de maladies archivées",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { viewModel.clearAllScanResults() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Effacer tout", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Effacer tout", fontSize = 11.sp)
                            }
                        }
                    }

                    // Render list of archived scans
                    items(history) { scan ->
                        var isExpanded by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (scan.targetType.contains("Plante")) Icons.Default.Grass else Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = if (scan.targetType.contains("Plante")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = scan.targetType,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (scan.targetType.contains("Plante")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Text(
                                            text = scan.pathology,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        // Formatted Date
                                        val dateStr = remember(scan.timestamp) {
                                            try {
                                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                                sdf.format(java.util.Date(scan.timestamp))
                                            } catch (e: Exception) {
                                                ""
                                            }
                                        }
                                        Text(
                                            text = "Scan le $dateStr",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.deleteScanResult(scan.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Réduire" else "Développer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "Symptôme décrit : \"${scan.symptoms}\"",
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isExpanded) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                        // Products / Treatments
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Vaccines, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1E88E5))
                                                Text("Produits et Traitements à utiliser :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E88E5))
                                            }
                                            Text(scan.treatments, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                                        }

                                        // To Avoid
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFB8C00))
                                                Text("Attitudes à éviter d'urgence :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFB8C00))
                                            }
                                            Text(scan.toAvoid, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                                        }

                                        // Prevention
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF43A047))
                                                Text("Moyens de Prévention & Biosécurité :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF43A047))
                                            }
                                            Text(scan.prevention, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
            // Category Segmented Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    val activeBg = if (isPlantScan) MaterialTheme.colorScheme.primary else Color.Transparent
                    val activeContent = if (isPlantScan) Color.White else MaterialTheme.colorScheme.onSurface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeBg)
                            .clickable { isPlantScan = true }
                            .padding(horizontal = 4.dp)
                            .testTag("toggle_plantes"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = "Plantes",
                                modifier = Modifier.size(18.dp),
                                tint = activeContent
                            )
                            Text(
                                text = "Plantes & Cultures",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeContent,
                                maxLines = 1
                            )
                        }
                    }

                    val inactiveBg = if (!isPlantScan) MaterialTheme.colorScheme.secondary else Color.Transparent
                    val inactiveContent = if (!isPlantScan) Color.White else MaterialTheme.colorScheme.onSurface
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(inactiveBg)
                            .clickable { isPlantScan = false }
                            .padding(horizontal = 4.dp)
                            .testTag("toggle_animaux"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = "Animaux",
                                modifier = Modifier.size(18.dp),
                                tint = inactiveContent
                            )
                            Text(
                                text = "Animaux & Élevage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = inactiveContent,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Photo Uploader Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_photo_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedUri != null || selectedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = selectedBitmap ?: selectedUri,
                                    contentDescription = "Symptom Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Clear Photo Button
                                IconButton(
                                    onClick = {
                                        selectedUri = null
                                        selectedBitmap = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { showPhotoSourceBottomSheet = true }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Pick Image",
                                        modifier = Modifier.size(36.dp),
                                        tint = if (isPlantScan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        "Charger une photo du symptôme",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Optionnel (Galerie ou Appareil)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Simulation / Presets Header
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Simuler",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Simuler avec un symptôme courant",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Presets List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = if (isPlantScan) plantPresets else animalPresets
                    presets.forEach { (title, description) ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { symptomsText = description },
                            colors = CardDefaults.cardColors(
                                containerColor = if (symptomsText == description) {
                                    if (isPlantScan) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }

            // Symptom Description TextField
            item {
                OutlinedTextField(
                    value = symptomsText,
                    onValueChange = { symptomsText = it },
                    label = { Text("Expliquez l'anomalie de la feuille / le mal de l'animal") },
                    placeholder = { Text("Ex: Taches noires bizarres sur les pousses, boiterie...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 200.dp)
                        .testTag("scan_symptoms_input"),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Action Button
            item {
                val canSubmit = (symptomsText.isNotBlank() || selectedBitmap != null) && !scanState.isLoading
                Button(
                    onClick = {
                        val type = if (isPlantScan) "Plante / Culture" else "Animal / Élevage"
                        val symptomsToSend = symptomsText.ifBlank { "Symptômes visuels observés sur la photo fournie." }
                        viewModel.performScan(type, symptomsToSend, selectedBitmap)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("scan_diagnose_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlantScan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = if (isPlantScan) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        }
                    ),
                    enabled = canSubmit,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (scanState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Healing, contentDescription = "Diagnostiquer")
                            Text("Lancer l'Analyse IA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Scanning Status Banner
            if (scanState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                            Text(
                                "L'intelligence artificielle analyse le symptôme...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Succesful Diagnose Reports
            if (scanState.isSuccess) {
                item {
                    Text(
                        "Rapport de Pathologie IA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 1. Diagnostic/Pathologie
                item {
                    ReportSectionCard(
                        title = "Pathologie Diagnostiquée",
                        content = scanState.pathology,
                        icon = Icons.Default.MedicalServices,
                        iconColor = Color(0xFFE53935),
                        containerColor = Color(0xFFFFEBEE)
                    )
                }

                // 2. Produits et Traitements
                item {
                    ReportSectionCard(
                        title = "Produits et Traitements à utiliser",
                        content = scanState.treatments,
                        icon = Icons.Default.Vaccines,
                        iconColor = Color(0xFF1E88E5),
                        containerColor = Color(0xFFE3F2FD)
                    )
                }

                // 3. À éviter
                item {
                    ReportSectionCard(
                        title = "Attitudes et Comportements à éviter",
                        content = scanState.toAvoid,
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFFB8C00),
                        containerColor = Color(0xFFFFF3E0)
                    )
                }

                // 4. Prévention
                item {
                    ReportSectionCard(
                        title = "Moyens de Prévention & Biosécurité",
                        content = scanState.prevention,
                        icon = Icons.Default.Shield,
                        iconColor = Color(0xFF43A047),
                        containerColor = Color(0xFFE8F5E9)
                    )
                }
            }

            } // Fin du bloc else (de showHistoryTab)

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showPhotoSourceBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceBottomSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sélectionner une photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Appareil Photo / Camera Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceBottomSheet = false
                            cameraLauncher.launch(null)
                        }
                        .testTag("photo_source_camera"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "Prendre une photo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Utiliser l'appareil photo du téléphone",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Galerie / Gallery Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceBottomSheet = false
                            pickerLauncher.launch("image/*")
                        }
                        .testTag("photo_source_gallery"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "Choisir depuis la galerie",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Sélectionner un fichier existant dans vos albums",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSectionCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
