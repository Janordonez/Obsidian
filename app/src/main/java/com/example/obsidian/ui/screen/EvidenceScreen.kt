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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .padding(16.dp)
    ) {
        Text(
            text = "EVIDENCE BOARD",
            color = neonYellow,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        
        Text(
            text = "Conecta pistas para revelar la verdad",
            color = CyanNeon,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 🧾 Evidence Board (LazyVerticalGrid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(clues) { clue ->
                ClueCard(
                    clue = clue,
                    isSelected = selectedClues.contains(clue.id),
                    onToggle = { viewModel.toggleClueSelection(clue.id) }
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

        // Botón de Deducción (Mecánica 5)
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
            Text("EVALUAR DEDUCCIÓN", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ClueCard(clue: Clue, isSelected: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        color = if (isSelected) CyanNeon.copy(alpha = 0.1f) else Color.Black,
        border = BorderStroke(1.dp, if (isSelected) CyanNeon else Color.DarkGray),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Search,
                contentDescription = null,
                tint = if (isSelected) CyanNeon else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = clue.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
