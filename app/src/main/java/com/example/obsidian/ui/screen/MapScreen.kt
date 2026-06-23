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
import androidx.compose.ui.draw.blur
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

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isGenerating = viewModel.isGenerating
    val currentCase = gameState.currentCase
    val availableClues = currentCase?.clues?.filter { it.isAvailable } ?: emptyList()

    var offset by remember { mutableStateOf(Offset.Zero) }

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

        // Pistas Disponibles
        Box(modifier = Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            availableClues.forEachIndexed { index, clue ->
                val xPos = (200 + (index * 150)) % 800
                val yPos = (300 + (index * 200)) % 1200
                
                ClueMarker(
                    clue = clue,
                    modifier = Modifier.offset { IntOffset(xPos, yPos) },
                    onClick = { viewModel.collectClue(clue.id) }
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
            Text("OBSIDIAN SCANNER v1.0", color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Botón de Exploración (Mecánica 3)
        Button(
            onClick = { viewModel.exploreScene() },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).height(56.dp).width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("INVESTIGAR ÁREA", fontWeight = FontWeight.Black)
        }
        
        // Contador de exploración
        Text(
            text = "SCAN COUNT: ${gameState.explorationCount}",
            color = CyanNeon.copy(alpha = 0.6f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
}

@Composable
fun ClueMarker(clue: com.example.obsidian.data.model.Clue, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = if (clue.isFound) Color.Gray else CyanNeon,
            shape = CircleShape,
            modifier = Modifier.size(12.dp).border(2.dp, Color.White, CircleShape)
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (clue.isFound) "[RECOLECTADO]" else clue.title,
            color = if (clue.isFound) Color.Gray else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.7f)).padding(horizontal = 4.dp)
        )
    }
}
