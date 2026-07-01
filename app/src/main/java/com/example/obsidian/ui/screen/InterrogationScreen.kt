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
import com.example.obsidian.ui.viewmodel.SpecialMoment

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
    
    // Fullscreen loading overlay removed to prevent "generando expediente" flashing. Inline loading is shown in Chat instead.

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
                                isCompleted = isCompleted,
                                isGenerating = isGenerating
                            )
                        }
                        1 -> StatementsTabContent(suspect = suspect, gameState = gameState)
                        2 -> TestimoniesComparisonTabContent(gameState = gameState, viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Render Special Moment Minigame overlay
    gameState.let {
        viewModel.activeSpecialMoment?.let { moment ->
            SpecialMomentOverlay(
                moment = moment,
                onDismiss = { viewModel.activeSpecialMoment = null }
            )
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
    isCompleted: Boolean,
    isGenerating: Boolean
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
                isGenerating = isGenerating,
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
                if (suspect.trustLevel <= 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1010)),
                        border = BorderStroke(1.dp, AggressiveRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⛔ SOSPECHOSO NO COOPERATIVO",
                                color = AggressiveRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${suspect.name} se niega a responder más preguntas en este estado.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.forceCooperation(suspect.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AggressiveRed, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("FORZAR COOPERACIÓN CON ORDEN (-150 PTS)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    if (isGenerating) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PanelColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = CyanNeon,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${suspect.name.uppercase()} ESTÁ REDACTANDO RESPUESTA...",
                                    color = CyanNeon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
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
                    }
                }

                val session = gameState.interrogationSessions[suspect.id]
                val remainingQuestions = suspect.availableQuestions.count { it.id !in (session?.askedQuestionIds ?: emptySet()) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (remainingQuestions == 0 || (session?.questionsThisRound ?: 0) >= 3 || suspect.trustLevel <= 0) {
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

fun getContradictionNarrative(key: String): Pair<String, String> {
    return when (key) {
        "carlos_manifiesto" -> Pair(
            "La Firma Falsificada",
            "Carlos alegaba no recordar quién autorizó el despacho, pero confrontaste su testimonio con el Manifiesto de Importación, demostrando que fue su propia firma la que despachó el contenedor."
        )
        "carlos_9pm" -> Pair(
            "La Fuga del Almacén",
            "Carlos juraba haber estado haciendo inventario solo en el almacén a las 9 PM. Sin embargo, confrontaste su coartada con el video del CCTV del puerto, el cual captó su vehículo saliendo a esa misma hora."
        )
        "carlos_borrador" -> Pair(
            "La Orden Escrita",
            "Carlos negó haber escrito el borrador de alteración de peso de la carga. Lo confrontaste con el documento recuperado en la oficina, probando su letra y autoría intelectual en el fraude."
        )
        "carlos_usb_fraude" -> Pair(
            "Conocimiento del Fraude",
            "Carlos sostenía que Don Aurelio era ajeno al fraude aduanero. Los correos de la USB demostraron que Don Aurelio lo había descubierto y Carlos lo mató en un forcejeo al verse acorralado."
        )
        "valentina_seguro" -> Pair(
            "La Autoría del Seguro",
            "Valentina afirmó que el seguro del contenedor se tramitó automáticamente. Lo confrontaste con la evidencia del log de terminales de la Fiscalía, demostrando que fue iniciado manualmente desde su sesión privada."
        )
        "valentina_offshore" -> Pair(
            "Las Cuentas en Panamá",
            "Valentina declaró no tener relación con transacciones extranjeras. La confrontación con los Registros Bancarios reveló firmas de desvíos y transferencias a su cuenta offshore por $180,000."
        )
        "tomas_carlos" -> Pair(
            "La Reunión Clandestina",
            "El inspector Tomás Guerrero juró no conocer a Carlos. Al comparar su testimonio con el CCTV o fotos, expusiste su mentira, revelando reuniones secretas fuera de horas de aduana."
        )
        "tomas_camara" -> Pair(
            "El Control Ignorado",
            "Tomás afirmó que validó e inspeccionó debidamente el contenedor 7-BETA. El CCTV del puerto demostró que le dio paso libre sin bajarse de su garita, recibiendo sobornos de la red."
        )
        "tomas_ignorar" -> Pair(
            "El Desliz de Aduanas",
            "Tomás declaró que no recibió instrucciones. Durante la presión del interrogatorio, cometió el desliz de admitir que 'nadie le dijo que ignorara ESE contenedor específico', implicando que sí lo hizo con otros."
        )
        "marisol_carlos_contrato" -> Pair(
            "El Pacto de Silencio",
            "Marisol Mendoza alegó no tener trato comercial con Carlos Herrera. Confrontaste su declaración con el Contrato Privado de Ganancias del 20%, probando su complicidad y silencio pagado."
        )
        "carlos_marisol_relation" -> Pair(
            "Vínculo Oculto",
            "Al comparar los testimonios de Carlos y Marisol, expusiste que ambos negaban conocerse en el ámbito personal, contradiciendo las reuniones secretas y el flujo de fondos entre ellos."
        )
        "carlos_valentina_relation" -> Pair(
            "La Ruta de Fondos de la Contadora",
            "Comparaste el testimonio de Carlos (quien negó conocer a Logística del Caribe SAS) con el de Valentina (quien afirmó que Carlos coordinaba las transferencias directamente con esa firma), revelando la red financiera."
        )
        else -> Pair("Contradicción Detectada", "Evidencia contradictoria expuesta durante el careo.")
    }
}

// ----------------------------------------------------
// TAB 1: DECLARACIONES FORMALES Y BITÁCORA DE MENTIRAS
// ----------------------------------------------------
@Composable
fun StatementsTabContent(suspect: Suspect, gameState: GameState) {
    val suspectStatements = gameState.statements.filter { it.suspectId == suspect.id }
    
    val suspectContradictions = gameState.discoveredContradictions.filter { key ->
        when (suspect.id) {
            "suspect_carlos" -> key.startsWith("carlos_")
            "suspect_tomas" -> key.startsWith("tomas_") || key == "carlos_marisol_relation" || key == "carlos_valentina_relation"
            "suspect_valentina" -> key.startsWith("valentina_") || key == "carlos_valentina_relation"
            "suspect_marisol" -> key.startsWith("marisol_") || key == "carlos_marisol_relation"
            else -> false
        }
    }

    val suspectNameKeyword = when (suspect.id) {
        "suspect_carlos" -> "Carlos"
        "suspect_tomas" -> "Tomás"
        "suspect_valentina" -> "Valentina"
        "suspect_marisol" -> "Marisol"
        else -> ""
    }
    
    val suspectExtraRevelations = gameState.extraInfoFound.filter { info ->
        suspectNameKeyword.isNotEmpty() && info.contains(suspectNameKeyword, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (suspectContradictions.isNotEmpty()) {
            item {
                Text(
                    text = "CONTRADICCIONES DEMOSTRADAS (${suspectContradictions.size})",
                    color = neonYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(suspectContradictions.toList()) { key ->
                val (title, description) = getContradictionNarrative(key)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, neonYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161508))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = neonYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title.uppercase(),
                                color = neonYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            item {
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        if (suspectExtraRevelations.isNotEmpty()) {
            item {
                Text(
                    text = "REVELACIONES Y CONEXIONES (${suspectExtraRevelations.size})",
                    color = CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(suspectExtraRevelations) { info ->
                val isRedHerring = info.contains("DESPISTE", ignoreCase = true)
                val borderCol = if (isRedHerring) AggressiveRed.copy(alpha = 0.5f) else CyanNeon.copy(alpha = 0.5f)
                val bgCol = if (isRedHerring) Color(0xFF1A0A0A) else Color(0xFF0A1A1A)
                val tagText = if (isRedHerring) "⚠️ HILO DE SOSPECHA" else "🔍 EVIDENCIA DIRECTA"
                val tagCol = if (isRedHerring) AggressiveRed else CyanNeon

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderCol, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = bgCol)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = tagText,
                            color = tagCol,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = info,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            item {
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        item {
            Text(
                text = "DECLARACIONES ARCHIVADAS (${suspectStatements.size})",
                color = CyanNeon,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (suspectStatements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No has archivado declaraciones formales de este sospechoso.\nRealiza preguntas en el Chat para registrar sus coartadas.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
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
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}

// ConfrontationTabContent removed. Momentos Especiales are triggered dynamically now.

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
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty() || isGenerating) {
            val targetIndex = if (isGenerating) messages.size else messages.size - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
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

        if (isGenerating) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        color = Color(0xFF141414),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.widthIn(max = 240.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = CyanNeon,
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escribiendo...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
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

// ----------------------------------------------------
// COMPOSABLE: MOMENTO ESPECIAL (OVERLAY MINIJUEGOS)
// ----------------------------------------------------
@Composable
fun SpecialMomentOverlay(moment: SpecialMoment, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* No dismiss on click outside to avoid losing state */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = Color(0xFF09090B),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(1.dp, neonYellow, RoundedCornerShape(12.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (moment) {
                        is SpecialMoment.ReconstructConversation -> Icons.Default.Extension
                        is SpecialMoment.RememberDetails -> Icons.Default.Psychology
                        is SpecialMoment.PsychologicalProfile -> Icons.Default.Fingerprint
                    },
                    contentDescription = null,
                    tint = neonYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (moment) {
                        is SpecialMoment.ReconstructConversation -> "RECONSTRUIR CONVERSACIÓN"
                        is SpecialMoment.RememberDetails -> "RECORDAR DETALLES"
                        is SpecialMoment.PsychologicalProfile -> "PERFIL PSICOLÓGICO"
                    }.uppercase(),
                    color = neonYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF091A1E)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OBJETIVO: ${moment.rewardText.uppercase()}",
                            color = CyanNeon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = when (moment) {
                        is SpecialMoment.ReconstructConversation -> "El sospechoso duda al narrar los hechos. Organiza cronológicamente sus frases para exponer inconsistencias."
                        is SpecialMoment.RememberDetails -> "Analiza con atención el testimonio y responde correctamente a la pregunta de memoria para asegurar credibilidad."
                        is SpecialMoment.PsychologicalProfile -> "Determina la actitud y estado mental del sospechoso a partir de sus respuestas y lenguaje no verbal."
                    },
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                when (moment) {
                    is SpecialMoment.ReconstructConversation -> {
                        var currentSequence by remember { mutableStateOf(moment.scrambledSequence) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentSequence.forEachIndexed { index, item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PanelColor),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        val newList = currentSequence.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index - 1]
                                                        newList[index - 1] = temp
                                                        currentSequence = newList
                                                    }
                                                },
                                                enabled = index > 0,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = if (index > 0) CyanNeon else Color.Gray, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (index < currentSequence.size - 1) {
                                                        val newList = currentSequence.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index + 1]
                                                        newList[index + 1] = temp
                                                        currentSequence = newList
                                                    }
                                                },
                                                enabled = index < currentSequence.size - 1,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = if (index < currentSequence.size - 1) CyanNeon else Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val success = currentSequence == moment.correctSequence
                                    moment.onComplete(success)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("COMPROBAR SECUENCIA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is SpecialMoment.RememberDetails -> {
                        var selectedOption by remember { mutableStateOf<String?>(null) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1206)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, neonYellow.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "\"${moment.contextStatement}\"",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(text = moment.questionText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            moment.options.forEach { option ->
                                val isSelected = selectedOption == option
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isSelected) CyanNeon else Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { selectedOption = option },
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) CyanNeon.copy(alpha = 0.1f) else PanelColor)
                                ) {
                                    Text(
                                        text = option,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (selectedOption != null) {
                                        val success = selectedOption == moment.correctOption
                                        moment.onComplete(success)
                                    }
                                },
                                enabled = selectedOption != null,
                                colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("CONFIRMAR RESPUESTA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is SpecialMoment.PsychologicalProfile -> {
                        var selectedOption by remember { mutableStateOf<String?>(null) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = moment.questionText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            moment.options.forEach { option ->
                                val isSelected = selectedOption == option
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isSelected) CyanNeon else Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { selectedOption = option },
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) CyanNeon.copy(alpha = 0.1f) else PanelColor)
                                ) {
                                    Text(
                                        text = option,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (selectedOption != null) {
                                        val success = selectedOption == moment.correctOption
                                        moment.onComplete(success)
                                    }
                                },
                                enabled = selectedOption != null,
                                colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("CONFIRMAR PERFILADO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { /* Handled internally by buttons */ }
    )
}


