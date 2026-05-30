package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.FoodAnalysisResult
import com.example.api.GeminiApiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MacrofyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "macrofy_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = MacrofyRepository(db.dao())

    private val prefs = application.getSharedPreferences("macrofy_prefs", android.content.Context.MODE_PRIVATE)

    // Auth & Calculation states
    val loggedInUser = MutableStateFlow<String?>(null)
    val latestCalculationExplanation = MutableStateFlow<String>("")
    val isCalculatingExplanation = MutableStateFlow(false)
    val latestCalculatedProfile = MutableStateFlow<UserProfile?>(null)

    // Google Fit/Health Simulated tracking states
    val todaySteps = MutableStateFlow(4580)
    val todayStepsGoal = MutableStateFlow(10000)
    val todayHeartPoints = MutableStateFlow(22)
    val todayHeartPointsGoal = MutableStateFlow(50)
    val activeTrackerMode = MutableStateFlow("apple")

    fun addSteps(amount: Int) {
        val current = todaySteps.value
        todaySteps.value = (current + amount).coerceAtLeast(0)
        prefs.edit().putInt("fit_steps_${loggedInUser.value ?: "LokalUser"}", todaySteps.value).apply()
    }

    fun addHeartPoints(amount: Int) {
        val current = todayHeartPoints.value
        todayHeartPoints.value = (current + amount).coerceAtLeast(0)
        prefs.edit().putInt("fit_hp_${loggedInUser.value ?: "LokalUser"}", todayHeartPoints.value).apply()
    }

    fun resetFitData() {
        todaySteps.value = 0
        todayHeartPoints.value = 0
        val u = loggedInUser.value ?: "LokalUser"
        prefs.edit().putInt("fit_steps_$u", 0).putInt("fit_hp_$u", 0).apply()
    }

    fun setTrackerMode(mode: String) {
        activeTrackerMode.value = mode
        prefs.edit().putString("active_tracker_mode_${loggedInUser.value ?: "LokalUser"}", mode).apply()
    }

    // App state
    val userProfile = repository.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _startOfDay = MutableStateFlow(getStartOfDayTimestamp())
    private val _endOfDay = MutableStateFlow(getEndOfDayTimestamp())

    val todayFoodEntries = combine(_startOfDay, _endOfDay) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        repository.getFoodEntriesForDayFlow(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWaterLogs = combine(_startOfDay, _endOfDay) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        repository.getWaterLogsForDayFlow(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs = repository.getWeightLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodEntries = repository.getAllFoodEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakCount = combine(allFoodEntries, weightLogs) { foods, weights ->
        calculateStreak(foods, weights)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val chatMessages = repository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedHistory = repository.getAllScannedHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Onboarding UI state
    val onboardingName = MutableStateFlow("Fit-Champ")
    val onboardingGender = MutableStateFlow("männlich")
    val onboardingAge = MutableStateFlow("")
    val onboardingHeight = MutableStateFlow("")
    val onboardingWeight = MutableStateFlow("")
    val onboardingGoal = MutableStateFlow("Abnehmen")
    val onboardingActivity = MutableStateFlow("leicht aktiv")
    val onboardingPace = MutableStateFlow("Normal")

    // AI scan state
    val scanBitmap = MutableStateFlow<Bitmap?>(null)
    val isScanning = MutableStateFlow(false)
    val scanResult = MutableStateFlow<FoodAnalysisResult?>(null)

    // AI Coach Chat input state
    val chatInput = MutableStateFlow("")
    val isCoachResponding = MutableStateFlow(false)

    init {
        // Load active user on start or default to LokalUser to bypass signup
        val savedUser = prefs.getString("active_user", "LokalUser") ?: "LokalUser"
        prefs.edit().putString("active_user", savedUser).apply()
        loggedInUser.value = savedUser
        restoreProfileToRoom(savedUser)

        // Load saved Google Fit tracking values
        todaySteps.value = prefs.getInt("fit_steps_$savedUser", 4580)
        todayHeartPoints.value = prefs.getInt("fit_hp_$savedUser", 22)
        activeTrackerMode.value = prefs.getString("active_tracker_mode_$savedUser", "apple") ?: "apple"

        // Pre-seed chat message if empty
        viewModelScope.launch {
            repository.getChatMessagesFlow().first().let { list ->
                if (list.isEmpty()) {
                    repository.insertChatMessage(
                        ChatMessage(
                            timestamp = System.currentTimeMillis() - 10000,
                            sender = "assistant",
                            text = "Hallo! Ich bin dein Macrofy AI Coach. 🚀 Was hast du heute Leckeres gegessen oder wie kann ich dich bei deinem Fitness-Ziel unterstützen?"
                        )
                    )
                }
            }
        }
    }

    fun restoreProfileToRoom(username: String) {
        viewModelScope.launch {
            val gender = prefs.getString("profile_gender_$username", null)
            if (gender != null) {
                val age = prefs.getInt("profile_age_$username", 25)
                val height = prefs.getFloat("profile_height_$username", 175f)
                val weight = prefs.getFloat("profile_weight_$username", 75f)
                val goal = prefs.getString("profile_goal_$username", "Abnehmen") ?: "Abnehmen"
                val activity = prefs.getString("profile_activity_$username", "leicht aktiv") ?: "leicht aktiv"
                val pace = prefs.getString("profile_pace_$username", "Normal") ?: "Normal"
                val calories = prefs.getInt("profile_calories_$username", 2000)
                val protein = prefs.getFloat("profile_protein_$username", 150f)
                val carbs = prefs.getFloat("profile_carbs_$username", 200f)
                val fat = prefs.getFloat("profile_fat_$username", 70f)
                val bmi = prefs.getFloat("profile_bmi_$username", 24f)
                val bmr = prefs.getFloat("profile_bmr_$username", 1600f)
                val tdee = prefs.getFloat("profile_tdee_$username", 2200f)

                val restoredProfile = UserProfile(
                    id = 1,
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight,
                    goal = goal,
                    activityLevel = activity,
                    pace = pace,
                    calorieTarget = calories,
                    proteinTarget = protein,
                    carbTarget = carbs,
                    fatTarget = fat,
                    bmi = bmi,
                    bmr = bmr,
                    tdee = tdee,
                    completedOnboarding = true
                )
                repository.saveUserProfile(restoredProfile)
                
                // Set onboarding form fields to restored values
                onboardingName.value = username
                onboardingGender.value = gender
                onboardingAge.value = age.toString()
                onboardingHeight.value = height.toInt().toString()
                onboardingWeight.value = weight.toInt().toString()
                onboardingGoal.value = goal
                onboardingActivity.value = activity
                onboardingPace.value = pace

                // Restore Google Fit metrics
                todaySteps.value = prefs.getInt("fit_steps_$username", 4580)
                todayHeartPoints.value = prefs.getInt("fit_hp_$username", 22)
            }
        }
    }

    fun signUp(username: String, email: String, password: String): Pair<Boolean, String> {
        val u = username.trim()
        val p = password.trim()
        val e = email.trim()

        if (u.length < 3) return Pair(false, "Benutzername muss mindestens 3 Zeichen lang sein.")
        if (p.length < 4) return Pair(false, "Passwort muss mindestens 4 Zeichen lang sein.")
        if (!e.contains("@")) return Pair(false, "Bitte gib eine gültige E-Mail-Adresse ein.")

        val exists = prefs.getString("user_password_$u", null)
        if (exists != null) return Pair(false, "Dieser Benutzername ist bereits vergeben.")

        // Save credentials
        prefs.edit()
            .putString("user_password_$u", p)
            .putString("user_email_$u", e)
            .putString("active_user", u)
            .apply()

        loggedInUser.value = u

        // Clear Room user profile for fresh onboarding questions
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfile(
                    id = 1,
                    gender = "männlich",
                    age = 25,
                    height = 175f,
                    weight = 75f,
                    goal = "Abnehmen",
                    activityLevel = "leicht aktiv",
                    pace = "Normal",
                    calorieTarget = 2000,
                    proteinTarget = 150f,
                    carbTarget = 200f,
                    fatTarget = 70f,
                    bmi = 24.5f,
                    bmr = 1600f,
                    tdee = 2200f,
                    completedOnboarding = false
                )
            )
            // Empty questions
            onboardingAge.value = ""
            onboardingHeight.value = ""
            onboardingWeight.value = ""
            onboardingGoal.value = "Abnehmen"
            onboardingActivity.value = "leicht aktiv"
            onboardingPace.value = "Normal"
        }

        return Pair(true, "Registrierung erfolgreich!")
    }

    fun logIn(username: String, password: String): Pair<Boolean, String> {
        val u = username.trim()
        val p = password.trim()

        val savedPass = prefs.getString("user_password_$u", null)
        if (savedPass == null || savedPass != p) {
            return Pair(false, "Falscher Benutzername oder Passwort.")
        }

        prefs.edit().putString("active_user", u).apply()
        loggedInUser.value = u

        // Restore user profile and check if onboarding is complete
        restoreProfileToRoom(u)

        return Pair(true, "Anmeldung erfolgreich!")
    }

    fun logOut() {
        prefs.edit().remove("active_user").apply()
        loggedInUser.value = null
        viewModelScope.launch {
            // Write a blank profile
            repository.saveUserProfile(
                UserProfile(
                    id = 1,
                    gender = "männlich",
                    age = 25,
                    height = 175f,
                    weight = 75f,
                    goal = "Abnehmen",
                    activityLevel = "leicht aktiv",
                    pace = "Normal",
                    calorieTarget = 2000,
                    proteinTarget = 150f,
                    carbTarget = 200f,
                    fatTarget = 70f,
                    bmi = 24.5f,
                    bmr = 1600f,
                    tdee = 2200f,
                    completedOnboarding = false
                )
            )
        }
    }

    fun setScanImage(bitmap: Bitmap?) {
        scanBitmap.value = bitmap
        scanResult.value = null
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val filename = "scanned_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(getApplication<Application>().filesDir, filename)
            val fos = java.io.FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteScannedHistoryItem(scanned: ScannedFood) {
        viewModelScope.launch {
            scanned.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.deleteScannedFood(scanned)
        }
    }

    fun clearScannedHistory() {
        viewModelScope.launch {
            repository.clearScannedHistory()
            try {
                val dir = getApplication<Application>().filesDir
                dir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("scanned_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startFoodScan(customPrompt: String = "Analyze this food") {
        viewModelScope.launch {
            isScanning.value = true
            try {
                val result = GeminiApiClient.scanFoodImage(scanBitmap.value, customPrompt)
                scanResult.value = result
                
                // Save to scanned history database with photo tracking
                val savedImagePath = saveBitmapToInternalStorage(scanBitmap.value)
                val scannedFood = ScannedFood(
                    name = result.name,
                    timestamp = System.currentTimeMillis(),
                    calories = result.calories,
                    protein = result.protein,
                    carbs = result.carbs,
                    fat = result.fat,
                    sugar = result.sugar,
                    portionGrams = result.portionGrams,
                    confidenceScore = result.confidenceScore,
                    isPackaging = result.isPackaging,
                    imagePath = savedImagePath
                )
                repository.insertScannedFood(scannedFood)
            } catch (e: Exception) {
                // Sinks gracefully into simulated fallback internally
            } finally {
                isScanning.value = false
            }
        }
    }

    fun addScannedFoodToDiary(mealType: String, customizedPortion: Float? = null) {
        val result = scanResult.value ?: return
        viewModelScope.launch {
            val portionFactor = if (customizedPortion != null) customizedPortion / result.portionGrams else 1f
            val entry = FoodEntry(
                name = result.name,
                timestamp = System.currentTimeMillis(),
                mealType = mealType,
                calories = (result.calories * portionFactor).toInt(),
                protein = result.protein * portionFactor,
                carbs = result.carbs * portionFactor,
                fat = result.fat * portionFactor,
                sugar = result.sugar * portionFactor,
                portionGrams = customizedPortion ?: result.portionGrams,
                confidenceScore = result.confidenceScore,
                isPackaging = result.isPackaging
            )
            repository.insertFoodEntry(entry)
            // Reset scan states after adding
            scanBitmap.value = null
            scanResult.value = null
        }
    }

    fun addManualFood(
        name: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        sugar: Float,
        mealType: String,
        portionGrams: Float
    ) {
        viewModelScope.launch {
            val entry = FoodEntry(
                name = name,
                timestamp = System.currentTimeMillis(),
                mealType = mealType,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                sugar = sugar,
                portionGrams = portionGrams,
                confidenceScore = 100
            )
            repository.insertFoodEntry(entry)
        }
    }

    fun deleteFood(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entry)
        }
    }

    fun addWaterLog(ml: Int) {
        viewModelScope.launch {
            repository.insertWaterLog(
                WaterLog(
                    timestamp = System.currentTimeMillis(),
                    amountMl = ml
                )
            )
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            repository.removeLastWaterLog(_startOfDay.value, _endOfDay.value)
        }
    }

    fun recordWeight(kg: Float) {
        viewModelScope.launch {
            repository.insertWeightLog(
                WeightLog(
                    timestamp = System.currentTimeMillis(),
                    weight = kg
                )
            )
            // If user profile is present, also update profile weight
            val profile = repository.getUserProfile()
            if (profile != null) {
                val updatedProfile = calculateMacros(
                    gender = profile.gender,
                    age = profile.age,
                    height = profile.height,
                    weight = kg,
                    goal = profile.goal,
                    activityLevel = profile.activityLevel,
                    pace = profile.pace
                )
                repository.saveUserProfile(updatedProfile)
            }
        }
    }

    fun sendChatMessage() {
        val text = chatInput.value.trim()
        if (text.isEmpty()) return
        chatInput.value = ""

        viewModelScope.launch {
            val userMsg = ChatMessage(timestamp = System.currentTimeMillis(), sender = "user", text = text)
            repository.insertChatMessage(userMsg)

            isCoachResponding.value = true
            try {
                val history = repository.getChatMessagesFlow().first()
                val responseText = GeminiApiClient.getCoachResponse(history, text)
                repository.insertChatMessage(
                    ChatMessage(
                        timestamp = System.currentTimeMillis(),
                        sender = "assistant",
                        text = responseText
                    )
                )
            } catch (e: Exception) {
                // handled by API fallback
            } finally {
                isCoachResponding.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.insertChatMessage(
                ChatMessage(
                    timestamp = System.currentTimeMillis(),
                    sender = "assistant",
                    text = "Chatverlauf gelöscht. Wie kann ich dir heute mit deiner Ernährung helfen? 🍎"
                )
            )
        }
    }

    fun getFallbackCalculationExplanation(p: UserProfile): String {
        val bmrVal = p.bmr.toInt()
        val tdeeVal = p.tdee.toInt()
        val calTarget = p.calorieTarget
        val protVal = p.proteinTarget.toInt()
        val carbVal = p.carbTarget.toInt()
        val fatVal = p.fatTarget.toInt()
        val bmiVal = String.format(java.util.Locale.US, "%.1f", p.bmi)
        
        val goalIntro = when (p.goal) {
            "Abnehmen" -> "📉 **Dein maßgeschneiderter Fettverbrennungs-Plan**\n\n" +
                    "Um gesund und nachhaltig Körperfett abzubauen bei gleichzeitigem Erhalt deiner Muskelmasse, haben wir ein energetisches Defizit berechnet. Dein tägliches Gesamt-Energiebudget (TDEE) liegt bei **$tdeeVal kcal**. Mit einem moderaten Defizit von circa 500 kcal pro Tag ergibt sich dein Ziel von **$calTarget kcal** pro Tag."
            "Muskelaufbau" -> "💪 **Dein hocheffektiver Muskelaufbau-Plan**\n\n" +
                    "Für einen maximalen Muskelaufbau mit minimalem Fettaufbau benötigt dein Körper Baustoff und einen leichten Energiekalorienüberschuss. Dein berechneter Tagesbedarf (TDEE) beträgt **$tdeeVal kcal**. Mit einem sportwissenschaftlich idealen Überschuss von 300 kcal setzen wir dein tägliches Ziel auf **$calTarget kcal**."
            else -> "⚖️ **Dein optimaler Gewichthalte- und Recompositions-Plan**\n\n" +
                    "Um dein aktuelles Körpergewicht zu stabilisieren und deine Körperkomposition (Verhältnis von Muskeln zu Fett) kontinuierlich zu verbessern, entspricht deine Zufuhr exakt deinem täglichen Gesamtenergiebedarf (TDEE) von **$calTarget kcal**."
        }

        val bmiEvaluation = when {
            p.bmi < 18.5f -> "Untergewicht ($bmiVal)"
            p.bmi < 25f -> "Normalgewicht / Optimaler Bereich ($bmiVal)"
            p.bmi < 30f -> "Leichtes Übergewicht ($bmiVal)"
            else -> "Übergewicht ($bmiVal)"
        }

        return "$goalIntro\n\n" +
                "✨ **Metabolische Analyse & Koeffizienten**\n" +
                "* 🛌 **Grundumsatz (BMR):** **$bmrVal kcal** – Das verbrennt dein Körper rein für lebenserhaltende Funktionen im Ruhezustand.\n" +
                "* 🏃 **Leistungsumsatz (TDEE):** **$tdeeVal kcal** – Inklusive deines Aktivitätslevels (*${p.activityLevel}*).\n" +
                "* 📊 **Body-Mass-Index (BMI):** **$bmiVal** (*$bmiEvaluation*).\n\n" +
                "💡 **Verteilung der Makronährstoffe (Warum diese Aufteilung?)**\n" +
                "* 🐣 **Protein ($protVal g):** Gesetzte Zufuhr von ${if (p.goal == "Abnehmen") "2.2g" else if (p.goal == "Muskelaufbau") "2.0g" else "1.8g"} pro kg Körpergewicht. Dies schützt deine beanspruchte Muskelatur im Defizit, maximiert die Muskelproteinsynthese bei Trainingsreizen und hemmt Heißhungerattacken dank extrem hoher Sättigungswirkung.\n" +
                "  * *Protein verbrennt sich selbst:* Ca. 20-30% des Nährwertes geht durch die Verdauung als Wärme verloren (Thermic Effect of Food)!\n" +
                "* 🥑 **Gesunde Fette ($fatVal g):** Berechnet mit 1.0g pro kg Körpergewicht. Fette sind essenziell für dein endokrines System (Testosteron, Östrogen), regeln die Aufnahme fettlöslicher Vitamine (A, D, E, K) und schützen Gelenke sowie Zellwände.\n" +
                "* 🍙 **Kohlenhydrate ($carbVal g):** Das verbleibende Kalorien-Budget wird ideal mit Carbs gefüllt. Sie dienen deinen Muskeln und deinem ZNS (Zentralnervensystem) als hocheffizienter, sofort abrufbarer Glykogen-Energielieferant für maximale Trainingsleistung.\n\n" +
                "🚀 **Macrofy AI Empfehlung für dein Ziel *${p.goal}***:\n" +
                "1. **Konsistenz:** Versuche, dein Kalorienziel von **$calTarget kcal** mit einer Abweichung von max. +/- 100 kcal täglich zu treffen.\n" +
                "2. **Proteine priorisieren:** Triff deinen Eiweißwert täglich. Es ist das Fundament deiner körperlichen Transformation.\n" +
                "3. **Hydration:** Trinke mindestens 2.5 bis 3 Liter Wasser, um Nährstoffe effizient in die Zellen zu transportieren."
    }

    fun completeOnboarding() {
        val gender = onboardingGender.value
        val age = onboardingAge.value.toIntOrNull() ?: 25
        val height = onboardingHeight.value.toFloatOrNull() ?: 175f
        val weight = onboardingWeight.value.toFloatOrNull() ?: 75f
        val goal = onboardingGoal.value
        val activity = onboardingActivity.value
        val pace = onboardingPace.value

        viewModelScope.launch {
            val profile = calculateMacros(gender, age, height, weight, goal, activity, pace)
            repository.saveUserProfile(profile)
            latestCalculatedProfile.value = profile

            // Backup profile to SharedPreferences for this active user
            val u = loggedInUser.value
            if (u != null) {
                prefs.edit()
                    .putBoolean("profile_completed_$u", true)
                    .putString("profile_gender_$u", profile.gender)
                    .putInt("profile_age_$u", profile.age)
                    .putFloat("profile_height_$u", profile.height)
                    .putFloat("profile_weight_$u", profile.weight)
                    .putString("profile_goal_$u", profile.goal)
                    .putString("profile_activity_$u", profile.activityLevel)
                    .putString("profile_pace_$u", profile.pace)
                    .putInt("profile_calories_$u", profile.calorieTarget)
                    .putFloat("profile_protein_$u", profile.proteinTarget)
                    .putFloat("profile_carbs_$u", profile.carbTarget)
                    .putFloat("profile_fat_$u", profile.fatTarget)
                    .putFloat("profile_bmi_$u", profile.bmi)
                    .putFloat("profile_bmr_$u", profile.bmr)
                    .putFloat("profile_tdee_$u", profile.tdee)
                    .apply()
            }

            // Seed initial analytics-friendly database demo data if food diaries are empty
            val foodList = repository.getAllFoodEntriesFlow().first()
            if (foodList.isEmpty()) {
                seedDemoData(weight)
            }

            // Generate AI explanation of calculations
            isCalculatingExplanation.value = true
            latestCalculationExplanation.value = ""
            viewModelScope.launch {
                try {
                    val prompt = "Erkläre dem Nutzer diese exakte sportwissenschaftliche Berechnung detailliert auf Deutsch. " +
                            "Biologisches Geschlecht: ${profile.gender}, Alter: ${profile.age} Jahre, " +
                            "Größe: ${profile.height} cm, Gewicht: ${profile.weight} kg, " +
                            "Ziel: ${profile.goal}, Aktivitätslevel: ${profile.activityLevel}, " +
                            "Tempo: ${profile.pace}.\n" +
                            "Berechnete Werte:\n" +
                            "- BMR (Grundumsatz): ${profile.bmr.toInt()} kcal\n" +
                            "- TDEE (Gesamtenergiebedarf): ${profile.tdee.toInt()} kcal\n" +
                            "- Kalorienziel: ${profile.calorieTarget} kcal\n" +
                            "- Protein-Bedarf: ${profile.proteinTarget.toInt()}g\n" +
                            "- Kohlenhydrat-Bedarf: ${profile.carbTarget.toInt()}g\n" +
                            "- Fett-Bedarf: ${profile.fatTarget.toInt()}g\n" +
                            "- BMI: ${String.format(java.util.Locale.US, "%.1f", profile.bmi)}\n\n" +
                            "Begründe das Defizit/Überschuss basierend auf dem Ziel und Tempo. " +
                            "Erkläre kurz den hohen Proteinbedarf (${profile.proteinTarget.toInt()}g) für optimalen Muskelschutz und Sättigung, " +
                            "gesunde Fette (${profile.fatTarget.toInt()}g) für Hormone und verbleibende Carbs als Leistungsquelle. " +
                            "Schreibe motivierend im Stil eines Elite-Coaches mit schönen Absätzen und übersichtlichen Bulletpoints."
                    
                    val aiText = GeminiApiClient.getCoachResponse(emptyList(), prompt)
                    if (aiText.isNotEmpty()) {
                        latestCalculationExplanation.value = aiText
                    } else {
                        latestCalculationExplanation.value = getFallbackCalculationExplanation(profile)
                    }
                } catch (e: Exception) {
                    latestCalculationExplanation.value = getFallbackCalculationExplanation(profile)
                } finally {
                    isCalculatingExplanation.value = false
                }
            }
        }
    }

    private suspend fun seedDemoData(currentWeight: Float) {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        // Day offset helpers to backdate food and logs
        // Let's backfill foods for today
        repository.insertFoodEntry(
            FoodEntry(
                name = "Heidelbeer Protein Porridge",
                timestamp = now - 4 * oneHour,
                mealType = "Frühstück",
                calories = 380,
                protein = 24.0f,
                carbs = 52.5f,
                fat = 6.2f,
                sugar = 12.8f,
                portionGrams = 350f,
                confidenceScore = 98
            )
        )
        repository.insertFoodEntry(
            FoodEntry(
                name = "Avocado Lachs Bowl",
                timestamp = now - 1 * oneHour,
                mealType = "Mittagessen",
                calories = 540,
                protein = 28.5f,
                carbs = 42.0f,
                fat = 26.4f,
                sugar = 3.2f,
                portionGrams = 420f,
                confidenceScore = 96
            )
        )

        // Seed some water logs
        repository.insertWaterLog(WaterLog(timestamp = now - 5 * oneHour, amountMl = 250))
        repository.insertWaterLog(WaterLog(timestamp = now - 3 * oneHour, amountMl = 500))
        repository.insertWaterLog(WaterLog(timestamp = now - 30 * 60 * 1000L, amountMl = 250))

        // Create a magnificent weight trend curve
        repository.insertWeightLog(WeightLog(timestamp = now - 35 * oneDay, weight = currentWeight + 2.5f))
        repository.insertWeightLog(WeightLog(timestamp = now - 28 * oneDay, weight = currentWeight + 1.9f))
        repository.insertWeightLog(WeightLog(timestamp = now - 21 * oneDay, weight = currentWeight + 1.2f))
        repository.insertWeightLog(WeightLog(timestamp = now - 14 * oneDay, weight = currentWeight + 0.8f))
        repository.insertWeightLog(WeightLog(timestamp = now - 7 * oneDay, weight = currentWeight + 0.3f))
        repository.insertWeightLog(WeightLog(timestamp = now, weight = currentWeight))
    }

    private fun calculateMacros(
        gender: String,
        age: Int,
        height: Float,
        weight: Float,
        goal: String,
        activityLevel: String,
        pace: String
    ): UserProfile {
        val heightInMeters = height / 100f
        val bmi = weight / (heightInMeters * heightInMeters)

        // Mifflin-St Jeor Formula
        val bmr = if (gender == "männlich") {
            10f * weight + 6.25f * height - 5f * age + 5f
        } else {
            10f * weight + 6.25f * height - 5f * age - 161f
        }

        val activityFactor = when (activityLevel) {
            "wenig aktiv" -> 1.2f
            "leicht aktiv" -> 1.375f
            "aktiv" -> 1.55f
            "sehr aktiv" -> 1.725f
            else -> 1.2f
        }

        val tdee = bmr * activityFactor

        // Goal offset based on weight loss / muscle construction / maintenance
        val calorieTarget = when (goal) {
            "Abnehmen" -> {
                val paceDeficit = when (pace) {
                    "Einfach" -> 300f
                    "Schnell" -> 700f
                    else -> 500f // "Normal"
                }
                val rawTarget = tdee - paceDeficit
                val floor = if (gender == "männlich") 1500f else 1200f
                rawTarget.coerceAtLeast(floor).toInt()
            }
            "Muskelaufbau" -> {
                val paceSurplus = when (pace) {
                    "Einfach" -> 200f
                    "Schnell" -> 500f
                    else -> 350f // "Normal"
                }
                (tdee + paceSurplus).toInt()
            }
            else -> tdee.toInt() // Gewicht halten
        }

        // Incorporate Premium Strategy Split Preference
        val u = loggedInUser.value ?: "LokalUser"
        val dietPref = prefs.getString("diet_strategy_$u", "Ausgewogen") ?: "Ausgewogen"

        val proteinTarget: Float
        val fatTarget: Float
        val carbTarget: Float

        when (dietPref) {
            "Low-Carb" -> {
                val pKcal = calorieTarget * 0.30f
                val fKcal = calorieTarget * 0.35f
                proteinTarget = pKcal / 4f
                fatTarget = fKcal / 9f
                carbTarget = (calorieTarget - pKcal - fKcal) / 4f
            }
            "High-Protein" -> {
                val pKcal = calorieTarget * 0.40f
                val fKcal = calorieTarget * 0.25f
                proteinTarget = pKcal / 4f
                fatTarget = fKcal / 9f
                carbTarget = (calorieTarget - pKcal - fKcal) / 4f
            }
            "Ketogen" -> {
                val pKcal = calorieTarget * 0.15f
                val fKcal = calorieTarget * 0.75f
                proteinTarget = pKcal / 4f
                fatTarget = fKcal / 9f
                carbTarget = (calorieTarget - pKcal - fKcal) / 4f
            }
            else -> {
                val proteinPerKg = when (goal) {
                    "Abnehmen" -> 2.2f // Protect muscles during cutting deficit
                    "Muskelaufbau" -> 2.0f // Power building & hypertrophy
                    else -> 1.8f // Maintenance and health
                }
                val pTarget = proteinPerKg * weight
                val pKcal = pTarget * 4f
                val fTarget = 1.0f * weight
                val fKcal = fTarget * 9f
                val remainingKcal = (calorieTarget - pKcal - fKcal).coerceAtLeast(100f)
                proteinTarget = pTarget
                fatTarget = fTarget
                carbTarget = remainingKcal / 4f
            }
        }

        return UserProfile(
            id = 1,
            gender = gender,
            age = age,
            height = height,
            weight = weight,
            goal = goal,
            activityLevel = activityLevel,
            pace = pace,
            calorieTarget = calorieTarget,
            proteinTarget = proteinTarget,
            carbTarget = carbTarget,
            fatTarget = fatTarget,
            bmi = bmi,
            bmr = bmr,
            tdee = tdee,
            completedOnboarding = true
        )
    }

    fun restartJourney() {
        viewModelScope.launch {
            // Delete user profile to trigger Onboarding again
            db.clearAllTables()
            // Reset fields
            onboardingName.value = "Fit-Champ"
            onboardingAge.value = ""
            onboardingHeight.value = ""
            onboardingWeight.value = ""
            onboardingGender.value = "männlich"
            onboardingGoal.value = "Abnehmen"
            onboardingActivity.value = "leicht aktiv"
            onboardingPace.value = "Normal"

            // Seed assistant introduction
            repository.insertChatMessage(
                ChatMessage(
                    timestamp = System.currentTimeMillis(),
                    sender = "assistant",
                    text = "Hallo! Ich bin dein Macrofy AI Coach. 🚀 Was hast du heute Leckeres gegessen oder wie kann ich dich bei deinem Fitness-Ziel unterstützen?"
                )
            )
        }
    }

    private fun getStartOfDayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun calculateStreak(foods: List<FoodEntry>, weights: List<WeightLog>): Int {
        val daysWithActivity = mutableSetOf<String>()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        
        foods.forEach { daysWithActivity.add(sdf.format(java.util.Date(it.timestamp))) }
        weights.forEach { daysWithActivity.add(sdf.format(java.util.Date(it.timestamp))) }
        
        if (daysWithActivity.isEmpty()) return 0
        
        val sortedDays = daysWithActivity.sortedDescending()
        val todayStr = sdf.format(java.util.Date())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        
        // If no entry today and no entry yesterday, streak is 0
        if (!daysWithActivity.contains(todayStr) && !daysWithActivity.contains(yesterdayStr)) {
            return 0
        }
        
        var currentStreak = 0
        var checkCal = java.util.Calendar.getInstance()
        
        // Start checking from today (or yesterday if today is not logged yet)
        if (!daysWithActivity.contains(todayStr)) {
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        
        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (daysWithActivity.contains(dateStr)) {
                currentStreak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return currentStreak.coerceAtLeast(1)
    }

    fun updateUserProfileTargets(calories: Int, protein: Float, carbs: Float, fat: Float) {
        viewModelScope.launch {
            val p = repository.getUserProfile() ?: UserProfile(
                id = 1,
                gender = "männlich",
                age = 28,
                height = 185f,
                weight = 82f,
                goal = "Muskelaufbau",
                activityLevel = "aktiv",
                pace = "Normal",
                calorieTarget = calories,
                proteinTarget = protein,
                carbTarget = carbs,
                fatTarget = fat,
                bmi = 24.3f,
                bmr = 1850f,
                tdee = 2500f,
                completedOnboarding = true
            )
            val updated = p.copy(
                calorieTarget = calories,
                proteinTarget = protein,
                carbTarget = carbs,
                fatTarget = fat
            )
            repository.saveUserProfile(updated)
            
            val u = loggedInUser.value ?: "admin"
            prefs.edit()
                .putInt("profile_calories_$u", calories)
                .putFloat("profile_protein_$u", protein)
                .putFloat("profile_carbs_$u", carbs)
                .putFloat("profile_fat_$u", fat)
                .apply()
        }
    }

    fun saveProfileEdit(
        name: String,
        gender: String,
        age: Int,
        height: Float,
        weight: Float,
        goal: String,
        activityLevel: String,
        pace: String
    ) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            loggedInUser.value = trimmed
            prefs.edit().putString("active_user", trimmed).apply()
        }
        viewModelScope.launch {
            val profile = calculateMacros(gender, age, height, weight, goal, activityLevel, pace)
            repository.saveUserProfile(profile)
            
            val u = loggedInUser.value
            if (u != null) {
                prefs.edit()
                    .putBoolean("profile_completed_$u", true)
                    .putString("profile_gender_$u", profile.gender)
                    .putInt("profile_age_$u", profile.age)
                    .putFloat("profile_height_$u", profile.height)
                    .putFloat("profile_weight_$u", profile.weight)
                    .putString("profile_goal_$u", profile.goal)
                    .putString("profile_activity_$u", profile.activityLevel)
                    .putString("profile_pace_$u", profile.pace)
                    .putInt("profile_calories_$u", profile.calorieTarget)
                    .putFloat("profile_protein_$u", profile.proteinTarget)
                    .putFloat("profile_carbs_$u", profile.carbTarget)
                    .putFloat("profile_fat_$u", profile.fatTarget)
                    .putFloat("profile_bmi_$u", profile.bmi)
                    .putFloat("profile_bmr_$u", profile.bmr)
                    .putFloat("profile_tdee_$u", profile.tdee)
                    .apply()
            }
        }
    }

    fun generateDemoHistory() {
        viewModelScope.launch {
            // Delete existing foods first to have a clean slate or append
            // We append to show rich graphs
            val nowStr = java.text.SimpleDateFormat("dd.MM", java.util.Locale.US)
            val cal = Calendar.getInstance()
            
            val testBreakfasts = listOf(
                Pair("Heidelbeer Protein Oats", 410) to listOf(24f, 55f, 6.5f, 12f),
                Pair("Rührei mit Avocado & Toast", 450) to listOf(26f, 32f, 22f, 1.5f),
                Pair("Quark-Beeren Bowl", 320) to listOf(35f, 24f, 2.5f, 14f),
                Pair("Erdnussbutter Bananen Toast", 390) to listOf(14f, 48f, 14f, 10f),
                Pair("Müsli mit griechischem Joghurt", 360) to listOf(22f, 42f, 6f, 12f)
            )

            val testLunches = listOf(
                Pair("Hähnchenbrust mit Reis & Brokkoli", 580) to listOf(48f, 65f, 8f, 1f),
                Pair("Wildlachs Filet mit Quinoa", 620) to listOf(42f, 52f, 18f, 2f),
                Pair("Puten-Gemüse Pfanne", 510) to listOf(44f, 42f, 11f, 3f),
                Pair("Rindersteak mit Süßkartoffeln", 670) to listOf(52f, 48f, 24f, 1f),
                Pair("Falafel Bowl mit Hummus", 590) to listOf(18f, 68f, 22f, 5f)
            )

            val testSnacks = listOf(
                Pair("Premium Protein Riegel", 220) to listOf(20f, 18f, 7.5f, 1.2f),
                Pair("Handvoll Mandeln", 160) to listOf(6f, 6f, 14f, 1.1f),
                Pair("Magerquark mit Honig", 180) to listOf(24f, 14f, 0.5f, 12f),
                Pair("Whey Protein Shake", 140) to listOf(26f, 3f, 1.8f, 1.5f),
                Pair("Spreewald Gewürzgurken & Reiswaffeln", 110) to listOf(3f, 22f, 0.5f, 2f)
            )

            for (i in 0..6) {
                val dCal = Calendar.getInstance()
                dCal.add(Calendar.DAY_OF_YEAR, -i)
                
                // Meal 1 Breakfast
                val bf = testBreakfasts[i % testBreakfasts.size]
                dCal.set(Calendar.HOUR_OF_DAY, 8)
                dCal.set(Calendar.MINUTE, 30)
                repository.insertFoodEntry(
                    FoodEntry(
                        name = bf.first.first,
                        timestamp = dCal.timeInMillis,
                        mealType = "Frühstück",
                        calories = bf.first.second,
                        protein = bf.second[0],
                        carbs = bf.second[1],
                        fat = bf.second[2],
                        sugar = bf.second[3],
                        portionGrams = 250f,
                        confidenceScore = 100
                    )
                )

                // Meal 2 Lunch
                val lc = testLunches[i % testLunches.size]
                dCal.set(Calendar.HOUR_OF_DAY, 13)
                dCal.set(Calendar.MINUTE, 15)
                repository.insertFoodEntry(
                    FoodEntry(
                        name = lc.first.first,
                        timestamp = dCal.timeInMillis,
                        mealType = "Mittagessen",
                        calories = lc.first.second,
                        protein = lc.second[0],
                        carbs = lc.second[1],
                        fat = lc.second[2],
                        sugar = lc.second[3],
                        portionGrams = 450f,
                        confidenceScore = 100
                    )
                )

                // Meal 3 Snack
                val sn = testSnacks[i % testSnacks.size]
                dCal.set(Calendar.HOUR_OF_DAY, 17)
                dCal.set(Calendar.MINUTE, 0)
                repository.insertFoodEntry(
                    FoodEntry(
                        name = sn.first.first,
                        timestamp = dCal.timeInMillis,
                        mealType = "Snack",
                        calories = sn.first.second,
                        protein = sn.second[0],
                        carbs = sn.second[1],
                        fat = sn.second[2],
                        sugar = sn.second[3],
                        portionGrams = 75f,
                        confidenceScore = 100
                    )
                )
            }
        }
    }

    fun generateWeightDemo() {
        viewModelScope.launch {
            // Seed Weight records over last 14 days
            val startingWeight = 85.5f
            for (i in 13 downTo 0) {
                val dCal = Calendar.getInstance()
                dCal.add(Calendar.DAY_OF_YEAR, -i)
                
                // Steadily descending weight with small random variation
                val factor = (13 - i).toFloat() / 13f
                val progressWeight = startingWeight - (3.5f * factor) + ((i % 3) * 0.15f) - 0.2f
                
                repository.insertWeightLog(
                    WeightLog(
                        timestamp = dCal.timeInMillis,
                        weight = String.format(java.util.Locale.US, "%.1f", progressWeight).toFloat()
                    )
                )
            }
            
            // Also update main profile weight to match the latest point
            val currentLatest = startingWeight - 3.7f
            recordWeight(String.format(java.util.Locale.US, "%.1f", currentLatest).toFloat())
        }
    }

    // Google Play Store Billing Integration state mapping
    val isPremiumSubscribed = com.example.data.PlayBillingHelper.isSubscribed
    val billingPurchaseStatus = com.example.data.PlayBillingHelper.purchaseStatus

    fun launchGooglePlaySubscription(activity: Activity, planId: String) {
        com.example.data.PlayBillingHelper.launchBillingFlow(activity, planId)
    }

    fun simulatedCancelSubscription() {
        val app = getApplication<Application>()
        com.example.data.PlayBillingHelper.cancelSubscriptionSimulated(app)
    }

    fun restorePlayStorePurchases() {
        val app = getApplication<Application>()
        com.example.data.PlayBillingHelper.queryPurchases(app, isManualTrigger = true)
    }
}
