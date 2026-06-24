package com.example.obsidian.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.CyanNeon
import com.example.obsidian.ui.viewmodel.GameViewModel
import androidx.compose.foundation.BorderStroke
import com.example.obsidian.ui.theme.BackgroundNoir
import com.example.obsidian.ui.theme.neonYellow
import com.example.obsidian.data.model.GamePhase
import com.example.obsidian.data.model.GameState
import androidx.compose.ui.text.style.TextAlign

data class LocationPin(
    val id: String,
    val name: String,
    val abbreviation: String,
    val description: String,
    val x: Float, // Map X offset (dp)
    val y: Float  // Map Y offset (dp)
)

sealed class PinLockStatus {
    object Unlocked : PinLockStatus()
    data class Blocked(val remainingScans: Int) : PinLockStatus()
    data class Locked(val message: String) : PinLockStatus()
}

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isGenerating = viewModel.isGenerating
    val currentCase = gameState.currentCase

    var offset by remember { mutableStateOf(Offset.Zero) }
    var showLocationListDialog by remember { mutableStateOf(false) }
    var selectedPin by remember { mutableStateOf<LocationPin?>(null) }
    var activeMinigameLocation by remember { mutableStateOf<String?>(null) }

    val locationsList = remember {
        listOf(
            LocationPin(
                id = "Almacén del Puerto",
                name = "Bodega 7 del Puerto",
                abbreviation = "B7",
                description = "La bodega tiene 40 contenedores. Solo uno tiene el sello roto. Tenés 90 segundos antes de que el guardia vuelva.",
                x = 80f,
                y = 180f
            ),
            LocationPin(
                id = "Oficina de Importaciones Atlántico",
                name = "Oficina de Importaciones",
                abbreviation = "OF",
                description = "En el cesto de basura encontrás un documento hecho pedazos. Alguien lo destruyó apresuradamente. Si lo armás, podrías tener algo.",
                x = 260f,
                y = 120f
            ),
            LocationPin(
                id = "Caseta de Aduanas",
                name = "Caseta de Aduanas",
                abbreviation = "CA",
                description = "La terminal de aduanas muestra el log de todos los movimientos del puerto esa noche. Algo está faltando, pero el sistema tiene 200 entradas.",
                x = 60f,
                y = 420f
            ),
            LocationPin(
                id = "Residencia Mendoza",
                name = "Residencia Mendoza",
                abbreviation = "RM",
                description = "Don Aurelio escribía sus notas más sensibles con un sistema de abreviaturas propio. La última página de la agenda tiene una nota que no se entiende a primera vista.",
                x = 280f,
                y = 460f
            ),
            LocationPin(
                id = "Fiscalía",
                name = "Fiscalía",
                abbreviation = "FI",
                description = "El fiscal te da acceso a los movimientos financieros pero el sistema muestra todo mezclado. Tenés que seguir el rastro del dinero desde la empresa fantasma hasta las cuentas finales.",
                x = 160f,
                y = 600f
            ),
            LocationPin(
                id = "Estudio de Abogados",
                name = "Estudio de Abogados",
                abbreviation = "EA",
                description = "El abogado Oscar Ríos coopera, pero la caja fuerte donde guardó los documentos que Don Aurelio le envió tiene una clave que solo el cliente conocía. Basándote en lo que sabés del caso, deducí la combinación.",
                x = 180f,
                y = 300f
            )
        )
    }

    fun checkPinLockStatus(pinId: String, state: GameState): PinLockStatus {
        if (pinId == "Almacén del Puerto") {
            val blockedUntil = state.blockedLocations["Almacén del Puerto"]
            if (blockedUntil != null && state.explorationCount < blockedUntil) {
                return PinLockStatus.Blocked(blockedUntil - state.explorationCount)
            }
        }
        if (pinId == "Fiscalía") {
            if (state.explorationCount < 3) {
                return PinLockStatus.Locked("El sistema financiero de la fiscalía requiere al menos 3 exploraciones previas en la barra de progreso.")
            }
        }
        if (pinId == "Estudio de Abogados") {
            val isAgendaLinked = state.clueAssignments.containsKey("clue_agenda")
            if (!isAgendaLinked) {
                return PinLockStatus.Locked("Requiere haber encontrado y vinculado la Agenda de Don Aurelio a un sospechoso en la pantalla de Evidencias.")
            }
        }
        return PinLockStatus.Unlocked
    }

    if (isGenerating) {
        Box(modifier = modifier.fillMaxSize().background(Color(0xFF09090B)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CyanNeon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("GENERANDO ENTORNO...", color = CyanNeon, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ -> offset += pan }
            }
    ) {
        // Grid Digital
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            val gridSize = 30.dp.toPx()
            for (x in -2000..2000 step gridSize.toInt()) {
                drawLine(color = CyanNeon.copy(alpha = 0.05f), start = Offset(x.toFloat(), -2000f), end = Offset(x.toFloat(), 2000f))
            }
            for (y in -2000..2000 step gridSize.toInt()) {
                drawLine(color = CyanNeon.copy(alpha = 0.05f), start = Offset(-2000f, y.toFloat()), end = Offset(2000f, y.toFloat()))
            }
        }

        // Location Node Pins
        Box(modifier = Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            locationsList.forEach { pin ->
                val lockStatus = checkPinLockStatus(pin.id, gameState)
                val playResult = gameState.minigameResults[pin.id]

                LocationMarker(
                    pin = pin,
                    lockStatus = lockStatus,
                    playResult = playResult,
                    modifier = Modifier.offset(x = pin.x.dp, y = pin.y.dp),
                    onClick = { selectedPin = pin }
                )
            }
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.Black.copy(alpha = 0.8f)).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyanNeon)
            Spacer(modifier = Modifier.width(12.dp))
            Text("OBSIDIAN INTEL MAP — LA CARGA DEL SILENCIO", color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }

        // Bottom Navigation/Action Bar
        Button(
            onClick = { showLocationListDialog = true },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).height(56.dp).width(240.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Radar, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SELECCIONAR DESTINO", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        
        // Scan Count Progress HUD
        Text(
            text = "SCAN COUNT: ${gameState.explorationCount}",
            color = CyanNeon.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }

    // Dialogue: Location Details Dialog
    if (selectedPin != null) {
        val pin = selectedPin!!
        val lockStatus = checkPinLockStatus(pin.id, gameState)
        val playResult = gameState.minigameResults[pin.id]

        AlertDialog(
            onDismissRequest = { selectedPin = null },
            title = {
                Text(
                    text = pin.name.uppercase(),
                    color = CyanNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = pin.description,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Lock/Status Information
                    when (lockStatus) {
                        is PinLockStatus.Locked -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1313)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "✗ BLOQUEADO: ${lockStatus.message}",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        is PinLockStatus.Blocked -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1313)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "✗ BLOQUEADO POR GUARDIA: Faltan ${lockStatus.remainingScans} exploraciones antes de regresar.",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        PinLockStatus.Unlocked -> {
                            if (playResult != null) {
                                val statusText = when (playResult) {
                                    "WON" -> "✓ COMPLETADO: Victoria total. Pista/Información registrada."
                                    "PARTIAL" -> "⚠ COMPLETADO: Desbloqueo parcial."
                                    else -> "✗ COMPLETADO: Intento fallido."
                                }
                                val statusColor = when (playResult) {
                                    "WON" -> Color.Green
                                    "PARTIAL" -> neonYellow
                                    else -> Color.Red
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyanNeon.copy(alpha = 0.05f)),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = "⚡ CONEXIÓN ESTABLE: Minijuego listo para iniciar.",
                                        color = CyanNeon,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (lockStatus is PinLockStatus.Unlocked && playResult == null) {
                    Button(
                        onClick = {
                            activeMinigameLocation = pin.id
                            selectedPin = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("INICIAR MINIJUEGO", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPin = null }) {
                    Text("CERRAR", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0D0E11),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )
    }

    // Dialogue: Selector menu for locations
    if (showLocationListDialog && currentCase != null) {
        AlertDialog(
            onDismissRequest = { showLocationListDialog = false },
            title = {
                Text(
                    text = "SELECCIONAR ÁREA DE INSPECCIÓN",
                    color = CyanNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    locationsList.forEach { location ->
                        val lockStatus = checkPinLockStatus(location.id, gameState)
                        val playResult = gameState.minigameResults[location.id]

                        val statusLabel = when {
                            lockStatus is PinLockStatus.Locked -> " [BLOQUEADO]"
                            lockStatus is PinLockStatus.Blocked -> " [BLOQUEADO]"
                            playResult == "WON" -> " [✓ COMPLETADO]"
                            playResult == "PARTIAL" -> " [⚠ PARCIAL]"
                            playResult == "LOST" -> " [✗ FALLADO]"
                            else -> ""
                        }

                        val itemBg = when {
                            lockStatus !is PinLockStatus.Unlocked -> Color(0xFF2C1313).copy(alpha = 0.2f)
                            playResult != null -> Color.DarkGray.copy(alpha = 0.2f)
                            else -> Color.Black
                        }

                        Surface(
                            onClick = {
                                selectedPin = location
                                showLocationListDialog = false
                            },
                            color = itemBg,
                            border = BorderStroke(1.dp, if (lockStatus is PinLockStatus.Unlocked && playResult == null) CyanNeon.copy(alpha = 0.3f) else Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = location.name,
                                    color = if (lockStatus is PinLockStatus.Unlocked) Color.White else Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = statusLabel,
                                    color = if (playResult == "WON") Color.Green else if (playResult == "PARTIAL") neonYellow else Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLocationListDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0D0E11),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )
    }

    // Minigame Overlay Display
    if (activeMinigameLocation != null) {
        MinigamesOverlay(
            locationName = activeMinigameLocation!!,
            viewModel = viewModel,
            onDismiss = { activeMinigameLocation = null }
        )
    }

    // INTRO synopsis card
    if (gameState.phase == GamePhase.INTRO && currentCase != null) {
        AlertDialog(
            onDismissRequest = { /* Lock */ },
            title = {
                Text(
                    text = currentCase.title.uppercase(),
                    color = neonYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EXPEDIENTE ADUANERO",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = currentCase.description,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.startInvestigation() },
                    colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("COMENZAR INVESTIGACIÓN", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BackgroundNoir,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, neonYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun LocationMarker(
    pin: LocationPin,
    lockStatus: PinLockStatus,
    playResult: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val color = when {
        lockStatus !is PinLockStatus.Unlocked -> Color.DarkGray
        playResult == "WON" -> Color.Green
        playResult == "PARTIAL" -> neonYellow
        playResult == "LOST" -> Color.Red
        else -> CyanNeon
    }

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp)
        ) {
            // Pulse circle animation for active playable nodes
            if (lockStatus is PinLockStatus.Unlocked && playResult == null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        .alpha(pulseAlpha)
                        .border(1.5.dp, color, CircleShape)
                )
            }

            // Core Marker Node
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, color, CircleShape)
            ) {
                Text(
                    text = pin.abbreviation,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = pin.name,
            color = if (lockStatus is PinLockStatus.Unlocked) Color.White else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            textAlign = TextAlign.Center
        )
    }
}
