package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FarmTask
import com.example.ui.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: FarmViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val crops by viewModel.allCrops.collectAsState()
    val livestock by viewModel.allLivestock.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Calendar state
    val calendarInstance = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) } // 0-indexed
    var selectedDay by remember { mutableStateOf(calendarInstance.get(Calendar.DAY_OF_MONTH)) }

    // Date formatting helper
    val monthNameFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.FRENCH) }
    
    val selectedCalendar = remember(currentYear, currentMonth, selectedDay) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }
    }

    val displayMonthString = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        monthNameFormat.format(cal.time).replaceFirstChar { it.uppercase() }
    }

    // Days in current selected month
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Calculate offset for the first day of the week
        // get(Calendar.DAY_OF_WEEK) returns 1 for Sunday, 2 for Monday, etc.
        // We want Monday as index 0, so: Sunday -> 6, Monday -> 0, Tuesday -> 1...
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = when (firstDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        
        Pair(maxDays, offset)
    }

    val (maxDays, offset) = daysInMonth

    // Group tasks of current month for visual dot indicators
    val monthTasksMap = remember(tasks, currentYear, currentMonth) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val map = mutableMapOf<String, MutableList<FarmTask>>()
        tasks.forEach { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.date }
            if (cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth) {
                val dayStr = formatter.format(cal.time)
                if (!map.containsKey(dayStr)) {
                    map[dayStr] = mutableListOf()
                }
                map[dayStr]?.add(task)
            }
        }
        map
    }

    // Filter tasks for the selected day
    val selectedDayTasks = remember(tasks, currentYear, currentMonth, selectedDay) {
        tasks.filter { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.date }
            cal.get(Calendar.YEAR) == currentYear &&
                    cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planification & Calendrier", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(
                        onClick = { showAddTaskDialog = true },
                        modifier = Modifier.testTag("calendar_add_task_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Planifier une tâche",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("calendar_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle tâche")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Switcher Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                            selectedDay = 1 // reset to first day
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mois précédent")
                        }

                        Text(
                            text = displayMonthString,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("calendar_month_year_text")
                        )

                        IconButton(onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                            selectedDay = 1 // reset to first day
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mois suivant")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days of Week Labels
                    val daysOfWeek = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysOfWeek.forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Calendar Grid Layout
                    val totalCells = offset + maxDays
                    val rowsCount = (totalCells + 6) / 7

                    for (row in 0 until rowsCount) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - offset + 1
                                val isValidDay = dayNum in 1..maxDays

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isValidDay) {
                                        val isSelected = dayNum == selectedDay
                                        val isToday = remember(dayNum, currentMonth, currentYear) {
                                            val today = Calendar.getInstance()
                                            today.get(Calendar.DAY_OF_MONTH) == dayNum &&
                                                    today.get(Calendar.MONTH) == currentMonth &&
                                                    today.get(Calendar.YEAR) == currentYear
                                        }

                                        val dayCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, currentYear)
                                            set(Calendar.MONTH, currentMonth)
                                            set(Calendar.DAY_OF_MONTH, dayNum)
                                        }
                                        val dayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val dayDateString = dayFormatter.format(dayCal.time)
                                        val hasTasks = monthTasksMap.containsKey(dayDateString)
                                        val incompleteCount = monthTasksMap[dayDateString]?.count { !it.isCompleted } ?: 0

                                        val backgroundModifier = when {
                                            isSelected -> Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                            isToday -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                            else -> Modifier
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .clickable { selectedDay = dayNum }
                                                .then(backgroundModifier),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            
                                            if (hasTasks) {
                                                val dotColor = if (incompleteCount > 0) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    Color(0xFF4CAF50)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else dotColor)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Head of Daily List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fullDateText = remember(selectedDay, currentMonth, currentYear) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    val format = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)
                    format.format(cal.time).replaceFirstChar { it.uppercase() }
                }

                Text(
                    text = fullDateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${selectedDayTasks.size} tâche(s)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tasks List
            if (selectedDayTasks.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Aucune tâche planifiée",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ajoutez un rappel d'entretien, de récolte, ou de vaccination pour ce jour.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("calendar_tasks_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(selectedDayTasks, key = { it.id }) { task ->
                        val taskColor = when (task.type) {
                            "ENTRETIEN" -> Color(0xFF2E7D32) // Soft Green
                            "RECOLTE" -> Color(0xFFEF6C00) // Soft Amber
                            "VACCINATION" -> Color(0xFF1565C0) // Soft Blue
                            else -> MaterialTheme.colorScheme.secondary
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Status checkbox
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (task.isCompleted) taskColor else Color.Transparent
                                        )
                                        .border(2.dp, taskColor, CircleShape)
                                        .clickable { viewModel.toggleTaskCompletion(task) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Info column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(taskColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (task.type) {
                                                    "ENTRETIEN" -> "Entretien"
                                                    "RECOLTE" -> "Récolte"
                                                    "VACCINATION" -> "Vaccination"
                                                    else -> "Général"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = taskColor
                                            )
                                        }

                                        if (task.targetName.isNotBlank()) {
                                            Text(
                                                text = task.targetName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = task.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Delete icon
                                IconButton(onClick = { viewModel.deleteTaskById(task.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer la tâche",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to compile dynamic tasks
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("ENTRETIEN") } // "ENTRETIEN", "RECOLTE", "VACCINATION"
        var targetName by remember { mutableStateOf("") }
        
        var dropdownExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Planifier une action",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Type Picker Segmented Row or buttons
                    Text("Catégorie de tâche", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ENTRETIEN", "RECOLTE", "VACCINATION").forEach { type ->
                            val isChosen = selectedType == type
                            val typeColor = when (type) {
                                "ENTRETIEN" -> Color(0xFF2E7D32)
                                "RECOLTE" -> Color(0xFFEF6C00)
                                "VACCINATION" -> Color(0xFF1565C0)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) typeColor else typeColor.copy(alpha = 0.1f))
                                    .border(
                                        1.dp,
                                        if (isChosen) Color.Transparent else typeColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (type) {
                                        "ENTRETIEN" -> "Entretien"
                                        "RECOLTE" -> "Récolte"
                                        "VACCINATION" -> "Vaccin"
                                        else -> "Général"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else typeColor
                                )
                            }
                        }
                    }

                    // Text inputs
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre de l'action *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_input_title")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description complémentaire") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Targeted crop or livestock
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetName,
                            onValueChange = { targetName = it },
                            label = { Text("Élément Ciblé (ex: Champ A, Mouton)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Choisir")
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            if (crops.isNotEmpty()) {
                                Text(
                                    "Mes Cultures",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                crops.forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop.name) },
                                        onClick = {
                                            targetName = crop.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }

                            if (livestock.isNotEmpty()) {
                                Text(
                                    "Mon Élevage",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                livestock.forEach { animal ->
                                    DropdownMenuItem(
                                        text = { Text(animal.name) },
                                        onClick = {
                                            targetName = animal.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }

                            if (crops.isEmpty() && livestock.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Saisissez manuellement ci-dessus") },
                                    onClick = { dropdownExpanded = false }
                                )
                            }
                        }
                    }

                    // Exposal of selected date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Date : " + SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(selectedCalendar.time),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Control actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddTaskDialog = false }) {
                            Text("Annuler")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.addTask(
                                        title = title,
                                        description = description,
                                        type = selectedType,
                                        date = selectedCalendar.timeInMillis,
                                        targetName = targetName
                                    )
                                    showAddTaskDialog = false
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier.testTag("task_submit_button")
                        ) {
                            Text("Sauvegarder")
                        }
                    }
                }
            }
        }
    }
}
