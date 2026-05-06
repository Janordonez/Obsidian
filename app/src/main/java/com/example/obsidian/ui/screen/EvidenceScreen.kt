package com.example.obsidian.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.BackgroundNoir
import com.example.obsidian.ui.theme.CyanNeon
import com.example.obsidian.ui.theme.neonYellow
import com.example.obsidian.ui.theme.SurfaceDark
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun EvidenceScreen(
    navController: NavController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameCase by viewModel.currentCase.collectAsStateWithLifecycle()
    val deductionAnalysis by viewModel.deductionAnalysis.collectAsStateWithLifecycle()
    val isAnalyzing = viewModel.isAnalyzing

    val suspects = gameCase?.suspects ?: emptyList()
    val clues = gameCase?.clues ?: emptyList()

    var selectedClue by remember { mutableStateOf<String?>(null) }
    
    // Key is Suspect Name, Value is list of Clue titles
    var assignedCluesBySuspect by remember {
        mutableStateOf(emptyMap<String, List<String>>())
    }

    fun assignClueToSuspect(suspectName: String) {
        val clueTitle = selectedClue ?: return

        val updatedMap = assignedCluesBySuspect.toMutableMap()
        
        // Remove clue from any other suspect first
        assignedCluesBySuspect.forEach { (name, list) ->
            updatedMap[name] = list.filterNot { it == clueTitle }
        }

        val currentClues = updatedMap[suspectName] ?: emptyList()
        updatedMap[suspectName] = (currentClues + clueTitle).distinct()

        assignedCluesBySuspect = updatedMap
        selectedClue = null
    }

    val scrollState = rememberScrollState()

    if (gameCase == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay expediente activo", color = Color.Gray)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050816), Color(0xFF000000))
                )
            )
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EVIDENCE BOARD",
            color = neonYellow,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
        )

        Text(
            text = "Connect clues. Expose contradictions.",
            color = CyanNeon,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundNoir),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AVAILABLE CLUES",
                        color = CyanNeon,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Select one clue, then tap a suspect card to assign it.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                clues.forEach { clue ->
                    val isSelected = clue.title == selectedClue
                    Button(
                        onClick = { selectedClue = clue.title },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) neonYellow else SurfaceDark,
                            contentColor = if (isSelected) Color.Black else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else neonYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = clue.title)
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundNoir),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SUSPECT FILES",
                    color = neonYellow,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                suspects.forEach { suspect ->
                    val assignedClues = assignedCluesBySuspect[suspect.name].orEmpty()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                assignClueToSuspect(suspect.name)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suspect.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Text(
                                text = if (assignedClues.isEmpty()) {
                                    "Assigned clues: none"
                                } else {
                                    "Assigned clues: ${assignedClues.joinToString(", ")}"
                                },
                                color = if (assignedClues.isEmpty()) Color(0xFF8B96A8) else CyanNeon,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = if (selectedClue == null) {
                                    "Tap a clue first, then tap this suspect to assign it."
                                } else {
                                    "Tap to assign: ${selectedClue ?: ""}"
                                },
                                color = neonYellow.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.analyzeDeductions(assignedCluesBySuspect)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnalyzing && assignedCluesBySuspect.values.any { it.isNotEmpty() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = neonYellow,
                contentColor = Color.Black
            )
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "ANALYZE DEDUCTION", fontWeight = FontWeight.Bold)
            }
        }

        if (deductionAnalysis.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundNoir),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = deductionAnalysis,
                    color = CyanNeon,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
