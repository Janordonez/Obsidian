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
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.BackgroundNoir
import com.example.obsidian.ui.theme.CyanNeon
import com.example.obsidian.ui.theme.neonYellow
import com.example.obsidian.ui.theme.SurfaceDark

@Composable
fun EvidenceScreen(navController: NavController, modifier: Modifier = Modifier) {
    val suspects = listOf("Elias Vance", "Mara Quinn", "Victor Hale", "Lena Crowe")
    val clues = listOf(
        "Black sedan near the dock",
        "Torn black glove",
        "Anonymous audio call",
        "Missing archive key",
        "Partial fingerprint"
    )

    var selectedClue by remember { mutableStateOf<String?>(null) }
    var deductionMessage by remember { mutableStateOf("") }
    var assignedCluesBySuspect by remember {
        mutableStateOf(
            suspects.associateWith { emptyList<String>() }
        )
    }

    fun assignClueToSuspect(suspect: String) {
        val clue = selectedClue ?: return

        val updatedMap = suspects.associateWith { currentSuspect ->
            val cluesForSuspect = assignedCluesBySuspect[currentSuspect].orEmpty()
            if (currentSuspect == suspect) {
                (cluesForSuspect + clue).distinct()
            } else {
                cluesForSuspect.filterNot { it == clue }
            }
        }

        assignedCluesBySuspect = updatedMap
        selectedClue = null
        deductionMessage = "$clue assigned to $suspect"
    }

    val scrollState = rememberScrollState()

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
                    val isSelected = clue == selectedClue
                    Button(
                        onClick = { selectedClue = clue },
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
                            Text(text = clue)
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
                    val assignedClues = assignedCluesBySuspect[suspect].orEmpty()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                assignClueToSuspect(suspect)
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
                                    text = suspect,
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

        Text(
            text = if (assignedCluesBySuspect.values.sumOf { it.size } >= 3) {
                "Ready for deduction analysis"
            } else {
                "Need more assigned clues before analysis"
            },
            color = CyanNeon,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        Button(
            onClick = {
                val totalAssignedClues = assignedCluesBySuspect.values.sumOf { it.size }
                deductionMessage = if (totalAssignedClues >= 3) {
                    "Deduction accepted. New lead unlocked."
                } else {
                    "Deduction incomplete. Keep investigating."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = neonYellow,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = if (assignedCluesBySuspect.values.sumOf { it.size } >= 3) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ANALYZE DEDUCTION",
                fontWeight = FontWeight.Bold
            )
        }

        if (deductionMessage.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundNoir),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = deductionMessage,
                    color = CyanNeon,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
