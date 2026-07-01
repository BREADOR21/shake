package com.example

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SpendingEntry
import com.example.data.SpendingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    ADD,
    SUMMARY,
    HISTORY,
    VERDICT,
    SETTINGS
}

class SpendingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpendingRepository = SpendingRepository(AppDatabase.getDatabase(application).spendingDao())
    private val prefs: SharedPreferences = application.getSharedPreferences("dur_bakalim_prefs", Context.MODE_PRIVATE)

    // Form inputs
    var amountInput by mutableStateOf("")
    var descriptionInput by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var isImpulse by mutableStateOf(false) // false = Planlıydı, true = Anlık geldi
    var selectedEmotion by mutableStateOf("Mantıklı") // "Sakin", "Mantıklı", "Kararsız", "Dürtüsel", "Aşırı İstek"

    // Savings Goal
    var savingsGoalTitle by mutableStateOf(prefs.getString("savings_goal_title", "Yeni Telefon") ?: "Yeni Telefon")
    var savingsGoalAmount by mutableStateOf(prefs.getFloat("savings_goal_amount", 15000f).toDouble())
    var savingsGoalCreatedAt by mutableStateOf(prefs.getLong("savings_goal_created_at", 0L))
    private val _savingsGoalCreatedAt = MutableStateFlow(prefs.getLong("savings_goal_created_at", 0L))

    // Countdown Timer for Impulse Break
    var impulseTimerSeconds by mutableStateOf(0)
    private var timerJob: kotlinx.coroutines.Job? = null

    fun updateSavingsGoal(title: String, amount: Double) {
        val now = System.currentTimeMillis()
        savingsGoalTitle = title
        savingsGoalAmount = amount
        savingsGoalCreatedAt = now
        _savingsGoalCreatedAt.value = now
        isGoalCelebrated = false
        prefs.edit()
            .putString("savings_goal_title", title)
            .putFloat("savings_goal_amount", amount.toFloat())
            .putLong("savings_goal_created_at", now)
            .putBoolean("is_goal_celebrated", false)
            .apply()
    }

    fun clearSavingsGoal() {
        savingsGoalTitle = ""
        savingsGoalAmount = 0.0
        savingsGoalCreatedAt = 0L
        _savingsGoalCreatedAt.value = 0L
        isGoalCelebrated = false
        prefs.edit()
            .putString("savings_goal_title", "")
            .putFloat("savings_goal_amount", 0f)
            .putLong("savings_goal_created_at", 0L)
            .putBoolean("is_goal_celebrated", false)
            .apply()
    }

    fun startImpulseTimer(seconds: Int) {
        timerJob?.cancel()
        impulseTimerSeconds = seconds
        timerJob = viewModelScope.launch {
            while (impulseTimerSeconds > 0) {
                delay(1000)
                impulseTimerSeconds--
            }
        }
    }

    fun cancelImpulseTimer() {
        timerJob?.cancel()
        impulseTimerSeconds = 0
    }

    // App state
    var hasCompletedOnboarding by mutableStateOf(prefs.getBoolean("has_completed_onboarding", false))
    var hasCompletedIncomeSetup by mutableStateOf(prefs.getBoolean("has_completed_income_setup", false))
    var currentScreen by mutableStateOf(AppScreen.ADD)

    var monthlyIncome by mutableStateOf(prefs.getFloat("monthly_income", 10000f).toDouble())
        private set

    var carryOverNegative by mutableStateOf(prefs.getBoolean("carry_over_negative", true))
        private set

    fun updateMonthlyIncome(income: Double) {
        monthlyIncome = income
        prefs.edit().putFloat("monthly_income", income.toFloat()).apply()
        updateMonthlyBudget(income)
    }

    fun updateCarryOverNegative(carryOver: Boolean) {
        carryOverNegative = carryOver
        prefs.edit().putBoolean("carry_over_negative", carryOver).apply()
    }

    fun completeIncomeSetup(income: Double, carryOver: Boolean) {
        updateMonthlyIncome(income)
        updateCarryOverNegative(carryOver)
        updateMonthlyBudget(income)
        hasCompletedIncomeSetup = true
        prefs.edit().putBoolean("has_completed_income_setup", true).apply()
        
        val currentCycleStart = getStartOfCurrentCycleTimestamp()
        prefs.edit().putLong("last_processed_cycle_start", currentCycleStart).apply()
    }

    fun completeOnboarding() {
        hasCompletedOnboarding = true
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
    }
    var isBreathingActive by mutableStateOf(false)
    var breathingText by mutableStateOf("Dur bakalım...")

    // Celebration state
    var isCelebrationActive by mutableStateOf(false)
    var isStreakResetActive by mutableStateOf(false)
    var celebrationAmount by mutableStateOf(0.0)

    // Goal Celebration state
    var isGoalCelebrationActive by mutableStateOf(false)
    var isGoalCelebrated by mutableStateOf(prefs.getBoolean("is_goal_celebrated", false))
    var showGoalDialogDirectly by mutableStateOf(false)

    // Pending spending for verdict evaluation
    var pendingSpending by mutableStateOf<SpendingEntry?>(null)

    // Streak logic
    var streakCount by mutableStateOf(prefs.getInt("streak_count", 4))
        private set

    // Monthly Budget state
    var monthlyBudget by mutableStateOf(prefs.getFloat("monthly_budget", 10000f).toDouble())
        private set

    private val _monthlyBudgetFlow = MutableStateFlow(prefs.getFloat("monthly_budget", 10000f).toDouble())

    fun updateMonthlyBudget(newBudget: Double) {
        monthlyBudget = newBudget
        _monthlyBudgetFlow.value = newBudget
        prefs.edit().putFloat("monthly_budget", newBudget.toFloat()).apply()
        if (monthlyIncome != newBudget) {
            monthlyIncome = newBudget
            prefs.edit().putFloat("monthly_income", newBudget.toFloat()).apply()
        }
    }

    init {
        // Seed initial streak if not set
        if (!prefs.contains("streak_count")) {
            prefs.edit().putInt("streak_count", 4).apply()
        }
        // Seed initial cycle day if not set
        if (!prefs.contains("cycle_start_day")) {
            val calendar = java.util.Calendar.getInstance()
            prefs.edit().putInt("cycle_start_day", calendar.get(java.util.Calendar.DAY_OF_MONTH)).apply()
        }
    }

    fun checkAndProcessRollover() {
        viewModelScope.launch {
            if (hasCompletedIncomeSetup) {
                val currentCycleStart = getStartOfCurrentCycleTimestamp()
                val lastProcessed = prefs.getLong("last_processed_cycle_start", -1L)
                if (lastProcessed != -1L && currentCycleStart > lastProcessed) {
                    try {
                        val list = repository.allSpendings.first()
                        val previousCycleSpent = list.filter { 
                            it.isAdded && it.timestamp >= lastProcessed && it.timestamp < currentCycleStart 
                        }.sumOf { it.amount }
                        
                        val overdraft = previousCycleSpent - monthlyBudget
                        var newBudget = monthlyIncome
                        if (carryOverNegative && overdraft > 0.0) {
                            newBudget -= overdraft
                        }
                        updateMonthlyBudget(newBudget)
                        prefs.edit().putLong("last_processed_cycle_start", currentCycleStart).apply()
                    } catch (e: Exception) {
                        val newBudget = monthlyIncome
                        updateMonthlyBudget(newBudget)
                        prefs.edit().putLong("last_processed_cycle_start", currentCycleStart).apply()
                    }
                } else if (lastProcessed == -1L) {
                    prefs.edit().putLong("last_processed_cycle_start", currentCycleStart).apply()
                }
            }
        }
    }

    private fun getEpochDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / (24 * 60 * 60 * 1000L)
    }

    private fun checkAndResetStreak() {
        val now = System.currentTimeMillis()
        val todayEpoch = getEpochDay(now)
        val lastCheckedEpoch = prefs.getLong("last_checked_epoch", -1)

        if (lastCheckedEpoch != -1L) {
            val diff = todayEpoch - lastCheckedEpoch
            if (diff > 1L) {
                // Streak broken! Reset to 0
                streakCount = 0
                prefs.edit().putInt("streak_count", 0).apply()
            }
        }
    }

    private fun updateStreakOnAction() {
        val now = System.currentTimeMillis()
        val todayEpoch = getEpochDay(now)
        val lastCheckedEpoch = prefs.getLong("last_checked_epoch", -1)

        if (lastCheckedEpoch == -1L) {
            streakCount = 1
        } else {
            val diff = todayEpoch - lastCheckedEpoch
            if (diff == 1L) {
                streakCount += 1
            } else if (diff > 1L) {
                streakCount = 1
            }
            // If diff == 0L, streak remains unchanged (already checked today)
        }

        prefs.edit()
            .putInt("streak_count", streakCount)
            .putLong("last_checked_epoch", todayEpoch)
            .apply()
    }

    // Expose all items from Room Database
    val allSpendings: StateFlow<List<SpendingEntry>> = repository.allSpendings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val wishlistItems: StateFlow<List<SpendingEntry>> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            list.filter { it.isAdded && it.stillWantState == "PENDING" && (it.isImpulse || it.emotion == "Dürtüsel" || it.emotion == "Aşırı İstek" || it.verdict == "red" || it.verdict == "yellow") }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateWishlistItemState(id: Int, stillWant: Boolean) {
        viewModelScope.launch {
            val state = if (stillWant) "YES" else "NO"
            val list = allSpendings.value
            val item = list.find { it.id == id }
            if (item != null) {
                val updated = item.copy(stillWantState = state)
                repository.insert(updated)
            }
        }
    }

    fun getRegretPercentageForEmotion(emotion: String): Int {
        val list = allSpendings.value
        // Match both plain emotion string and emoji-prefixed version if stored differently
        val emotionSpendings = list.filter { (it.emotion == emotion || it.emotion.endsWith(emotion)) && it.isAdded }
        if (emotionSpendings.isEmpty()) {
            return when {
                emotion.contains("Sakin") -> 5
                emotion.contains("Mantıklı") -> 2
                emotion.contains("Kararsız") -> 35
                emotion.contains("Dürtüsel") || emotion.contains("Dürtü") -> 70
                emotion.contains("Aşırı İstek") || emotion.contains("İstek") -> 85
                else -> 40
            }
        }
        val regretted = emotionSpendings.filter { it.stillWantState == "NO" }.size
        return ((regretted.toDouble() / emotionSpendings.size) * 100).toInt()
    }

    fun getStartOfCurrentCycleTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val cycleStartDay = prefs.getInt("cycle_start_day", currentDay)
        
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        if (currentDay >= cycleStartDay) {
            calendar.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay)
        } else {
            calendar.add(java.util.Calendar.MONTH, -1)
            val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay.coerceAtMost(maxDays))
        }
        return calendar.timeInMillis
    }

    fun getCycleRangeString(): String {
        val calendar = java.util.Calendar.getInstance()
        val cycleStartDay = prefs.getInt("cycle_start_day", calendar.get(java.util.Calendar.DAY_OF_MONTH))
        
        val startCal = java.util.Calendar.getInstance()
        val currentDay = startCal.get(java.util.Calendar.DAY_OF_MONTH)
        if (currentDay >= cycleStartDay) {
            startCal.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay)
        } else {
            startCal.add(java.util.Calendar.MONTH, -1)
            val maxDays = startCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            startCal.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay.coerceAtMost(maxDays))
        }
        
        val endCal = java.util.Calendar.getInstance()
        if (currentDay >= cycleStartDay) {
            endCal.add(java.util.Calendar.MONTH, 1)
            val maxDays = endCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            endCal.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay.coerceAtMost(maxDays))
        } else {
            val maxDays = endCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            endCal.set(java.util.Calendar.DAY_OF_MONTH, cycleStartDay.coerceAtMost(maxDays))
        }
        
        val sdf = java.text.SimpleDateFormat("d MMMM", java.util.Locale("tr"))
        return "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"
    }

    private fun getStartOfTodayTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Spendings in the current dynamic billing cycle
    val totalSpentThisMonth: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val startOfCycle = getStartOfCurrentCycleTimestamp()
            list.filter { it.isAdded && it.timestamp >= startOfCycle }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBudget: StateFlow<Double> = totalSpentThisMonth
        .combine(_monthlyBudgetFlow) { spent, budget ->
            budget - spent
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    val dailyAverageLimit: StateFlow<Double> = remainingBudget
        .combine(MutableStateFlow(System.currentTimeMillis())) { remaining, _ ->
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val remainingDays = (maxDays - currentDay + 1).coerceAtLeast(1)
            if (remaining > 0.0) remaining / remainingDays else 0.0
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendingTempo: StateFlow<String> = totalSpentThisMonth
        .combine(_monthlyBudgetFlow) { spent, budget ->
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val fraction = currentDay.toDouble() / maxDays.toDouble()
            val targetLinear = budget * fraction
            if (targetLinear <= 0.0) return@combine "Dengeli"
            val ratio = spent / targetLinear
            when {
                ratio <= 0.9 -> "Yavaş"
                ratio <= 1.1 -> "Dengeli"
                else -> "Hızlı ⚡"
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dengeli")

    val budgetStatusLevel: StateFlow<String> = totalSpentThisMonth
        .combine(_monthlyBudgetFlow) { spent, budget ->
            if (budget <= 0.0) return@combine "red"
            val ratio = spent / budget
            when {
                ratio <= 0.6 -> "green"
                ratio <= 0.9 -> "yellow"
                else -> "red"
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "green")

    // Daily saved money (avoided spendings made today)
    val dailyAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val startOfToday = getStartOfTodayTimestamp()
            list.filter { !it.isAdded && it.timestamp >= startOfToday }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Weekly saved money (avoided spendings made in last 7 days)
    val weeklyAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            list.filter { !it.isAdded && it.timestamp >= sevenDaysAgo }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Monthly saved money (avoided spendings made in current dynamic billing cycle)
    val monthlyAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val startOfCycle = getStartOfCurrentCycleTimestamp()
            list.filter { !it.isAdded && it.timestamp >= startOfCycle }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Yearly saved money (avoided spendings made in current calendar year)
    val yearlyAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val startOfYear = getStartOfYearTimestamp()
            list.filter { !it.isAdded && it.timestamp >= startOfYear }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // All-time saved money
    val allTimeAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            list.filter { !it.isAdded }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Savings for the active goal only (timestamp >= savingsGoalCreatedAt)
    val activeGoalAvoided: StateFlow<Double> = allSpendings
        .combine(_savingsGoalCreatedAt) { list, createdAt ->
            list.filter { !it.isAdded && it.timestamp >= createdAt }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun getStartOfYearTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Derived states (Weekly)
    val totalSpent: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            // Spendings (isAdded == true) this week (last 7 days)
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            list.filter { it.isAdded && it.timestamp >= sevenDaysAgo }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailySpent: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val startOfToday = getStartOfTodayTimestamp()
            list.filter { it.isAdded && it.timestamp >= startOfToday }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allTimeSpent: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            list.filter { it.isAdded }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalAvoided: StateFlow<Double> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            // Avoided spendings (isAdded == false)
            list.filter { !it.isAdded }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category distribution for this week's spent money
    val categorySpentDistribution: StateFlow<Map<String, Double>> = allSpendings
        .combine(MutableStateFlow(System.currentTimeMillis())) { list, _ ->
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            list.filter { it.isAdded && it.timestamp >= sevenDaysAgo }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun selectCategory(category: String) {
        selectedCategory = category
    }

    fun setPlanningType(impulse: Boolean) {
        isImpulse = impulse
    }

    // Trigger evaluation with breathing timer
    fun checkSpending() {
        val amount = amountInput.toDoubleOrNull() ?: return
        if (amount <= 0.0 || selectedCategory.isEmpty()) return

        viewModelScope.launch {
            // Trigger 2-3 seconds full screen breathing animation
            isBreathingActive = true
            breathingText = "Dur bakalım..."
            delay(1200)
            breathingText = "Bir saniye düşün."
            delay(1200)
            isBreathingActive = false

            // Perform evaluation based on inputs
            val verdict = evaluate(amount, selectedCategory, isImpulse, selectedEmotion)
            val regretProb = calculateRegretProbability(isImpulse, selectedEmotion)
            pendingSpending = SpendingEntry(
                amount = amount,
                description = descriptionInput.trim(),
                category = selectedCategory,
                isImpulse = isImpulse,
                verdict = verdict,
                emotion = selectedEmotion,
                regretProbability = regretProb,
                stillWantState = "PENDING"
            )
            currentScreen = AppScreen.VERDICT
        }
    }

    fun calculateRegretProbability(isImpulse: Boolean, emotion: String): String {
        return when {
            emotion == "Aşırı İstek" || emotion == "Dürtüsel" -> "high"
            isImpulse && emotion == "Kararsız" -> "medium"
            emotion == "Kararsız" -> "medium"
            else -> "low"
        }
    }

    // Decision Engine Logic (equivalent to offline rule-based risk evaluation)
    private fun evaluate(amount: Double, category: String, isImpulse: Boolean, emotion: String): String {
        // Dynamic Risk Engine (V14)
        val incomeRef = if (monthlyIncome > 0.0) monthlyIncome else 10000.0
        val ratioOfIncome = amount / incomeRef

        var riskScore = 30 // Base risk score

        // 1. Ratio of expense to monthly income/budget reference
        val ratioPoints = when {
            ratioOfIncome <= 0.01 -> 5
            ratioOfIncome <= 0.05 -> 15
            ratioOfIncome <= 0.15 -> 30
            ratioOfIncome <= 0.30 -> 50
            else -> 75
        }
        riskScore += ratioPoints

        // 2. Night time check (Gece 22:00 - 06:00) -> Minor risk modifier
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 6) {
            riskScore += 5
        }

        // 3. Category check
        val catRisk = when (category) {
            "Market" -> -15
            "Eğitim" -> -20
            "Yemek" -> -5
            "Giyim" -> +5
            "Teknoloji" -> +15
            "Oyun" -> +15
            "Dijital Abonelikler" -> +10
            else -> 0 // Diğer
        }
        riskScore += catRisk

        // 4. Planlı vs Anlık check: Lower risk for planned actions, raise risk for impulsive actions
        if (isImpulse) {
            riskScore += 20
        } else {
            riskScore -= 20
        }

        // 5. Emotion check modifier (V7)
        val emotionModifier = when (emotion) {
            "Sakin" -> -15
            "Mantıklı" -> -15
            "Kararsız" -> +5
            "Dürtüsel" -> +25
            "Aşırı İstek" -> +30
            else -> 0
        }
        riskScore += emotionModifier

        // 6. Remaining Budget and Eksi Bakiye (Negative Budget) Modu Modifier
        val remaining = remainingBudget.value
        if (remaining < 0.0) {
            // Eksi Bakiye Modu: Make green (<= 35) very difficult to reach
            riskScore += 35
            // If the overdraft is more than 10% of monthly income, make it even more protective
            if (remaining < -0.1 * incomeRef) {
                riskScore += 15
            }
        } else {
            // Not in negative, but low budget checks
            if (remaining < amount) {
                riskScore += 20
            } else if (remaining < 0.15 * incomeRef) {
                riskScore += 10
            }
        }

        // Clamp final score to 0..100 range
        val finalScore = riskScore.coerceIn(0, 100)

        // Thresholds: green (onaylanabilir) if <= 35, yellow if <= 70, red otherwise
        return when {
            finalScore <= 35 -> "green"
            finalScore <= 70 -> "yellow"
            else -> "red"
        }
    }

    // "Yine de ekle" action - Adds transaction to local db
    fun addSpending() {
        val pending = pendingSpending ?: return
        val isRedVerdict = pending.verdict == "red"
        
        if (isRedVerdict) {
            // First show the streak broken warning overlay! Do not save or reset form yet.
            isStreakResetActive = true
            return
        }

        viewModelScope.launch {
            repository.insert(pending.copy(isAdded = true, timestamp = System.currentTimeMillis()))
            updateStreakOnAction()
            resetForm()
            pendingSpending = null
            currentScreen = AppScreen.SUMMARY
        }
    }

    // Called when the user clicks 'Tamam' or 'Devam Et' on the streak loss warning overlay
    fun confirmAddSpendingAndResetStreak() {
        val pending = pendingSpending ?: return
        viewModelScope.launch {
            repository.insert(pending.copy(isAdded = true, timestamp = System.currentTimeMillis()))
            
            // Streak broken! Reset to 0
            streakCount = 0
            prefs.edit().putInt("streak_count", 0).apply()
            
            resetForm()
            pendingSpending = null
            isStreakResetActive = false
            currentScreen = AppScreen.SUMMARY
        }
    }

    // "Vazgeç" action - Increments streak and saves as avoided transaction
    fun skipSpending() {
        val pending = pendingSpending ?: return
        viewModelScope.launch {
            // Store as avoided spending to calculate total avoided money
            repository.insert(pending.copy(isAdded = false, timestamp = System.currentTimeMillis()))
            updateStreakOnAction()
            
            // Set up celebration state
            celebrationAmount = pending.amount
            
            val currentAvoided = activeGoalAvoided.value + pending.amount
            if (currentAvoided >= savingsGoalAmount && !isGoalCelebrated) {
                isGoalCelebrated = true
                prefs.edit().putBoolean("is_goal_celebrated", true).apply()
                isGoalCelebrationActive = true
            } else {
                isCelebrationActive = true
            }

            resetForm()
            pendingSpending = null
            currentScreen = AppScreen.ADD

            // Auto dismiss after 4 seconds
            delay(4000)
            if (isCelebrationActive) {
                isCelebrationActive = false
            }
        }
    }

    fun dismissCelebration() {
        isCelebrationActive = false
    }

    fun dismissStreakReset() {
        isStreakResetActive = false
    }

    fun resetForm() {
        amountInput = ""
        descriptionInput = ""
        selectedCategory = ""
        isImpulse = false
        selectedEmotion = "Mantıklı"
        cancelImpulseTimer()
    }

    init {
        checkAndResetStreak()
        checkAndProcessRollover()

        // Automatically trigger goal celebration if progress reaches 100% or more
        viewModelScope.launch {
            activeGoalAvoided.collect { avoided ->
                if (savingsGoalAmount > 0.0 && avoided >= savingsGoalAmount && !isGoalCelebrated) {
                    isGoalCelebrated = true
                    prefs.edit().putBoolean("is_goal_celebrated", true).apply()
                    isGoalCelebrationActive = true
                }
            }
        }
    }
}
