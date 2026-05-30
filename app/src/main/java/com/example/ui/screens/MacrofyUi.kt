package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.FoodAnalysisResult
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.MacrofyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.window.DialogProperties

data class FoodTemplate(val name: String, val calories: Int, val protein: Float, val carbs: Float, val fat: Float, val sugar: Float)

// Sleek Minimalist Card Modifier with Premium Soft Shadows and Solid Slate-White Surfaces
fun Modifier.glassCard(
    borderColor: Color = ObsidianBorder,
    backgroundColor: Color = ObsidianSurface,
    cornerRadius: Dp = 16.dp,
    glowingHighlight: Color? = null
) = this
    .shadow(
        elevation = if (glowingHighlight != null) 4.dp else 2.dp,
        shape = RoundedCornerShape(cornerRadius),
        clip = false,
        ambientColor = (glowingHighlight ?: Color(0xFF0F172A)).copy(alpha = 0.03f),
        spotColor = (glowingHighlight ?: Color(0xFF0F172A)).copy(alpha = 0.07f)
    )
    .background(
        color = backgroundColor,
        shape = RoundedCornerShape(cornerRadius)
    )
    .border(
        width = 1.dp,
        color = if (glowingHighlight != null) glowingHighlight.copy(0.35f) else borderColor,
        shape = RoundedCornerShape(cornerRadius)
    )

// Custom Premium Water Glass/Beaker fluid visualizer with interactive waves
@Composable
fun WaterBeaker(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Premium trapezoidal container shape with rounded bottom base
        val beakerPath = Path().apply {
            moveTo(w * 0.15f, h * 0.05f)
            lineTo(w * 0.23f, h * 0.88f)
            quadraticBezierTo(w * 0.25f, h * 0.94f, w * 0.35f, h * 0.94f)
            lineTo(w * 0.65f, h * 0.94f)
            quadraticBezierTo(w * 0.75f, h * 0.94f, w * 0.77f, h * 0.88f)
            lineTo(w * 0.85f, h * 0.05f)
        }
        
        // Draw subtle glass container base backing shadow
        drawPath(
            path = beakerPath,
            color = ObsidianBorder.copy(alpha = 0.5f)
        )
        
        // Wave filling logic
        if (progress > 0.01f) {
            val fill = progress.coerceAtMost(1f)
            val fillY = h - (h * 0.82f * fill) - (h * 0.06f)
            
            // Build waves filling path
            val wavePath = Path().apply {
                moveTo(w * 0.23f, h * 0.88f)
                
                // Sine wave coordinate generation
                val leftX = w * 0.15f + (w * 0.7f * (1f - fill))
                lineTo(leftX, fillY)
                
                // Smooth sine/quadratic bridge for beautiful ripple effect
                quadraticBezierTo(
                    w * 0.5f, fillY - 6.dp.toPx(),
                    w * 0.85f - (w * 0.7f * (1f - fill)), fillY
                )
                
                lineTo(w * 0.77f, h * 0.88f)
                quadraticBezierTo(w * 0.75f, h * 0.94f, w * 0.65f, h * 0.94f)
                lineTo(w * 0.35f, h * 0.94f)
                quadraticBezierTo(w * 0.25f, h * 0.94f, w * 0.23f, h * 0.88f)
            }
            
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CoachTeal,
                        ProteinBlue.copy(alpha = 0.75f)
                    )
                )
            )
        }
        
        // Draw measuring hatch tickmarks representing volume milestones
        val ticks = listOf(0.25f, 0.5f, 0.75f)
        ticks.forEach { t ->
            val tickY = h - (h * 0.82f * t) - (h * 0.06f)
            val tickWidth = 8.dp.toPx()
            drawLine(
                color = TextGray.copy(alpha = 0.4f),
                start = Offset(w * 0.35f, tickY),
                end = Offset(w * 0.35f + tickWidth, tickY),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        
        // Glass bezel glowing edge
        drawPath(
            path = beakerPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    TextGray.copy(alpha = 0.3f),
                    TextGray.copy(alpha = 0.15f)
                )
            ),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MacrofyAppContent(viewModel: MacrofyViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeTrackerMode by viewModel.activeTrackerMode.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Landing) }

    LaunchedEffect(userProfile) {
        val profile = userProfile
        if (profile != null && profile.completedOnboarding) {
            currentScreen = AppScreen.Dashboard(DashboardTab.Home)
        } else {
            val screen = currentScreen
            if (screen !is AppScreen.Onboarding && screen !is AppScreen.CalculationResult) {
                currentScreen = AppScreen.Landing
            }
        }
    }

    val backgroundGradientColors = remember(activeTrackerMode) {
        if (activeTrackerMode == "google") {
            listOf(
                Color(0xFFF1F6FF), // Soft clean Google blue tint
                Color(0xFFE8F1FF), // Soft clean Google blue-grey tint
                ObsidianSurface
            )
        } else {
            listOf(
                ObsidianDarkBg,
                ObsidianDarkBg,
                ObsidianSurface
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = backgroundGradientColors
                )
            )
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is AppScreen.Landing -> LandingPageScreen(
                    viewModel = viewModel,
                    onNavigateTo = { currentScreen = it },
                    onStartOnboarding = {
                        currentScreen = AppScreen.Onboarding
                    },
                    onDemoStart = {}
                )
                is AppScreen.Login -> { /* Unused */ }
                is AppScreen.Register -> { /* Unused */ }
                is AppScreen.Onboarding -> OnboardingScreen(
                    viewModel = viewModel,
                    onOnboardingComplete = {
                        viewModel.completeOnboarding()
                        currentScreen = AppScreen.CalculationResult
                    }
                )
                is AppScreen.CalculationResult -> CalculationResultScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        currentScreen = AppScreen.Dashboard(DashboardTab.Home)
                    }
                )
                is AppScreen.Dashboard -> AppAreaScreen(
                    viewModel = viewModel,
                    activeTab = screen.tab,
                    onTabChanged = { currentScreen = AppScreen.Dashboard(it) }
                )
            }
        }
    }
}

// Sealed screen navigation structures
sealed class AppScreen {
    object Landing : AppScreen()
    object Login : AppScreen()
    object Register : AppScreen()
    object Onboarding : AppScreen()
    object CalculationResult : AppScreen()
    data class Dashboard(val tab: DashboardTab) : AppScreen()
}

sealed class DashboardTab {
    object Home : DashboardTab()
    object Diary : DashboardTab()
    object Scanner : DashboardTab()
    object Coach : DashboardTab()
    object Profile : DashboardTab()
}

