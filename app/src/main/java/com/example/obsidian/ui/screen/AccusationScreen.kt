package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidian.navigation.Screen
import com.example.obsidian.ui.theme.*
import com.example.obsidian.ui.viewmodel.GameViewModel
import com.example.obsidian.data.model.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.font.FontFamily

import androidx.compose.material.icons.filled.Gavel

@Composable
fun AccusationScreen(
    navController: NavController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state: GameState by viewModel.gameState.collectAsStateWithLifecycle()
    val gameCase = state.currentCase

    // Restricción: No se puede acceder sin al menos 3 pistas vinculadas
    if (state.clueAssignments.size < 3) {
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
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = AggressiveRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ACUSACIÓN FORMAL BLOQUEADA",
                    color = AggressiveRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Debes vincular al menos 3 pistas en el tablero de evidencias antes de poder dictar una acusación formal.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(Screen.Evidence.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("IR AL TABLERO DE EVIDENCIAS", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var selectedSuspectId by remember { mutableStateOf<String?>(null) }
    var selectedClueId by remember { mutableStateOf<String?>(null) }
    val isSubmitting = viewModel.isSubmittingAccusation

    val foundClues = gameCase?.clues?.filter { it.isFound } ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "DICTAR DICTAMEN FISCAL",
                color = AggressiveRed,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            
            Text(
                "Selecciona al culpable y la prueba reina de la acusación.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Título Sospechosos
        item {
            Text(
                text = "1. ACUSAR SOSPECHOSO",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
        }

        // Lista de sospechosos
        items(gameCase?.suspects ?: emptyList()) { suspect ->
            val isSelected = selectedSuspectId == suspect.id
            Surface(
                onClick = { selectedSuspectId = suspect.id },
                color = if (isSelected) AggressiveRed.copy(alpha = 0.2f) else Color.Black,
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) AggressiveRed else Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedSuspectId = suspect.id },
                        colors = RadioButtonDefaults.colors(selectedColor = AggressiveRed)
                    )
                    Column {
                        Text(suspect.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(suspect.relation, color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }

        // Título Prueba Reina
        item {
            Text(
                text = "2. SELECCIONAR PRUEBA REINA",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
        }

        if (foundClues.isEmpty()) {
            item {
                Text(
                    text = "No has recolectado pistas aún.",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Lista de pruebas reinas
            items(foundClues) { clue ->
                val isSelected = selectedClueId == clue.id
                Surface(
                    onClick = { selectedClueId = clue.id },
                    color = if (isSelected) CyanNeon.copy(alpha = 0.15f) else Color.Black,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) CyanNeon else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedClueId = clue.id },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                        )
                        Column {
                            Text(clue.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(clue.locationName, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Botón Presentar Cargos
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    selectedSuspectId?.let { sId ->
                        selectedClueId?.let { cId ->
                            viewModel.submitAccusation(sId, cId)
                            navController.navigate(Screen.Verdict.route)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AggressiveRed),
                enabled = selectedSuspectId != null && selectedClueId != null && !isSubmitting,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("DICTAR SENTENCIA CONDENATORIA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}
