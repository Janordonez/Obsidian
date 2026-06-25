package com.example.obsidian.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.obsidian.data.model.GameState
import com.example.obsidian.ui.theme.BackgroundNoir
import com.example.obsidian.ui.theme.CyanNeon
import com.example.obsidian.ui.theme.neonYellow
import com.example.obsidian.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun MinigamesOverlay(
    locationName: String,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) {} // block touches below
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .border(2.dp, CyanNeon, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = BackgroundNoir),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = locationName.uppercase(),
                        color = CyanNeon,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DIAGNOSTIC MODE",
                        color = neonYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CyanNeon.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Select Minigame Content based on location
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (locationName) {
                        "Almacén del Puerto" -> ContainerMinigame(viewModel, onDismiss)
                        "Oficina de Importaciones Atlántico" -> ShreddedDocumentMinigame(viewModel, onDismiss)
                        "Caseta de Aduanas" -> LogInspectionMinigame(viewModel, onDismiss)
                        "Residencia Mendoza" -> DecryptAgendaMinigame(viewModel, onDismiss)
                        "Fiscalía" -> MoneyFlowMinigame(viewModel, onDismiss)
                        "Estudio de Abogados" -> SafeCrackMinigame(viewModel, onDismiss)
                        else -> {
                            Text("Minijuego no implementado.", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 1. Bodega 7 / Almacén del Puerto: Contenedor Alterado
// ----------------------------------------------------
data class ContainerItem(
    val index: Int,
    val serial: String,
    val weight: String,
    val sealBroken: Boolean,
    val padlocks: Int
)

@Composable
fun ContainerMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var timeLeft by remember { mutableStateOf(90) }
    var attemptsLeft by remember { mutableStateOf(2) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var statusMessage by remember { mutableStateOf("Encuentra el contenedor con las 3 anomalías:\n1. Sello roto  2. Peso incorrecto  3. Serie fuera de secuencia.") }

    val containers = remember {
        val temp = mutableListOf<ContainerItem>()
        for (i in 0 until 42) {
            val isTarget = (i == 23)
            val serial = if (isTarget) "B-777" else "C-4${String.format("%02d", i + 1)}"
            val weight = if (isTarget) "24,000 kg" else "12,000 kg"
            val seal = if (isTarget) true else false
            val padlocks = if (isTarget) 1 else 2
            temp.add(ContainerItem(i, serial, weight, seal, padlocks))
        }
        // Add noise / partial anomalies
        temp[5] = temp[5].copy(sealBroken = true)
        temp[12] = temp[12].copy(weight = "24,000 kg")
        temp[18] = temp[18].copy(serial = "B-777")
        temp[30] = temp[30].copy(sealBroken = true, weight = "24,000 kg")
        temp[35] = temp[35].copy(serial = "B-777", sealBroken = true)
        temp
    }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        } else {
            viewModel.applyMinigameResult("Almacén del Puerto", "LOST")
            onDismiss()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Status & Timer
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TIEMPO: ${timeLeft}s", color = if (timeLeft < 15) Color.Red else neonYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("INTENTOS: $attemptsLeft", color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Text(
            text = statusMessage,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // 6x7 Grid of Containers
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until 6) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        val item = containers[index]
                        val isSelected = selectedIndex == index

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.2f) else Color(0xFF16161A), RoundedCornerShape(4.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon else Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (item.serial == "B-777") "B-77" else "C-${index + 1}",
                                    color = if (isSelected) CyanNeon else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (item.sealBroken) Color.Red else Color.Green, RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Details Panel and Submit
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101014))
        ) {
            if (selectedIndex != null) {
                val selected = containers[selectedIndex!!]
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("DETALLES CONTENEDOR #${selected.index + 1}", color = neonYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Serie: ${selected.serial}", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("Peso: ${selected.weight}", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sello: ${if (selected.sealBroken) "ROTO ✗" else "INTACTO ✓"}", color = if (selected.sealBroken) Color.Red else Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Candados: ${selected.padlocks}", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (selected.index == 23) {
                                viewModel.applyMinigameResult("Almacén del Puerto", "WON")
                                onDismiss()
                            } else {
                                attemptsLeft -= 1
                                if (attemptsLeft <= 0) {
                                    viewModel.applyMinigameResult("Almacén del Puerto", "LOST")
                                    onDismiss()
                                } else {
                                    statusMessage = "✗ Contenedor incorrecto. No contiene todas las anomalías simultáneamente. Te queda 1 intento."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("CONFIRMAR CONTENEDOR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Selecciona un contenedor de la grilla para inspeccionar sus sellos y especificaciones.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Oficina de Importaciones Atlántico: Documento Destruido
// ----------------------------------------------------
@Composable
fun ShreddedDocumentMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var timeLeft by remember { mutableStateOf(75) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isDone by remember { mutableStateOf(false) }

    val correctPieces = remember {
        listOf(
            "...Logística del Car...",
            "...ibes SAS...",
            "...importe neto: $48.0...",
            "...00...",
            "...firmado: C. Herr...",
            "...era...",
            "...no declarar el con...",
            "...tenedor...",
            "...peso real: 12t...",
            "...declarado: 24t...",
            "...evadir aduana...",
            "...sello roto..."
        )
    }

    // Shuffled list with original index tracked
    val pieces = remember {
        val original = correctPieces.mapIndexed { idx, text -> Pair(idx, text) }.toMutableList()
        // Ensure it is shuffled but not solved
        do {
            original.shuffle()
        } while (original.map { it.first } == (0 until 12).toList())
        mutableStateListOf<Pair<Int, String>>().apply { addAll(original) }
    }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0 && !isDone) {
            delay(1000)
            timeLeft -= 1
        } else if (timeLeft == 0 && !isDone) {
            viewModel.applyMinigameResult("Oficina de Importaciones Atlántico", "LOST")
            onDismiss()
        }
    }

    // Check Win
    fun checkWinCondition() {
        val currentOrder = pieces.map { it.first }
        if (currentOrder == (0 until 12).toList()) {
            isDone = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TIEMPO: ${timeLeft}s", color = if (timeLeft < 15) Color.Red else neonYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("RECONSTRUIR DOCUMENTO", color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Text(
            text = "Toca un fragmento y luego otro para intercambiar sus posiciones. Organízalos de forma que el texto fluya coherentemente en una grilla de 3 columnas.",
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // 3x4 Grid of Shredded Pieces
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val piece = pieces[index]
                        val isSelected = selectedIndex == index
                        val isCorrectPos = piece.first == index

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    if (isSelected) CyanNeon.copy(alpha = 0.25f)
                                    else if (isCorrectPos) Color(0xFF0F2C20)
                                    else Color(0xFF16161A),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon
                                    else if (isCorrectPos) Color.Green.copy(alpha = 0.5f)
                                    else Color.DarkGray,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    if (selectedIndex == null) {
                                        selectedIndex = index
                                    } else {
                                        val firstIdx = selectedIndex!!
                                        val temp = pieces[firstIdx]
                                        pieces[firstIdx] = pieces[index]
                                        pieces[index] = temp
                                        selectedIndex = null
                                        checkWinCondition()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = piece.second,
                                color = if (isCorrectPos) Color.Green else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isDone) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2C20)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Green, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡DOCUMENTO COMPLETAMENTE RECONSTRUIDO!", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contenido: 'Logística del Caribe SAS... importe neto: $48.000... firmado: C. Herrera... no declarar el contenedor... peso real: 12t... declarado: 24t... evadir aduanas... sello roto...'",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyMinigameResult("Oficina de Importaciones Atlántico", "WON")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("FINALIZAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. Caseta de Aduanas: Encontrá el turno sin registro
// ----------------------------------------------------
data class LogEntryItem(
    val index: Int,
    val text: String,
    val isAnomaly: Boolean,
    val anomalyReason: String
)

@Composable
fun LogInspectionMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var errors by remember { mutableStateOf(0) }
    val foundIndices = remember { mutableStateListOf<Int>() }
    var resultMessage by remember { mutableStateOf("Identifica las 3 anomalías en los logs de aduanas:\n1. Un contenedor sin estado  2. Un inspector que no firmó  3. Un salto temporal no secuencial.") }

    val logs = remember {
        val list = mutableListOf<LogEntryItem>()
        // Generate 30 lines
        val inspectors = listOf("GUERRERO", "GUERRERO", "MARTINEZ", "SOTO", "GUERRERO", "MARTINEZ")
        for (i in 0 until 30) {
            val hh = String.format("%02d", 19 + (i / 10))
            val mm = String.format("%02d", (i * 7) % 60)
            val container = "#${1000 + i * 87}"
            val inspector = inspectors[i % inspectors.size]
            val stateSymbol = if (i % 3 == 0) "✓" else "✗"

            var finalHh = hh
            var finalMm = mm
            var finalInspector = inspector
            var finalState = stateSymbol
            var isAnomaly = false
            var reason = ""

            // Insert anomalies at specific indices
            if (i == 7) {
                // Contenedor sin estado
                finalState = "[ ]"
                isAnomaly = true
                reason = "Contenedor sin estado registrado"
            } else if (i == 14) {
                // Turno sin firma
                finalInspector = "[SIN FIRMA]"
                isAnomaly = true
                reason = "Inspector no firmó"
            } else if (i == 21) {
                // Timestamp jump: Jumps from 21:47 to 22:31, and next is 21:52
                finalHh = "22"
                finalMm = "31"
                isAnomaly = true
                reason = "Timestamp no secuencial"
            }

            val text = "[$finalHh:$finalMm] — Contenedor $container — Inspector: $finalInspector — Estado: $finalState"
            list.add(LogEntryItem(i, text, isAnomaly, reason))
        }
        // Force the sequential anomaly visual hints
        // Make index 20 be [21:47]
        list[20] = LogEntryItem(20, "[21:47] — Contenedor #1140 — Inspector: SOTO — Estado: ✓", false, "")
        // Make index 21 be the anomaly [22:31]
        list[21] = LogEntryItem(21, "[22:31] — Contenedor #1145 — Inspector: GUERRERO — Estado: ✓", true, "Timestamp no secuencial")
        // Make index 22 be [21:52]
        list[22] = LogEntryItem(22, "[21:52] — Contenedor #1150 — Inspector: MARTINEZ — Estado: ✗", false, "")

        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ERRORES: $errors / 3", color = if (errors >= 2) Color.Red else neonYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("ANOMALÍAS ENCONTRADAS: ${foundIndices.size} / 3", color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Text(
            text = resultMessage,
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // Scrollable List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).background(Color(0xFF0B0B0D)),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(logs) { index, item ->
                val isSelected = foundIndices.contains(index)
                val cardBorder = if (isSelected) Color.Green else Color.DarkGray.copy(alpha = 0.5f)
                val cardBg = if (isSelected) Color(0xFF0F2C20) else Color(0xFF141416)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(4.dp))
                        .clickable(enabled = !isSelected && errors < 3 && foundIndices.size < 3) {
                            if (item.isAnomaly) {
                                foundIndices.add(index)
                                resultMessage = "¡Encontrada! Anomalía: ${item.anomalyReason}."
                                if (foundIndices.size == 3) {
                                    viewModel.applyMinigameResult("Caseta de Aduanas", "WON")
                                    onDismiss()
                                }
                            } else {
                                errors += 1
                                resultMessage = "✗ Esa entrada sigue la secuencia y protocolo estándar. Cuidado."
                                if (errors >= 3) {
                                    viewModel.applyMinigameResult("Caseta de Aduanas", "LOST")
                                    onDismiss()
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.text,
                        color = if (isSelected) Color.Green else Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. Residencia Mendoza: Descifrar agenda en clave
// ----------------------------------------------------
@Composable
fun DecryptAgendaMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) } // 1: Select definitions, 2: Feedback & retry, 3: Over
    var verificationCount by remember { mutableStateOf(0) }

    val terms = listOf("CH", "LdC", "TG", "7B", "M", "OR")
    val correctMappings = mapOf(
        "CH" to "Carlos Herrera",
        "LdC" to "Logística del Caribe",
        "TG" to "Tomás Guerrero",
        "7B" to "Contenedor 7-BETA",
        "M" to "Marisol",
        "OR" to "Oscar Ríos (abogado)"
    )

    val options = mapOf(
        "CH" to listOf("Carlos Herrera", "Casa Herrera", "Cuenta Habiente"),
        "LdC" to listOf("Logística del Caribe", "La Carga", "Lista de Contactos"),
        "TG" to listOf("Tomás Guerrero", "Tráfico General", "Tercer Garante"),
        "7B" to listOf("Contenedor 7-BETA", "7ma Bodega", "Bloque 7"),
        "M" to listOf("Marisol", "Mendoza", "El Mercado"),
        "OR" to listOf("Oscar Ríos (abogado)", "Oficina Regional", "Orden de Registro")
    )

    // User selections
    val selections = remember { mutableStateMapOf<String, String>() }

    // Init selections
    LaunchedEffect(Unit) {
        terms.forEach { term -> selections[term] = options[term]!![0] }
    }

    var correctCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "NOTA ORIGINAL:",
            color = neonYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A24))
        ) {
            Text(
                text = "\"CH sabe. LdC → TG → 7B. M también. Llamar a OR mañana. Si algo pasa: arch. LdC en carp. azul.\"",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }

        Text(
            text = "Establece el glosario de abreviaturas para descifrar el mensaje.",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Options form
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(terms.size) { i ->
                val term = terms[i]
                val selected = selections[term] ?: ""
                val isCorrect = selected == correctMappings[term]

                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(term, color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Custom Option Selector (cycle onClick for simplicity & beautiful mobile layout)
                        val opts = options[term]!!
                        val currentIdx = opts.indexOf(selected).coerceAtLeast(0)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, if (step == 2 && !isCorrect) Color.Red else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .clickable(enabled = step < 3) {
                                    val nextIdx = (currentIdx + 1) % opts.size
                                    selections[term] = opts[nextIdx]
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selected, color = Color.White, fontSize = 12.sp)
                                if (step == 2) {
                                    Text(
                                        text = if (isCorrect) "✓ OK" else "✗ ERROR",
                                        color = if (isCorrect) Color.Green else Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (step < 3) {
            Button(
                onClick = {
                    correctCount = terms.count { selections[it] == correctMappings[it] }
                    verificationCount += 1
                    if (correctCount == 6) {
                        viewModel.applyMinigameResult("Residencia Mendoza", "WON")
                        onDismiss()
                    } else {
                        if (verificationCount >= 2) {
                            step = 3
                            // Submit final count
                            val res = if (correctCount >= 5) "WON"
                                      else if (correctCount >= 3) "PARTIAL"
                                      else "LOST"
                            viewModel.applyMinigameResult("Residencia Mendoza", res)
                        } else {
                            step = 2
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (step == 2) "CONFIRMAR INTENTO FINAL" else "VERIFICAR CLAVE",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (correctCount >= 3) Color(0xFF0F2C20) else Color(0xFF2C0F0F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (correctCount >= 5) "DECIFRADO CON ÉXITO"
                               else if (correctCount >= 3) "DESCIFRADO PARCIAL"
                               else "FALLO EN EL DESCIFRADO",
                        color = if (correctCount >= 3) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Identificados correctamente: $correctCount / 6 términos.",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("FINALIZAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. Fiscalía: Rastreá el flujo del dinero
// ----------------------------------------------------
data class FinancialNode(
    val name: String,
    val detail: String,
    val stepIndex: Int // -1 if trap
)

@Composable
fun MoneyFlowMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var errors by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) } // 0 to 4
    val connectedList = remember { mutableStateListOf<String>() }
    var statusMessage by remember { mutableStateOf("Conecta los nodos financieros en el orden en que fluyó el dinero:\n1. Logística del Caribe SAS\n2. Cuenta offshore Panamá\n3. Cuenta Tomás Guerrero ($45k)\n4. Cuenta Carlos Herrera ($120k)") }

    val nodes = remember {
        listOf(
            FinancialNode("Logística del Caribe SAS", "Origen de fondos ilícitos - Facturas falsas de importación", 0),
            FinancialNode("Cuenta offshore Panamá", "Sociedad fantasma registrada en Colón - Transf. $287,000", 1),
            FinancialNode("Cuenta Tomás Guerrero ($45,000)", "Inspector de aduanas - Transferencia el 18/04", 2),
            FinancialNode("Cuenta Carlos Herrera ($120,000)", "Jefe Logística - Transferencia el 20/04", 3),
            
            // Traps
            FinancialNode("Pago Proveedor Agrícola", "Transf. ordinaria de repuestos por $12,000", -1),
            FinancialNode("Servicios Públicos Municipales", "Facturación de luz y agua de oficinas", -1),
            FinancialNode("Club Campestre Mendoza", "Pago de membresía familiar anual por $8,000", -1),
            FinancialNode("Supermercado El Sol", "Reembolso caja menor de compras de papelería", -1)
        ).shuffled()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ERRORES: $errors / 5", color = if (errors >= 4) Color.Red else neonYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("PASOS COMPLETADOS: $currentStep / 4", color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Text(
            text = statusMessage,
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // Step connection flow indicators
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (stepIdx in 0 until 4) {
                val isCompleted = stepIdx < currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            if (isCompleted) Color.Green else Color.DarkGray,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // Nodes Grid layout
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until 2) {
                        val nodeIdx = r * 2 + c
                        val node = nodes[nodeIdx]
                        val isConnected = connectedList.contains(node.name)
                        val isCurrentStepNode = node.stepIndex == currentStep

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(
                                    1.dp,
                                    if (isConnected) Color.Green
                                    else Color.DarkGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isConnected && errors < 5 && currentStep < 4) {
                                    if (isCurrentStepNode) {
                                        connectedList.add(node.name)
                                        currentStep += 1
                                        statusMessage = "¡Paso ${currentStep} conectado! ${node.name}."
                                        if (currentStep == 4) {
                                            viewModel.applyMinigameResult("Fiscalía", "WON")
                                            onDismiss()
                                        }
                                    } else {
                                        errors += 1
                                        statusMessage = "✗ Flujo incorrecto. El dinero no se movió a ${node.name} en esta fase."
                                        if (errors >= 5) {
                                            viewModel.applyMinigameResult("Fiscalía", "LOST")
                                            onDismiss()
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isConnected) Color(0xFF0F2C20) else Color(0xFF141416)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = node.name,
                                    color = if (isConnected) Color.Green else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = node.detail,
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. Estudio de Abogados: Abrir la caja fuerte
// ----------------------------------------------------
@Composable
fun SafeCrackMinigame(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var attemptsLeft by remember { mutableStateOf(3) }
    val correctDigits = listOf(2, 3, 4, 3)
    val dialDigits = remember { mutableStateListOf(0, 0, 0, 0) }
    var mastermindFeedback by remember { mutableStateOf("") }
    var isUnlocked by remember { mutableStateOf(false) }

    val clues = listOf(
        "Dígito 1: Años que lleva activo el esquema (desde contrato 2022 a 2024)",
        "Dígito 2: Contenedores que pasaron sin inspección según el log de aduanas",
        "Dígito 3: Socios del esquema, incluyendo al difunto",
        "Dígito 4: Número de mes del primer manifiesto falso (marzo)"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CAJA FUERTE ELECTRÓNICA",
            color = neonYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            text = mastermindFeedback.ifEmpty { "Deduce la combinación basándote en la información recopilada del caso." },
            color = if (isUnlocked) Color.Green else if (attemptsLeft < 3) Color.Red else Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )

        // Dials
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            if (attemptsLeft > 0 && !isUnlocked) {
                                dialDigits[i] = (dialDigits[i] + 1) % 10
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("▲", color = CyanNeon, fontSize = 16.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(2.dp, CyanNeon, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dialDigits[i].toString(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = {
                            if (attemptsLeft > 0 && !isUnlocked) {
                                dialDigits[i] = (dialDigits[i] + 9) % 10
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("▼", color = CyanNeon, fontSize = 16.sp)
                    }
                }
            }
        }

        // Logic Clues lists
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101014))
        ) {
            LazyColumn(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Text("PISTAS LÓGICAS:", color = neonYellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                items(clues.size) { index ->
                    Text(
                        text = clues[index],
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        if (!isUnlocked && attemptsLeft > 0) {
            Button(
                onClick = {
                    val correctCount = dialDigits.zip(correctDigits).count { it.first == it.second }
                    if (correctCount == 4) {
                        isUnlocked = true
                        mastermindFeedback = "¡CAJA ABIERTA CON ÉXITO!"
                        viewModel.applyMinigameResult("Estudio de Abogados", "WON")
                    } else {
                        attemptsLeft -= 1
                        if (attemptsLeft <= 0) {
                            mastermindFeedback = "✗ CAJA BLOQUEADA ELECTRÓNICAMENTE."
                            viewModel.applyMinigameResult("Estudio de Abogados", "LOST")
                        } else {
                            mastermindFeedback = "Combianción incorrecta. Dígitos en posición correcta: $correctCount."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("PROBAR COMBINACIÓN ($attemptsLeft intentos)", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUnlocked) Color.Green else Color.Red,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("FINALIZAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
