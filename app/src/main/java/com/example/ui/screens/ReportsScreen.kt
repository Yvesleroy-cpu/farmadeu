package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.toArgb
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

// Data class representing monthly agricultural records
data class MonthlyReportData(
    val month: String,      // e.g., "Janv", "Févr", "Mars", "Avril", "Mai", "Juin"
    val cropYield: Double,   // Total harvest in Tons
    val cropType: String,    // Main crop harvested
    val milkQty: Double,     // For Livestock: Milk production in Liters (if cows/sheep) or egg rate
    val livestockHealthRate: Double // e.g. 90% (Average health score or weight gain index)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FarmViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect crops & livestock to enrich default values
    val crops by viewModel.allCrops.collectAsState()
    val livestock by viewModel.allLivestock.collectAsState()

    // 12 months default historical report data
    var reportList by remember {
        mutableStateOf(
            listOf(
                MonthlyReportData("Janv", 12.5, "Betterave", 1200.0, 94.0),
                MonthlyReportData("Févr", 9.8, "Chou-fleur", 1350.0, 92.0),
                MonthlyReportData("Mars", 15.2, "Salade", 1400.0, 95.0),
                MonthlyReportData("Avril", 22.0, "Pomme de terre", 1680.0, 97.0),
                MonthlyReportData("Mai", 34.5, "Blé tendre", 1850.0, 98.0),
                MonthlyReportData("Juin", 45.0, "Tomate", 2100.0, 96.0),
                MonthlyReportData("Juil", 48.2, "Abricot", 1950.0, 95.0),
                MonthlyReportData("Août", 42.1, "Pêche", 1720.0, 94.0),
                MonthlyReportData("Sept", 35.8, "Maïs grain", 1600.0, 96.0),
                MonthlyReportData("Oct", 28.4, "Vignes / Raisin", 1450.0, 95.0),
                MonthlyReportData("Nov", 18.2, "Endives", 1300.0, 93.0),
                MonthlyReportData("Déc", 10.5, "Poireaux", 1150.0, 92.0)
            )
        )
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = Cultures, 1 = Élevage
    var selectedMonthIndex by remember { mutableStateOf(5) } // Default June (Peak season 2026)
    var showAddReportDialog by remember { mutableStateOf(false) }

    val selectedReport = reportList.getOrNull(selectedMonthIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rapports de Production",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analyses mensuelles & Graphiques interactifs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reports_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour au tableau de bord"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddReportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_report_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saisir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Header Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Performances de l'Exploitation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Visualisation interactive inspirée de Recharts. Cliquez sur les éléments graphiques ci-dessous pour inspecter les détails par mois.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Tab Selector System
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    text = "Rendements des Cultures (T)",
                    icon = Icons.Default.Grass,
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f).testTag("culture_charts_tab")
                )
                TabButton(
                    text = "Production Élevage (L)",
                    icon = Icons.Default.Pets,
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f).testTag("livestock_charts_tab")
                )
            }

            // The main Chart Card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_container_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Chart header metrics representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (activeTab == 0) "Historique des récoltes" else "Rendement de la traite / ponte",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Mois sélectionné : ${selectedReport?.month ?: "?"} 2026",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedReport != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (activeTab == 0) "${selectedReport.cropYield} Tonnes" else "${selectedReport.milkQty} Litres",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // --- Custom Recharts-style Native Composable Graph ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        if (activeTab == 0) {
                            // Render Bar Chart
                            RechartsBarChart(
                                reports = reportList,
                                selectedIndex = selectedMonthIndex,
                                onSelectIndex = { selectedMonthIndex = it }
                            )
                        } else {
                            // Render Line / Area Chart
                            RechartsAreaChart(
                                reports = reportList,
                                selectedIndex = selectedMonthIndex,
                                onSelectIndex = { selectedMonthIndex = it }
                            )
                        }
                    }

                    // Legends Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (activeTab == 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondary
                                    )
                            )
                            Text(
                                text = if (activeTab == 0) "Rendement de récoltes (Tonnes)" else "Production Laitière / Active (Litres)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive Month Detail panel
            selectedReport?.let { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_details_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Détails du mois : ${report.month} 2026",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Crops yields summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Rendements parcelles", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Principal légume/culture : ${report.cropType}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Récolté", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${report.cropYield} t",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Livestock summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Performances d'Élevage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Production Laitière / Œufs cumulée",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${report.milkQty} L / p",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Health index rating bar
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Indicateur de santé globale du troupeau",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${report.livestockHealthRate.toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (report.livestockHealthRate / 100f).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Agricultural insight summary depending on stats
            val highestYieldMonth = reportList.maxByOrNull { it.cropYield }
            val highestTraiteMonth = reportList.maxByOrNull { it.milkQty }
            if (highestYieldMonth != null && highestTraiteMonth != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Analyse proactive IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Le pic annuel de récolte s'est concentré sur le mois de ${highestYieldMonth.month} avec un rendement maximal de ${highestYieldMonth.cropYield} Tonnes enregistrées pour la culture de ${highestYieldMonth.cropType}.\n" +
                                   "Côté élevage, la production laitière a atteint son maximum en ${highestTraiteMonth.month} (${highestTraiteMonth.milkQty} L). Les indicateurs globaux révèlent une corrélation forte entre la hausse des températures de printemps et l'augmentation des performances.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Modal view for custom reports submission
    if (showAddReportDialog) {
        AddReportDialog(
            crops = crops,
            onDismiss = { showAddReportDialog = false },
            onSave = { newRecord ->
                // Look up if that month already exists, update or add to index
                val listCopy = reportList.toMutableList()
                val existingIndex = listCopy.indexOfFirst { it.month.lowercase() == newRecord.month.lowercase() }
                if (existingIndex >= 0) {
                    listCopy[existingIndex] = newRecord
                } else {
                    listCopy.add(newRecord)
                }
                reportList = listCopy
                selectedMonthIndex = listCopy.indexOf(newRecord).coerceAtLeast(0)
                showAddReportDialog = false
            }
        )
    }
}

// Custom Toggle Tab buttons style
@Composable
fun TabButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tab_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content_color"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

// Beautiful Custom Bar chart recreating Recharts visual grid & behavior using Jetpack Compose Canvas
@Composable
fun RechartsBarChart(
    reports: List<MonthlyReportData>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val barColor = MaterialTheme.colorScheme.primary
    val barSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface

    // Animation progress
    val animationProgress = animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(durationMillis = 800),
        label = "bar_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(reports) {
                detectTapGestures { offset ->
                    val totalWidth = size.width
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val listSize = reports.size
                    val chartWidth = totalWidth - paddingLeft - paddingRight
                    val barSpacing = chartWidth / listSize

                    val tappedX = offset.x - paddingLeft
                    if (tappedX >= 0 && tappedX < chartWidth) {
                        val index = (tappedX / barSpacing).toInt().coerceIn(0, listSize - 1)
                        onSelectIndex(index)
                    }
                }
            }
    ) {
        val totalWidth = size.width
        val totalHeight = size.height

        val paddingLeft = 32.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val chartWidth = totalWidth - paddingLeft - paddingRight
        val chartHeight = totalHeight - paddingTop - paddingBottom

        // Draw basic grids lines
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = paddingTop + (chartHeight / gridLinesCount) * i
            drawLine(
                color = strokeColor,
                start = Offset(paddingLeft, y),
                end = Offset(totalWidth - paddingRight, y),
                strokeWidth = 1f
            )
        }

        // Draw vertical scale & base lines
        drawLine(
            color = strokeColor,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, totalHeight - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = strokeColor,
            start = Offset(paddingLeft, totalHeight - paddingBottom),
            end = Offset(totalWidth - paddingRight, totalHeight - paddingBottom),
            strokeWidth = 2f
        )

        // Find max element to scale heights properly
        val maxY = (reports.maxOfOrNull { it.cropYield } ?: 50.0).toFloat().coerceAtLeast(10f)

        // Draw each monthly bar
        val barSpacing = chartWidth / reports.size
        val barWidth = (barSpacing * 0.55f).coerceAtLeast(12f)

        reports.forEachIndexed { index, report ->
            val barHeight = (report.cropYield.toFloat() / maxY) * chartHeight * animationProgress.value
            val x = paddingLeft + barSpacing * index + (barSpacing - barWidth) / 2
            val y = totalHeight - paddingBottom - barHeight

            // Selected/Hover overlay
            if (index == selectedIndex) {
                drawRect(
                    color = primaryColor.copy(alpha = 0.15f),
                    topLeft = Offset(paddingLeft + barSpacing * index, paddingTop),
                    size = Size(barSpacing, chartHeight)
                )
            }

            // Draw rounded bar
            drawRoundRect(
                color = if (index == selectedIndex) barSelectedColor else barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw month label at the bottom
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(
                    report.month,
                    x + barWidth / 2,
                    totalHeight - 8.dp.toPx(),
                    paint
                )
            }
        }
    }
}

