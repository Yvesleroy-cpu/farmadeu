package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.PlanOption
import com.example.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FarmViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val crops by viewModel.allCrops.collectAsState()
    val livestock by viewModel.allLivestock.collectAsState()
    
    val currentSubSku by viewModel.currentSubscription.collectAsState()
    val subExpiry by viewModel.subscriptionExpiry.collectAsState()

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var editNameExpanded by remember { mutableStateOf(false) }
    
    var userName by remember { mutableStateOf("Adyctif") }
    var userEmail by remember { mutableStateOf("adyctif@gmail.com") }

    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH) }
    val formattedExpiry = remember(subExpiry) { formatter.format(Date(subExpiry)) }

    // Map plan characteristics
    val planDetails = remember(currentSubSku) {
        when (currentSubSku) {
            "GermeFree" -> Triple("Plan Germe", "Gratuit", Color(0xFFE8F5E9))
            "HarvestPro" -> Triple("Plan Récolte Pro", "9,99 € / mois", Color(0xFF4CAF50))
            "AgriExpert" -> Triple("Plan AgriExpert", "19,99 € / mois", Color(0xFF1976D2))
            else -> Triple("Plan Personnalisé", "À la demande", Color(0xFFE0E0E0))
        }
    }
    val (planName, planPrice, planPrimaryColor) = planDetails

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profil & Abonnement", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Cards and Avatar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier le nom",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { editNameExpanded = !editNameExpanded }
                            )
                        }
                        Text(
                            text = userEmail,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (editNameExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Votre Nom") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = userEmail,
                                onValueChange = { userEmail = it },
                                label = { Text("Adresse Email") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { editNameExpanded = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Enregistrer")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Farm quick metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${crops.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Cultures",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${livestock.sumOf { it.count }}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Têtes d'Élevage",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subscription Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isFree = currentSubSku == "GermeFree"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Abonnement Actuel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isFree) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    else planPrimaryColor.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFree) "Gratuit" else "PLAN ACTIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFree) MaterialTheme.colorScheme.outline else planPrimaryColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFree) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    else planPrimaryColor.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentSubSku == "AgriExpert") Icons.Default.Stars else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isFree) MaterialTheme.colorScheme.outline else planPrimaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = planName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = planPrice,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Billing Expiry / renewal information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isFree) "Illimité" else "Prochain renouvellement",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isFree) "Aucun prélèvement" else formattedExpiry,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!isFree) {
                            TextButton(
                                onClick = {
                                    viewModel.renewSubscription()
                                    Toast.makeText(context, "Abonnement prolongé de 30 jours !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = planPrimaryColor),
                                modifier = Modifier.testTag("profile_renew_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Renouveler", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Subscription modification section
            Text(
                text = "Gérer les abonnements",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Changez d'abonnement pour augmenter vos limites de diagnostic, étendre l'accès au Chat conseils IA, ou sauvegarder de multiples troupeaux.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showUpgradeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_change_plan_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Upgrade, contentDescription = null)
                            Text("Prendre une nouvelle offre ou changer", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (currentSubSku != "GermeFree") {
                        OutlinedButton(
                            onClick = {
                                viewModel.cancelSubscription()
                                Toast.makeText(context, "Abonnement résilié", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("profile_cancel_sub_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Résilier l'abonnement", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Quick support / Billing logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Billing",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            "Historique des factures",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Visualisez vos transactions précédentes et téléchargez vos reçus fiscaux.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Full layout change Dialog for picking other options
    if (showUpgradeDialog) {
        Dialog(onDismissRequest = { showUpgradeDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Choisir une offre",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    listOf(
                        Triple("GermeFree", "Plan Germe (Gratuit)", "Idéal pour débuter l'agro-suivi"),
                        Triple("HarvestPro", "Plan Récolte Pro (9,99€)", "Volume de parcelles illimité"),
                        Triple("AgriExpert", "Plan AgriExpert (19,99€)", "IA Conseils & Biosécurité")
                    ).forEach { (sku, title, desc) ->
                        val isCurrent = currentSubSku == sku
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSubscription(sku)
                                    showUpgradeDialog = false
                                    Toast.makeText(context, "Plan mis à jour : $title", Toast.LENGTH_SHORT).show()
                                }
                                .border(
                                    if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isCurrent) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Actuel",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Changer",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showUpgradeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}
