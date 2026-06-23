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

@Composable
fun AccusationScreen(
    navController: NavController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Corregido: Acceso al StateFlow de gameState con tipado explícito e importaciones añadidas
    val state: GameState by viewModel.gameState.collectAsStateWithLifecycle()
    val gameCase = state.currentCase

    var selectedSuspectId by remember { mutableStateOf<String?>(null) }
    val isSubmitting = viewModel.isSubmittingAccusation

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PRESENTAR CARGOS",
            color = AggressiveRed,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        
        Text(
            "Selecciona al culpable. Un error terminará la investigación.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedSuspectId = suspect.id },
                            colors = RadioButtonDefaults.colors(selectedColor = AggressiveRed)
                        )
                        Column {
                            Text(suspect.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(suspect.relation, color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                selectedSuspectId?.let { id ->
                    viewModel.submitAccusation(id)
                    navController.navigate(Screen.Verdict.route)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AggressiveRed),
            enabled = selectedSuspectId != null && !isSubmitting,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("DICTAR VEREDICTO", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}
