package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun VerdictScreen(
    navController: NavController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val result by viewModel.gameResult.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    if (result == null) {
        Box(modifier = modifier.fillMaxSize().background(BackgroundNoir), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanNeon)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNoir)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (result!!.isCorrect) "CASO RESUELTO" else "CASO CERRADO",
            color = if (result!!.isCorrect) CyanNeon else AggressiveRed,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (result!!.isCorrect) CyanNeon else AggressiveRed, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "INFORME FINAL",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result!!.epilogue,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "ACUSADO",
                value = result!!.accusedSuspectName,
                color = if (result!!.isCorrect) CyanNeon else AggressiveRed,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "CULPABLE REAL",
                value = result!!.actualGuiltyName,
                color = CyanNeon,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            label = "PISTAS ASIGNADAS",
            value = "${result!!.correctClueAssignments} / ${result!!.totalClues}",
            color = neonYellow,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.resetGame()
                navController.navigate(Screen.Case.route) {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Replay, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("NUEVA INVESTIGACIÓN", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Black,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
