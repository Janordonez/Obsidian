package com.example.obsidian.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.obsidian.R
import com.example.obsidian.data.model.*
import com.example.obsidian.ui.theme.*
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun InterrogationScreen(
    navController: NavHostController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val allMessages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating = viewModel.isGenerating
    
    val suspects = gameState.currentCase?.suspects ?: emptyList()
    
    if (isGenerating) {
        Box(modifier = modifier.fillMaxSize().background(BackgroundNoir), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CyanNeon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("GENERANDO EXPEDIENTE...", color = CyanNeon, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    if (suspects.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay expediente activo", color = Color.Gray)
        }
        return
    }

    // Phase check
    if (gameState.explorationCount < 1) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundNoir)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AggressiveRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SALA DE INTERROGATORIOS BLOQUEADA",
                    color = AggressiveRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Primero debes investigar el Puerto y encontrar al menos una pista antes de interrogar a los sospechosos.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(com.example.obsidian.navigation.Screen.Map.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("IR AL MAPA SCANNER", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LaunchedEffect(suspects.map { it.id }) {
        val ids = suspects.map { it.id }
        val current = viewModel.selectedInterrogationSuspectId
        if (current == null || current !in ids) {
            viewModel.selectInterrogationSuspect(ids.firstOrNull().orEmpty())
        }
    }

    val selectedSuspectId = viewModel.selectedInterrogationSuspectId ?: suspects.firstOrNull()?.id.orEmpty()
    val selectedSuspect = suspects.find { it.id == selectedSuspectId }
    val currentMessages = allMessages[selectedSuspectId] ?: emptyList()
    var userText by remember { mutableStateOf("") }

    val isCompleted = viewModel.isInterrogationCompleted(selectedSuspectId)
    val isActive = viewModel.isInterrogationActive(selectedSuspectId)
    val timeRemaining = viewModel.getInterrogationTimeRemaining(selectedSuspectId)
    val summary = viewModel.getInterrogationSummary(selectedSuspectId)
    
    // Tab Navigation
    var activeTab by remember { mutableStateOf(0) } // 0: Chat, 1: Declaraciones, 2: Confrontar, 3: Testimonios

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
    ) {
        // Selector de Sospechosos
        SuspectSelectorRow(
            suspects = suspects,
            selectedId = selectedSuspectId,
            interrogatedIds = gameState.interrogatedSuspects,
            onSelect = { viewModel.selectInterrogationSuspect(it) }
        )

        selectedSuspect?.let { suspect ->
            val baseSusp = when (suspect.id) {
                "suspect_carlos" -> 65
                "suspect_tomas" -> 50
                "suspect_marisol" -> 35
                "suspect_valentina" -> 40
                else -> 0
            }
            val suspicionVal = gameState.suspicionLevels[suspect.id] ?: baseSusp

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Interrogation Header: Displays name and detailed progress bars
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = suspect.name.uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = suspect.relation,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("CONFIANZA", color = CyanNeon, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            LinearProgressIndicator(
                                progress = { suspect.trustLevel / 100f },
                                modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (suspect.trustLevel < 30) AggressiveRed else CyanNeon,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SOSPECHA", color = neonYellow, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            LinearProgressIndicator(
                                progress = { suspicionVal / 100f },
                                modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (suspicionVal >= 75) AggressiveRed else neonYellow,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Timer bar (solo durante interrogatorio activo)
                if (isActive) {
                    InterrogationTimerBar(
                        timeRemaining = timeRemaining,
                        questionsThisRound = gameState.interrogationSessions[selectedSuspectId]?.questionsThisRound ?: 0
                    )
                }

                // Interactive Tab Indicators
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = CyanNeon,
                    divider = { HorizontalDivider(color = Color.DarkGray, thickness = 1.dp) }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("DECLARACIONES", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("CONFRONTAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("TESTIMONIOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                    )
                }

                // Tab Content Switcher
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (activeTab) {
                        0 -> if (isCompleted && summary != null) {
                            InterrogationSummaryContent(summary = summary)
                        } else {
                            ChatTabContent(
                                suspect = suspect,
                                messages = currentMessages,
                                gameState = gameState,
                                viewModel = viewModel,
                                userText = userText,
                                onTextChange = { userText = it },
                                isActive = isActive,
                                isCompleted = isCompleted
                            )
                        }
                        1 -> StatementsTabContent(suspect = suspect, gameState = gameState)
                        2 -> ConfrontationTabContent(suspect = suspect, gameState = gameState, viewModel = viewModel)
                        3 -> TestimoniesComparisonTabContent(gameState = gameState, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun InterrogationTimerBar(timeRemaining: Int, questionsThisRound: Int) {
    val progress = timeRemaining / 90f
    val timerColor = when {
        timeRemaining <= 15 -> AggressiveRed
        timeRemaining <= 30 -> neonYellow
        else -> CyanNeon
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏱ ${timeRemaining}s",
                color = timerColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "PREGUNTAS: $questionsThisRound/3",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = timerColor,
            trackColor = Color.Black.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun InterrogationSummaryContent(summary: InterrogationSummary) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = PanelColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTRO DE INTERROGATORIO",
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = summary.suspectName.uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Finalizado a las ${summary.completedAt} · ${summary.questionsAsked} preguntas realizadas",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "PUNTOS CLAVE",
                color = neonYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(summary.keyPoints) { point ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "▸", color = CyanNeon, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = point, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }

        if (summary.contradictionsFound.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CONTRADICCIONES DETECTADAS",
                    color = AggressiveRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            items(summary.contradictionsFound) { contradiction ->
                Text(
                    text = "⚠ $contradiction",
                    color = AggressiveRed.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }

        if (summary.cluesUnlocked.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PISTAS DESBLOQUEADAS",
                    color = CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            items(summary.cluesUnlocked) { clue ->
                Text(
                    text = "🔓 $clue",
                    color = CyanNeon.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Este sospechoso ya fue interrogado. Usa las pestañas Declaraciones, Confrontar o Testimonios para continuar la investigación.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}

// ----------------------------------------------------
// TAB 0: CHAT TRADICIONAL & REVELACIONES (PRESIONAR)
// ----------------------------------------------------
@Composable
fun ChatTabContent(
    suspect: Suspect,
    messages: List<InterrogationMessage>,
    gameState: GameState,
    viewModel: GameViewModel,
    userText: String,
    onTextChange: (String) -> Unit,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val carlosContradictions = gameState.discoveredContradictions.filter { it.startsWith("carlos_") }
    val canPress = suspect.id == "suspect_carlos" && carlosContradictions.size >= 3 && !gameState.hasPressedCarlos
    val roundQuestions = viewModel.getQuestionsForRound(suspect.id)

    Column(modifier = Modifier.fillMaxSize()) {
        CompactPersonalityBar(suspect)
        Spacer(modifier = Modifier.height(6.dp))

        if (canPress) {
            PressureCarlosBanner {
                viewModel.pressSuspect("suspect_carlos")
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
            shape = RoundedCornerShape(10.dp)
        ) {
            ConversationHistory(
                messages = messages,
                suspectName = suspect.name,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isCompleted -> { /* Resumen en tab principal */ }
            !isActive -> {
                Button(
                    onClick = { viewModel.startInterrogation(suspect.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("INICIAR INTERROGATORIO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            else -> {
                Text(
                    text = "ELIGE EL TONO ADECUADO PARA ESTE SOSPECHOSO",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roundQuestions.forEach { question ->
                        QuestionChip(
                            question = question,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onQuestionSelected(suspect, question) }
                        )
                    }
                    repeat((3 - roundQuestions.size).coerceAtLeast(0)) {
                        EmptyQuestionSlot(modifier = Modifier.weight(1f))
                    }
                }

                val session = gameState.interrogationSessions[suspect.id]
                val remainingQuestions = suspect.availableQuestions.count { it.id !in (session?.askedQuestionIds ?: emptySet()) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (remainingQuestions == 0 || (session?.questionsThisRound ?: 0) >= 3) {
                        Button(
                            onClick = { viewModel.endInterrogation(suspect.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("FINALIZAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.endInterrogation(suspect.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("TERMINAR AHORA", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PressureCarlosBanner(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsePress")
    val pulseBorder by infiniteTransition.animateColor(
        initialValue = neonYellow,
        targetValue = AggressiveRed,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, pulseBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C0F))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚡ PRESIÓN DISPONIBLE",
                    color = neonYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Carlos tiene 3 contradicciones descubiertas. Presiona para que confiese.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(neonYellow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("PRESIONAR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: DECLARACIONES FORMALES
// ----------------------------------------------------
@Composable
fun StatementsTabContent(suspect: Suspect, gameState: GameState) {
    val suspectStatements = gameState.statements.filter { it.suspectId == suspect.id }

    if (suspectStatements.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No has obtenido declaraciones formales de este sospechoso todavía.\nRealiza preguntas en la pestaña de Chat para archivar sus coartadas.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suspectStatements) { stmt ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = PanelColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DECLARACIÓN #${stmt.id.substringAfter("stmt_q_").uppercase()}",
                            color = CyanNeon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${stmt.text}\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: CONFRONTAR (DECLARACIÓN + EVIDENCIA)
// ----------------------------------------------------
@Composable
fun ConfrontationTabContent(suspect: Suspect, gameState: GameState, viewModel: GameViewModel) {
    val suspectStatements = gameState.statements.filter { it.suspectId == suspect.id }
    val foundClues = gameState.discoveredClues

    var selectedStatementId by remember { mutableStateOf<String?>(null) }
    var selectedClueId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            text = "PASO 1: SELECCIONA UNA DECLARACIÓN",
            color = CyanNeon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Statements List
        Box(modifier = Modifier.weight(0.45f).fillMaxWidth().padding(vertical = 4.dp)) {
            if (suspectStatements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay declaraciones archivadas de este sospechoso.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(suspectStatements) { stmt ->
                        val isSelected = selectedStatementId == stmt.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon else Color.DarkGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedStatementId = stmt.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CyanNeon.copy(alpha = 0.1f) else PanelColor
                            )
                        ) {
                            Text(
                                text = "\"${stmt.text}\"",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "PASO 2: SELECCIONA LA EVIDENCIA CONTRADICTORIA",
            color = neonYellow,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Clues List
        Box(modifier = Modifier.weight(0.45f).fillMaxWidth().padding(vertical = 4.dp)) {
            if (foundClues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No has recolectado evidencias en tu tablero todavía.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(foundClues) { clue ->
                        val isSelected = selectedClueId == clue.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) neonYellow else Color.DarkGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedClueId = clue.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) neonYellow.copy(alpha = 0.1f) else PanelColor
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = clue.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "[${clue.locationName}]", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Confront Action Button
        Button(
            onClick = {
                if (selectedStatementId != null && selectedClueId != null) {
                    viewModel.confrontStatementWithClue(selectedStatementId!!, selectedClueId!!)
                    selectedStatementId = null
                    selectedClueId = null
                }
            },
            enabled = selectedStatementId != null && selectedClueId != null,
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("CONFRONTAR CON EVIDENCIA", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

// ----------------------------------------------------
// TAB 3: COMPARAR TESTIMONIOS (SOSPECHOSO VS SOSPECHOSO)
// ----------------------------------------------------
@Composable
fun TestimoniesComparisonTabContent(gameState: GameState, viewModel: GameViewModel) {
    val statements = gameState.statements
    var selectedStmtId1 by remember { mutableStateOf<String?>(null) }
    var selectedStmtId2 by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            text = "SELECCIONA DOS TESTIMONIOS CONTRADICTORIOS ENTRE SÍ",
            color = CyanNeon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp)) {
            if (statements.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Necesitas al menos 2 declaraciones archivadas para compararlas.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(statements) { stmt ->
                        val isSelected1 = selectedStmtId1 == stmt.id
                        val isSelected2 = selectedStmtId2 == stmt.id
                        val isSelected = isSelected1 || isSelected2

                        val borderCol = if (isSelected1) CyanNeon else if (isSelected2) neonYellow else Color.DarkGray.copy(alpha = 0.5f)
                        val bgCol = if (isSelected1) CyanNeon.copy(alpha = 0.1f) else if (isSelected2) neonYellow.copy(alpha = 0.1f) else PanelColor

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                                .clickable {
                                    when {
                                        isSelected1 -> selectedStmtId1 = null
                                        isSelected2 -> selectedStmtId2 = null
                                        selectedStmtId1 == null -> selectedStmtId1 = stmt.id
                                        selectedStmtId2 == null -> selectedStmtId2 = stmt.id
                                        else -> {
                                            selectedStmtId1 = stmt.id
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = bgCol)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val senderName = when (stmt.suspectId) {
                                        "suspect_carlos" -> "Carlos Herrera"
                                        "suspect_tomas" -> "Tomás Guerrero"
                                        "suspect_marisol" -> "Marisol Mendoza"
                                        "suspect_valentina" -> "Valentina Ríos"
                                        else -> "Sospechoso"
                                    }
                                    Text(text = senderName, color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    if (isSelected) {
                                        Text(
                                            text = if (isSelected1) "TESTIMONIO A" else "TESTIMONIO B",
                                            color = if (isSelected1) CyanNeon else neonYellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "\"${stmt.text}\"", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Compare Action Button
        Button(
            onClick = {
                if (selectedStmtId1 != null && selectedStmtId2 != null) {
                    viewModel.compareStatements(selectedStmtId1!!, selectedStmtId2!!)
                    selectedStmtId1 = null
                    selectedStmtId2 = null
                }
            },
            enabled = selectedStmtId1 != null && selectedStmtId2 != null,
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("COMPARAR TESTIMONIOS", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
fun CompactPersonalityBar(suspect: Suspect) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyanNeon.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "PERFIL PSICOLÓGICO — elige un enfoque acorde a su personalidad",
                color = CyanNeon,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = suspect.personality,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "COARTADA: ${suspect.alibi}",
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun EmptyQuestionSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(88.dp)
            .border(1.dp, Color.DarkGray.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("—", color = Color.DarkGray, fontSize = 18.sp)
    }
}

@Composable
fun QuestionChip(question: Question, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF111111),
        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(88.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = question.text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 4,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun SuspectStatusCard(suspect: Suspect) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = if (suspect.imageId != 0) suspect.imageId else R.drawable.sus1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.4f)
            )
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                Text(text = "RASGOS: ${suspect.personality}", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "COARTADA: ${suspect.alibi}", color = Color.LightGray, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
fun ConversationHistory(
    messages: List<InterrogationMessage>,
    suspectName: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(messages) { msg ->
            if (msg.sender == "SISTEMA") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color(0xFF1A0F10),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (msg.isContradiction) AggressiveRed else Color.DarkGray)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (msg.isContradiction) AggressiveRed else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                val isDetective = msg.isDetective
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isDetective) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isDetective) Color(0xFF1F2937) else Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (msg.isContradiction) neonYellow
                            else if (isDetective) CyanNeon.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                text = if (isDetective) "DETECTIVE" else msg.sender.uppercase(),
                                color = if (isDetective) CyanNeon else neonYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuspectSelectorRow(
    suspects: List<Suspect>,
    selectedId: String,
    interrogatedIds: List<String>,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(suspects) { suspect ->
            val isSelected = suspect.id == selectedId
            val isInterrogated = suspect.id in interrogatedIds
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(suspect.id) }
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                when {
                                    isInterrogated -> Color.Gray
                                    isSelected -> CyanNeon
                                    else -> Color.DarkGray
                                },
                                CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = if (suspect.imageId != 0) suspect.imageId else R.drawable.sus1),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(if (isInterrogated) 0.5f else 1f)
                        )
                    }
                    if (isInterrogated) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                Text(
                    text = suspect.name.split(" ").first(),
                    color = when {
                        isInterrogated -> Color.Gray
                        isSelected -> CyanNeon
                        else -> Color.Gray
                    },
                    fontSize = 10.sp
                )
            }
        }
    }
}


