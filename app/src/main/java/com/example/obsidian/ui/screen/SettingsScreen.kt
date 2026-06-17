package com.example.obsidian.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.*
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    navController: NavController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var savedMessageVisible by remember { mutableStateOf(false) }
    val currentCase by viewModel.currentCase.collectAsState()
    val scrollState = rememberScrollState()

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
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    // --- Audio Settings ---
                    Text(
                        text = "AUDIO CONFIGURATION",
                        color = CyanNeon,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sound Effects", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = viewModel.isEffectsEnabled, 
                            onCheckedChange = { viewModel.isEffectsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanNeon)
                        )
                    }
                    
                    Slider(
                        value = viewModel.effectsVolume,
                        onValueChange = { viewModel.effectsVolume = it },
                        colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanDark)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Atmospheric Music", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = viewModel.isMusicEnabled, 
                            onCheckedChange = { viewModel.isMusicEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanNeon)
                        )
                    }
                    
                    Slider(
                        value = viewModel.musicVolume,
                        onValueChange = { viewModel.musicVolume = it },
                        colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanDark)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MutedText.copy(alpha = 0.2f))

                    // --- Case Information (AI Data) ---
                    Text(
                        text = "ACTIVE INVESTIGATION DATA",
                        color = CyanNeon,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentCase != null) {
                        val case = currentCase!!
                        CaseInfoItem(label = "CASE TITLE", value = case.title)
                        CaseInfoItem(label = "PRIMARY LOCATION", value = case.clues.firstOrNull()?.locationName ?: "Unknown")
                        CaseInfoItem(label = "SUSPECTS COUNT", value = "${case.suspects.size} Profiles")
                        CaseInfoItem(label = "SUSPECT NAMES", value = case.suspects.joinToString(", ") { it.name })
                        CaseInfoItem(label = "EVIDENCE COUNT", value = "${case.clues.size} Detected Items")
                    } else {
                        Text(
                            text = "NO ACTIVE CASE DATA FOUND",
                            color = Color.Red.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MutedText.copy(alpha = 0.2f))

                    Text("System Status", color = MutedText, style = MaterialTheme.typography.labelLarge)
                    Text("GROQ CORE: ONLINE", color = CyanNeon, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("GEMINI CORE: ACTIVE", color = CyanNeon, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("MEMORY: SECURE", color = CyanNeon, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
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

                Button(
                    onClick = { savedMessageVisible = true },
                    colors = ButtonDefaults.buttonColors(containerColor = neonYellow, contentColor = Color.Black)
                ) {
                    Text("SAVE SETTINGS", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            if (savedMessageVisible) {
                Text(
                    text = "Settings applied to local storage",
                    color = neonYellow,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CaseInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
