package com.example

import android.os.Bundle
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.SpendingEntry
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CreamBg
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkSoft
import com.example.ui.theme.LineDivider
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.TerraSoft
import com.example.ui.theme.TerraRed
import java.text.NumberFormat
import java.util.Locale

fun formatPrice(amount: Double): String {
    val integerPart = amount.toInt()
    val dfs = java.text.DecimalFormatSymbols()
    dfs.groupingSeparator = '.'
    val df = java.text.DecimalFormat("#,##0", dfs)
    val formatted = df.format(integerPart)
    return if (formatted.isEmpty()) "0 TL" else "$formatted TL"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: SpendingViewModel = viewModel()) {
    if (!viewModel.hasCompletedOnboarding) {
        OnboardingScreen(onFinish = { viewModel.completeOnboarding() })
    } else if (!viewModel.hasCompletedIncomeSetup) {
        IncomeSetupScreen(viewModel = viewModel)
    } else {
        val spendings by viewModel.allSpendings.collectAsStateWithLifecycle()
        val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
        val totalAvoided by viewModel.totalAvoided.collectAsStateWithLifecycle()
        val categorySpentDistribution by viewModel.categorySpentDistribution.collectAsStateWithLifecycle()

        val focusManager = LocalFocusManager.current

        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Persistent Custom M3 Navigation Bar
            CustomBottomNavigation(
                currentScreen = viewModel.currentScreen,
                onTabSelected = { screen ->
                    focusManager.clearFocus()
                    viewModel.currentScreen = screen
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen content with crossfade transitions
            Crossfade(
                targetState = viewModel.currentScreen,
                animationSpec = tween(durationMillis = 300),
                label = "ScreenTransition"
            ) { screen ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    if (screen != AppScreen.SETTINGS) {
                        // Header Bar (stays persistent at the top of content screens, matches HTML top bar)
                        HeaderBar(onSettingsClick = { viewModel.currentScreen = AppScreen.SETTINGS })
                    }

                    when (screen) {
                        AppScreen.ADD -> AddSpendingScreen(viewModel = viewModel)
                        AppScreen.VERDICT -> VerdictScreen(viewModel = viewModel)
                        AppScreen.SUMMARY -> SummaryScreen(viewModel = viewModel)
                        AppScreen.HISTORY -> HistoryScreen(spendings = spendings, viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }

            // Breath/Pause Fullscreen Overlay
            AnimatedVisibility(
                visible = viewModel.isBreathingActive,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                BreathingOverlay(text = viewModel.breathingText)
            }

            // Streak Reset Feedback Fullscreen Overlay
            AnimatedVisibility(
                visible = viewModel.isStreakResetActive,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                StreakResetOverlay(
                    onConfirm = { viewModel.confirmAddSpendingAndResetStreak() },
                    onCancel = { viewModel.isStreakResetActive = false }
                )
            }

            // Dopamine Celebration Fullscreen Overlay
            AnimatedVisibility(
                visible = viewModel.isCelebrationActive,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                val dailyAvoidedVal by viewModel.dailyAvoided.collectAsStateWithLifecycle()
                CelebrationOverlay(
                    amount = viewModel.celebrationAmount,
                    totalAvoided = totalAvoided,
                    dailyAvoided = dailyAvoidedVal,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }

            // Goal Achievement Fullscreen Overlay
            AnimatedVisibility(
                visible = viewModel.isGoalCelebrationActive,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                GoalCelebrationOverlay(
                    goalTitle = viewModel.savingsGoalTitle,
                    goalAmount = viewModel.savingsGoalAmount,
                    onNewGoal = {
                        viewModel.clearSavingsGoal()
                        viewModel.isGoalCelebrationActive = false
                        viewModel.currentScreen = AppScreen.SUMMARY
                        viewModel.showGoalDialogDirectly = true
                    },
                    onDismiss = {
                        viewModel.clearSavingsGoal()
                        viewModel.isGoalCelebrationActive = false
                    }
                )
            }

            // Global Savings Goal Edit Dialog
            if (viewModel.showGoalDialogDirectly) {
                var goalTitleInputText by remember { mutableStateOf(if (viewModel.savingsGoalTitle.isEmpty()) "" else viewModel.savingsGoalTitle) }
                var goalAmountInputText by remember { mutableStateOf(if (viewModel.savingsGoalAmount <= 0.0) "" else viewModel.savingsGoalAmount.toInt().toString()) }

                AlertDialog(
                    onDismissRequest = { viewModel.showGoalDialogDirectly = false },
                    title = {
                        Text(
                            text = "Birikim Hedefini Güncelle",
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Neye odaklanmak istersin? (örn. Yeni Telefon, Tatil, Kulaklık)",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                            OutlinedTextField(
                                value = goalTitleInputText,
                                onValueChange = { goalTitleInputText = it },
                                label = { Text("Hedef Adı") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = LineDivider
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                            )
                            OutlinedTextField(
                                value = goalAmountInputText,
                                onValueChange = { goalAmountInputText = it.filter { char -> char.isDigit() } },
                                label = { Text("Hedef Fiyatı (₺)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = LineDivider
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("goal_amount_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val title = if (goalTitleInputText.trim().isEmpty()) "Tasarruf Hedefi" else goalTitleInputText.trim()
                                val amount = goalAmountInputText.toDoubleOrNull() ?: 1000.0
                                viewModel.updateSavingsGoal(title, if (amount <= 0.0) 1000.0 else amount)
                                viewModel.showGoalDialogDirectly = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.showGoalDialogDirectly = false }) {
                            Text("Vazgeç", color = InkSoft, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CardWhite,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
    }
}

@Composable
fun HeaderBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Styled wordmark matching: "Dur Bakalım" where "Bakalım" is italicized
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ForestDeep, fontWeight = FontWeight.Bold)) {
                    append("Dur ")
                }
                withStyle(style = SpanStyle(color = ForestGreen, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic)) {
                    append("Bakalım")
                }
            },
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            letterSpacing = (-0.01).sp
        )

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SageSoft)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ayarlar",
                tint = ForestGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(color = LineDivider, thickness = 1.dp)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun AddSpendingScreen(viewModel: SpendingViewModel) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = "Ne harcayacaksın?",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestDeep
            )
            Text(
                text = "Harcamadan önce bir nefes al, sonra karar ver.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        item {
            StreakCard(streakCount = viewModel.streakCount)
        }

        // Tutar input
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TUTAR",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.dp,
                            color = Color.Transparent
                        )
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "₺",
                        fontFamily = FontFamily.Serif,
                        fontSize = 26.sp,
                        color = InkSoft,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.amountInput,
                        onValueChange = { viewModel.amountInput = it },
                        placeholder = { Text("0", fontSize = 28.sp, fontFamily = FontFamily.Serif) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = LineDivider,
                            errorBorderColor = TerraRed
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input")
                    )
                }
            }
        }

        // Açıklama input
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NE İÇİN",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = viewModel.descriptionInput,
                    onValueChange = { viewModel.descriptionInput = it },
                    placeholder = { Text("Örn. kahve, tişört, oyun...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = LineDivider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input")
                )
            }
        }

        // Kategori Selection
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "KATEGORİ",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Render Categories into three visually elegant rows to guarantee touch target clearance and responsive design
                val row1 = listOf("Market", "Yemek", "Giyim")
                val row2 = listOf("Eğitim", "Teknoloji", "Oyun")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEach { cat ->
                            CategoryChip(
                                categoryName = cat,
                                isSelected = viewModel.selectedCategory == cat,
                                onClick = { viewModel.selectCategory(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2.forEach { cat ->
                            CategoryChip(
                                categoryName = cat,
                                isSelected = viewModel.selectedCategory == cat,
                                onClick = { viewModel.selectCategory(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip(
                            categoryName = "Dijital Abonelikler",
                            isSelected = viewModel.selectedCategory == "Dijital Abonelikler",
                            onClick = { viewModel.selectCategory("Dijital Abonelikler") },
                            modifier = Modifier.weight(2f)
                        )
                        CategoryChip(
                            categoryName = "Diğer",
                            isSelected = viewModel.selectedCategory == "Diğer",
                            onClick = { viewModel.selectCategory("Diğer") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Planlı / Anlık Selection Slider Toggle
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BU HARCAMA",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val isImpulse = viewModel.isImpulse
                val activeToggleColor by animateColorAsState(
                    targetValue = if (isImpulse) TerraRed else ForestGreen,
                    animationSpec = tween(400, easing = EaseInOut),
                    label = "activeToggleColor"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SageSoft)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isImpulse) activeToggleColor else Color.Transparent)
                            .clickable { viewModel.setPlanningType(false) }
                            .padding(vertical = 12.dp)
                            .testTag("planning_toggle_planli"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Planlıydı",
                            color = if (!isImpulse) Color.White else ForestDeep,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isImpulse) activeToggleColor else Color.Transparent)
                            .clickable { viewModel.setPlanningType(true) }
                            .padding(vertical = 12.dp)
                            .testTag("planning_toggle_anlik"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Anlık geldi",
                            color = if (isImpulse) Color.White else ForestDeep,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Şimdiki Duygu Durumu Selection
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ŞU ANKİ DUYGU DURUMUN",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val emotionOptions = listOf(
                        "😌 Sakin" to "Sakin",
                        "🙂 Mantıklı" to "Mantıklı",
                        "😐 Kararsız" to "Kararsız",
                        "😵 Dürtüsel" to "Dürtüsel",
                        "🔥 İstek" to "Aşırı İstek"
                    )

                    emotionOptions.forEach { (label, fullName) ->
                        val isSelected = viewModel.selectedEmotion == fullName
                        val activeColor = when (fullName) {
                            "Sakin", "Mantıklı" -> ForestGreen
                            "Kararsız" -> AmberYellow
                            else -> TerraRed
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.12f) else CardWhite)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) activeColor else LineDivider,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectedEmotion = fullName }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label.split(" ")[0], // emoji
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label.split(" ").getOrElse(1) { "" }, // label text
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) activeColor else InkDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Check spending button
        item {
            val amount = viewModel.amountInput.toDoubleOrNull() ?: 0.0
            val isEnabled = amount > 0.0 && viewModel.selectedCategory.isNotEmpty()
            val isImpulse = viewModel.isImpulse

            val buttonColor by animateColorAsState(
                targetValue = if (isImpulse) TerraRed else ForestGreen,
                animationSpec = tween(450, easing = EaseOutBack),
                label = "checkButtonColor"
            )

            val buttonScale by animateFloatAsState(
                targetValue = if (isEnabled) 1.0f else 0.97f,
                animationSpec = spring(
                    dampingRatio = if (isImpulse) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "buttonScaleAnim"
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.checkSpending()
                },
                enabled = isEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                    disabledContainerColor = LineDivider,
                    disabledContentColor = InkSoft
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale)
                    .testTag("check_spending_button")
            ) {
                Text(
                    text = "Kontrol Et",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryChip(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ForestGreen else CardWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) ForestGreen else LineDivider,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag("category_chip_$categoryName"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = categoryName,
            color = if (isSelected) Color.White else InkSoft,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

private fun getAdviceSentences(category: String, amount: Double, isImpulse: Boolean, budget: Double): List<String> {
    val pool = mutableListOf<String>()
    
    // ⏳ Erteleme önerileri (Delaying)
    pool.add("Bunu şimdi almak yerine 24 saat ertelemeyi dene.")
    pool.add("Yarın hâlâ istiyor musun kontrol et; istek geçici olabilir.")
    pool.add("Almak için neden tam olarak şu anı seçtin? Acele etme.")

    // 🧠 Farkındalık (Mindfulness)
    if (isImpulse) {
        pool.add("Şu an hissettiğin anlık satın alma dürtüsü mü yoksa gerçek bir ihtiyaç mı?")
        pool.add("Bu kararı vermene yol açan şey o anki stres, can sıkıntısı veya açlık olabilir mi?")
    } else {
        pool.add("Bu harcama gerçekten planlarına dâhil mi, yoksa sonradan mı haklı çıkardın?")
        pool.add("Bu ürünü almadan önceki hayatın ile aldıktan sonraki hayatın arasındaki fark nedir?")
    }

    // 💰 Bütçe etkisi (Budget effect)
    if (budget > 0) {
        val pct = ((amount / budget) * 100).toInt().coerceAtLeast(1)
        pool.add("Bu harcama, toplam aylık bütçenin yaklaşık %$pct'sini tek başına götürüyor.")
        pool.add("Bu parayı harcarsan, bu ayki günlük ortalama limitini kısmak zorunda kalabilirsin.")
    } else {
        pool.add("Bütçeni ayarlamadan bu ölçekte bir harcama yapmak finansal dengeni bozabilir.")
    }

    // 🎯 Alternatif öneriler (Alternative)
    when (category) {
        "Market" -> {
            pool.add("Bu gerçekten temel bir ihtiyaç listesi elemanı mı, yoksa abur cubur mu?")
            pool.add("Evdeki mevcut stokları kontrol ettin mi? Belki de evde zaten vardır.")
        }
        "Yemek" -> {
            pool.add("Evde daha sağlıklı ve çok daha ekonomik bir alternatif hazırlayabilir misin?")
            pool.add("Sadece canın tatlı/tuzlu bir şeyler çektiği için mi sipariş veriyorsun?")
        }
        "Giyim" -> {
            pool.add("Dolabında buna çok benzer kaç tane daha alternatif giysi var?")
            pool.add("Gerçekten giyecek misin yoksa birkaç kez giyilip kenarda mı bekleyecek?")
        }
        "Eğitim" -> {
            pool.add("Kendine yapacağın bu yatırıma aktif zaman ayırabileceğinden emin misin?")
            pool.add("Benzer kaliteli ücretsiz eğitim ve kaynakları internette araştırdın mı?")
        }
        "Teknoloji" -> {
            pool.add("Bu teknolojik cihaz gerçekten şu anki hayatını kolaylaştıracak mı?")
            pool.add("Eski cihazın gerçekten işlevini yitirdi mi, yoksa yenilik hevesi mi?")
        }
        "Oyun" -> {
            pool.add("Bu oyunu/öğeyi gerçekten saatlerce oynayacak mısın yoksa anlık bir heves mi?")
            pool.add("Boş zamanını değerlendirmek için ücretsiz bir oyun alternatifi var mı?")
        }
        "Dijital Abonelikler" -> {
            pool.add("Bu aboneliğin yıllık toplam maliyeti yaklaşık ₺${(amount * 12).toInt()} tutuyor.")
            pool.add("Mevcut diğer aboneliklerini (Netflix, Spotify, gamepass vb.) gerçekten aktif kullanıyor musun?")
        }
        else -> {
            pool.add("Bu parayı başka bir tasarruf hedefine veya gelecekteki kendine yönlendirebilirsin.")
            pool.add("Bu harcamayı yapmasan hayatında ne gibi bir eksiklik hissederdin?")
        }
    }

    // 🔥 Psikolojik öneriler (Psychological)
    pool.add("Bu harcamayı yapmadığında hissedeceğin o finansal özgürlük ve rahatlığı hayal et.")
    pool.add("Farklı satıcılarda veya platformlarda daha uygun fiyatlı alternatifleri var mı?")
    pool.add("Bu harcama yerine, parayı doğrudan tasarruf hesabına aktarmayı düşün.")

    return pool.distinct().take(8)
}

@Composable
fun VerdictScreen(viewModel: SpendingViewModel) {
    val pending = viewModel.pendingSpending ?: return
    val level = pending.verdict

    val cardBg = when (level) {
        "green" -> SageSoft
        "yellow" -> AmberSoft
        else -> TerraSoft
    }
    val borderColor = when (level) {
        "green" -> SageGreen
        "yellow" -> AmberYellow
        else -> TerraRed
    }
    val icon = when (level) {
        "green" -> Icons.Default.Check
        "yellow" -> Icons.Default.PriorityHigh
        else -> Icons.Default.Close
    }
    val iconBg = when (level) {
        "green" -> ForestGreen
        "yellow" -> AmberYellow
        else -> TerraRed
    }
    val textColor = when (level) {
        "green" -> ForestDeep
        "yellow" -> Color(0xFF92400E)
        else -> TerraRed
    }
    val title = when (level) {
        "green" -> "Mantıklı Harcama"
        "yellow" -> "Biraz Daha Düşün"
        else -> "Muhtemelen Gereksiz"
    }
    val formattedDesc = pending.description.takeIf { it.isNotEmpty() } ?: pending.category
    val bLevel = viewModel.budgetStatusLevel.value
    val msg = when (level) {
        "green" -> {
            if (!pending.isImpulse) {
                "Bu planlı harcama bütçeni ciddi etkilemiyor. Makul görünüyor."
            } else if (bLevel == "red") {
                "Harcama kategorisi mantıklı görünse de, bütçen şu an Sınırda! Bu harcamayı yaparken ekstra dikkatli olmanı öneririz."
            } else {
                "Harika! Bu harcama bütçenle uyumlu ve dengeli görünüyor. Gönül rahatlığıyla devam edebilirsin."
            }
        }
        "yellow" -> {
            if (pending.isImpulse) {
                "Bu anlık harcama dikkatli değerlendirilmelidir."
            } else if (bLevel == "red") {
                "DİKKAT: Bütçen risk altındayken bu harcamayı yapmak seni daha da zorlayacaktır. En azından ertelemeyi düşünmelisin!"
            } else {
                "Bu harcama bütçeni doğrudan sarsmayabilir ama biraz daha düşünmende yarar var. Farklı alternatifleri değerlendirebilirsin."
            }
        }
        else -> {
            if (pending.isImpulse) {
                "Bu anlık harcama dikkatli değerlendirilmelidir. Bütçen veya dürtüsellik risk seviyesi çok yüksek!"
            } else if (bLevel == "red") {
                "DUR BAKALIM! Bütçen Kırmızı Alarm verirken bu harcamayı yapmak kesinlikle finansal dengeni bozacaktır. Lütfen hemen vazgeç!"
            } else {
                "Dur bakalım! Bu harcama bütçen rahat olsa da dürtüsel veya bütçe oranına göre yüksek görünüyor. Kendine biraz zaman verip kararını gözden geçirmelisin."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Değerlendirme",
            style = MaterialTheme.typography.headlineMedium,
            color = ForestDeep
        )
        Text(
            text = "Harcama öncesi durup düşündüğün için tebrikler. İşte analizin:",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Verdict Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Colored badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Verdict Status",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pişmanlık Tahmini Badge (V7)
                val regretPct = viewModel.getRegretPercentageForEmotion(pending.emotion)
                val (regretColor, regretLabel, regretIcon) = when {
                    regretPct >= 65 -> Triple(TerraRed, "Yüksek Pişmanlık Riski", "⚠️")
                    regretPct >= 35 -> Triple(AmberYellow, "Orta Pişmanlık Riski", "🤔")
                    else -> Triple(ForestGreen, "Düşük Pişmanlık Riski", "✅")
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(regretColor.copy(alpha = 0.12f))
                        .border(1.dp, regretColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$regretIcon  ", fontSize = 12.sp)
                    Text(
                        text = "$regretLabel: %$regretPct",
                        color = if (regretColor == AmberYellow) Color(0xFF92400E) else regretColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Msg Description
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                val remainingBudgetVal by viewModel.remainingBudget.collectAsStateWithLifecycle()
                if (remainingBudgetVal < 0.0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "ℹ️", fontSize = 14.sp)
                        Text(
                            text = "Bu ay bütçenin üzerine çıktığın için karar motoru daha temkinli öneriler sunuyor.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                // Zaman Koruma Sistemi Countdown (V7)
                if (viewModel.impulseTimerSeconds > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ForestDeep)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏱️ DÜRTÜ KALKANI AKTİF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gelecekteki Seni korumak için mola verildi. Biraz nefes al, su iç ve gerçekten isteyip istemediğini düşün.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${viewModel.impulseTimerSeconds} saniye kaldı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.startImpulseTimer(0) }, // Set to 0 to fast-forward
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Hızlandır ⚡", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Verdict Actions
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Impulse delay button option if timer not active
                    if (viewModel.impulseTimerSeconds == 0) {
                        val isHighRisk = level == "red" || level == "yellow" || pending.isImpulse
                        if (isHighRisk) {
                            Button(
                                onClick = { viewModel.startImpulseTimer(30) }, // Start a 30-second mola for easy testing!
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestDeep,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("delay_button")
                            ) {
                                Text(text = "⏱️ Kendime 10 Dk. Mola Ver", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Vazgeç Button
                        Button(
                            onClick = { viewModel.skipSpending() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (level == "red" || level == "yellow") ForestGreen else CardWhite,
                                contentColor = if (level == "red" || level == "yellow") Color.White else InkSoft
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .border(1.dp, if (level == "red" || level == "yellow") Color.Transparent else LineDivider, RoundedCornerShape(12.dp))
                                .testTag("skip_spending_button")
                        ) {
                            Text(text = "Vazgeç", fontWeight = FontWeight.Bold)
                        }

                        // Yine de Ekle / Ekle Button (only active if countdown is finished or not active)
                        val timerActive = viewModel.impulseTimerSeconds > 0
                        Button(
                            onClick = { viewModel.addSpending() },
                            enabled = !timerActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (timerActive) LineDivider else if (level == "red" || level == "yellow") TerraRed else ForestGreen,
                                contentColor = if (timerActive) InkSoft else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .testTag("add_spending_button")
                        ) {
                            val btnLabel = if (timerActive) "Süre Bekleniyor" else if (level == "green") "Ekle" else "Yine de ekle"
                            Text(text = btnLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2 Kimlik Sistemi Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧠 Karar Psikolojisi (2 Kimlik)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestDeep,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bugünün Sen
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberSoft)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🧍 Bugünkü Sen",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val bugunMsg = when {
                            pending.emotion == "Aşırı İstek" -> "Şu an yoğun bir arzu içindesin. Hemen bu doyuma ulaşmak, kendini ödüllendirmek istiyorsun."
                            pending.emotion == "Dürtüsel" -> "Anlık bir dürtüyle hareket ediyorsun. Duyguların mantığının önüne geçmiş durumda."
                            pending.emotion == "Kararsız" -> "Net değilsin, içten içe bu harcamayı rasyonalize etmeye çalışıyorsun."
                            else -> "Sakin ve dengelisin ama yine de satın alma arzusu tetiklenmiş durumda."
                        }
                        Text(
                            text = bugunMsg,
                            fontSize = 11.sp,
                            color = InkDark,
                            lineHeight = 15.sp
                        )
                    }

                    // Gelecekteki Sen
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageSoft)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🧑‍🦳 Gelecekteki Sen",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val gelecektekiMsg = when {
                            pending.emotion == "Aşırı İstek" || pending.emotion == "Dürtüsel" -> "Gelecekteki sen muhtemelen pişman olacak. Bu para hedefin olan '${viewModel.savingsGoalTitle}' için birikmeliydi."
                            pending.emotion == "Kararsız" -> "Eğer ertelersen yarın 'iyi ki almamışım' diyebilirsin. Kararsızlık pişmanlık getirebilir."
                            else -> "Makul bir harcama gibi dursa da bu parayı birikime eklemek seni hedeflerine yaklaştırır."
                        }
                        Text(
                            text = gelecektekiMsg,
                            fontSize = 11.sp,
                            color = InkDark,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Suggestions / Questions Section
        Text(
            text = "Düşünmen İçin Sorular",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ForestDeep,
            modifier = Modifier.padding(top = 8.dp)
        )

        val advices = getAdviceSentences(pending.category, pending.amount, pending.isImpulse, viewModel.monthlyBudget)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            advices.forEachIndexed { index, advice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LineDivider, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SageSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(viewModel: SpendingViewModel) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))
    
    val streakCount = viewModel.streakCount
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val totalAvoided by viewModel.totalAvoided.collectAsStateWithLifecycle()
    val categorySpentDistribution by viewModel.categorySpentDistribution.collectAsStateWithLifecycle()
    
    val monthlyBudget = viewModel.monthlyBudget
    val totalSpentThisMonth by viewModel.totalSpentThisMonth.collectAsStateWithLifecycle()
    val remainingBudget by viewModel.remainingBudget.collectAsStateWithLifecycle()
    val dailyAverageLimit by viewModel.dailyAverageLimit.collectAsStateWithLifecycle()
    val spendingTempo by viewModel.spendingTempo.collectAsStateWithLifecycle()
    val budgetStatusLevel by viewModel.budgetStatusLevel.collectAsStateWithLifecycle()

    val dailyAvoided by viewModel.dailyAvoided.collectAsStateWithLifecycle()
    val weeklyAvoided by viewModel.weeklyAvoided.collectAsStateWithLifecycle()
    val monthlyAvoided by viewModel.monthlyAvoided.collectAsStateWithLifecycle()
    val yearlyAvoided by viewModel.yearlyAvoided.collectAsStateWithLifecycle()
    val allTimeAvoided by viewModel.allTimeAvoided.collectAsStateWithLifecycle()
    val activeGoalAvoided by viewModel.activeGoalAvoided.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Tasarruf Kasası",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestDeep
            )
            Text(
                text = "Kararlarının ve birikimlerinin finansal özeti.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // 💰 TASARRUF KASASI (SAVINGS VAULT) - PREMIUM CENTERPIECE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForestGreen.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FBF9)), // Ultra pale soft sage/mint
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Tasarruf Kasam",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Large Grand Saved Amount with smooth float animation
                    val animatedAllTime by animateFloatAsState(
                        targetValue = allTimeAvoided.toFloat(),
                        animationSpec = tween(1500, easing = FastOutSlowInEasing),
                        label = "allTimeAvoidedAnim"
                    )

                    Text(
                        text = formatPrice(animatedAllTime.toDouble()),
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestGreen,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // 2x2 Grid of intervals
                    val animatedDaily by animateFloatAsState(targetValue = dailyAvoided.toFloat(), animationSpec = tween(1200))
                    val animatedWeekly by animateFloatAsState(targetValue = weeklyAvoided.toFloat(), animationSpec = tween(1200))
                    val animatedMonthly by animateFloatAsState(targetValue = monthlyAvoided.toFloat(), animationSpec = tween(1200))
                    val animatedYearly by animateFloatAsState(targetValue = yearlyAvoided.toFloat(), animationSpec = tween(1200))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bugün Card
                            SmallVaultCard(
                                title = "Bugün",
                                value = formatPrice(animatedDaily.toDouble()),
                                emoji = "📅",
                                modifier = Modifier.weight(1f)
                            )
                            // Bu Hafta Card
                            SmallVaultCard(
                                title = "Bu Hafta",
                                value = formatPrice(animatedWeekly.toDouble()),
                                emoji = "📆",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bu Ay Card
                            SmallVaultCard(
                                title = "Bu Ay",
                                value = formatPrice(animatedMonthly.toDouble()),
                                emoji = "🗓️",
                                modifier = Modifier.weight(1f)
                            )
                            // Bu Yıl Card
                            SmallVaultCard(
                                title = "Bu Yıl",
                                value = formatPrice(animatedYearly.toDouble()),
                                emoji = "📈",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(color = LineDivider.copy(alpha = 0.6f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "“Bu uygulama sayesinde vazgeçtiğin harcamaların toplamı.”",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = InkSoft,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        // Monthly Budget & Status Card (NEW FEATURE V5)
        item {
            var showEditDialog by remember { mutableStateOf(false) }
            var budgetInputText by remember(monthlyBudget) { mutableStateOf(monthlyBudget.toInt().toString()) }

            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = {
                        Text(
                            text = "Aylık Bütçeni Güncelle",
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Lütfen aylık harcama bütçeni gir (TL):",
                                fontSize = 14.sp,
                                color = InkSoft
                            )
                            OutlinedTextField(
                                value = budgetInputText,
                                onValueChange = { budgetInputText = it.filter { char -> char.isDigit() } },
                                label = { Text("Aylık Bütçe (₺)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = LineDivider
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("budget_edit_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val value = budgetInputText.toDoubleOrNull() ?: 10000.0
                                viewModel.updateMonthlyBudget(value)
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Vazgeç", color = InkSoft, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CardWhite,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title and Edit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Aylık Bütçe Durumu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestDeep
                            )
                            Text(
                                text = "Döngü: ${viewModel.getCycleRangeString()}",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                        IconButton(
                            onClick = {
                                budgetInputText = monthlyBudget.toInt().toString()
                                showEditDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SageSoft)
                                .testTag("edit_budget_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Budget",
                                tint = ForestGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = LineDivider, thickness = 1.dp)

                    // Budget Progress Bar
                    val budgetProgress = if (monthlyBudget > 0) (totalSpentThisMonth / monthlyBudget).toFloat().coerceIn(0f, 1f) else 1f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aylık Harcanan: ${formatPrice(totalSpentThisMonth)}",
                                fontSize = 12.sp,
                                color = InkDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Limit: ${formatPrice(monthlyBudget)}",
                                fontSize = 12.sp,
                                color = InkSoft,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (budgetStatusLevel) {
                                "red" -> TerraRed
                                "yellow" -> AmberYellow
                                "green" -> ForestGreen
                                else -> ForestGreen
                            },
                            trackColor = SageSoft
                        )
                    }

                    // Grid metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Kalan Bütçe Card
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kalan Bütçe",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                            Text(
                                text = formatPrice(remainingBudget),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget >= 0) ForestGreen else TerraRed
                            )
                        }

                        // Günlük Ortalama Limit Card
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Günlük Limit",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                            Text(
                                text = formatPrice(dailyAverageLimit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestDeep
                            )
                        }

                        // Tempo & Status Card
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Harcama Tempo",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                            Text(
                                text = spendingTempo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (spendingTempo) {
                                    "Hızlı ⚡" -> TerraRed
                                    "Yavaş" -> ForestGreen
                                    else -> AmberYellow
                                }
                            )
                        }
                    }

                    // Budget Status Banner
                    val statusText = when (budgetStatusLevel) {
                        "red" -> "Bütçe Aşımı Riski!"
                        "yellow" -> "Harcamalar Hızlanıyor"
                        else -> "Bütçe Dengeli ve Rahat"
                    }
                    val statusBg = when (budgetStatusLevel) {
                        "red" -> Color(0xFFFDE8E8)
                        "yellow" -> Color(0xFFFCEED4)
                        else -> SageSoft
                    }
                    val statusColor = when (budgetStatusLevel) {
                        "red" -> TerraRed
                        "yellow" -> Color(0xFF92400E)
                        else -> ForestGreen
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBg)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Baskı Seviyesi: $statusText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }

        // Eksi Bakiye Modu Uyarı Kartı (V14)
        if (remainingBudget < 0.0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerraRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .testTag("eksi_bakiye_alert"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🚨", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eksi Bakiye Modu Aktif",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Bu ay bütçeni aştın. Yeni harcamalarda daha dikkatli olmanı öneriyoruz.",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Stat Grid (Weekly spent & skipped total)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // spent card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = formatPrice(totalSpent),
                            fontFamily = FontFamily.Serif,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bu hafta harcanan",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }

                // avoided card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = formatPrice(totalAvoided),
                            fontFamily = FontFamily.Serif,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vazgeçilen tutar",
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }
            }
        }

        // Category breakdown list
        item {
            Text(
                text = "KATEGORİYE GÖRE",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }

        if (categorySpentDistribution.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🌱", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Henüz harcama eklemedin.\nİlk kaydını yaptığında burada göreceksin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            val maxAmount = categorySpentDistribution.values.maxOrNull() ?: 1.0

            items(categorySpentDistribution.toList()) { (category, amount) ->
                var isLoaded by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isLoaded = true
                }
                val animProgress by animateFloatAsState(
                    targetValue = if (isLoaded) (amount / maxAmount).toFloat() else 0f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "barFill"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category title
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                        modifier = Modifier.width(80.dp)
                    )

                    // Track & fill bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SageSoft)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = animProgress)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ForestGreen)
                        )
                    }

                    // Value amount
                    Text(
                        text = formatPrice(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        modifier = Modifier
                            .width(76.dp)
                            .padding(start = 8.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // 🎯 BIRIKIM HEDEFI (SAVINGS GOAL CARD)
        item {
            val goalProgress = if (viewModel.savingsGoalAmount > 0) (activeGoalAvoided / viewModel.savingsGoalAmount).toFloat().coerceIn(0f, 1f) else 0f
            val isGoalCompleted = viewModel.savingsGoalAmount > 0.0 && goalProgress >= 1f
            val hasActiveGoal = viewModel.savingsGoalAmount > 0.0 && !viewModel.savingsGoalTitle.isEmpty() && !isGoalCompleted

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!hasActiveGoal) {
                        // EMPTY STATE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Large icon / target symbol
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SageSoft)
                                    .clickable { viewModel.showGoalDialogDirectly = true }
                                    .testTag("empty_goal_plus_icon"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎯", fontSize = 28.sp)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Henüz aktif bir hedefin yok.",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestDeep,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("empty_goal_title")
                                )
                                Text(
                                    text = "Yeni bir hedef ekleyerek tasarruf etmeye başlayabilirsin.",
                                    fontSize = 12.sp,
                                    color = InkSoft,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("empty_goal_description")
                                )
                            }

                            Button(
                                onClick = { viewModel.showGoalDialogDirectly = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("add_goal_empty_state_button")
                            ) {
                                Text(
                                    text = "+ Hedef Ekle",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // ACTIVE STATE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🎯 Birikim Hedefin",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestDeep
                                )
                                Text(
                                    text = "Harcamaktan vazgeçtiğin parayla alınan hedef",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.showGoalDialogDirectly = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SageSoft)
                                    .testTag("edit_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Goal",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = LineDivider, thickness = 1.dp)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = viewModel.savingsGoalTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Text(
                                    text = "%${(goalProgress * 100).toInt()}",
                                    fontSize = 14.sp,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { goalProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ForestGreen,
                                trackColor = SageSoft
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Biriken: ${formatPrice(activeGoalAvoided)}",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                                Text(
                                    text = "Hedef: ${formatPrice(viewModel.savingsGoalAmount)}",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                        }
                    }
                }
            }
        }

        // ⏱️ KEŞKE LİSTESİ (WISHLIST SECTION)
        item {
            Text(
                text = "KEŞKE LİSTESİ",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }

        if (wishlistItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Keşke listen tamamen temiz!",
                            fontWeight = FontWeight.Bold,
                            color = ForestDeep,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Vazgeçtiğin veya mola verdiğin dürtüsel harcamaların hepsi burada birikir.",
                            color = InkSoft,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(wishlistItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.description.takeIf { it.isNotEmpty() } ?: item.category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Text(
                                    text = "${item.category} · ${item.emotion}",
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                            Text(
                                text = formatPrice(item.amount),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ForestGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⏱️ Bu harcamadan vazgeçtin. Hâlâ almak istiyor musun?",
                            fontSize = 11.sp,
                            color = InkSoft,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateWishlistItemState(item.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = SageSoft, contentColor = ForestDeep),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Evet, İstiyorum 👍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.updateWishlistItemState(item.id, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerraSoft, contentColor = TerraRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Hayır, Pişmanım 🙅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 🧠 DÜRTÜ HARİTASI (IMPULSE MAP & BEHAVIORAL ANALYTICS)
        item {
            Text(
                text = "DÜRTÜ HARİTASI",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }

        item {
            val list = viewModel.allSpendings.collectAsStateWithLifecycle().value
            val impulseCount = list.count { it.isImpulse }
            val totalCount = list.size.coerceAtLeast(1)
            val impulseRatio = ((impulseCount.toDouble() / totalCount) * 100).toInt()

            val mostRiskyCategory = list.filter { it.isImpulse }
                .groupBy { it.category }
                .maxByOrNull { it.value.size }?.key ?: "Yok"

            val mostRiskyEmotion = list.filter { it.isImpulse }
                .groupBy { it.emotion }
                .maxByOrNull { it.value.size }?.key ?: "Yok"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🧠 Davranışsal Analiz",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestDeep
                            )
                            Text(
                                text = "Satın alma davranışlarının psikolojik haritası",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                    }

                    HorizontalDivider(color = LineDivider, thickness = 1.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Dürtüsellik Oranı", fontSize = 12.sp, color = InkSoft)
                            Text(text = "%$impulseRatio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (impulseRatio > 40) TerraRed else ForestGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "En Tetikleyici Kategori", fontSize = 12.sp, color = InkSoft)
                            Text(text = mostRiskyCategory, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "En Riskli Duygu Durumu", fontSize = 12.sp, color = InkSoft)
                            Text(text = mostRiskyEmotion, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SmallStatCard(
    title: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = InkSoft,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@Composable
fun SpentItemCard(
    spending: SpendingEntry,
    formatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LineDivider.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TerraSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💸",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spending.description.takeIf { it.isNotEmpty() } ?: spending.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = spending.category,
                        fontSize = 11.sp,
                        color = InkSoft,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "·", fontSize = 11.sp, color = InkSoft)
                    Text(
                        text = if (spending.isImpulse) "⚠️ Anlık" else "📅 Planlı",
                        fontSize = 11.sp,
                        color = if (spending.isImpulse) TerraRed else InkSoft,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatPrice(spending.amount),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = TerraRed
            )
        }
    }
}

@Composable
fun SavedItemCard(
    spending: SpendingEntry,
    formatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FBF9)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SageSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌱",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spending.description.takeIf { it.isNotEmpty() } ?: spending.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = ForestDeep
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = spending.category,
                        fontSize = 11.sp,
                        color = InkSoft,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "·", fontSize = 11.sp, color = InkSoft)
                    Text(
                        text = "🧠 ${spending.emotion}",
                        fontSize = 11.sp,
                        color = InkSoft,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatPrice(spending.amount),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = ForestGreen
            )
        }
    }
}

@Composable
fun HistoryScreen(spendings: List<SpendingEntry>, viewModel: SpendingViewModel) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val allTimeSpent by viewModel.allTimeSpent.collectAsStateWithLifecycle()
    val dailySpent by viewModel.dailySpent.collectAsStateWithLifecycle()
    val weeklySpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val monthlySpent by viewModel.totalSpentThisMonth.collectAsStateWithLifecycle()

    val allTimeAvoided by viewModel.allTimeAvoided.collectAsStateWithLifecycle()
    val dailyAvoided by viewModel.dailyAvoided.collectAsStateWithLifecycle()
    val weeklyAvoided by viewModel.weeklyAvoided.collectAsStateWithLifecycle()
    val monthlyAvoided by viewModel.monthlyAvoided.collectAsStateWithLifecycle()

    val addedSpendings = spendings.filter { it.isAdded }
    val avoidedSpendings = spendings.filter { !it.isAdded }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Karar Geçmişi",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ForestDeep
                )
                Text(
                    text = "Dürtüsel anlar ve bilinçli harcamaların günlüğü.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LineDivider.copy(alpha = 0.4f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Harcamalarım", "Tasarruflarım")
                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    val activeBg = if (index == 0) TerraSoft else SageSoft
                    val activeText = if (index == 0) TerraRed else ForestDeep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp)
                            .testTag("history_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) activeText else InkSoft
                        )
                    }
                }
            }
        }

        item {
            if (selectedTab == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerraRed.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = TerraSoft.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💸 TOPLAM HARCAMA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerraRed,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatPrice(allTimeSpent),
                            fontSize = 34.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkDark
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SmallStatCard(
                                title = "Bugün",
                                value = formatPrice(dailySpent),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = InkDark,
                                modifier = Modifier.weight(1f)
                            )
                            SmallStatCard(
                                title = "Bu Hafta",
                                value = formatPrice(weeklySpent),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = InkDark,
                                modifier = Modifier.weight(1f)
                            )
                            SmallStatCard(
                                title = "Bu Ay",
                                value = formatPrice(monthlySpent),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = InkDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ForestGreen.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SageSoft.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💰 TOPLAM TASARRUF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatPrice(allTimeAvoided),
                            fontSize = 34.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SmallStatCard(
                                title = "Bugün",
                                value = formatPrice(dailyAvoided),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = ForestDeep,
                                modifier = Modifier.weight(1f)
                            )
                            SmallStatCard(
                                title = "Bu Hafta",
                                value = formatPrice(weeklyAvoided),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = ForestDeep,
                                modifier = Modifier.weight(1f)
                            )
                            SmallStatCard(
                                title = "Bu Ay",
                                value = formatPrice(monthlyAvoided),
                                bgColor = CardWhite,
                                borderColor = LineDivider,
                                textColor = ForestDeep,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (selectedTab == 0) "KAYDEDİLEN HARCAMALAR" else "VAZGEÇİLEN DÜRTÜLER",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val currentList = if (selectedTab == 0) addedSpendings else avoidedSpendings
        if (currentList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "🛒" else "🌱",
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedTab == 0) "Henüz kaydedilmiş bir harcaman yok." else "Henüz vazgeçilen bir harcama yok.\nDürtülerine direnmeye devam et!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            items(currentList, key = { it.id }) { spending ->
                if (selectedTab == 0) {
                    SpentItemCard(spending = spending, formatter = formatter)
                } else {
                    SavedItemCard(spending = spending, formatter = formatter)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CustomBottomNavigation(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite)
            .border(width = 1.dp, color = LineDivider, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val screens = listOf(
            Triple(AppScreen.ADD, "Ekle", Icons.Default.Add),
            Triple(AppScreen.SUMMARY, "Özet", Icons.Default.PieChart),
            Triple(AppScreen.HISTORY, "Geçmiş", Icons.Default.History)
        )

        screens.forEach { (screen, label, icon) ->
            val isActive = currentScreen == screen || (screen == AppScreen.ADD && currentScreen == AppScreen.VERDICT)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) SageSoft else Color.Transparent)
                    .clickable { onTabSelected(screen) }
                    .padding(vertical = 8.dp)
                    .testTag("bottom_nav_tab_${label.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) ForestDeep else InkSoft,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) ForestDeep else InkSoft
                )
            }
        }
    }
}

@Composable
fun BreathingOverlay(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestDeep)
            .clickable(enabled = false) {}, // Absorb all interactions
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Concentric breathing layers representing a high-fidelity soft glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer Glow Layer 1
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(scale * 1.25f)
                        .clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.08f))
                )
                // Outer Glow Layer 2
                Box(
                    modifier = Modifier
                        .size(155.dp)
                        .scale(scale * 1.12f)
                        .clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.16f))
                )
                // Main Single Colored Core Circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(ForestGreen)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Calm breathing instructions text
            Text(
                text = text,
                color = SageGreen,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                modifier = Modifier.alpha(textAlpha),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class CelebrationConfetti(
    val xFraction: Float,
    val ySpeed: Float,
    val color: Color,
    val size: Float
)

@Composable
fun CelebrationOverlay(
    amount: Double,
    totalAvoided: Double,
    dailyAvoided: Double,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))

    // Elegant, high-FPS falling confetti canvas
    val particles = remember {
        List(30) {
            CelebrationConfetti(
                xFraction = (0..100).random() / 100f,
                ySpeed = (300..700).random().toFloat(),
                color = listOf(
                    Color(0xFF2E9C86), // Teal
                    Color(0xFFD6ECE6), // Pale Teal
                    Color(0xFF0C6E5E), // Deep Teal
                    Color(0xFFE8C468), // Soft Gold
                    Color(0xFF6FBBA9)  // Bright Teal
                ).random(),
                size = (8..22).random().toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Dynamic scale-up zoom animation for the center circular badge
    var startZoom by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startZoom = true
    }
    val badgeScale by animateFloatAsState(
        targetValue = if (startZoom) 1f else 0.3f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "badgeScale"
    )

    // Animate the total avoided amount up to reward the user visually (dopamine loop)
    val animatedTotalAvoided by animateFloatAsState(
        targetValue = totalAvoided.toFloat(),
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "totalAvoidedAnimation"
    )

    // Animate the daily avoided amount
    val animatedDailyAvoided by animateFloatAsState(
        targetValue = dailyAvoided.toFloat(),
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "dailyAvoidedAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECF7F4), // Gentle pale mint top (Açık yeşil soft)
                        CreamBg            // Soft krem background bottom
                    )
                )
            )
            .clickable(enabled = false) {}, // Absorb touch events under overlay
        contentAlignment = Alignment.Center
    ) {
        // Draw Confetti Particles (Minimal Confetti)
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val y = (time * p.ySpeed + (p.xFraction * size.height)) % size.height
                val x = p.xFraction * size.width
                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated Big Green Check badge (Büyük Check Icon)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(badgeScale)
                    .clip(CircleShape)
                    .background(ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Dopamine pop-up element: "+1 Gün" with organic spring bounce animation
            var plusOneScaleActive by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(300)
                plusOneScaleActive = true
            }
            val plusOneScale by animateFloatAsState(
                targetValue = if (plusOneScaleActive) 1.1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "plusOneScale"
            )

            if (plusOneScale > 0.01f) {
                Row(
                    modifier = Modifier
                        .scale(plusOneScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFCEED4))
                        .border(1.5.dp, Color(0xFFC4841A), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 20.sp)
                    Text(
                        text = "Serini devam ettirdin.",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB4790A),
                        fontSize = 14.sp
                    )
                }
            }

            // Congratulations Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(badgeScale),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "İyi seçim. Kontrol sende.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestDeep,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${formatPrice(amount)} tasarruf ettin.",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Kararlılığın sayesinde tasarruf serini başarıyla korudun.",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = LineDivider, thickness = 1.dp)

                    // Daily Saved Money Display (NEW DOPAMINE ELEMENT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bugün Kurtarılan Para:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Text(
                            text = formatPrice(animatedDailyAvoided.toDouble()),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }

                    // Dopamine Accumulator: visual metric tracking level progress
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Toplam Kurtarılan Para:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkDark
                            )
                            Text(
                                text = formatPrice(animatedTotalAvoided.toDouble()),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        val nextMilestone = (((totalAvoided.toInt() / 1000) + 1) * 1000).toFloat()
                        val progress = (animatedTotalAvoided / nextMilestone).coerceIn(0f, 1f)

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ForestGreen,
                            trackColor = SageSoft
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tasarruf Seviyesi",
                                fontSize = 10.sp,
                                color = InkSoft
                            )
                            Text(
                                text = "Sonraki Hedef: ₺${nextMilestone.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSoft
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium Dismiss/Continue CTA button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("celebration_continue_button")
            ) {
                Text(
                    text = "Devam Et",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

fun getStreakFlameEmoji(streak: Int): String {
    return when {
        streak >= 30 -> "🔥💥⚡✨"
        streak >= 15 -> "🔥💥✨"
        streak >= 7 -> "🔥💥"
        streak >= 3 -> "🔥"
        else -> "✨"
    }
}

@Composable
fun StreakCard(streakCount: Int) {
    val flameText = getStreakFlameEmoji(streakCount)
    
    // Scale pulse animation for high streaks (30+ days)
    val infiniteTransition = rememberInfiniteTransition(label = "streakGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (streakCount >= 30) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .border(
                width = 1.dp,
                color = if (streakCount >= 30) ForestGreen else if (streakCount >= 15) SageGreen else LineDivider,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (streakCount >= 30) Color(0xFFEFFAF7) else if (streakCount >= 15) Color(0xFFF6FBFA) else CardWhite
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = flameText,
                    fontSize = if (streakCount >= 15) 20.sp else 16.sp
                )
                Text(
                    text = "$streakCount Gün Serisi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestDeep
                )
            }
            Text(
                text = when {
                    streakCount >= 30 -> "İnanılmaz! 🏆"
                    streakCount >= 15 -> "Harika! 💪"
                    streakCount >= 7 -> "Mükemmel! ⭐"
                    streakCount >= 3 -> "Süper! 👍"
                    else -> "Başlayalım! ✨"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = InkSoft
            )
        }
    }
}

@Composable
fun StreakResetOverlay(onConfirm: () -> Unit, onCancel: () -> Unit) {
    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnims = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0.7f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val opacity by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0f,
        animationSpec = tween(400),
        label = "opacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { /* absorb taps */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(scale)
                .graphicsLayer(alpha = opacity)
                .border(1.dp, LineDivider, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LineDivider.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 40.sp)
                }

                Text(
                    text = "Günlük Serin Bozuldu",
                    style = MaterialTheme.typography.titleLarge,
                    color = TerraRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Bugünkü kararın nedeniyle tasarruf serin sona erdi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = TerraRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("streak_reset_confirm_button")
                ) {
                    Text(
                        text = "Devam Et",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("streak_reset_cancel_button")
                ) {
                    Text(
                        text = "Vazgeç",
                        color = InkSoft,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GoalCelebrationOverlay(
    goalTitle: String,
    goalAmount: Double,
    onNewGoal: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))

    // Elegant, high-FPS falling confetti canvas
    val particles = remember {
        List(30) {
            CelebrationConfetti(
                xFraction = (0..100).random() / 100f,
                ySpeed = (300..700).random().toFloat(),
                color = listOf(
                    Color(0xFF2E9C86), // Teal
                    Color(0xFFD6ECE6), // Pale Teal
                    Color(0xFF0C6E5E), // Deep Teal
                    Color(0xFFE8C468), // Soft Gold
                    Color(0xFF6FBBA9)  // Bright Teal
                ).random(),
                size = (8..22).random().toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "goalConfetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "goalTime"
    )

    var startZoom by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startZoom = true
    }
    val badgeScale by animateFloatAsState(
        targetValue = if (startZoom) 1f else 0.3f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "goalBadgeScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECF7F4), // Pale mint top
                        CreamBg            // Soft cream bottom
                    )
                )
            )
            .clickable { /* Block interaction with background */ }
            .testTag("goal_celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Render beautiful floating confetti
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            particles.forEach { p ->
                val elapsedSeconds = time * 3.5f
                val yPos = (p.ySpeed * elapsedSeconds) % (canvasHeight + p.size) - p.size
                val xOffset = Math.sin((elapsedSeconds * 2 + p.xFraction * 10).toDouble()).toFloat() * 40f
                val xPos = p.xFraction * canvasWidth + xOffset
                drawCircle(
                    color = p.color,
                    radius = p.size / 2f,
                    center = Offset(xPos, yPos)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large Sparkling Trophy Emoji
            Box(
                modifier = Modifier
                    .scale(badgeScale)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SageSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏆", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animate total savings count
            val animatedGoalAmount by animateFloatAsState(
                targetValue = goalAmount.toFloat(),
                animationSpec = tween(1800, easing = FastOutSlowInEasing),
                label = "goalAmountAnimation"
            )

            // Congratulations Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(badgeScale),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎉 Tebrikler! Hedefine ulaştın.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestDeep,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Disiplinin karşılığını aldı.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreen,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatPrice(animatedGoalAmount.toDouble()),
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        color = ForestDeep,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Tasarruf etmek istediğin '" + goalTitle + "' hedefine başarıyla ulaştın!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onNewGoal,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("goal_new_target_button")
                ) {
                    Text(
                        text = "Yeni Hedef Ekle",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = ForestDeep),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.5.dp, ForestDeep, RoundedCornerShape(24.dp))
                        .testTag("goal_dismiss_button")
                ) {
                    Text(
                        text = "Tamam",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmallVaultCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, LineDivider.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = emoji, fontSize = 13.sp)
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    color = InkSoft,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ForestDeep
            )
        }
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by rememberSaveable { mutableStateOf(0) }
    val pageCount = 4
    var dragOffsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffsetX > 150f) {
                            if (currentPage > 0) currentPage--
                        } else if (dragOffsetX < -150f) {
                            if (currentPage < pageCount - 1) {
                                currentPage++
                            } else {
                                onFinish()
                            }
                        }
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount
                    }
                )
            }
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant tiny brand mark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌱",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dur Bakalım",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ForestDeep
                )
            }

            // Skip Button
            Text(
                text = "Atla",
                color = InkSoft,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onFinish() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("onboarding_skip_button")
            )
        }

        // Page content with Crossfade transition for premium feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = currentPage,
                animationSpec = tween(durationMillis = 400),
                label = "OnboardingPageTransition"
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> OnboardingProblemPage()
                        1 -> OnboardingSolutionPage()
                        2 -> OnboardingWorksPage()
                        3 -> OnboardingFinalPage()
                    }
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until pageCount) {
                    val isSelected = i == currentPage
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "dotWidth"
                    )
                    val color = if (isSelected) ForestGreen else InkSoft.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { currentPage = i }
                            .testTag("onboarding_dot_$i")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button
            Button(
                onClick = {
                    if (currentPage < pageCount - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_cta_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (currentPage == pageCount - 1) "Hadi Başlayalım! 🚀" else "İlerle",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingProblemPage() {
    // Large elegant icon card
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(TerraSoft.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Text("🛑", fontSize = 64.sp)
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(CardWhite)
                .border(1.dp, LineDivider, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🛒", fontSize = 24.sp)
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
        text = "Anlık kararlar,\nen pahalı kararlar olabilir.",
        style = TextStyle(
            color = InkDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Dur Bakalım, satın alma anını yavaşlatır ve dürtüsel harcamaların önüne geçer.",
        style = TextStyle(
            color = InkSoft,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    )
}

@Composable
fun OnboardingSolutionPage() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing aura
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(CircleShape)
                .background(SageSoft)
        )
        // Central icon container
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(CardWhite)
                .border(1.dp, ForestGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🧘", fontSize = 38.sp)
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
        text = "Sana satın almadan önce\ndüşünme alanı verir.",
        style = TextStyle(
            color = InkDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Sadece 3 saniyelik bir duraklama ve nefes arası, daha bilinçli finansal kararlar yaratır.",
        style = TextStyle(
            color = InkSoft,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    )
}

@Composable
fun OnboardingWorksPage() {
    // Beautiful mini spending preview card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LineDivider, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AmberSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☕", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kahve & Tatlı",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerraSoft)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("⚠️ Anlık", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TerraRed)
                            }
                        }
                    }
                }
                Text(
                    text = "₺180",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkDark
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = LineDivider)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🧠 Dur bakalım! Gerçekten ihtiyacın var mı, yoksa sadece anlık bir can sıkıntısı mı? Bu harcamayı 2 gün ertelesek ne değişir?",
                fontSize = 11.5.sp,
                color = InkSoft,
                lineHeight = 16.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
        text = "Sadece bir şey yaz,\nbiz analiz edelim.",
        style = TextStyle(
            color = InkDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Dürtü analizi, planlı ve anlık farkları ile kategori bazlı detaylı psikolojik değerlendirme alırsın.",
        style = TextStyle(
            color = InkSoft,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    )
}

@Composable
fun OnboardingFinalPage() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating sparkles container
        Box(
            modifier = Modifier
                .size(140.dp)
                .rotate(rotation)
        ) {
            Text("✨", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter))
            Text("✨", fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomCenter))
            Text("✨", fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterStart))
            Text("✨", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterEnd))
        }

        // Center icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SageSoft),
            contentAlignment = Alignment.Center
        ) {
            Text("💰", fontSize = 48.sp)
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
        text = "Daha az harca.\nDaha doğru karar ver.",
        style = TextStyle(
            color = InkDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "Gereksiz alışveriş dürtülerini erteleyerek birikim hedeflerine adım adım yaklaşırsın.",
        style = TextStyle(
            color = InkSoft,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    )
}

@Composable
fun IncomeSetupScreen(viewModel: SpendingViewModel) {
    var incomeInput by remember { mutableStateOf("") }
    var carryOver by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LineDivider, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji or visual header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SageSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💰", fontSize = 32.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Aylık Gelirini Belirle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestDeep,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Aylık maaşını veya düzenli harçlığını gir. Bu tutar her yeni bütçe döneminde otomatik olarak bütçene eklenecektir.",
                        fontSize = 13.sp,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { newValue ->
                        incomeInput = newValue.filter { it.isDigit() }
                    },
                    label = { Text("Aylık Gelir (₺)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = LineDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setup_income_input")
                )

                if (incomeInput.isNotEmpty()) {
                    val dVal = incomeInput.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "Formatlı: ${formatPrice(dVal)}",
                        fontSize = 13.sp,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = LineDivider, thickness = 1.dp)

                // Checkbox row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = carryOver,
                            onCheckedChange = { carryOver = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                checkedColor = ForestGreen,
                                uncheckedColor = InkSoft
                            ),
                            modifier = Modifier.testTag("setup_carry_over_checkbox")
                        )
                        Text(
                            text = "Önceki aydan kalan eksi bakiyeyi yeni ay bütçemden otomatik düş.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark,
                            lineHeight = 16.sp
                        )
                    }
                    Text(
                        text = "Bu seçenek açıksa, önceki ay bütçeni aştıysan eksik kalan tutar yeni ay bütçenden otomatik düşülür.",
                        fontSize = 11.sp,
                        color = InkSoft,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val income = incomeInput.toDoubleOrNull() ?: 10000.0
                        viewModel.completeIncomeSetup(income, carryOver)
                    },
                    enabled = incomeInput.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        disabledContainerColor = LineDivider
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("setup_income_submit_button")
                ) {
                    Text("Devam Et", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SpendingViewModel) {
    var incomeInput by remember(viewModel.monthlyIncome) { mutableStateOf(viewModel.monthlyIncome.toInt().toString()) }
    var carryOver by remember(viewModel.carryOverNegative) { mutableStateOf(viewModel.carryOverNegative) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestDeep
            )
            IconButton(
                onClick = { viewModel.currentScreen = AppScreen.SUMMARY },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SageSoft)
                    .testTag("settings_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "Gelir ve bütçe tercihlerinizi buradan yönetebilirsiniz.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Aylık Gelir Girişi Kartı
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Aylık Gelir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestDeep
                )
                Text(
                    text = "Düzenli aylık gelirini veya harçlığını gir. Bu tutar her yeni bütçe döneminde bütçene eklenecektir.",
                    fontSize = 12.sp,
                    color = InkSoft,
                    lineHeight = 16.sp
                )
                
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        incomeInput = digits
                        val dVal = digits.toDoubleOrNull() ?: 0.0
                        viewModel.updateMonthlyIncome(dVal)
                    },
                    label = { Text("Aylık Gelir (₺)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = LineDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("settings_income_input")
                )

                if (incomeInput.isNotEmpty()) {
                    val dVal = incomeInput.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "Formatlı: ${formatPrice(dVal)}",
                        fontSize = 12.sp,
                        color = ForestGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Eksi Bakiye Devri Kartı
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LineDivider, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = carryOver,
                        onCheckedChange = { isChecked ->
                            carryOver = isChecked
                            viewModel.updateCarryOverNegative(isChecked)
                        },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = ForestGreen,
                            uncheckedColor = InkSoft
                        ),
                        modifier = Modifier.testTag("settings_carry_over_checkbox")
                    )
                    Text(
                        text = "Önceki aydan kalan eksi bakiyeyi yeni ay bütçemden otomatik düş.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark,
                        lineHeight = 18.sp
                    )
                }
                Text(
                    text = "Bu seçenek açıksa, önceki ay bütçeni aştıysan eksik kalan tutar yeni ay bütçenden otomatik düşülür.",
                    fontSize = 12.sp,
                    color = InkSoft,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 36.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.currentScreen = AppScreen.SUMMARY },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Ayarları Kaydet ve Kapat", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

