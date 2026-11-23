package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryItem
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: FarmViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inventoryList by viewModel.allInventory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Tous") }
    var showOnlyLowStock by remember { mutableStateOf(false) }

    var showUpsertDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }

    val categories = listOf("Tous", "Engrais", "Semences", "Aliments pour animaux")

    val filteredList = remember(inventoryList, searchQuery, selectedCategoryFilter, showOnlyLowStock) {
        inventoryList.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || 
                                item.notes.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "Tous" || item.category == selectedCategoryFilter
            val matchesLowStock = !showOnlyLowStock || item.isLowStock()
            matchesSearch && matchesCategory && matchesLowStock
        }
    }

    val totalItemsCount = inventoryList.size
    val lowStockItemsCount = inventoryList.count { it.isLowStock() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestion d'Inventaire",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("inventory_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour au tableau de bord"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingItem = null
                    showUpsertDialog = true
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_inventory_item_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un produit")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Stats Summary Cards ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_total_items_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Produits répertoriés",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            "$totalItemsCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Low stock alerting card
                val lowStockContainerColor = if (lowStockItemsCount > 0) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
                val lowStockContentColor = if (lowStockItemsCount > 0) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showOnlyLowStock = !showOnlyLowStock }
                        .testTag("stat_alert_items_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = lowStockContainerColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (showOnlyLowStock) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Alertes Seuil Bas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = lowStockContentColor.copy(alpha = 0.8f)
                            )
                            if (lowStockItemsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                            }
                        }
                        Text(
                            "$lowStockItemsCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockItemsCount > 0) MaterialTheme.colorScheme.error else lowStockContentColor
                        )
                    }
                }
            }

            // --- Search and Filters Row ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search item
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un engrais, semence, aliment...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                // Category badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("filter_chip_$cat"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Alert Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOnlyLowStock = !showOnlyLowStock }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (showOnlyLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Afficher uniquement les stocks faibles",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (showOnlyLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = showOnlyLowStock,
                        onCheckedChange = { showOnlyLowStock = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.error,
                            checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.testTag("filter_low_stock_switch")
                    )
                }
            }

            // --- Product items list ---
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Text(
                            text = if (inventoryList.isEmpty()) "Aucun produit en stock" else "Aucun résultat pour vos filtres",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (inventoryList.isEmpty()) {
                            Text(
                                text = "Cliquez sur + pour enregistrer vos premiers engrais, semences ou aliments.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("inventory_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        InventoryItemRow(
                            item = item,
                            onIncrement = {
                                viewModel.updateInventoryItem(item.copy(quantity = item.quantity + 1.0))
                            },
                            onDecrement = {
                                if (item.quantity >= 1.0) {
                                    viewModel.updateInventoryItem(item.copy(quantity = item.quantity - 1.0))
                                } else if (item.quantity > 0.0) {
                                    viewModel.updateInventoryItem(item.copy(quantity = 0.0))
                                }
                            },
                            onEdit = {
                                editingItem = item
                                showUpsertDialog = true
                            },
                            onDelete = {
                                viewModel.deleteInventoryItemById(item.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Upsert Add/Edit Dialog ---
    if (showUpsertDialog) {
        InventoryItemDialog(
            item = editingItem,
            onDismiss = { showUpsertDialog = false },
            onConfirm = { name, category, quantity, unit, threshold, notes ->
                if (editingItem == null) {
                    viewModel.addInventoryItem(name, category, quantity, unit, threshold, notes)
                } else {
                    viewModel.updateInventoryItem(
                        editingItem!!.copy(
                            name = name,
                            category = category,
                            quantity = quantity,
                            unit = unit,
                            lowThreshold = threshold,
                            notes = notes
                        )
                    )
                }
                showUpsertDialog = false
            }
        )
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLow = item.isLowStock()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("inventory_item_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLow) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Category tag and Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category tag
                val tagColor = when (item.category) {
                    "Engrais" -> MaterialTheme.colorScheme.primary
                    "Semences" -> MaterialTheme.colorScheme.secondary
                    "Aliments pour animaux" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val tagBg = tagColor.copy(alpha = 0.12f)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val icon = when (item.category) {
                        "Engrais" -> Icons.Default.WaterDrop
                        "Semences" -> Icons.Default.Grass
                        "Aliments pour animaux" -> Icons.Default.Pets
                        else -> Icons.Default.Category
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = tagColor
                    )
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_inventory_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Product name & notes
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.notes.isNotEmpty()) {
                    Text(
                        text = item.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            // Progress level indicator
            val progressFactor = if (item.lowThreshold > 0) {
                (item.quantity / (item.lowThreshold * 2)).coerceIn(0.0, 1.0).toFloat()
            } else {
                1.0f
            }
            val progressColor = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressFactor },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock : ${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Seuil min : ${item.lowThreshold} ${item.unit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom actions: Increment/Decrement and alert banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLow) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("low_stock_badge_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "STOCK FAIBLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Plus / Minus buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                            .testTag("decrement_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuer de 1",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .testTag("increment_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Augmenter de 1",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemDialog(
    item: InventoryItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, quantity: Double, unit: String, threshold: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Engrais") }
    var quantityStr by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "kg") }
    var thresholdStr by remember { mutableStateOf(item?.lowThreshold?.toString() ?: "") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }

    val categories = listOf("Engrais", "Semences", "Aliments pour animaux")
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("inventory_upsert_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (item == null) "Ajouter un produit" else "Modifier le produit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du produit *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_item_name"),
                    singleLine = true
                )

                // Category selection dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("dialog_item_category"),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("dialog_category_item_$cat")
                            )
                        }
                    }
                }

                // Quantity and Unit in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Stock initial *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("dialog_item_quantity"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unité (ex. kg, l)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_item_unit"),
                        singleLine = true
                    )
                }

                // Warning low threshold limit
                OutlinedTextField(
                    value = thresholdStr,
                    onValueChange = { thresholdStr = it },
                    label = { Text("Seuil d'alerte Minimum *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_item_threshold"),
                    singleLine = true
                )

                // Description and notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Précisions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("dialog_item_notes"),
                    maxLines = 3
                )

                // Dialog Buttons actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val qty = quantityStr.toDoubleOrNull() ?: 0.0
                            val limit = thresholdStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onConfirm(name, category, qty, unit, limit, notes)
                            }
                        },
                        modifier = Modifier.testTag("dialog_confirm_button"),
                        enabled = name.isNotBlank() && quantityStr.isNotBlank() && thresholdStr.isNotBlank()
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
