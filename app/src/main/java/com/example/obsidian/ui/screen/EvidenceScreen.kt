package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidian.data.model.Clue
import com.example.obsidian.ui.theme.*
import com.example.obsidian.ui.viewmodel.GameViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun EvidenceScreen(
    navController: NavController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val selectedClues by viewModel.selectedClues.collectAsStateWithLifecycle()
    val deductionResult by viewModel.deductionResult.collectAsStateWithLifecycle()

    val clues = gameState.currentCase?.clues?.filter { it.isFound } ?: emptyList()
    var selectedClueDetail by remember { mutableStateOf<Clue?>(null) }

    // Bloqueo de Tablero: Se necesitan al menos 2 sospechosos interrogados
    if (gameState.interrogatedSuspects.size < 2) {
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
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TABLERO DE EVIDENCIAS BLOQUEADA",
                    color = CyanNeon,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Debes terminar de interrogar a al menos 2 sospechosos antes de analizar y relacionar las evidencias en el tablero.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(com.example.obsidian.navigation.Screen.Interrogation.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("IR A INTERROGATORIOS", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EVIDENCE BOARD",
                    color = neonYellow,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Conecta pistas y vincula sospechosos",
                    color = CyanNeon,
                    fontSize = 12.sp
                )
            }

            // Si hay al menos 3 pistas vinculadas, muestra el botón para presentar cargos
            if (gameState.clueAssignments.size >= 3) {
                Button(
                    onClick = { navController.navigate(com.example.obsidian.navigation.Screen.Accusation.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = AggressiveRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("CARGOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de Evidencias
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(clues) { clue ->
                val assignedSuspectId = gameState.clueAssignments[clue.id]
                val assignedSuspectName = gameState.currentCase?.suspects?.find { it.id == assignedSuspectId }?.name
                ClueCard(
                    clue = clue,
                    isSelected = selectedClues.contains(clue.id),
                    assignedSuspectName = assignedSuspectName,
                    onToggle = { selectedClueDetail = clue }
                )
            }
        }

        // Feedback de Deducción
        if (deductionResult != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (deductionResult!!.isValid) Color(0xFF001A1A) else Color(0xFF1A0000)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, if (deductionResult!!.isValid) CyanNeon else AggressiveRed, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = deductionResult!!.message,
                    color = if (deductionResult!!.isValid) CyanNeon else Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Botón de Deducción
        Button(
            onClick = { viewModel.evaluateDeduction() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            enabled = selectedClues.size >= 2
        ) {
            Icon(Icons.Default.Psychology, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EVALUAR DEDUCCIÓN COGNITIVA", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Diálogo de Detalle y Vinculación de Pistas
    selectedClueDetail?.let { clue ->
        val assignedSuspectId = gameState.clueAssignments[clue.id]
        AlertDialog(
            onDismissRequest = { selectedClueDetail = null },
            title = {
                Text(clue.title.uppercase(), color = CyanNeon, fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(text = clue.description, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.toggleClueSelection(clue.id)
                        }
                    ) {
                        Checkbox(
                            checked = selectedClues.contains(clue.id),
                            onCheckedChange = { viewModel.toggleClueSelection(clue.id) },
                            colors = CheckboxDefaults.colors(checkedColor = CyanNeon)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar para análisis de conexión", color = Color.LightGray, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("VINCULAR SOSPECHOSO:", color = neonYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        gameState.currentCase?.suspects?.forEach { suspect ->
                            val isLinked = assignedSuspectId == suspect.id
                            Surface(
                                onClick = {
                                    viewModel.linkClueToSuspect(clue.id, suspect.id)
                                    selectedClueDetail = null
                                },
                                color = if (isLinked) CyanNeon.copy(alpha = 0.15f) else Color.Black,
                                border = BorderStroke(1.dp, if (isLinked) CyanNeon else Color.DarkGray),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = suspect.name,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        // Desvincular
                        Surface(
                            onClick = {
                                viewModel.linkClueToSuspect(clue.id, null)
                                selectedClueDetail = null
                            },
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DESVINCULAR PISTA",
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedClueDetail = null }) {
                    Text("CERRAR", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0D0E11),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun ClueCard(clue: Clue, isSelected: Boolean, assignedSuspectName: String?, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        color = if (isSelected) CyanNeon.copy(alpha = 0.1f) else Color.Black,
        border = BorderStroke(1.dp, if (isSelected) CyanNeon else Color.DarkGray),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Search,
                contentDescription = null,
                tint = if (isSelected) CyanNeon else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = clue.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
            if (assignedSuspectName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📎 ${assignedSuspectName.split(" ").first()}",
                    color = neonYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