// Beautiful Custom Area / Line chart reproducing Recharts gradient vector line layout
@Composable
fun RechartsAreaChart(
    reports: List<MonthlyReportData>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val areaColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurface

    // Animation progress
    val animationProgress = animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "area_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(reports) {
                detectTapGestures { offset ->
                    val totalWidth = size.width
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val listSize = reports.size
                    val chartWidth = totalWidth - paddingLeft - paddingRight
                    val spacing = chartWidth / (listSize - 1)

                    val tappedX = offset.x - paddingLeft
                    if (tappedX >= 0 && tappedX < chartWidth) {
                        val index = ((tappedX + spacing / 2) / spacing).toInt().coerceIn(0, listSize - 1)
                        onSelectIndex(index)
                    }
                }
            }
    ) {
        val totalWidth = size.width
        val totalHeight = size.height

        val paddingLeft = 32.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val chartWidth = totalWidth - paddingLeft - paddingRight
        val chartHeight = totalHeight - paddingTop - paddingBottom

        // Draw horizontal grid lines
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = paddingTop + (chartHeight / gridLinesCount) * i
            drawLine(
                color = strokeColor,
                start = Offset(paddingLeft, y),
                end = Offset(totalWidth - paddingRight, y),
                strokeWidth = 1f
            )
        }

        // Draw vertical base scale lines
        drawLine(
            color = strokeColor,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, totalHeight - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = strokeColor,
            start = Offset(paddingLeft, totalHeight - paddingBottom),
            end = Offset(totalWidth - paddingRight, totalHeight - paddingBottom),
            strokeWidth = 2f
        )

        // Find max element to scale heights properly
        val maxY = (reports.maxOfOrNull { it.milkQty } ?: 2500.0).toFloat().coerceAtLeast(10f)

        val pointsSpacing = chartWidth / (reports.size - 1)
        val areaPoints = ArrayList<Offset>()

        // Compute coordinate points
        reports.forEachIndexed { index, report ->
            val ratio = (report.milkQty.toFloat() / maxY) * animationProgress.value
            val px = paddingLeft + pointsSpacing * index
            val py = totalHeight - paddingBottom - ratio * chartHeight
            areaPoints.add(Offset(px, py))
        }

        if (areaPoints.isNotEmpty()) {
            // Draw gradient/area fill beneath the curve line
            val fillPath = Path().apply {
                moveTo(paddingLeft, totalHeight - paddingBottom)
                areaPoints.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(areaPoints.last().x, totalHeight - paddingBottom)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(areaColor.copy(alpha = 0.4f), Color.Transparent),
                    startY = paddingTop,
                    endY = totalHeight - paddingBottom
                )
            )

            // Draw the line vector curve path
            val strokePath = Path().apply {
                moveTo(areaPoints.first().x, areaPoints.first().y)
                for (i in 1 until areaPoints.size) {
                    lineTo(areaPoints[i].x, areaPoints[i].y)
                }
            }

            drawPath(
                path = strokePath,
                color = areaColor,
                style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round)
            )

            // Hover indicator overlay vertical dashed/dotted line
            val selectedPoint = areaPoints[selectedIndex]
            drawLine(
                color = areaColor.copy(alpha = 0.5f),
                start = Offset(selectedPoint.x, paddingTop),
                end = Offset(selectedPoint.x, totalHeight - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )

            // Highlight selected dot point
            drawCircle(
                color = areaColor,
                radius = 6.dp.toPx(),
                center = selectedPoint
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = selectedPoint
            )
        }

        // Draw axis month texts
        reports.forEachIndexed { index, report ->
            val px = paddingLeft + pointsSpacing * index
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 9.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(
                    report.month,
                    px,
                    totalHeight - 8.dp.toPx(),
                    paint
                )
            }
        }
    }
}