// ----------------------------------------------------
// 1. LANDING PAGE
// ----------------------------------------------------
@Composable
fun LandingPageScreen(
    viewModel: MacrofyViewModel,
    onNavigateTo: (AppScreen) -> Unit,
    onStartOnboarding: () -> Unit,
    onDemoStart: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val activeUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Glowing Orbs in Background
        GlowOrb(
            color = MacrofyGreen.copy(alpha = 0.15f),
            size = 350.dp,
            offset = Offset(100f, 150f)
        )
        GlowOrb(
            color = CoachTeal.copy(alpha = 0.12f),
            size = 400.dp,
            offset = Offset(800f, 1200f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Brand Logo with stunning visual polish and subtle gradients
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    // Stylized diamond background with gradient
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer(rotationZ = 45f)
                            .background(
                                Brush.linearGradient(listOf(MacrofyGreen, CoachTeal)),
                                RoundedCornerShape(10.dp)
                            )
                    )
                    // Overlapping inner glowing circle ring containing the icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ObsidianDarkBg, CircleShape)
                            .border(1.5.dp, Brush.linearGradient(listOf(CoachTeal, MacrofyGreen)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Logo icon",
                            tint = MacrofyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Light, color = TextWhite)) {
                            append("Macro")
                        }
                        withStyle(SpanStyle(
                            brush = Brush.linearGradient(colors = listOf(MacrofyGreen, CoachTeal)),
                            fontWeight = FontWeight.Black
                        )) {
                            append("fy")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Light, color = CoachTeal)) {
                            append(" AI")
                        }
                    },
                    fontSize = 28.sp,
                    letterSpacing = 1.2.sp
                )
            }

            // Core Focus Badge (without pricing)
            Box(
                modifier = Modifier
                    .glassCard(borderColor = CoachTeal.copy(alpha = 0.25f), backgroundColor = CoachTeal.copy(alpha = 0.06f), cornerRadius = 12.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✨ ELITE AI-DIARY & SMART NUTRITION SCANNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoachTeal,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Headline
            Text(
                text = buildAnnotatedString {
                    append("Track deine\n")
                    withStyle(SpanStyle(
                        brush = Brush.linearGradient(colors = listOf(MacrofyGreen, CoachTeal, CarbOrange)),
                        fontWeight = FontWeight.Black
                    )) {
                        append("Ernährung ")
                    }
                    append("mit KI.")
                },
                fontSize = 44.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subheadline
            Text(
                text = "Fotografiere dein Essen oder Verpackungen – Macrofy AI erkennt automatisch Kalorien, Proteine und Makros in Sekunden.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Active user welcome card (if already logged in)
            if (activeUser != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .glassCard(glowingHighlight = MacrofyGreen)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Willkommen zurück, $activeUser! 👋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dein Account ist aktiv eingeloggt.",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val profile = viewModel.userProfile.value
                                    if (profile != null && profile.completedOnboarding) {
                                        onNavigateTo(AppScreen.Dashboard(DashboardTab.Home))
                                    } else {
                                        onNavigateTo(AppScreen.Onboarding)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Zum Dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            } else {
                // CTAs
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onStartOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MacrofyGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("onboarding_start_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "start-now", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Jetzt starten",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Safe and Local Data Hinweis (instead of Demo launch button)
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(LightGlassSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = "Safe and Local",
                            tint = MacrofyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Sicher, anonym & lokal auf deinem Gerät gespeichert",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray
                        )
                    }

                    // End of CTA section
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Live App Preview Mock / Bento Box Style Showcase
            Text(
                text = "Das Macrofy AI Ökosystem",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // First bento block - Scanner Preview with Glowing Emerald Highlight Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(glowingHighlight = MacrofyGreen)
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MacrofyGreen.copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.CameraAlt, "Scan", tint = MacrofyGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Food Scanner", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Keine manuelle Suche mehr. Fotografiere deinen Teller oder scanne die Zutaten-Tabelle. Das Modell analysiert Portionsgröße und Nährstoffe.",
                        fontSize = 13.sp,
                        color = TextGray,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // High-quality Scan UI preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(ObsidianBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥑", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Avocado Lachs Bowl", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text("Confidence: 98% (Sehr sicher)", fontSize = 11.sp, color = MacrofyGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("540 kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("28g Protein", fontSize = 11.sp, color = ProteinBlue)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two Column Bento below with Glowing Cyan and Glowing Amber Highlights
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassCard(glowingHighlight = CoachTeal)
                        .padding(16.dp)
                        .height(150.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                        Icon(Icons.Outlined.AutoAwesome, "Coach", tint = CoachTeal, modifier = Modifier.size(24.dp))
                        Column {
                            Text("AI Fitness-Coach", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Personalisiertes Feedback zu deinem Ernährungstag.", fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassCard(glowingHighlight = CarbOrange)
                        .padding(16.dp)
                        .height(150.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                        Icon(Icons.Outlined.BarChart, "Analytics", tint = CarbOrange, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Makro-Verlauf", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Analysiere Gewichts- und Kalorienkurven.", fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // FAQ Section
            Text(
                text = "Häufig gestellte Fragen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            FaqItem(
                question = "Gibt es kostenfreie Testmöglichkeiten?",
                answer = "Ja! Unsere App bietet standardmäßig eine Basisvariante mit täglichem Koch-Support an. Für unbegrenzte KI-Scans (Mahlzeiten & Etiketten) sowie unbegrenztes Premium-Livecoaching bieten wir flexible Premium-Abonnements für volle Flexibilität an."
            )
            FaqItem(
                question = "Wie genau ist der KI-Scanner?",
                answer = "Das integrierte Modell (Gemini 3.5 AI) liefert hochpräzise Schätzungen für Standardgerichte, verpackte Lebensmittel mit Nährstofftabellen sowie gescannten Barcode-Angaben."
            )
            FaqItem(
                question = "Werden meine Daten lokal gespeichert?",
                answer = "Deine Daten werden primär offline und absolut sicher in der lokalen Room-Datenbank auf deinem Android-Gerät abgelegt. Privatsphäre steht bei uns an oberster Stelle."
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Text(
                text = "Macrofy AI © 2026. Made for elite health tracking.",
                fontSize = 11.sp,
                color = TextGray.copy(0.6f)
            )
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .glassCard(cornerRadius = 16.dp)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(question, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextWhite, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand FAQ",
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = ObsidianBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(answer, fontSize = 12.sp, color = TextGray, lineHeight = 18.sp)
            }
        }
    }
}

// ----------------------------------------------------
// 2. MULTI-STEP ONBOARDING
// ----------------------------------------------------
@Composable
fun OnboardingScreen(viewModel: MacrofyViewModel, onOnboardingComplete: () -> Unit) {
    val currentStep = remember { mutableStateOf(1) }
    val maxSteps = 8

    val name by viewModel.onboardingName.collectAsStateWithLifecycle()
    val gender by viewModel.onboardingGender.collectAsStateWithLifecycle()
    val age by viewModel.onboardingAge.collectAsStateWithLifecycle()
    val height by viewModel.onboardingHeight.collectAsStateWithLifecycle()
    val weight by viewModel.onboardingWeight.collectAsStateWithLifecycle()
    val goal by viewModel.onboardingGoal.collectAsStateWithLifecycle()
    val activity by viewModel.onboardingActivity.collectAsStateWithLifecycle()
    val pace by viewModel.onboardingPace.collectAsStateWithLifecycle()

    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoachTeal.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(0.9f, 0.1f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Steps Header & Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VITALITY INTRO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CoachTeal,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Schritt ${currentStep.value} von $maxSteps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Custom linear progress bar with modern ambient blur glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(ObsidianBorder, RoundedCornerShape(3.dp))
                ) {
                    val progressFraction = currentStep.value.toFloat() / maxSteps.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(6.dp)
                            .background(
                                Brush.linearGradient(listOf(MacrofyGreen, CoachTeal)),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Active Step Content
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep.value,
                    transitionSpec = {
                        slideInHorizontally(animationSpec = tween(220)) { width -> if (targetState > initialState) width else -width } togetherWith
                                slideOutHorizontally(animationSpec = tween(180)) { width -> if (targetState > initialState) -width else width }
                    },
                    label = "OnboardingStep"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            1 -> {
                                Text("Wie heißt du?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Gib deinen Namen ein, um dein Erlebnis zu personalisieren.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(30.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { viewModel.onboardingName.value = it },
                                    placeholder = { Text("Z.B. Alex", color = TextGray) },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .testTag("onboarding_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CoachTeal,
                                        unfocusedBorderColor = ObsidianBorder,
                                        focusedContainerColor = LightGlassSurface,
                                        unfocusedContainerColor = LightGlassSurface.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            2 -> {
                                Text("Biologisches Geschlecht", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bestimmt deine biochemischen Grundumsatzkoeffizienten.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(30.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    SelectionCard(
                                        label = "Männlich",
                                        icon = Icons.Rounded.Male,
                                        selected = gender == "männlich",
                                        onClick = { viewModel.onboardingGender.value = "männlich" },
                                        activeColor = ProteinBlue,
                                        modifier = Modifier.weight(1f).testTag("gender_male")
                                    )
                                    SelectionCard(
                                        label = "Weiblich",
                                        icon = Icons.Rounded.Female,
                                        selected = gender == "weiblich",
                                        onClick = { viewModel.onboardingGender.value = "weiblich" },
                                        activeColor = FatPink,
                                        modifier = Modifier.weight(1f).testTag("gender_female")
                                    )
                                }
                            }
                            3 -> {
                                Text("Wie alt bist du?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Beeinflusst die zelluläre Stoffwechselrate.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))

                                val ageFloat = age.toFloatOrNull() ?: 28f
                                if (age.isEmpty()) {
                                    LaunchedEffect(Unit) {
                                        viewModel.onboardingAge.value = "28"
                                    }
                                }

                                PremiumRulerPicker(
                                    value = ageFloat,
                                    onValueChange = { viewModel.onboardingAge.value = it.toInt().toString() },
                                    valueRange = 15f..90f,
                                    unit = "Jahre",
                                    accentColor = MacrofyGreen
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (ageFloat < 30) "Zellulärer Hochleistungsstoffwechsel aktiv." else "Optimierte Fettverbrennung durch Kraftreize empfohlen.",
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            4 -> {
                                Text("Wie groß bist du?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Ermöglicht eine präzise BMI-Berechnung.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))

                                val heightFloat = height.toFloatOrNull() ?: 178f
                                if (height.isEmpty()) {
                                    LaunchedEffect(Unit) {
                                        viewModel.onboardingHeight.value = "178"
                                    }
                                }

                                PremiumRulerPicker(
                                    value = heightFloat,
                                    onValueChange = { viewModel.onboardingHeight.value = it.toInt().toString() },
                                    valueRange = 120f..220f,
                                    unit = "cm",
                                    accentColor = CoachTeal
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Dein Körperbau bietet erstklassiges Potenzial für neue Reize.",
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            5 -> {
                                Text("Dein aktuelles Gewicht?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Die energetische Basis deines Energiehaushalts.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))

                                val weightFloat = weight.toFloatOrNull() ?: 75f
                                if (weight.isEmpty()) {
                                    LaunchedEffect(Unit) {
                                        viewModel.onboardingWeight.value = "75"
                                    }
                                }

                                PremiumRulerPicker(
                                    value = weightFloat,
                                    onValueChange = { viewModel.onboardingWeight.value = String.format(Locale.US, "%.1f", it) },
                                    valueRange = 40f..150f,
                                    unit = "kg",
                                    accentColor = ProteinBlue
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Grundlage für Kalorien & essentielle Fett- und Proteinbereiche.",
                                    fontSize = 12.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                            6 -> {
                                Text("Was ist dein primäres Ziel?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Wir steuern deine Kalorienzufuhr dementsprechend.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(20.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    SelectionCardRow(
                                        label = "Gewichtsabnahme",
                                        desc = "Schnittiges Energiedefizit für gesunden Fettabbau",
                                        icon = Icons.Rounded.TrendingDown,
                                        selected = goal == "Abnehmen",
                                        onClick = { viewModel.onboardingGoal.value = "Abnehmen" },
                                        activeColor = FatPink,
                                        modifier = Modifier.testTag("goal_lose")
                                    )
                                    SelectionCardRow(
                                        label = "Muskelaufbau",
                                        desc = "Sauberer, kontrollierter Aufbau neuer Muskelkraft",
                                        icon = Icons.Rounded.FitnessCenter,
                                        selected = goal == "Muskelaufbau",
                                        onClick = { viewModel.onboardingGoal.value = "Muskelaufbau" },
                                        activeColor = MacrofyGreen,
                                        modifier = Modifier.testTag("goal_gain")
                                    )
                                    SelectionCardRow(
                                        label = "Gewicht halten",
                                        desc = "Energie-Erhaltung zur Optimierung von Fitness",
                                        icon = Icons.Rounded.TrendingFlat,
                                        selected = goal == "Gewicht halten",
                                        onClick = { viewModel.onboardingGoal.value = "Gewicht halten" },
                                        activeColor = ProteinBlue,
                                        modifier = Modifier.testTag("goal_keep")
                                    )
                                }
                            }
                            7 -> {
                                Text("Tägliches Aktivitätslevel", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bestimmt den Aktivitätsfaktor für das tägliche Energielimit.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(20.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    SelectionCardRow(
                                        label = "Wenig aktiv (PAL 1.2)",
                                        desc = "Sitzender Arbeitsrhythmus, fast kein Sport",
                                        icon = Icons.Rounded.Weekend,
                                        selected = activity == "wenig aktiv",
                                        onClick = { viewModel.onboardingActivity.value = "wenig aktiv" },
                                        activeColor = TextGray,
                                        modifier = Modifier.testTag("activity_sedentary")
                                    )
                                    SelectionCardRow(
                                        label = "Leicht aktiv (PAL 1.375)",
                                        desc = "Stehend/gehend, Sport 1-2x wöchentlich",
                                        icon = Icons.Rounded.DirectionsWalk,
                                        selected = activity == "leicht aktiv",
                                        onClick = { viewModel.onboardingActivity.value = "leicht aktiv" },
                                        activeColor = ProteinBlue,
                                        modifier = Modifier.testTag("activity_light")
                                    )
                                    SelectionCardRow(
                                        label = "Aktiv (PAL 1.55)",
                                        desc = "Gewalkt/Fitness 3-5x wöchentlich",
                                        icon = Icons.Rounded.DirectionsRun,
                                        selected = activity == "aktiv",
                                        onClick = { viewModel.onboardingActivity.value = "aktiv" },
                                        activeColor = MacrofyGreen,
                                        modifier = Modifier.testTag("activity_moderate")
                                    )
                                    SelectionCardRow(
                                        label = "Sehr aktiv (PAL 1.725)",
                                        desc = "Körperlicher Beruf & Leistungssportler",
                                        icon = Icons.Rounded.Speed,
                                        selected = activity == "sehr aktiv",
                                        onClick = { viewModel.onboardingActivity.value = "sehr aktiv" },
                                        activeColor = CarbOrange,
                                        modifier = Modifier.testTag("activity_heavy")
                                    )
                                }
                            }
                            8 -> {
                                Text("Bevorzugtes Diättempo", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextWhite, textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Bestimmt die Geschwindigkeit deines Fortschritts.", fontSize = 13.sp, color = TextGray, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(30.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    SelectionCard(
                                        label = "Gemäßigt",
                                        icon = Icons.Rounded.SelfImprovement,
                                        selected = pace == "Langsam",
                                        onClick = { viewModel.onboardingPace.value = "Langsam" },
                                        activeColor = CoachTeal,
                                        modifier = Modifier.weight(1f).testTag("pace_easy")
                                    )
                                    SelectionCard(
                                        label = "Standard",
                                        icon = Icons.Rounded.CheckCircleOutline,
                                        selected = pace == "Normal",
                                        onClick = { viewModel.onboardingPace.value = "Normal" },
                                        activeColor = MacrofyGreen,
                                        modifier = Modifier.weight(1f).testTag("pace_normal")
                                    )
                                }
                            }
                        }

                        // Real-Time Cockpit Preview shows up from Step 3 onwards
                        if (step >= 3) {
                            Spacer(modifier = Modifier.height(24.dp))
                            LiveTargetPreviewCard(
                                gender = gender,
                                ageStr = age,
                                heightStr = height,
                                weightStr = weight,
                                goal = goal,
                                activity = activity
                            )
                        }

                        errorMsg?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(it, color = FatPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep.value > 1) {
                    IconButton(
                        onClick = { currentStep.value -= 1 },
                        modifier = Modifier
                            .size(56.dp)
                            .glassCard()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Zurück",
                            tint = TextWhite
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }

                Button(
                    onClick = {
                        // Validations
                        when {
                            currentStep.value == 1 && name.trim().isEmpty() -> {
                                errorMsg = "Bitte gib deinen Namen ein."
                            }
                            currentStep.value == 3 && age.isEmpty() -> {
                                errorMsg = "Bitte gib dein Alter ein."
                            }
                            currentStep.value == 4 && height.isEmpty() -> {
                                errorMsg = "Bitte gib deine Größe ein."
                            }
                            currentStep.value == 5 && weight.isEmpty() -> {
                                errorMsg = "Bitte gib dein Gewicht ein."
                            }
                            currentStep.value < maxSteps -> {
                                if (currentStep.value == 1) {
                                    viewModel.signUp(name, "local@macrofy.ai", "local123")
                                }
                                currentStep.value += 1
                                errorMsg = null
                            }
                            else -> {
                                onOnboardingComplete()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MacrofyGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .height(56.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentStep.value == maxSteps) "Kalkulieren & Starten" else "Weiter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// Custom physical sliding scale for millon-dollar high fidelity tactile control
@Composable
fun PremiumRulerPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val coercedValue = value.coerceIn(valueRange)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(borderColor = ObsidianBorder, backgroundColor = LightGlassSurface.copy(alpha = 0.05f))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Readout Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = if (coercedValue % 1f == 0f) "${coercedValue.toInt()}" else String.format(Locale.US, "%.1f", coercedValue),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = (-1.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = unit,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Custom Tick Mark Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val width = size.width
                val height = size.height

                // Base Line
                drawLine(
                    color = ObsidianBorder.copy(alpha = 0.5f),
                    start = Offset(0f, height / 2),
                    end = Offset(width, height / 2),
                    strokeWidth = 1.dp.toPx()
                )

                val centerVal = coercedValue
                val minVal = valueRange.start
                val maxVal = valueRange.endInclusive
                val rangeLen = (maxVal - minVal).coerceAtLeast(1f)

                // We render ticks dynamically around the center
                // 30 marks total
                val numTicks = 30
                val spacingVal = rangeLen * 0.12f / numTicks // local zoom range

                for (i in -numTicks..numTicks) {
                    val tickVal = centerVal + (i * spacingVal)
                    if (tickVal in minVal..maxVal) {
                        val x = width / 2f + (i * (width / (numTicks * 2.2f)))
                        if (x in 0f..width) {
                            val isMajor = (tickVal.toInt() % 5 == 0) && (Math.abs(tickVal - tickVal.toInt()) < 0.2f)
                            val tHeight = if (isMajor) 24.dp.toPx() else 12.dp.toPx()
                            val weightStroke = if (isMajor) 2.2f.dp.toPx() else 1.2f.dp.toPx()

                            val colorAlpha = (1f - (Math.abs(i) / (numTicks * 1.1f))).coerceIn(0.1f, 1f)
                            val color = if (i == 0) accentColor else Color.White.copy(alpha = colorAlpha * 0.25f)

                            drawLine(
                                color = color,
                                start = Offset(x, height / 2 - tHeight / 2),
                                end = Offset(x, height / 2 + tHeight / 2),
                                strokeWidth = weightStroke
                            )
                        }
                    }
                }

                // Draw central focus needle indicator
                drawLine(
                    color = accentColor,
                    start = Offset(width / 2, 0f),
                    end = Offset(width / 2, height),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sleek Slider overlaid under
            Slider(
                value = coercedValue,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = ObsidianBorder,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Gorgeous live target previews
@Composable
fun LiveTargetPreviewCard(
    gender: String,
    ageStr: String,
    heightStr: String,
    weightStr: String,
    goal: String,
    activity: String
) {
    val currentAge = ageStr.toIntOrNull() ?: 25
    val currentHeight = heightStr.toFloatOrNull() ?: 175f
    val currentWeight = weightStr.toFloatOrNull() ?: 75f
    
    val heightInMeters = currentHeight / 100f
    val currentBmi = if (currentHeight > 0f) currentWeight / (heightInMeters * heightInMeters) else 22.5f
    
    val currentBmr = if (gender == "männlich") {
        10f * currentWeight + 6.25f * currentHeight - 5f * currentAge + 5f
    } else {
        10f * currentWeight + 6.25f * currentHeight - 5f * currentAge - 161f
    }
    
    val currentActivityFactor = when (activity) {
        "wenig aktiv" -> 1.2f
        "leicht aktiv" -> 1.375f
        "aktiv" -> 1.55f
        "sehr aktiv" -> 1.725f
        else -> 1.2f
    }
    val currentTdee = currentBmr * currentActivityFactor
    val currentCalorieTarget = when (goal) {
        "Abnehmen" -> (currentTdee - 500f).toInt().coerceAtLeast(1200)
        "Muskelaufbau" -> (currentTdee + 300f).toInt()
        "Gewicht halten" -> currentTdee.toInt()
        else -> currentTdee.toInt()
    }
    
    val currentProteinTarget = 2.0f * currentWeight
    val currentFatTarget = 0.8f * currentWeight
    val currentProteinKcal = currentProteinTarget * 4f
    val currentFatKcal = currentFatTarget * 9f
    val currentRemainingKcal = currentCalorieTarget - currentProteinKcal - currentFatKcal
    val currentCarbTarget = (currentRemainingKcal / 4f).coerceAtLeast(20f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(borderColor = CoachTeal.copy(0.3f), backgroundColor = LightGlassSurface.copy(0.04f), glowingHighlight = CoachTeal)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Analytics, 
                        contentDescription = null, 
                        tint = CoachTeal, 
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Echtzeit-Analyse", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = CoachTeal,
                        letterSpacing = 0.5.sp
                    )
                }
                
                val bmiText = String.format(Locale.US, "%.1f", currentBmi)
                val bmiColor = when {
                    currentBmi < 18.5f -> CarbOrange
                    currentBmi < 25f -> MacrofyGreen
                    currentBmi < 30f -> CarbOrange
                    else -> FatPink
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "BMI: $bmiText", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        color = bmiColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(bmiColor, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Tagesbudget Vorschau", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(
                        "$currentCalorieTarget kcal", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroMicroBadge("Prot", "${currentProteinTarget.toInt()}g", ProteinBlue)
                    MacroMicroBadge("Carb", "${currentCarbTarget.toInt()}g", CarbOrange)
                    MacroMicroBadge("Fett", "${currentFatTarget.toInt()}g", FatPink)
                }
            }
        }
    }
}

@Composable
fun MacroMicroBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextWhite)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(4.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SelectionCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, label = "card_scale")
    
    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .glassCard(
                borderColor = if (selected) activeColor else ObsidianBorder,
                backgroundColor = if (selected) activeColor.copy(alpha = 0.08f) else LightGlassSurface
            )
            .clickable { onClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) activeColor else TextGray,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (selected) activeColor else TextWhite, letterSpacing = (-0.2).sp)
        }
    }
}

@Composable
fun SelectionCardRow(
    label: String,
    desc: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.02f else 1f, label = "card_scale_row")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .glassCard(
                borderColor = if (selected) activeColor else ObsidianBorder,
                backgroundColor = if (selected) activeColor.copy(alpha = 0.08f) else LightGlassSurface
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (selected) activeColor.copy(0.12f) else ObsidianBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = if (selected) activeColor else TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (selected) activeColor else TextWhite)
            Text(desc, fontSize = 11.sp, color = TextGray)
        }
        if (selected) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = "Checked", tint = activeColor, modifier = Modifier.size(20.dp))
        }
    }
}

// ----------------------------------------------------
// 3. MAIN DASHBOARD NAVIGATION WRAPPER (MOBILE BOTTOM NAV)
// ----------------------------------------------------
@Composable
fun AppAreaScreen(
    viewModel: MacrofyViewModel,
    activeTab: DashboardTab,
    onTabChanged: (DashboardTab) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianDarkBg,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.border(0.5.dp, ObsidianBorder.copy(alpha = 0.6f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Home,
                    onClick = { onTabChanged(DashboardTab.Home) },
                    icon = { Icon(Icons.Rounded.Home, "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = if (activeTab == DashboardTab.Home) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MacrofyGreen,
                        selectedTextColor = MacrofyGreen,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Diary,
                    onClick = { onTabChanged(DashboardTab.Diary) },
                    icon = { Icon(Icons.Rounded.MenuBook, "Tagebuch") },
                    label = { Text("Tagebuch", fontSize = 11.sp, fontWeight = if (activeTab == DashboardTab.Diary) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CarbOrange,
                        selectedTextColor = CarbOrange,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Scanner,
                    onClick = { onTabChanged(DashboardTab.Scanner) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    Brush.linearGradient(listOf(MacrofyGreen, CoachTeal)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Camera, "Scanner", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    },
                    label = { Text("Scanner", fontSize = 11.sp, fontWeight = if (activeTab == DashboardTab.Scanner) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MacrofyGreen,
                        selectedTextColor = MacrofyGreen,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Coach,
                    onClick = { onTabChanged(DashboardTab.Coach) },
                    icon = { Icon(Icons.Rounded.SmartToy, "AI Coach") },
                    label = { Text("AI Coach", fontSize = 11.sp, fontWeight = if (activeTab == DashboardTab.Coach) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoachTeal,
                        selectedTextColor = CoachTeal,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.Profile,
                    onClick = { onTabChanged(DashboardTab.Profile) },
                    icon = { Icon(Icons.Rounded.Person, "Profil") },
                    label = { Text("Profil", fontSize = 11.sp, fontWeight = if (activeTab == DashboardTab.Profile) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ProteinBlue,
                        selectedTextColor = ProteinBlue,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    is DashboardTab.Home -> DashboardHomeScreen(viewModel)
                    is DashboardTab.Diary -> FoodDiaryScreen(viewModel)
                    is DashboardTab.Scanner -> FoodScannerScreen(viewModel)
                    is DashboardTab.Coach -> CoachChatScreen(viewModel)
                    is DashboardTab.Profile -> UserProfileScreen(viewModel)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3.5 HEALTH ACTIVITY RINGS COMPOSABLE (wie health diese ringe)
// ----------------------------------------------------
@Composable
fun MacrofyHealthRings(
    caloriesProgress: Float,
    proteinProgress: Float,
    carbProgress: Float,
    fatProgress: Float,
    modifier: Modifier = Modifier
) {
    val animatedCal by animateFloatAsState(
        targetValue = caloriesProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val animatedProt by animateFloatAsState(
        targetValue = proteinProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val animatedCarb by animateFloatAsState(
        targetValue = carbProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val animatedFat by animateFloatAsState(
        targetValue = fatProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    Canvas(modifier = modifier) {
        val strokeWidthPx = 7.dp.toPx()
        val spacingPx = 2.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        // Track colors - dark version of active color with subtle opacity
        val trackAlpha = 0.12f

        // Radii calculations for 4 concentric rings
        val r1 = size.width / 2f - strokeWidthPx
        val r2 = r1 - strokeWidthPx - spacingPx
        val r3 = r2 - strokeWidthPx - spacingPx
        val r4 = r3 - strokeWidthPx - spacingPx

        // Ring 1: Calories (Outer)
        drawCircle(color = MacrofyGreen.copy(alpha = trackAlpha), radius = r1, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedCal > 0f) {
            drawArc(
                brush = Brush.linearGradient(colors = listOf(MacrofyGreen, CoachTeal)),
                startAngle = -90f,
                sweepAngle = (animatedCal * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r1, center.y - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            // Draw overlapping arc if progress exceeds 100%
            if (animatedCal > 1f) {
                drawArc(
                    brush = Brush.linearGradient(colors = listOf(MacrofyGreen, CoachTeal)),
                    startAngle = -90f,
                    sweepAngle = ((animatedCal - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r1, center.y - r1),
                    size = Size(r1 * 2, r1 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Ring 2: Protein (Second)
        drawCircle(color = ProteinBlue.copy(alpha = trackAlpha), radius = r2, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedProt > 0f) {
            drawArc(
                color = ProteinBlue,
                startAngle = -90f,
                sweepAngle = (animatedProt * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r2, center.y - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            if (animatedProt > 1f) {
                drawArc(
                    color = ProteinBlue,
                    startAngle = -90f,
                    sweepAngle = ((animatedProt - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r2, center.y - r2),
                    size = Size(r2 * 2, r2 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Ring 3: Kohlenhydrate (Third)
        drawCircle(color = CarbOrange.copy(alpha = trackAlpha), radius = r3, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedCarb > 0f) {
            drawArc(
                color = CarbOrange,
                startAngle = -90f,
                sweepAngle = (animatedCarb * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r3, center.y - r3),
                size = Size(r3 * 2, r3 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            if (animatedCarb > 1f) {
                drawArc(
                    color = CarbOrange,
                    startAngle = -90f,
                    sweepAngle = ((animatedCarb - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r3, center.y - r3),
                    size = Size(r3 * 2, r3 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Ring 4: Fett (Innermost)
        drawCircle(color = FatPink.copy(alpha = trackAlpha), radius = r4, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedFat > 0f) {
            drawArc(
                color = FatPink,
                startAngle = -90f,
                sweepAngle = (animatedFat * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r4, center.y - r4),
                size = Size(r4 * 2, r4 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            if (animatedFat > 1f) {
                drawArc(
                    color = FatPink,
                    startAngle = -90f,
                    sweepAngle = ((animatedFat - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r4, center.y - r4),
                    size = Size(r4 * 2, r4 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 3.6 GOOGLE FIT DUAL ACTIVITY RINGS COMPOSABLE
// ----------------------------------------------------
@Composable
fun GoogleFitRings(
    stepsProgress: Float,
    heartPointsProgress: Float,
    modifier: Modifier = Modifier
) {
    val animatedSteps by animateFloatAsState(
        targetValue = stepsProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val animatedHP by animateFloatAsState(
        targetValue = heartPointsProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    Canvas(modifier = modifier) {
        val strokeWidthPx = 11.dp.toPx() // Google Fit-style thick paths
        val spacingPx = 3.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        val trackAlpha = 0.12f

        // Radii calculations for 2 thick concentric tracks
        val r1 = size.width / 2f - strokeWidthPx
        val r2 = r1 - strokeWidthPx - spacingPx - 2.dp.toPx()

        // Authentic Google Fit palette
        val fitBlue = Color(0xFF4285F4)
        val fitGreen = Color(0xFF0F9D58)

        // Ring 1 (Outer): Steps (Google Fit Blue)
        drawCircle(color = fitBlue.copy(alpha = trackAlpha), radius = r1, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedSteps > 0f) {
            drawArc(
                brush = Brush.linearGradient(colors = listOf(fitBlue, Color(0xFF00E5FF))),
                startAngle = -90f,
                sweepAngle = (animatedSteps * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r1, center.y - r1),
                size = Size(r1 * 2, r1 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            // Handle overflow past 100%
            if (animatedSteps > 1f) {
                drawArc(
                    brush = Brush.linearGradient(colors = listOf(fitBlue, Color(0xFF00E5FF))),
                    startAngle = -90f,
                    sweepAngle = ((animatedSteps - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r1, center.y - r1),
                    size = Size(r1 * 2, r1 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Ring 2 (Inner): Heart Points / Cardio (Google Fit Green)
        drawCircle(color = fitGreen.copy(alpha = trackAlpha), radius = r2, center = center, style = Stroke(width = strokeWidthPx))
        if (animatedHP > 0f) {
            drawArc(
                brush = Brush.linearGradient(colors = listOf(fitGreen, Color(0xFF00FFCC))),
                startAngle = -90f,
                sweepAngle = (animatedHP * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(center.x - r2, center.y - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            // Handle overflow past 100%
            if (animatedHP > 1f) {
                drawArc(
                    brush = Brush.linearGradient(colors = listOf(fitGreen, Color(0xFF00FFCC))),
                    startAngle = -90f,
                    sweepAngle = ((animatedHP - 1f) * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - r2, center.y - r2),
                    size = Size(r2 * 2, r2 * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 4. BENTO GRID DASHBOARD SCREEN
// ----------------------------------------------------
@Composable
fun DashboardHomeScreen(viewModel: MacrofyViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayFoods by viewModel.todayFoodEntries.collectAsStateWithLifecycle()
    val todayWater by viewModel.todayWaterLogs.collectAsStateWithLifecycle()
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val todayStepsGoal by viewModel.todayStepsGoal.collectAsStateWithLifecycle()
    val todayHeartPoints by viewModel.todayHeartPoints.collectAsStateWithLifecycle()
    val todayHeartPointsGoal by viewModel.todayHeartPointsGoal.collectAsStateWithLifecycle()
    val activeTrackerMode by viewModel.activeTrackerMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isPremium by viewModel.isPremiumSubscribed.collectAsStateWithLifecycle()
    val billingStatus by viewModel.billingPurchaseStatus.collectAsStateWithLifecycle()
    var weightInputVisible by remember { mutableStateOf(false) }
    var weightInputVal by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    if (showSubscriptionDialog) {
        PremiumPaywallDialog(
            viewModel = viewModel,
            onDismiss = { showSubscriptionDialog = false }
        )
    }

    LaunchedEffect(billingStatus) {
        billingStatus?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val profile = userProfile ?: return

    // Calculations for dashboard
    val totalCaloriesTarget = profile.calorieTarget
    val consumedCalories = todayFoods.sumOf { it.calories }
    val remainingCalories = (totalCaloriesTarget - consumedCalories).coerceAtLeast(0)

    val proteinTarget = profile.proteinTarget
    val proteinConsumed = todayFoods.sumOf { it.protein.toDouble() }.toFloat()

    val carbTarget = profile.carbTarget
    val carbConsumed = todayFoods.sumOf { it.carbs.toDouble() }.toFloat()

    val fatTarget = profile.fatTarget
    val fatConsumed = todayFoods.sumOf { it.fat.toDouble() }.toFloat()

    val waterConsumedMl = todayWater.sumOf { it.amountMl }



    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = ObsidianSurface,
            title = { Text("App zurücksetzen?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Möchtest du wirklich alle eingetragenen Mahlzeiten, Gewichtseinträge und deinen Fortschritt löschen und das Onboarding neu starten?", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        viewModel.restartJourney()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FatPink)
                ) {
                    Text("Zurücksetzen", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Abbrechen", color = TextGray)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // App bar top
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val greeting = remember {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    when {
                        hour in 5..11 -> "Guten Morgen"
                        hour in 12..17 -> "Hallo"
                        hour in 18..22 -> "Guten Abend"
                        else -> "Gute Nacht"
                    }
                }
                val userName = remember(loggedInUser) {
                    if (loggedInUser.isNullOrBlank() || loggedInUser == "null") "Fit-Champ"
                    else loggedInUser!!.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }

                Column {
                    Text("MACROFY AI HEALTH SUITE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoachTeal, letterSpacing = 1.2.sp)
                    Text("$greeting, $userName! 👋", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextWhite)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Streak badge
                    Row(
                        modifier = Modifier
                            .glassCard(borderColor = CarbOrange.copy(0.3f), backgroundColor = CarbOrange.copy(0.08f), cornerRadius = 16.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 $streakCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CarbOrange)
                    }
                }
            }
        }

        // PREMIUM GOOGLE PLAY MEMBERSHIP CARD (Stunning professional banner)
        item {
            val premiumCardBg = if (isPremium) {
                Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
            } else {
                Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))
            }
            val premiumCardBorder = if (isPremium) {
                Brush.linearGradient(colors = listOf(MacrofyGreen, CoachTeal))
            } else {
                Brush.linearGradient(colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clickable { showSubscriptionDialog = true }
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = false,
                        ambientColor = if (isPremium) MacrofyGreen.copy(0.15f) else Color(0xFFFBBF24).copy(0.15f),
                        spotColor = if (isPremium) MacrofyGreen.copy(0.25f) else Color(0xFFFBBF24).copy(0.25f)
                    )
                    .background(premiumCardBg, RoundedCornerShape(20.dp))
                    .border(1.5.dp, premiumCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isPremium) MacrofyGreen.copy(0.2f) else Color(0xFFFBBF24).copy(0.15f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isPremium) "👑" else "✨", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isPremium) "MACROFY PREMIUM AKTIV" else "ERHALTE MACROFY AI PRO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPremium) Color(0xFF60A5FA) else Color(0xFFFBBF24),
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                if (isPremium) "Google Play Abo: Aktiv • Alle Profile verknüpft" else "Unbegrenzter AI-Scanner, Coach & Cloud-Sync.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isPremium) MacrofyGreen.copy(0.3f) else Color(0xFFFBBF24).copy(0.2f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isPremium) MacrofyGreen else Color(0xFFFBBF24),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (isPremium) "Aktiv" else "Upgrade",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPremium) Color.White else Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }

        // BENTO BOX 1: Concentric Health Rings Card (wie health diese ringe / google fit)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(glowingHighlight = if (activeTrackerMode == "apple") MacrofyGreen else Color(0xFF4285F4))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (activeTrackerMode == "apple") "AKTIVITÄTS- & NÄHRSTOFFRINGE" else "GOOGLE FIT DASHBOARD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeTrackerMode == "apple") MacrofyGreen else Color(0xFF4285F4),
                                letterSpacing = 1.sp
                            )
                            Text("Tages-Fortschritt", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        // Mode switcher: Apple Health vs Google Fit
                        Row(
                            modifier = Modifier
                                .background(HeavyGlassSurface, RoundedCornerShape(10.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeTrackerMode == "apple") MacrofyGreen.copy(0.12f) else Color.Transparent)
                                    .clickable { viewModel.setTrackerMode("apple") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Apple Health",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTrackerMode == "apple") MacrofyGreen else TextGray
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeTrackerMode == "google") Color(0xFF4285F4).copy(0.12f) else Color.Transparent)
                                    .clickable { viewModel.setTrackerMode("google") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Google Fit",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTrackerMode == "google") Color(0xFF4285F4) else TextGray
                                )
                            }
                        }
                    }

                    if (activeTrackerMode == "apple") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Left Column: Concentric Apple Health-style activity rings
                            Box(
                                modifier = Modifier.size(136.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val ratioCal = if (totalCaloriesTarget > 0) consumedCalories.toFloat() / totalCaloriesTarget.toFloat() else 0f
                                val ratioProt = if (proteinTarget > 0) proteinConsumed / proteinTarget else 0f
                                val ratioCarb = if (carbTarget > 0) carbConsumed / carbTarget else 0f
                                val ratioFat = if (fatTarget > 0) fatConsumed / fatTarget else 0f

                                MacrofyHealthRings(
                                    caloriesProgress = ratioCal,
                                    proteinProgress = ratioProt,
                                    carbProgress = ratioCarb,
                                    fatProgress = ratioFat,
                                    modifier = Modifier.size(136.dp)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = remainingCalories.toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "kcal übrig",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Right Column: Polished legend/key for the concentric rings
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Calories Row
                                val calPct = if (totalCaloriesTarget > 0) (consumedCalories * 100 / totalCaloriesTarget) else 0
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(MacrofyGreen, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Kalorien", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        }
                                        Text("${calPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MacrofyGreen)
                                    }
                                    Text(
                                        text = "$consumedCalories / $totalCaloriesTarget kcal",
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }

                                androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Protein Row
                                val protPct = if (proteinTarget > 0) (proteinConsumed * 100 / proteinTarget).toInt() else 0
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(ProteinBlue, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Protein", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        }
                                        Text("${protPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ProteinBlue)
                                    }
                                    Text(
                                        text = "${proteinConsumed.toInt()}g / ${proteinTarget.toInt()}g",
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }

                                androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Carbs Row
                                val carbPct = if (carbTarget > 0) (carbConsumed * 100 / carbTarget).toInt() else 0
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(CarbOrange, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Kohlenhyd.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        }
                                        Text("${carbPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = CarbOrange)
                                    }
                                    Text(
                                        text = "${carbConsumed.toInt()}g / ${carbTarget.toInt()}g",
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }

                                androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                // Fat Row
                                val fatPct = if (fatTarget > 0) (fatConsumed * 100 / fatTarget).toInt() else 0
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(FatPink, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Fett", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        }
                                        Text("${fatPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = FatPink)
                                    }
                                    Text(
                                        text = "${fatConsumed.toInt()}g / ${fatTarget.toInt()}g",
                                        fontSize = 10.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Google Fit layout - concentric dual thick rings and statistics
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Double circular dynamic ring for Google Fit
                                Box(
                                    modifier = Modifier.size(136.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val rSteps = if (todayStepsGoal > 0) todaySteps.toFloat() / todayStepsGoal.toFloat() else 0f
                                    val rHP = if (todayHeartPointsGoal > 0) todayHeartPoints.toFloat() / todayHeartPointsGoal.toFloat() else 0f

                                    GoogleFitRings(
                                        stepsProgress = rSteps,
                                        heartPointsProgress = rHP,
                                        modifier = Modifier.size(136.dp)
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🏃",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = todaySteps.toString(),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Text(
                                            text = "Schritte",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4285F4)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // Right-side: Google Fit metrics and indicators
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Steps Row
                                    val stepsPct = if (todayStepsGoal > 0) (todaySteps * 100 / todayStepsGoal) else 0
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(Color(0xFF4285F4), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Schritte", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                            }
                                            Text("${stepsPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                                        }
                                        Text(
                                            text = "$todaySteps / $todayStepsGoal",
                                            fontSize = 10.sp,
                                            color = TextGray,
                                            modifier = Modifier.padding(start = 14.dp)
                                        )
                                    }

                                    androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                    // Heart Points Row
                                    val hpPct = if (todayHeartPointsGoal > 0) (todayHeartPoints * 100 / todayHeartPointsGoal) else 0
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(Color(0xFF0F9D58), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Herzpunkte", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                            }
                                            Text("${hpPct}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F9D58))
                                        }
                                        Text(
                                            text = "$todayHeartPoints / $todayHeartPointsGoal HP",
                                            fontSize = 10.sp,
                                            color = TextGray,
                                            modifier = Modifier.padding(start = 14.dp)
                                        )
                                    }

                                    androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                                    // Active cardio minutes estimated
                                    val activeMinutes = (todaySteps / 110).coerceAtLeast(0)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(CoachTeal, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Aktiv-Zeit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                            }
                                            Text("Aktiv", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoachTeal)
                                        }
                                        Text(
                                            text = "ca. $activeMinutes Min. Bewegung",
                                            fontSize = 10.sp,
                                            color = TextGray,
                                            modifier = Modifier.padding(start = 14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Walk & Heart points simulator buttons!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.addSteps(1500) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("+1.500 Schritte", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.addHeartPoints(5) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("+5 Herzpunkte", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.resetFitData() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Fit zurücksetzen",
                                        tint = TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // BENTO BOX 2: 3-column Bento grid for Protein, Carbs, Fats
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Protein box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassCard()
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(ProteinBlue, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Protein", fontSize = 12.sp, color = TextGray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${proteinConsumed.toInt()}g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(text = "von ${proteinTarget.toInt()}g", fontSize = 10.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = if (proteinTarget > 0) proteinConsumed / proteinTarget else 0f,
                            color = ProteinBlue,
                            trackColor = ObsidianBorder,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Carb box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassCard()
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(CarbOrange, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kohlenhyd.", fontSize = 12.sp, color = TextGray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${carbConsumed.toInt()}g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(text = "von ${carbTarget.toInt()}g", fontSize = 10.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = if (carbTarget > 0) carbConsumed / carbTarget else 0f,
                            color = CarbOrange,
                            trackColor = ObsidianBorder,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Fat box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassCard()
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(FatPink, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fett", fontSize = 12.sp, color = TextGray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${fatConsumed.toInt()}g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(text = "von ${fatTarget.toInt()}g", fontSize = 10.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = if (fatTarget > 0) fatConsumed / fatTarget else 0f,
                            color = FatPink,
                            trackColor = ObsidianBorder,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // BENTO BOX 3: Water Tracker & Weight Bento panel side by side
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Water Tracker
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .glassCard(glowingHighlight = CoachTeal)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Wassertracker", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(waterConsumedMl / 1000f)}L",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(text = "Tagesziel 3.0 L", fontSize = 11.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.addWaterLog(250) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CoachTeal.copy(0.12f),
                                        contentColor = CoachTeal
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+250ml", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                                
                                Button(
                                    onClick = { viewModel.addWaterLog(500) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CoachTeal.copy(0.12f),
                                        contentColor = CoachTeal
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+500ml", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }

                                if (waterConsumedMl > 0) {
                                    IconButton(
                                        onClick = { viewModel.removeLastWater() },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .glassCard(borderColor = FatPink.copy(0.2f), backgroundColor = FatPink.copy(0.05f))
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, "remove", tint = FatPink, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Beautiful dynamic fluid beaker
                        WaterBeaker(
                            progress = waterConsumedMl / 3000f,
                            modifier = Modifier.size(width = 50.dp, height = 84.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Weight card
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .glassCard()
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Körpergewicht", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                            Icon(Icons.Rounded.MonitorWeight, "Weight", tint = ProteinBlue, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${profile.weight} kg",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Text(text = "BMI (M3): ${String.format("%.1f", profile.bmi)}", fontSize = 11.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { viewModel.recordWeight((profile.weight - 0.5f).coerceAtLeast(30f)) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightGlassSurface, contentColor = ProteinBlue),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("-0.5", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { viewModel.recordWeight((profile.weight + 0.5f).coerceAtMost(250f)) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightGlassSurface, contentColor = ProteinBlue),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("+0.5", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            IconButton(
                                onClick = { weightInputVisible = !weightInputVisible },
                                modifier = Modifier.size(38.dp).glassCard(borderColor = ProteinBlue.copy(0.3f))
                            ) {
                                Icon(Icons.Rounded.Edit, "Edit", tint = ProteinBlue, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Weight Dialog trigger
        if (weightInputVisible) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(borderColor = ProteinBlue.copy(0.4f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightInputVal,
                            onValueChange = { weightInputVal = it },
                            label = { Text("Neues Gewicht in kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.3f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ProteinBlue,
                                unfocusedBorderColor = ObsidianBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parseWeight = weightInputVal.toFloatOrNull()
                                if (parseWeight != null) {
                                    viewModel.recordWeight(parseWeight)
                                    weightInputVisible = false
                                    weightInputVal = ""
                                }
                            },
                            modifier = Modifier.weight(0.7f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProteinBlue)
                        ) {
                            Text("Speichern", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // BENTO BOX 4: AI Assessment Advice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(borderColor = CoachTeal.copy(0.4f), backgroundColor = CoachTeal.copy(0.04f))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, "AI assessment", tint = CoachTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Tagesbewertung", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val assessmentText = if (consumedCalories == 0) {
                        "Du hast heute noch keine Lebensmittel eingetragen. Nutze den AI Scanner oder frage deinen AI Coach!"
                    } else if (consumedCalories < totalCaloriesTarget * 0.5) {
                        "Das Kaloriendefizit ist aktuell noch sehr hoch. Achte darauf, dein Proteinziel optimal zu füllen."
                    } else if (consumedCalories <= totalCaloriesTarget) {
                        "Perfekt im Plan! Du hältst dein optimales Makronährstoffverhältnis ein. Weiter so!"
                    } else {
                        "Du hast dein Tageskalorienziel überschritten. Konzentriere dich jetzt auf ausreichend Trinken und Regeneration."
                    }

                    Text(
                        assessmentText,
                        fontSize = 12.sp,
                        color = TextGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Recently Scanned Meals Row Title
        item {
            Text("Zuletzt verzehrte Mahlzeiten", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.padding(bottom = 10.dp))
        }

        if (todayFoods.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Fastfood, "Diary Empty", tint = TextGray, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Noch keine Mahlzeiten hinzugefügt", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
        } else {
            items(todayFoods, key = { it.id }) { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .glassCard()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                        val mealEmoji = remember(food.mealType) {
                            when (food.mealType.lowercase()) {
                                "frühstück" -> "🍳"
                                "mittagessen" -> "🍱"
                                "abendessen" -> "🍛"
                                else -> "🥝"
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(ObsidianSurface, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mealEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(food.name, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${food.portionGrams.toInt()}g • ", fontSize = 10.sp, color = TextGray)
                                Text("P: ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ProteinBlue)
                                Text("${food.protein.toInt()}g ", fontSize = 10.sp, color = TextGray)
                                Text("• K: ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CarbOrange)
                                Text("${food.carbs.toInt()}g ", fontSize = 10.sp, color = TextGray)
                                Text("• F: ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = FatPink)
                                Text("${food.fat.toInt()}g", fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${food.calories} kcal", fontWeight = FontWeight.Black, color = TextWhite, fontSize = 14.sp)
                            Text(food.mealType, fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { viewModel.deleteFood(food) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteForever, "delete meal", tint = FatPink, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. FOOD DIARY SCREEN (TAGEBUCH)
// ----------------------------------------------------
@Composable
fun FoodDiaryScreen(viewModel: MacrofyViewModel) {
    val todayFoods by viewModel.todayFoodEntries.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMealTypeForAdd by remember { mutableStateOf("Frühstück") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Feed Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ernährungstagebuch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CarbOrange, letterSpacing = 1.sp)
                Text("Deine Mahlzeiten", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CarbOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, "add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snack / Food", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary macros of the entries
        val calories = todayFoods.sumOf { it.calories }
        val protein = todayFoods.sumOf { it.protein.toDouble() }.toInt()
        val carbs = todayFoods.sumOf { it.carbs.toDouble() }.toInt()
        val fat = todayFoods.sumOf { it.fat.toDouble() }.toInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = CarbOrange.copy(0.3f), backgroundColor = CarbOrange.copy(0.04f))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$calories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Kalorien", fontSize = 11.sp, color = TextGray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${protein}g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ProteinBlue)
                    Text("Protein", fontSize = 11.sp, color = TextGray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${carbs}g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CarbOrange)
                    Text("Carbs", fontSize = 11.sp, color = TextGray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${fat}g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FatPink)
                    Text("Fett", fontSize = 11.sp, color = TextGray)
                }
            }
        }

        Text(
            "⚡ Quick-Add (Gesunde Favoriten)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
        )
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            val quickTemplates = listOf(
                com.example.ui.screens.FoodTemplate("Hähnchenbrust gegrillt", 165, 31f, 0f, 3.6f, 0f),
                com.example.ui.screens.FoodTemplate("Magerquark (M3)", 68, 12f, 4f, 0.2f, 4f),
                com.example.ui.screens.FoodTemplate("Banane frisch", 89, 1.1f, 22.8f, 0.3f, 12.2f),
                com.example.ui.screens.FoodTemplate("Protein Shake (Vanille)", 125, 25f, 3f, 1.5f, 1.5f),
                com.example.ui.screens.FoodTemplate("Gekochtes Ei", 155, 13f, 1.1f, 11f, 1.1f),
                com.example.ui.screens.FoodTemplate("Vollkornbrot (Scheibe)", 110, 4.5f, 21f, 1.2f, 1.5f),
                com.example.ui.screens.FoodTemplate("Naturreis gekocht", 130, 2.7f, 28f, 0.3f, 0.1f),
                com.example.ui.screens.FoodTemplate("Mandeln (Handvoll)", 162, 6f, 6f, 14f, 1.2f),
                com.example.ui.screens.FoodTemplate("Wildlachs Filet", 208, 20f, 0f, 13f, 0f)
            )

            items(quickTemplates, key = { it.name }) { template ->
                Box(
                    modifier = Modifier
                        .glassCard(borderColor = CarbOrange.copy(0.2f), backgroundColor = LightGlassSurface)
                        .clickable {
                            viewModel.addManualFood(
                                name = template.name,
                                calories = template.calories,
                                protein = template.protein,
                                carbs = template.carbs,
                                fat = template.fat,
                                sugar = template.sugar,
                                mealType = "Snack",
                                portionGrams = 100f
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(template.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("${template.calories} kcal • 100g", fontSize = 9.sp, color = TextGray)
                        Text("P: ${template.protein.toInt()}g | K: ${template.carbs.toInt()}g", fontSize = 9.sp, color = CarbOrange, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categorized LazyColumn list
        val mealCategories = listOf("Frühstück", "Mittagessen", "Abendessen", "Snack")

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(mealCategories, key = { it }) { category ->
                val mealsInCategory = todayFoods.filter { it.mealType == category }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        val totalKcalInCategory = mealsInCategory.sumOf { it.calories }
                        Text(
                            text = "$totalKcalInCategory kcal gesamt",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (mealsInCategory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassCard()
                                .clickable {
                                    selectedMealTypeForAdd = category
                                    showAddDialog = true
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Mahlzeit zu $category hinzufügen", fontSize = 12.sp, color = TextGray)
                        }
                    } else {
                        mealsInCategory.forEach { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .glassCard()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(food.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)
                                    Text(
                                        "${food.portionGrams.toInt()}g • P: ${food.protein.toInt()}g | C: ${food.carbs.toInt()}g | F: ${food.fat.toInt()}g",
                                        fontSize = 11.sp, color = TextGray
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${food.calories} kcal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteFood(food) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.DeleteForever, "Löschen", tint = FatPink, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Manual Add Food Dialog
    if (showAddDialog) {
        var foodName by remember { mutableStateOf("") }
        var foodKcal by remember { mutableStateOf("") }
        var foodProtein by remember { mutableStateOf("") }
        var foodCarb by remember { mutableStateOf("") }
        var foodFat by remember { mutableStateOf("") }
        var foodPortion by remember { mutableStateOf("100") }
        var foodSugar by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Mahlzeit manuell loggen", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Meal selection
                    Text("Kategorie des Verzehrs", fontSize = 11.sp, color = TextGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Frühstück", "Mittagessen", "Abendessen", "Snack").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassCard(
                                        borderColor = if (selectedMealTypeForAdd == type) CarbOrange else ObsidianBorder,
                                        backgroundColor = if (selectedMealTypeForAdd == type) CarbOrange.copy(0.12f) else LightGlassSurface
                                    )
                                    .clickable { selectedMealTypeForAdd = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type.take(4) + ".", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Gerichts-Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = foodKcal,
                            onValueChange = { foodKcal = it },
                            label = { Text("kcal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                        OutlinedTextField(
                            value = foodPortion,
                            onValueChange = { foodPortion = it },
                            label = { Text("Gramm") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = foodProtein,
                            onValueChange = { foodProtein = it },
                            label = { Text("Eiweiß g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                        OutlinedTextField(
                            value = foodCarb,
                            onValueChange = { foodCarb = it },
                            label = { Text("Carb g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = foodFat,
                            onValueChange = { foodFat = it },
                            label = { Text("Fett g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                        OutlinedTextField(
                            value = foodSugar,
                            onValueChange = { foodSugar = it },
                            label = { Text("Zucker g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedBorderColor = CarbOrange)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = foodName.ifEmpty { "Manuelles Food" }
                        val kcalVal = foodKcal.toIntOrNull() ?: 200
                        val protVal = foodProtein.toFloatOrNull() ?: 12f
                        val carbVal = foodCarb.toFloatOrNull() ?: 25f
                        val fatVal = foodFat.toFloatOrNull() ?: 5f
                        val sugVal = foodSugar.toFloatOrNull() ?: 0f
                        val portVal = foodPortion.toFloatOrNull() ?: 100f

                        viewModel.addManualFood(
                            name = nameStr,
                            calories = kcalVal,
                            protein = protVal,
                            carbs = carbVal,
                            fat = fatVal,
                            sugar = sugVal,
                            mealType = selectedMealTypeForAdd,
                            portionGrams = portVal
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbOrange)
                ) {
                    Text("Speichern", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Abbrechen", color = TextGray)
                }
            }
        )
    }
}

// ----------------------------------------------------
// 6. AI FOOD SCANNER SCREEN
// ----------------------------------------------------
@Composable
fun FoodScannerScreen(viewModel: MacrofyViewModel) {
    val scanBitmap by viewModel.scanBitmap.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val scannedHistory by viewModel.scannedHistory.collectAsStateWithLifecycle()

    var customPromptText by remember { mutableStateOf("") }
    var mealLogCategory by remember { mutableStateOf("Frühstück") }
    var adjustPortionText by remember { mutableStateOf("") }
    var selectedZoom by remember { mutableStateOf("1.0X") }
    var selectedScopeUnit by remember { mutableStateOf("Teller-Scan") }

    var activeHistoryDetail by remember { mutableStateOf<ScannedFood?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ScannedFood?>(null) }

    val context = LocalContext.current

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.setScanImage(bitmap)
            } catch (e: Exception) {
                // Sinks elegantly
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Scanner header
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Camera,
                        contentDescription = "Scanner",
                        tint = MacrofyGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI MULTI-VISION SCANNER v3.1", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MacrofyGreen, letterSpacing = 1.5.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Essen & Barcode Erfassung", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextWhite)
                Text("Halte die Kamera auf deine Mahlzeit oder die Nährwerttabelle einer Verpackung.", fontSize = 12.sp, color = TextGray)
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .glassCard(cornerRadius = 14.dp)
                    .padding(4.dp)
            ) {
                listOf("Teller-Scan", "Etikett-Scan").forEach { scope ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedScopeUnit == scope) MacrofyGreen.copy(0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selectedScopeUnit == scope) MacrofyGreen.copy(0.4f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedScopeUnit = scope }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (scope == "Teller-Scan") Icons.Rounded.Fastfood else Icons.Rounded.QrCodeScanner,
                                contentDescription = scope,
                                tint = if (selectedScopeUnit == scope) MacrofyGreen else TextGray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                scope,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedScopeUnit == scope) TextWhite else TextGray
                            )
                        }
                    }
                }
            }
        }

        // Camera Simulated Premium Viewport
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = if (isScanning) MacrofyGreen.copy(0.15f) else Color.Black.copy(0.04f),
                        spotColor = if (isScanning) MacrofyGreen.copy(0.25f) else Color.Black.copy(0.08f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        1.5.dp,
                        if (isScanning) MacrofyGreen.copy(0.8f) else Color(0xFF1E293B),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { selectImageLauncher.launch("image/*") }
            ) {
                // Interactive Camera grid drawing inside viewport (Rule of thirds)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing camera grid thirds (Thin gray lines)
                    drawLine(
                        color = Color.White.copy(0.15f),
                        start = Offset(w / 3f, 0f),
                        end = Offset(w / 3f, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(0.15f),
                        start = Offset(2 * w / 3f, 0f),
                        end = Offset(2 * w / 3f, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(0.15f),
                        start = Offset(0f, h / 3f),
                        end = Offset(w, h / 3f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(0.15f),
                        start = Offset(0f, 2 * h / 3f),
                        end = Offset(w, 2 * h / 3f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Camera viewfinder focus corner L-ticks
                    val len = 25.dp.toPx()
                    val thick = 3.dp.toPx()
                    val padding = 15.dp.toPx()
                    val tickColor = if (isScanning) MacrofyGreen else CarbOrange.copy(0.65f)

                    // Top-Left corner ticks
                    drawLine(tickColor, Offset(padding, padding), Offset(padding + len, padding), thick)
                    drawLine(tickColor, Offset(padding, padding), Offset(padding, padding + len), thick)

                    // Top-Right corner ticks
                    drawLine(tickColor, Offset(w - padding, padding), Offset(w - padding - len, padding), thick)
                    drawLine(tickColor, Offset(w - padding, padding), Offset(w - padding, padding + len), thick)

                    // Bottom-Left corner ticks
                    drawLine(tickColor, Offset(padding, h - padding), Offset(padding + len, h - padding), thick)
                    drawLine(tickColor, Offset(padding, h - padding), Offset(padding, h - padding - len), thick)

                    // Bottom-Right corner ticks
                    drawLine(tickColor, Offset(w - padding, h - padding), Offset(w - padding - len, h - padding), thick)
                    drawLine(tickColor, Offset(w - padding, h - padding), Offset(w - padding, h - padding - len), thick)
                }

                // Photographed Bitmap or default placeholder overlay
                if (scanBitmap != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = scanBitmap!!.asImageBitmap(),
                            contentDescription = "Gescannter Teller",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        // Subdued dark layer to overlay HUD indicators clearly
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.25f))
                        )
                    }
                }

                // Futuristic HUD Labels & Indicators
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top HUD row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flashing Pulsing Record Dot
                        val infinitePulse = rememberInfiniteTransition(label = "pulse")
                        val blinkAlpha by infinitePulse.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOut),
                                repeatMode = RepeatMode.Reverse
                            ), label = "record_blink"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = blinkAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI LENS MATCHING",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Code details HUD tag
                        Text(
                            text = if (selectedScopeUnit == "Teller-Scan") "FOOD_MODEL_V3" else "OCR_MATRICES_ACTIVE",
                            fontSize = 8.sp,
                            color = MacrofyGreen,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Centered focus point if no image loaded (Stunning illustration look)
                    if (scanBitmap == null) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(MacrofyGreen.copy(0.25f), Color.Transparent)
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedScopeUnit == "Teller-Scan") Icons.Rounded.CameraAlt else Icons.Rounded.QrCodeScanner,
                                    contentDescription = "Camera Tap",
                                    tint = MacrofyGreen,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.Black.copy(0.3f), CircleShape)
                                        .border(1.dp, Color.White.copy(0.12f), CircleShape)
                                        .padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedScopeUnit == "Teller-Scan") "Multi-Vision AI Teller-Scan" else "Barcode & Nährwert Scanner",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Kamera öffnen oder per Galerie hochladen",
                                fontSize = 10.sp,
                                color = Color.White.copy(0.6f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(0.15f), RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.White.copy(0.25f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.CloudUpload, "Cloud Info", tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto hochladen", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        // Small overlay indicating we have an active photo
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, "Loaded", tint = MacrofyGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Foto erfolgreich erfasst 📸 Tippe zum Ersetzen", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Bottom HUD row (Zoom and lens parameters simulator)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom simulator capsules
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(3.dp)
                        ) {
                            listOf("0.5X", "1.0X", "2.0X").forEach { zoom ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedZoom == zoom) MacrofyGreen else Color.Transparent)
                                        .clickable { selectedZoom = zoom }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        zoom,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Scale aspect ratios HUD tag
                        Text(
                            "SCALE: 4:3  |  ISO 100",
                            fontSize = 8.sp,
                            color = Color.White.copy(0.6f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Laser sweep animation logic
                if (isScanning) {
                    val infiniteTransition = rememberInfiniteTransition(label = "laser")
                    val offsetPercentage by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "laser_sweeper"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val posY = size.height * offsetPercentage
                        // Wide glow paint logic
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MacrofyGreen.copy(0.25f), Color.Transparent),
                                startY = posY - 25.dp.toPx(),
                                endY = posY + 25.dp.toPx()
                            ),
                            topLeft = Offset(0f, posY - 25.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width, 50.dp.toPx())
                        )
                        // Precise sharp line
                        drawLine(
                            color = MacrofyGreen,
                            start = Offset(0f, posY),
                            end = Offset(size.width, posY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }

        // Quick Preset Simulations
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Layers, "Presets", tint = TextGray, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mit Premium-Preset simulieren:", fontSize = 11.sp, color = TextGray)
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                PresetBadge("Avocado Lachs Bowl", "🥑") {
                    customPromptText = "Lachs Bowl mit Avocado"
                    viewModel.startFoodScan(customPromptText)
                }
                PresetBadge("Protein Porridge", "🥣") {
                    customPromptText = "Heidelbeer Haferflocken"
                    viewModel.startFoodScan(customPromptText)
                }
                PresetBadge("Verpackung Joghurt", "🥛") {
                    customPromptText = "Nährwerttabelle Griechischer Joghurt"
                    viewModel.startFoodScan(customPromptText)
                }
                PresetBadge("Zuckerfreier Riegel", "🍫") {
                    customPromptText = "Milcheiweiß Protein Bar"
                    viewModel.startFoodScan(customPromptText)
                }
            }
        }

        // Extra info & Analysis launch button
        item {
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = customPromptText,
                onValueChange = { customPromptText = it },
                label = { Text("Optionale Zusatz-Details (z.B. Zutaten, Soßen)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.startFoodScan(
                        customPromptText.ifEmpty {
                            if (selectedScopeUnit == "Etikett-Scan") "Analyze nutritional label packaging barcode" else "Analyze this food item"
                        }
                    )
                },
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, "Scan Logo", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Macrofy AI-Scan starten", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Active Instant scan result panels
        scanResult?.let { result ->
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️ AI-Analyse Ergebnisse", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextWhite)
                    TextButton(onClick = { viewModel.setScanImage(null) }) {
                        Text("Verwerfen", color = FatPink, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(borderColor = MacrofyGreen.copy(0.4f))
                        .padding(18.dp)
                ) {
                    Column {
                        // Title block with confidence
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(result.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = MacrofyGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Konfidenz: ${result.confidenceScore}% (Extrem hoch)",
                                        fontSize = 11.sp,
                                        color = MacrofyGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Nutritional label packaging badges
                            if (result.isPackaging) {
                                Row(
                                    modifier = Modifier
                                        .glassCard(borderColor = CarbOrange, backgroundColor = CarbOrange.copy(0.12f), cornerRadius = 12.dp)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Assessment, "Etikett", tint = CarbOrange, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verpackung", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CarbOrange)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macros Grid summary
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MacroStat("kcal", "${result.calories}", MacrofyGreen)
                            MacroStat("Protein", "${result.protein.toInt()}g", ProteinBlue)
                            MacroStat("Carbs", "${result.carbs.toInt()}g", CarbOrange)
                            MacroStat("Fett", "${result.fat.toInt()}g", FatPink)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ingredients list
                        if (result.detectedItems.isNotEmpty()) {
                            Text("Bestandteile & Inhaltsstoffe:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.detectedItems.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .background(LightGlassSurface, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(item, fontSize = 11.sp, color = TextGray)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Logging targets selectors
                        Text("In welches Tagebuch loggen?", fontSize = 11.sp, color = TextGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Frühstück", "Mittagessen", "Abendessen", "Snack").forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .glassCard(
                                            borderColor = if (mealLogCategory == type) MacrofyGreen else ObsidianBorder,
                                            backgroundColor = if (mealLogCategory == type) MacrofyGreen.copy(0.12f) else LightGlassSurface
                                        )
                                        .clickable { mealLogCategory = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Portions customizing
                        OutlinedTextField(
                            value = adjustPortionText,
                            onValueChange = { adjustPortionText = it },
                            placeholder = { Text("Standard Portion: ${result.portionGrams.toInt()}g") },
                            label = { Text("Portionsgröße anpassen (Gramm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = CoachTeal,
                                unfocusedBorderColor = ObsidianBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val customPortion = adjustPortionText.toFloatOrNull()
                                viewModel.addScannedFoodToDiary(mealLogCategory, customPortion)
                                adjustPortionText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Bestätigen & Zum Tagebuch hinzufügen", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // LOKALE SCANNED HISTORY DATABASE SECTION
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CloudDone, "Database", tint = CoachTeal, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Scanned-History (Lokale DB)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }

                if (scannedHistory.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearScannedHistory() }) {
                        Text("Verlauf löschen", color = FatPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (scannedHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = "Empty DB",
                            tint = TextGray.copy(0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noch keine Scans gespeichert",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Führe deinen ersten AI-Scan oben aus, um haargenaue Makros abzuspeichern.",
                            fontSize = 11.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Render historical items
            items(scannedHistory, key = { it.id }) { historical ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .glassCard(borderColor = ObsidianBorder)
                        .clickable { activeHistoryDetail = historical }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Saved thumbnail picture rendering
                            if (!historical.imagePath.isNullOrEmpty()) {
                                coil.compose.AsyncImage(
                                    model = historical.imagePath,
                                    contentDescription = historical.name,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(LightGlassSurface, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, ObsidianBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (historical.isPackaging) Icons.Rounded.Assessment else Icons.Rounded.Fastfood,
                                        contentDescription = "Default Thumbnail",
                                        tint = CoachTeal.copy(0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    historical.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(MacrofyGreen.copy(0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${historical.calories} kcal",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MacrofyGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "P:${historical.protein.toInt()} K:${historical.carbs.toInt()} F:${historical.fat.toInt()}",
                                        fontSize = 9.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }

                        // Actions: Log again & delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick-log pill
                            IconButton(
                                onClick = { activeHistoryDetail = historical },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Schnell loggen",
                                    tint = CoachTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            // Delete from history
                            IconButton(
                                onClick = { showDeleteConfirmDialog = historical },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Aus Verlauf löschen",
                                    tint = FatPink.copy(0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // Historical food detail viewer & re-logging modal dialog
    activeHistoryDetail?.let { sc ->
        var selectedMealType by remember { mutableStateOf("Frühstück") }
        var customizedWeightText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeHistoryDetail = null },
            containerColor = ObsidianSurface,
            modifier = Modifier.border(1.dp, CoachTeal.copy(0.5f), RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, "DB Scan", tint = CoachTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Details: ${sc.name}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Du hast diese Speise am ${SimpleDateFormat("dd.MM.yyyy 'um' HH:mm", Locale.GERMAN).format(Date(sc.timestamp))} gescannt.",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    // Large photo if available
                    if (!sc.imagePath.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = sc.imagePath,
                            contentDescription = sc.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MacroStat("kcal", "${sc.calories}", MacrofyGreen)
                        MacroStat("Prot", "${sc.protein.toInt()}g", ProteinBlue)
                        MacroStat("Carb", "${sc.carbs.toInt()}g", CarbOrange)
                        MacroStat("Fett", "${sc.fat.toInt()}g", FatPink)
                    }

                    // Log parameters
                    Text("Erneut loggen in:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Frühstück", "Mittagessen", "Abendessen", "Snack").forEach { meal ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassCard(
                                        borderColor = if (selectedMealType == meal) CoachTeal else ObsidianBorder,
                                        backgroundColor = if (selectedMealType == meal) CoachTeal.copy(0.12f) else LightGlassSurface,
                                        cornerRadius = 10.dp
                                    )
                                    .clickable { selectedMealType = meal }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(meal, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customizedWeightText,
                        onValueChange = { customizedWeightText = it },
                        placeholder = { Text("Standard: ${sc.portionGrams.toInt()}g") },
                        label = { Text("Portion anpassen (g)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = CoachTeal,
                            unfocusedBorderColor = ObsidianBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedWeight = customizedWeightText.toFloatOrNull()
                        val ratio = if (parsedWeight != null) parsedWeight / sc.portionGrams else 1f
                        viewModel.addManualFood(
                            name = sc.name,
                            calories = (sc.calories * ratio).toInt(),
                            protein = sc.protein * ratio,
                            carbs = sc.carbs * ratio,
                            fat = sc.fat * ratio,
                            sugar = sc.sugar * ratio,
                            mealType = selectedMealType,
                            portionGrams = parsedWeight ?: sc.portionGrams
                        )
                        activeHistoryDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White)
                ) {
                    Text("Zu Tagebuch hinzufügen", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeHistoryDetail = null }) {
                    Text("Schließen", color = TextGray)
                }
            }
        )
    }

    // Historical item single delete confirm dialog
    showDeleteConfirmDialog?.let { delFood ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = ObsidianSurface,
            modifier = Modifier.border(1.dp, FatPink.copy(0.4f), RoundedCornerShape(20.dp)),
            title = { Text("Scan löschen?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Möchtest du '${delFood.name}' wirklich dauerhaft aus deiner Scanned-Datenbank löschen?", color = TextGray, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScannedHistoryItem(delFood)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FatPink)
                ) {
                    Text("Löschen", fontWeight = FontWeight.Bold, color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Abbrechen", color = TextGray)
                }
            }
        )
    }
}

// Function helper load saved file image Bitmap
fun loadSavedBitmap(path: String?): Bitmap? {
    if (path == null) return null
    return try {
        BitmapFactory.decodeFile(path)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun PresetBadge(label: String, icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .glassCard()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon)
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}

@Composable
fun RowScope.MacroStat(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(LightGlassSurface, RoundedCornerShape(12.dp))
            .border(0.5.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = color)
            Text(label, fontSize = 10.sp, color = TextGray)
        }
    }
}

// Custom FlowRow mimicking standard wrapping
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Basic fallbacks layout row - simplifies compilation checks
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// Glow Orb element representation
@Composable
fun GlowOrb(color: Color, size: Dp, offset: Offset) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    Canvas(modifier = Modifier.size(size)) {
        val animatedSizePx = size.toPx() * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = offset,
                radius = animatedSizePx / 2f
            ),
            radius = animatedSizePx / 2f,
            center = offset
        )
    }
}

// ----------------------------------------------------
// 7. AI COACH CHAT SCREEN (CHAT WITH DYNAMIC DIALOGS)
// ----------------------------------------------------
@Composable
fun CoachChatScreen(viewModel: MacrofyViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInput by viewModel.chatInput.collectAsStateWithLifecycle()
    val isCoachResponding by viewModel.isCoachResponding.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Coach Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CoachTeal.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SmartToy, "Coach", tint = CoachTeal, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Macrofy AI Coach", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoachTeal, letterSpacing = 1.sp)
                    Text("Personal Trainer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .size(40.dp)
                    .glassCard()
            ) {
                Icon(Icons.Rounded.DeleteSweep, "Clear history", tint = FatPink, modifier = Modifier.size(18.dp))
            }
        }

        // Quick suggestions prompts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PromptChip("🚀 Was darf ich heute essen?") {
                viewModel.chatInput.value = "Was darf ich heute noch essen?"
                viewModel.sendChatMessage()
            }
            PromptChip("🍗 Mein Proteinziel füllen") {
                viewModel.chatInput.value = "Wie erreiche ich mein Protein Ziel?"
                viewModel.sendChatMessage()
            }
            PromptChip("📉 Abnehm-Tipps holen") {
                viewModel.chatInput.value = "Ist mein Tag gut zum Abnehmen?"
                viewModel.sendChatMessage()
            }
            PromptChip("📅 Ernährungsplan erstellen") {
                viewModel.chatInput.value = "Mach mir einen Ernährungsplan"
                viewModel.sendChatMessage()
            }
        }

        // Chat message bubbles lazy list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isAssistant = msg.sender == "assistant"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .glassCard(
                                    borderColor = if (isAssistant) CoachTeal.copy(0.3f) else ObsidianBorder,
                                    backgroundColor = if (isAssistant) CoachTeal.copy(0.04f) else LightGlassSurface,
                                    cornerRadius = 20.dp
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isAssistant) "Macrofy Coach" else "Ich",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAssistant) CoachTeal else MacrofyGreen,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isCoachResponding) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .glassCard(borderColor = CoachTeal.copy(0.2f))
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = CoachTeal,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Coach überlegt...", fontSize = 11.sp, color = TextGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chat input container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.chatInput.value = it },
                placeholder = { Text("Frage den Coach...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CoachTeal,
                    unfocusedBorderColor = ObsidianBorder
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { viewModel.sendChatMessage() },
                modifier = Modifier
                    .size(56.dp)
                    .glassCard(borderColor = CoachTeal.copy(0.5f), backgroundColor = CoachTeal.copy(0.12f))
                    .testTag("send_chat_button"),
            ) {
                Icon(Icons.Filled.Send, "Senden", tint = CoachTeal)
            }
        }
    }
}

@Composable
fun PromptChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .glassCard()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
    }
}

// ----------------------------------------------------
// 8. CUSTOM INDIVIDUAL PROFILE & HEALTH CENTER
// ----------------------------------------------------
@Composable
fun UserProfileScreen(viewModel: MacrofyViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val todayFoods by viewModel.todayFoodEntries.collectAsStateWithLifecycle()
    val allFoods by viewModel.allFoodEntries.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumSubscribed.collectAsStateWithLifecycle()

    val profile = userProfile ?: return

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(loggedInUser ?: "User") }

    // Edit state loaded on demand from current profile
    var editGender by remember { mutableStateOf(profile.gender) }
    var editAge by remember { mutableStateOf(profile.age.toString()) }
    var editHeight by remember { mutableStateOf(profile.height.toInt().toString()) }
    var editWeight by remember { mutableStateOf(profile.weight.toInt().toString()) }
    var editGoal by remember { mutableStateOf(profile.goal) }
    var editActivity by remember { mutableStateOf(profile.activityLevel) }
    var editPace by remember { mutableStateOf(profile.pace) }

    var weightInputVisible by remember { mutableStateOf(false) }
    var weightInputVal by remember { mutableStateOf("") }

    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("macrofy_prefs", android.content.Context.MODE_PRIVATE) }
    val username = loggedInUser ?: "LokalUser"

    // Diet strategy state (Ausgewogen, Low-Carb, High-Protein, Ketogen)
    var selectedDietStrategy by remember(username) {
        mutableStateOf(prefs.getString("diet_strategy_$username", "Ausgewogen") ?: "Ausgewogen")
    }

    // Target Weight (Zielgewicht)
    var targetWeightInput by remember(username) {
        val defaultTarget = if (profile.goal == "Abnehmen") (profile.weight - 5f).coerceAtLeast(40f) else (profile.weight + 5f)
        mutableStateOf(prefs.getFloat("target_weight_$username", defaultTarget).toString())
    }

    // Water level tracker
    val todayStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    var waterIntake by remember(username, todayStr) {
        mutableStateOf(prefs.getInt("water_intake_${username}_$todayStr", 0))
    }
    val waterGoal = remember { 2500 } // standard 2.5 Liters

    val targetKcal = profile.calorieTarget
    val targetProtein = profile.proteinTarget.toInt()
    val targetCarbs = profile.carbTarget.toInt()
    val targetFat = profile.fatTarget.toInt()

    val caloriesHistory = remember(allFoods, targetKcal) {
        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
        val last7DaysMap = java.util.LinkedHashMap<String, Int>()
        for (i in 6 downTo 0) {
            val dCal = Calendar.getInstance()
            dCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStr = sdf.format(dCal.time)
            last7DaysMap[dayStr] = 0
        }
        allFoods.forEach { food ->
            val dayStr = sdf.format(Date(food.timestamp))
            if (last7DaysMap.containsKey(dayStr)) {
                last7DaysMap[dayStr] = (last7DaysMap[dayStr] ?: 0) + food.calories
            }
        }
        last7DaysMap.toList()
    }

    // 1. DYNAMIC HEALTH SCORE MATHEMATICS
    val healthScore = remember(weightLogs, todayFoods, profile) {
        var score = 30 // base
        if (weightLogs.size >= 2) score += 20
        else if (weightLogs.isNotEmpty()) score += 10
        if (todayFoods.isNotEmpty()) score += 25
        if (profile.activityLevel == "sehr aktiv") score += 25
        else if (profile.activityLevel == "aktiv") score += 20
        else if (profile.activityLevel == "leicht aktiv") score += 15
        else score += 5
        if (profile.bmi in 18.5f..25.0f) score += 20
        else score += 10
        score.coerceIn(0, 100)
    }

    // 2. MILESTONES & ACHIEVEMENTS DATA
    val hasThreeMeals = remember(allFoods) {
        allFoods.groupBy {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.timestamp))
        }.size >= 3
    }
    val hasProteinChamp = remember(todayFoods) {
        todayFoods.sumOf { it.protein.toDouble() } >= 100.0
    }
    val hasWeightWatcher = remember(weightLogs) { weightLogs.size >= 2 }
    val hasScannerPro = remember(allFoods) { allFoods.any { it.isPackaging } }

    val badges = listOf(
        Triple("Tagebuch-Pionier 📔", "Mind. 3 Tage geloggt", hasThreeMeals),
        Triple("Protein-König 🍗", ">100g Protein heute", hasProteinChamp),
        Triple("Gewichts-Wächter ⚖️", "Mind. 2 Messungen", hasWeightWatcher),
        Triple("Scanmaster 📸", "Barcode-Scanner genutzt", hasScannerPro)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Header
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
            Text("MACROFY PROFILE & HEALTH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoachTeal, letterSpacing = 1.2.sp)
            Text("Mein Gesundheitsprofil 👤", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
        }

        // Account card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = ObsidianBorder)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ProteinBlue.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (loggedInUser ?: "U").take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ProteinBlue,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    val name = loggedInUser?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "User"
                    Text(name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextWhite)
                    Text(if (isPremium) "👑 Premium Mitglied (Aktiv)" else "Kostenloser App-Dutzungsverband", fontSize = 11.sp, color = TextGray)
                }
            }
        }

        // CLINICAL HEALTH SCORE WIDGET
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = MacrofyGreen.copy(0.2f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(70.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.LightGray.copy(0.12f),
                            style = Stroke(width = 6.dp.toPx())
                        )
                        drawArc(
                            color = if (healthScore > 80) MacrofyGreen else if (healthScore > 50) CarbOrange else Color.Red,
                            startAngle = -90f,
                            sweepAngle = (healthScore / 100f * 360f),
                            useCenter = false,
                            style = Stroke(
                                width = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$healthScore", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        Text("Score", fontSize = 9.sp, color = TextGray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            healthScore >= 80 -> "Hervorragender Fitness-Status! 🌟"
                            healthScore >= 60 -> "Guter Weg — bleib dran! 💪"
                            else -> "Ausbaufähig — leg jetzt los! 🔥"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dein Score basiert auf täglichem Essen tracken, regelmäßigen Gewichtsmessungen und deinem gesunden BMI.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }
        }

        // Profile details and editor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = CoachTeal.copy(0.2f))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gesundheitsdaten & Ziele", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    TextButton(
                        onClick = {
                            if (!isEditing) {
                                editName = loggedInUser ?: "User"
                                editGender = profile.gender
                                editAge = profile.age.toString()
                                editHeight = profile.height.toInt().toString()
                                editWeight = profile.weight.toInt().toString()
                                editGoal = profile.goal
                                editActivity = profile.activityLevel
                                editPace = profile.pace
                            }
                            isEditing = !isEditing
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (isEditing) "Abbrechen" else "Bearbeiten ⚙️", color = CoachTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMiniBadge("Geschlecht", profile.gender, Modifier.weight(1f))
                            StatMiniBadge("Alter", "${profile.age} Jahre", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMiniBadge("Größe", "${profile.height.toInt()} cm", Modifier.weight(1f))
                            StatMiniBadge("Gewicht", "${profile.weight} kg", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMiniBadge("Hauptziel", profile.goal, Modifier.weight(1f))
                            StatMiniBadge("Aktivität", profile.activityLevel, Modifier.weight(1f))
                        }

                        androidx.compose.material3.HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMiniBadge("BMR (Umsatz)", "${profile.bmr.toInt()} kcal", Modifier.weight(1f), labelColor = MacrofyGreen)
                            val bmiCategory = when {
                                profile.bmi < 18.5f -> "Untergewicht"
                                profile.bmi < 25f -> "Normalgewicht"
                                profile.bmi < 30f -> "Übergewicht"
                                else -> "Adipositas"
                            }
                            StatMiniBadge("BMI", "${String.format(Locale.US, "%.1f", profile.bmi)} ($bmiCategory)", Modifier.weight(1f), labelColor = ProteinBlue)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // ACTIVE PREMIUM DIET STRATEGY INDICATOR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, ObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.VerifiedUser, "Strategy", tint = MacrofyGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Deine aktive Diät-Ausrichtung", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = when (selectedDietStrategy) {
                                        "Low-Carb" -> "Protein- & Low-Carb-Modus 🥩 (30% Protein, 35% Fett, 35% Carbs)"
                                        "High-Protein" -> "Maximaler Muskelaufbautyp 🍗 (40% Protein, 25% Fett, 35% Carbs)"
                                        "Ketogen" -> "Ketogene Fettverbrennung 🥑 (15% Protein, 75% Fett, 10% Carbs)"
                                        else -> "Ausgewogene Ernährungsbilanz 🥗 (Maser-Formel Standard)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Möchtest du deine Körperdaten ändern? Kalorien & Makros berechnen sich instant neu.", fontSize = 11.sp, color = TextGray)

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Name", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoachTeal)
                        )

                        Column {
                            Text("Geschlecht", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { editGender = "männlich" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editGender == "männlich") CoachTeal else ObsidianBorder,
                                        contentColor = if (editGender == "männlich") Color.White else TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("Männlich", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { editGender = "weiblich" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (editGender == "weiblich") CoachTeal else ObsidianBorder,
                                        contentColor = if (editGender == "weiblich") Color.White else TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("Weiblich", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { editAge = it },
                                label = { Text("Alter", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoachTeal)
                            )
                            OutlinedTextField(
                                value = editHeight,
                                onValueChange = { editHeight = it },
                                label = { Text("Größe (cm)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoachTeal)
                            )
                            OutlinedTextField(
                                value = editWeight,
                                onValueChange = { editWeight = it },
                                label = { Text("Gewicht (kg)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoachTeal)
                            )
                        }

                        // DIET STRATEGY CONFIGURATION CHIPS
                        Column {
                            Text("Diät-Ausrichtung / Makro-Verteilung", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val strategies = listOf("Ausgewogen", "Low-Carb", "High-Protein", "Ketogen")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                strategies.forEach { strat ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (selectedDietStrategy == strat) MacrofyGreen.copy(0.12f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (selectedDietStrategy == strat) MacrofyGreen else ObsidianBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedDietStrategy = strat }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(strat, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (selectedDietStrategy == strat) MacrofyGreen else TextWhite)
                                    }
                                }
                            }
                        }

                        // TARGET WEIGHT SELECTION
                        OutlinedTextField(
                            value = targetWeightInput,
                            onValueChange = { targetWeightInput = it },
                            label = { Text("Zielgewicht (kg)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoachTeal)
                        )

                        Column {
                            Text("Deine Zielsetzung", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val goals = listOf("Abnehmen", "Muskelaufbau", "Gewicht halten")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                goals.forEach { g ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (editGoal == g) CoachTeal.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (editGoal == g) CoachTeal else ObsidianBorder, RoundedCornerShape(8.dp))
                                            .clickable { editGoal = g }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(g, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (editGoal == g) CoachTeal else TextWhite)
                                    }
                                }
                            }
                        }

                        Column {
                            Text("Aktivitätslevel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val activities = listOf("wenig aktiv", "leicht aktiv", "aktiv", "sehr aktiv")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                activities.forEach { act ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (editActivity == act) CoachTeal.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (editActivity == act) CoachTeal else ObsidianBorder, RoundedCornerShape(8.dp))
                                            .clickable { editActivity = act }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(act, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (editActivity == act) CoachTeal else TextWhite)
                                    }
                                }
                            }
                        }

                        Column {
                            Text("Tempo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val paces = listOf("Einfach", "Normal", "Schnell")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                paces.forEach { pc ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (editPace == pc) CoachTeal.copy(0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (editPace == pc) CoachTeal else ObsidianBorder, RoundedCornerShape(8.dp))
                                            .clickable { editPace = pc }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(pc, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (editPace == pc) CoachTeal else TextWhite)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val ageVal = editAge.toIntOrNull() ?: profile.age
                                val heightVal = editHeight.toFloatOrNull() ?: profile.height
                                val weightVal = editWeight.toFloatOrNull() ?: profile.weight
                                val targetWeightVal = targetWeightInput.toFloatOrNull() ?: (weightVal - 5f)

                                // Preserve strategies in preferences
                                prefs.edit()
                                    .putString("diet_strategy_$username", selectedDietStrategy)
                                    .putFloat("target_weight_$username", targetWeightVal)
                                    .apply()

                                viewModel.saveProfileEdit(
                                    name = editName,
                                    gender = editGender,
                                    age = ageVal,
                                    height = heightVal,
                                    weight = weightVal,
                                    goal = editGoal,
                                    activityLevel = editActivity,
                                    pace = editPace
                                )
                                isEditing = false
                                android.widget.Toast.makeText(context, "Profil erfolgreich aktualisiert!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoachTeal, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Speichern & Ziele anpassen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // DAILY ACHIEVEMENT BADGES SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = ObsidianBorder)
                .padding(16.dp)
        ) {
            Column {
                Text("Meilensteine & Abzeichen 🏆", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                Text("Beweise deine Disziplin", fontSize = 11.sp, color = TextGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.take(2).forEach { (title, desc, unlocked) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassCard(
                                    borderColor = if (unlocked) MacrofyGreen.copy(0.4f) else ObsidianBorder,
                                    backgroundColor = if (unlocked) MacrofyGreen.copy(0.06f) else Color.Transparent
                                )
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) TextWhite else TextWhite.copy(0.4f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 9.sp,
                                    color = if (unlocked) MacrofyGreen else TextGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (unlocked) "Freigeschaltet 🎉" else "Gesperrt 🔒",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) MacrofyGreen else TextGray
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.drop(2).forEach { (title, desc, unlocked) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .glassCard(
                                    borderColor = if (unlocked) MacrofyGreen.copy(0.4f) else ObsidianBorder,
                                    backgroundColor = if (unlocked) MacrofyGreen.copy(0.06f) else Color.Transparent
                                )
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) TextWhite else TextWhite.copy(0.4f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    fontSize = 9.sp,
                                    color = if (unlocked) MacrofyGreen else TextGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (unlocked) "Freigeschaltet 🎉" else "Gesperrt 🔒",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) MacrofyGreen else TextGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Weight trends splined graph (Yazio Style!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = ProteinBlue.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gewichtsverlauf (Trend)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text("Historie deiner Gewichtsmessungen", fontSize = 11.sp, color = TextGray)
                    }
                    Button(
                        onClick = { weightInputVisible = !weightInputVisible },
                        colors = ButtonDefaults.buttonColors(containerColor = ProteinBlue.copy(0.1f), contentColor = ProteinBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+ Neu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (weightInputVisible) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInputVal,
                            onValueChange = { weightInputVal = it },
                            placeholder = { Text("z.B. 78.5", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProteinBlue),
                            modifier = Modifier.weight(1f).height(46.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                        Button(
                            onClick = {
                                val wFloat = weightInputVal.replace(",", ".").toFloatOrNull()
                                if (wFloat != null && wFloat > 30f) {
                                    viewModel.recordWeight(wFloat)
                                    weightInputVal = ""
                                    weightInputVisible = false
                                    android.widget.Toast.makeText(context, "Gewicht geloggt!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Fehlerhafter Wert!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProteinBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Sichern", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                ) {
                    if (weightLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Noch kein Verlauf", fontSize = 11.sp, color = TextGray)
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val points = weightLogs.takeLast(7)
                            val sizeW = size.width
                            val sizeH = size.height

                            val minW = points.minOf { it.weight }
                            val maxW = points.maxOf { it.weight }
                            val rangeW = (maxW - minW).coerceAtLeast(1f)

                            val spacingX = sizeW / (points.size - 1).coerceAtLeast(1)

                            // Background elegant grid guidelines
                            listOf(0.25f, 0.75f).forEach { scale ->
                                val gridY = sizeH * scale
                                drawLine(
                                    color = ObsidianBorder,
                                    start = Offset(0f, gridY),
                                    end = Offset(sizeW, gridY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            val coords = points.mapIndexed { index, weightLog ->
                                val x = index * spacingX
                                val normY = (weightLog.weight - minW) / rangeW
                                val y = sizeH - (normY * sizeH * 0.65f) - (sizeH * 0.18f)
                                Offset(x, y)
                            }

                            if (coords.isNotEmpty()) {
                                val path = Path()
                                val fillPath = Path()

                                path.moveTo(coords[0].x, coords[0].y)
                                fillPath.moveTo(coords[0].x, sizeH)
                                fillPath.lineTo(coords[0].x, coords[0].y)

                                for (i in 1 until coords.size) {
                                    val curr = coords[i]
                                    val prev = coords[i - 1]
                                    val ctrlX1 = prev.x + (curr.x - prev.x) / 2f
                                    val ctrlY1 = prev.y
                                    val ctrlX2 = prev.x + (curr.x - prev.x) / 2f
                                    val ctrlY2 = curr.y

                                    path.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, curr.x, curr.y)
                                    fillPath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, curr.x, curr.y)
                                }

                                if (coords.size > 1) {
                                    fillPath.lineTo(coords.last().x, sizeH)
                                    fillPath.close()

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                ProteinBlue.copy(alpha = 0.15f),
                                                ProteinBlue.copy(alpha = 0.01f),
                                                Color.Transparent
                                             )
                                         )
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = ProteinBlue,
                                    style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
                                )

                                coords.forEach { pt ->
                                    drawCircle(
                                        color = ProteinBlue.copy(alpha = 0.25f),
                                        radius = 5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weightLogs.takeLast(5).forEach { log ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val form = SimpleDateFormat("dd.MM", Locale.getDefault())
                            Text(form.format(Date(log.timestamp)), fontSize = 10.sp, color = TextGray)
                            Text("${log.weight}kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }

                // WEIGHT GOAL PROGRESS ROAD MAP
                Spacer(modifier = Modifier.height(14.dp))
                val currentWeightVal = profile.weight
                val startWeightVal = remember(weightLogs, currentWeightVal) {
                    weightLogs.firstOrNull()?.weight ?: (currentWeightVal + (if (profile.goal == "Abnehmen") 4.0f else -4.0f))
                }
                val targetWeightVal = prefs.getFloat("target_weight_$username", (currentWeightVal - 5f).coerceAtLeast(40f))

                val progressPctInWeight = remember(startWeightVal, currentWeightVal, targetWeightVal) {
                    val denom = startWeightVal - targetWeightVal
                    if (denom == 0f) 100
                    else {
                        val ratio = (startWeightVal - currentWeightVal) / denom
                        (ratio.coerceIn(0f, 1f) * 100).toInt()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.12f), RoundedCornerShape(12.dp))
                        .border(1.dp, ObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gewichts-Meilenstein Fortschritt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("$progressPctInWeight% geschafft", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProteinBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.White.copy(0.08f), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (progressPctInWeight / 100f).coerceIn(0f, 1f))
                                .background(ProteinBlue, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Start: ${String.format(Locale.US, "%.1f", startWeightVal)} kg", fontSize = 9.sp, color = TextGray)
                        Text("Aktuell: ${String.format(Locale.US, "%.1f", currentWeightVal)} kg", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Ziel: ${String.format(Locale.US, "%.1f", targetWeightVal)} kg", fontSize = 9.sp, color = TextGray)
                    }

                    val neededDiff = Math.abs(currentWeightVal - targetWeightVal)
                    if (neededDiff > 0.1f) {
                        Text(
                            text = "Noch ${String.format(Locale.US, "%.1f", neededDiff)} kg bis zu deinem Traumgewicht! 🚀",
                            fontSize = 10.sp,
                            color = MacrofyGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Zielgewicht erreicht! Absolute Spitzenklasse! 🎉",
                            fontSize = 10.sp,
                            color = MacrofyGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // HYDRATION TRACER (WATER GLASS TRACKER)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = ProteinBlue.copy(0.2f))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Wasser-Tracker 💧", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text("Trinke für optimale metabolische Gesundheit", fontSize = 11.sp, color = TextGray)
                    }
                    Text(
                        "$waterIntake / $waterGoal ml",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (i in 1..8) {
                        val glassCapacity = 250
                        val isDrunk = waterIntake >= (i * glassCapacity)
                        val interactionColor = if (isDrunk) Color(0xFF0284C7) else Color.White.copy(0.1f)

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(interactionColor, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isDrunk) Color(0xFF38BDF8) else ObsidianBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isDrunk) {
                                        waterIntake = ((i - 1) * glassCapacity).coerceAtLeast(0)
                                    } else {
                                        waterIntake = (i * glassCapacity)
                                    }
                                    prefs.edit().putInt("water_intake_${username}_$todayStr", waterIntake).apply()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isDrunk) "💧" else "🥤", fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (waterIntake >= waterGoal) "Optimal hydriert! Hervorragend! 🎉" else "Tippe auf die Becher, um je 250ml zu loggen.",
                        fontSize = 10.sp,
                        color = if (waterIntake >= waterGoal) MacrofyGreen else TextGray,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = {
                            waterIntake = 0
                            prefs.edit().putInt("water_intake_${username}_$todayStr", waterIntake).apply()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reset", color = Color.Red.copy(0.7f), fontSize = 10.sp)
                    }
                }
            }
        }

        // Nutrition performance summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(borderColor = MacrofyGreen.copy(0.2f))
                .padding(16.dp)
        ) {
            Column {
                Text("Nährstoff-Makros heute", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                Text("Deine Erfüllungsquoten im Detail", fontSize = 11.sp, color = TextGray)

                Spacer(modifier = Modifier.height(14.dp))

                val caloriesVal = todayFoods.sumOf { it.calories }
                val proteinVal = todayFoods.sumOf { it.protein.toDouble() }.toFloat()
                val carbVal = todayFoods.sumOf { it.carbs.toDouble() }.toFloat()
                val fatVal = todayFoods.sumOf { it.fat.toDouble() }.toFloat()

                MacroUsagePercentBar("Energie", caloriesVal, targetKcal, MacrofyGreen)
                Spacer(modifier = Modifier.height(8.dp))
                MacroUsagePercentBar("Protein", proteinVal.toInt(), targetProtein, ProteinBlue)
                Spacer(modifier = Modifier.height(8.dp))
                MacroUsagePercentBar("Kohlenhydrate", carbVal.toInt(), targetCarbs, CarbOrange)
                Spacer(modifier = Modifier.height(8.dp))
                MacroUsagePercentBar("Fett", fatVal.toInt(), targetFat, FatPink)
            }
        }

        // Data Reset Section
        var showResetDialog by remember { mutableStateOf(false) }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Alle Daten löschen?", fontWeight = FontWeight.Bold, color = TextWhite) },
                text = { Text("Möchtest du wirklich alle eingetragenen Mahlzeiten, Gewichtseinträge und dein Profil unwiderruflich von diesem Gerät löschen?", color = TextGray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
                            viewModel.restartJourney()
                        }
                    ) {
                        Text("Ja, löschen", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Abbrechen", color = TextWhite)
                    }
                },
                containerColor = ObsidianSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(0.08f),
                contentColor = Color.Red
            ),
            border = BorderStroke(1.dp, Color.Red.copy(0.12f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_data_button")
        ) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = "Reset Data", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alle Daten zurücksetzen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatMiniBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = TextGray
) {
    Box(
        modifier = modifier
            .background(ObsidianDarkBg, RoundedCornerShape(10.dp))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}

@Composable
fun MacroUsagePercentBar(label: String, consumed: Int, target: Int, color: Color) {
    val percent = if (target > 0) (consumed.toFloat() / target.toFloat() * 100).toInt() else 0
    val ratioVal = if (target > 0) consumed.toFloat() / target.toFloat() else 0f
    
    val animatedRatio by animateFloatAsState(
        targetValue = ratioVal.coerceIn(0f, 1.5f), 
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "RatioAnimation"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Text("$consumed / $target ($percent%)", fontSize = 11.sp, color = TextGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(ObsidianBorder, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio.coerceAtMost(1.0f))
                    .height(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MacrofyViewModel,
    onNavigateBack: () -> Unit,
    onSuccessLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(MacrofyGreen, CoachTeal)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Login Key",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Willkommen zurück",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
            Text(
                text = "Melde dich an, um dein Makro-Tagebuch fortzuführen.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; isError = null },
                label = { Text("Benutzername") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedLabelColor = MacrofyGreen,
                    unfocusedLabelColor = TextGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; isError = null },
                label = { Text("Passwort") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedLabelColor = MacrofyGreen,
                    unfocusedLabelColor = TextGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                singleLine = true
            )

            if (isError != null) {
                Text(
                    text = isError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (successMsg != null) {
                Text(
                    text = successMsg!!,
                    color = MacrofyGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val result = viewModel.logIn(username, password)
                    if (result.first) {
                        successMsg = result.second
                        isError = null
                        onSuccessLogin()
                    } else {
                        isError = result.second
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("login_submit_button")
            ) {
                Text("Anmelden", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Noch kein Konto? Registrieren", color = CoachTeal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }


        }

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .size(48.dp)
                .glassCard()
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MacrofyViewModel,
    onNavigateBack: () -> Unit,
    onSuccessRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(MacrofyGreen, CoachTeal)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAdd,
                    contentDescription = "Register Icon",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Konto erstellen",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
            Text(
                text = "Melde dich an, um deine Ernährung präzise zu kalkulieren.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; isError = null },
                label = { Text("Benutzername") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedLabelColor = MacrofyGreen,
                    unfocusedLabelColor = TextGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; isError = null },
                label = { Text("E-Mail-Adresse") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedLabelColor = MacrofyGreen,
                    unfocusedLabelColor = TextGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; isError = null },
                label = { Text("Passwort") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = MacrofyGreen,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedLabelColor = MacrofyGreen,
                    unfocusedLabelColor = TextGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                singleLine = true
            )

            if (isError != null) {
                Text(
                    text = isError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (successMsg != null) {
                Text(
                    text = successMsg!!,
                    color = MacrofyGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val result = viewModel.signUp(username, email, password)
                    if (result.first) {
                        successMsg = result.second
                        isError = null
                        onSuccessRegister()
                    } else {
                        isError = result.second
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("register_submit_button")
            ) {
                Text("Registrieren & Weiter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Bereits ein Konto? Anmelden", color = CoachTeal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .size(48.dp)
                .glassCard()
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextWhite)
        }
    }
}

@Composable
fun CalculationResultScreen(
    viewModel: MacrofyViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val profile by viewModel.latestCalculatedProfile.collectAsStateWithLifecycle()
    val isExplanationLoading by viewModel.isCalculatingExplanation.collectAsStateWithLifecycle()
    val explanationText by viewModel.latestCalculationExplanation.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Action Box
            Box(
                modifier = Modifier
                    .glassCard(borderColor = MacrofyGreen.copy(0.3f), backgroundColor = MacrofyGreen.copy(0.08f), cornerRadius = 16.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🤖 KI ERNÄHRUNGSPLANKALKULATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MacrofyGreen,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dein optimaler Plan steht!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Text(
                text = "Die Macrofy AI hat deine biologischen Faktoren berechnet und deinen tagesgenauen Kalorien- und Makronährstoffbedarf ermittelt.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Calories Card
            if (profile != null) {
                val p = profile!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(glowingHighlight = MacrofyGreen)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEIN KALORIENZIEL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoachTeal,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${p.calorieTarget}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            lineHeight = 54.sp
                        )
                        Text(
                            text = "kcal pro Tag",
                            fontSize = 14.sp,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Progress macro visual bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Protein
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassCard(borderColor = ProteinBlue.copy(alpha = 0.2f), backgroundColor = ProteinBlue.copy(alpha = 0.05f), cornerRadius = 14.dp)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PROTEIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ProteinBlue)
                                Text("${p.proteinTarget.toInt()}g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                Text("${(p.proteinTarget * 4).toInt()} kcal", fontSize = 10.sp, color = TextGray)
                            }
                            // Carbs
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassCard(borderColor = CarbOrange.copy(alpha = 0.2f), backgroundColor = CarbOrange.copy(alpha = 0.05f), cornerRadius = 14.dp)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("CARBS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CarbOrange)
                                Text("${p.carbTarget.toInt()}g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                Text("${(p.carbTarget * 4).toInt()} kcal", fontSize = 10.sp, color = TextGray)
                            }
                            // Fat
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassCard(borderColor = FatPink.copy(alpha = 0.2f), backgroundColor = FatPink.copy(alpha = 0.05f), cornerRadius = 14.dp)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("FETT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FatPink)
                                Text("${p.fatTarget.toInt()}g", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                                Text("${(p.fatTarget * 9).toInt()} kcal", fontSize = 10.sp, color = TextGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .glassCard()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("GRUNDUMSATZ (BMR)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Text("${p.bmr.toInt()} kcal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .glassCard()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("GESAMTBEDARF (TDEE)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Text("${p.tdee.toInt()} kcal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Advice Section
                Text(
                    text = "🧠 KI-Analyse & Ernährungs-Strategie",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(glowingHighlight = CoachTeal)
                        .padding(20.dp)
                ) {
                    if (isExplanationLoading || explanationText.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CoachTeal)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Macrofy Coach analysiert Daten...", fontSize = 14.sp, color = TextGray, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        // AI Response Text
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = explanationText,
                                fontSize = 14.sp,
                                color = TextWhite,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onNavigateToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = MacrofyGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(58.dp).testTag("enter_dashboard_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Dashboard betreten", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, contentDescription = "Dashboard", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun PremiumPaywallDialog(
    viewModel: MacrofyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isPremium by viewModel.isPremiumSubscribed.collectAsStateWithLifecycle()
    val billingStatus by viewModel.billingPurchaseStatus.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .border(1.dp, CoachTeal.copy(0.3f), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(CoachTeal.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MACROFY SPECIAL PRO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoachTeal,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Premium Fitness & Nutrition",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BenefitRow("📸 Unbegrenzter AI Foto-Scanner", "Scanne Mahlzeiten direkt über deine Handy-Kamera ohne tägliches Limit.")
                    BenefitRow("🔑 100% Login- & Registrierungsfrei!", "Die Aktivierung läuft direkt & anonym über dein Google Play-Konto – ohne extra Account.")
                    BenefitRow("💬 Elite AI-Coach Chat rund um die Uhr", "Erhalte personalisierte Diät-Pläne, Rezepte und Fitness-Feedback.")
                    BenefitRow("🔒 100% Privatsphäre & Offline-First", "Deine Daten werden komplett lokal, anonym und sicher auf deinem Gerät gespeichert.")
                    BenefitRow("📊 Detaillierte Makro-Analysen & Trends", "Verstehe deine Ernährungsgewohnheiten tiefer mit interaktiven Grafiken.")
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MacrofyGreen.copy(0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, MacrofyGreen.copy(0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("✓ Deine Mitgliedschaft ist aktiv", color = MacrofyGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Abwicklung erfolgt sicher über Google Play Billing.", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Verbunden mit deinem Google Play Account", color = CoachTeal, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Schließen", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.simulatedCancelSubscription()
                            },
                            modifier = Modifier.weight(1.2f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.12f), contentColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Abo beenden", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    var selectedPlan by remember { mutableStateOf("yearly") }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        PlanOptionRow(
                            title = "Monatliches Spar-Abo",
                            price = "7,99 € / Monat",
                            subtitle = "Jederzeit kündbar",
                            isSelected = selectedPlan == "monthly",
                            onClick = { selectedPlan = "monthly" }
                        )

                        PlanOptionRow(
                            title = "Elite Jahres-Abo",
                            price = "49,99 € / Jahr",
                            subtitle = "Entspricht nur 4,16 €/Monat • 45% Rabatt",
                            badgeText = "Bestpreis",
                            isSelected = selectedPlan == "yearly",
                            onClick = { selectedPlan = "yearly" }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Accountless Info Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4285F4).copy(0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF4285F4).copy(0.25f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ℹ️", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                            Column {
                                Text(
                                    "Keine Registrierung nötig",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    "Das Abo wird direkt an deine Google-ID im Play Store gekoppelt. Du musst dich weder anmelden noch deine Mailadresse angeben.",
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            activity?.let { act ->
                                val planId = if (selectedPlan == "monthly") com.example.data.PlayBillingHelper.PLAN_MONTHLY else com.example.data.PlayBillingHelper.PLAN_YEARLY
                                viewModel.launchGooglePlaySubscription(act, planId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoachTeal),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, "Google Play Store", tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Im Google Play Store abonnieren", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.restorePlayStorePurchases() },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("🔍 Käufe wiederherstellen", color = CoachTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Vielleicht später", color = TextGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun BenefitRow(iconPlusTitle: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(iconPlusTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(1.dp))
            Text(description, fontSize = 11.sp, color = TextGray, lineHeight = 15.sp)
        }
    }
}

@Composable
fun PlanOptionRow(
    title: String,
    price: String,
    subtitle: String,
    badgeText: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isSelected) CoachTeal else ObsidianBorder,
                RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) CoachTeal.copy(0.06f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    badgeText?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(CarbOrange, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(it, fontSize = 9.sp, fontWeight = FontWeight.Black, color = TextWhite)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextGray, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(price, color = if (isSelected) CoachTeal else TextWhite, fontWeight = FontWeight.Black, fontSize = 13.sp)
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = CoachTeal, unselectedColor = TextGray)
                )
            }
        }
    }
}


