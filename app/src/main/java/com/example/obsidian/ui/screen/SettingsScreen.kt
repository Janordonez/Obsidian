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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    var soundEffects by remember { mutableStateOf(true) }
    var music by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(false) }
    var textSpeed by remember { mutableStateOf("Normal") }
    var difficulty by remember { mutableStateOf("Normal") }
    var savedMessageVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(DarkNavy, DeepBlack)))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "SYSTEM SETTINGS",
                style = MaterialTheme.typography.headlineLarge.copy(color = neonYellow),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                color = SettingsSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound Effects", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = soundEffects, onCheckedChange = { soundEffects = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MutedText.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Music", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = music, onCheckedChange = { music = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MutedText.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vibration", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = vibration, onCheckedChange = { vibration = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MutedText.copy(alpha = 0.12f))

                    Text("Text Speed", color = MutedText, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val speeds = listOf("Slow", "Normal", "Fast")
                        speeds.forEach { s ->
                            val selected = s == textSpeed
                            Button(
                                onClick = { textSpeed = s },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) neonYellow else ButtonDark,
                                    contentColor = if (selected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(s, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MutedText.copy(alpha = 0.12f))

                    Text("Difficulty", color = MutedText, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val levels = listOf("Easy", "Normal", "Hard")
                        levels.forEach { lvl ->
                            val selected = lvl == difficulty
                            Button(
                                onClick = { difficulty = lvl },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) neonYellow else ButtonDark,
                                    contentColor = if (selected) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(lvl, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = neonYellow),
                    border = BorderStroke(1.dp, neonYellow)
                ) {
                    Text("BACK", style = MaterialTheme.typography.labelLarge)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = {
                            savedMessageVisible = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black)
                    ) {
                        Text("SAVE SETTINGS", style = MaterialTheme.typography.labelLarge)
                    }

                    if (savedMessageVisible) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Settings saved successfully",
                            color = neonYellow,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }
        }
    }
}
