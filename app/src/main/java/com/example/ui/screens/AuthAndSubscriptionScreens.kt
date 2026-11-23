package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Farmadeu",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Votre compagnon agricole intelligent",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Connexion",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            errorMsg = null
                        },
                        label = { Text("Adresse email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMsg = null
                        },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMsg = "Veuillez remplir tous les champs."
                            } else if (!email.contains("@")) {
                                errorMsg = "Veuillez saisir une adresse email valide."
                            } else if (password.length < 4) {
                                errorMsg = "Le mot de passe doit contenir au moins 4 caractères."
                            } else {
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Se connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nouveau sur Farmadeu ?",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToSignup,
                            modifier = Modifier.testTag("login_to_signup")
                        ) {
                            Text("S'inscrire", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Créer un compte",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Rejoignez notre communauté d'agriculteurs modernes",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Inscription",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            errorMsg = null
                        },
                        label = { Text("Nom complet") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            errorMsg = null
                        },
                        label = { Text("Adresse email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMsg = null
                        },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            errorMsg = null
                        },
                        label = { Text("Confirmer le mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input")
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMsg = "Veuillez remplir tous les champs."
                            } else if (!email.contains("@")) {
                                errorMsg = "Veuillez saisir une adresse email valide."
                            } else if (password.length < 4) {
                                errorMsg = "Le mot de passe doit contenir au moins 4 caractères."
                            } else if (password != confirmPassword) {
                                errorMsg = "Les mots de passe ne correspondent pas."
                            } else {
                                onSignupSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("S'inscrire", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Déjà inscrit ?",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.testTag("signup_to_login")
                        ) {
                            Text("Se connecter", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

data class PlanOption(
    val sku: String,
    val name: String,
    val price: String,
    val subtitle: String,
    val isPopular: Boolean = false,
    val features: List<String>,
    val color: Color,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onPlanSelected: (String) -> Unit
) {
    var selectedPlanSku by remember { mutableStateOf("HarvestPro") }

    val plans = listOf(
        PlanOption(
            sku = "GermeFree",
            name = "Plan Germe",
            price = "Gratuit",
            subtitle = "Idéal pour débuter",
            features = listOf(
                "Suivi jusqu'à 2 parcelles de cultures",
                "Suivi jusqu'à 1 troupeau d'animaux",
                "Rappels de soins quotidiens basiques",
                "Tableau de bord synoptique",
                "1 Diagnostic IA par jour"
            ),
            color = MaterialTheme.colorScheme.outline,
            gradientColors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
        ),
        PlanOption(
            sku = "HarvestPro",
            name = "Plan Récolte Pro",
            price = "9,99 € / mois",
            subtitle = "Recommandé pour les maraîchers",
            isPopular = true,
            features = listOf(
                "Nombre illimité de parcelles et troupeaux",
                "Diagnostics et Analyses IA illimités",
                "Rappels météo avancés",
                "Modèles prédictifs de rendements",
                "Support prioritaire"
            ),
            color = MaterialTheme.colorScheme.primary,
            gradientColors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50))
        ),
        PlanOption(
            sku = "AgriExpert",
            name = "Plan AgriExpert",
            price = "19,99 € / mois",
            subtitle = "Pour les exploitations d'envergure",
            features = listOf(
                "Tout ce qui est dans le plan Pro",
                "Accès au Chat Conseils IA illimité",
                "Télémétrie et alertes de biosécurité",
                "Consultation d'experts vétérinaires à distance",
                "Statistiques d'exploitation exportables"
            ),
            color = MaterialTheme.colorScheme.secondary,
            gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Élever vos standards", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Trouvez le forfait parfait pour votre ferme",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Bénéficiez de la puissance maximale de l'agriculture numérique avec des analyses illimitées, et de meilleurs rendements.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Plan Selector Cards
            plans.forEach { plan ->
                val isSelected = selectedPlanSku == plan.sku
                val borderAlpha = if (isSelected) 1f else 0.3f
                val borderWidth = if (isSelected) 2.dp else 1.dp
                val borderColor = if (isSelected) plan.color else MaterialTheme.colorScheme.outlineVariant

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlanSku = plan.sku }
                        .testTag("subscription_plan_${plan.sku}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(borderWidth, borderColor.copy(alpha = borderAlpha))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = plan.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (plan.isPopular) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "POPULAIRE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = plan.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = plan.price,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = plan.color
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Features List
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            plan.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = plan.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = feature,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isSelected) {
                            val backgroundModifier = if (plan.sku == "GermeFree") {
                                Modifier.background(Color.Transparent)
                            } else {
                                Modifier.background(Brush.horizontalGradient(plan.gradientColors))
                            }

                            val borderModifier = if (plan.sku == "GermeFree") {
                                Modifier.border(BorderStroke(1.5.dp, plan.color), RoundedCornerShape(10.dp))
                            } else {
                                Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .then(backgroundModifier)
                                    .clickable { onPlanSelected(plan.sku) }
                                    .then(borderModifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Activer ce forfait",
                                    color = if (plan.sku == "GermeFree") plan.color else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onPlanSelected(selectedPlanSku) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("subscription_continue_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Valider mon abonnement & Continuer", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
