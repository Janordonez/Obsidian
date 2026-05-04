package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val darkNavy = Color(0xFF030617)
    val deepBlack = Color(0xFF000000)
    val neonYellow = Color(0xFFFFF05A)
    val mutedText = Color(0xFF7F8EA3)

    // UI state (local only)
    var soundEffects by remember { mutableStateOf(true) }
    var music by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(false) }
    var textSpeed by remember { mutableStateOf("Normal") }
    var difficulty by remember { mutableStateOf("Normal") }
    var savedMessageVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(darkNavy, deepBlack)))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title
            Text(
                text = "SYSTEM SETTINGS",
                style = TextStyle(
                    color = neonYellow,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                color = Color(0xFF06070A)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Sound Effects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound Effects", color = Color.White)
                        Switch(checked = soundEffects, onCheckedChange = { soundEffects = it })
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = mutedText.copy(alpha = 0.12f))

                    // Music
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Music", color = Color.White)
                        Switch(checked = music, onCheckedChange = { music = it })
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = mutedText.copy(alpha = 0.12f))

                    // Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vibration", color = Color.White)
                        Switch(checked = vibration, onCheckedChange = { vibration = it })
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = mutedText.copy(alpha = 0.12f))

                    // Text Speed
                    Text("Text Speed", color = mutedText, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val speeds = listOf("Slow", "Normal", "Fast")
                        speeds.forEach { s ->
                            val selected = s == textSpeed
                            Button(
                                onClick = { textSpeed = s },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) neonYellow else Color(0xFF0B0F14),
                                    contentColor = if (selected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(s)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = mutedText.copy(alpha = 0.12f))

                    // Difficulty
                    Text("Difficulty", color = mutedText, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val levels = listOf("Easy", "Normal", "Hard")
                        levels.forEach { lvl ->
                            val selected = lvl == difficulty
                            Button(
                                onClick = { difficulty = lvl },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) neonYellow else Color(0xFF0B0F14),
                                    contentColor = if (selected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(lvl)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onBack() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = neonYellow),
                    border = BorderStroke(1.dp, neonYellow)
                ) {
                    Text("BACK")
                }

                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = {
                            // Save settings in local UI state (no persistence requested)
                            savedMessageVisible = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black)
                    ) {
                        Text("SAVE SETTINGS")
                    }

                    if (savedMessageVisible) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Settings saved successfully",
                            color = neonYellow,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    SettingsScreen(onBack = {})
}