// Dialog to submit custom records
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportDialog(
    crops: List<com.example.data.model.Crop>,
    onDismiss: () -> Unit,
    onSave: (MonthlyReportData) -> Unit
) {
    val months = listOf("Janv", "Févr", "Mars", "Avril", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc")
    var selectedMonth by remember { mutableStateOf("Juin") }
    var yieldStr by remember { mutableStateOf("45.0") }
    var mainCropSelected by remember { mutableStateOf(if (crops.isNotEmpty()) crops.first().name else "Tomate") }
    var milkStr by remember { mutableStateOf("1500") }
    var healthScore by remember { mutableStateOf(95f) }

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var cropDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("add_report_dialog_card"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Saisir un Rapport Mensuel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Month Dropdown selector
                ExposedDropdownMenuBox(
                    expanded = monthDropdownExpanded,
                    onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded }
                ) {
                    TextField(
                        value = selectedMonth,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mois d'enregistrement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = monthDropdownExpanded,
                        onDismissRequest = { monthDropdownExpanded = false }
                    ) {
                        months.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    selectedMonth = m
                                    monthDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Vegetable Crops selector
                ExposedDropdownMenuBox(
                    expanded = cropDropdownExpanded,
                    onExpandedChange = { cropDropdownExpanded = !cropDropdownExpanded }
                ) {
                    TextField(
                        value = mainCropSelected,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Culture principale") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cropDropdownExpanded,
                        onDismissRequest = { cropDropdownExpanded = false }
                    ) {
                        val allAvailableCrops = if (crops.isNotEmpty()) crops.map { it.name } else listOf("Tomate", "Blé", "Maïs", "Betterave", "Pomme de terre")
                        allAvailableCrops.forEach { cropName ->
                            DropdownMenuItem(
                                text = { Text(cropName) },
                                onClick = {
                                    mainCropSelected = cropName
                                    cropDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Yield Amount
                OutlinedTextField(
                    value = yieldStr,
                    onValueChange = { yieldStr = it },
                    label = { Text("Rendement des cultures (Tonnes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_yield_input")
                )

                // Milk volume qty and performance
                OutlinedTextField(
                    value = milkStr,
                    onValueChange = { milkStr = it },
                    label = { Text("Volume Élevage Lait/Œufs (Litres/Unités)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_milk_input")
                )

                // Troupeau health rating slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Indice santé troupeau", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${healthScore.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = healthScore,
                        onValueChange = { healthScore = it },
                        valueRange = 50f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val yieldVal = yieldStr.toDoubleOrNull() ?: 0.0
                            val milkVal = milkStr.toDoubleOrNull() ?: 0.0
                            onSave(
                                MonthlyReportData(
                                    month = selectedMonth,
                                    cropYield = yieldVal,
                                    cropType = mainCropSelected,
                                    milkQty = milkVal,
                                    livestockHealthRate = healthScore.toDouble()
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
