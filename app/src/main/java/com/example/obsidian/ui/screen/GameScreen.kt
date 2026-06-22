package com.example.obsidian.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Case) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050816),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedScreen) {
                    Screen.Case -> CaseScreen(
                        onTabSelected = { screen ->
                            selectedScreen = screen
                        }
                    )
                    Screen.Clue -> ClueScreen()
                    Screen.Evidence -> EvidenceBoardScreen()
                    Screen.Interrogation -> InterrogationScreen()
                    Screen.Map -> MapScreen()
                }
            }

            GameBottomBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { selectedScreen = it }
            )
        }
    }
}

@Composable
private fun GameBottomBar(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(containerColor = Color(0xFF09090B).copy(alpha = 0.95f)) {
        listOf(Screen.Case, Screen.Clue, Screen.Evidence, Screen.Interrogation, Screen.Map).forEach { screen ->
            NavigationBarItem(
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Text(
                        text = when (screen) {
                            Screen.Case -> "Case"
                            Screen.Clue -> "Clue"
                            Screen.Evidence -> "Evidence"
                            Screen.Interrogation -> "Talk"
                            Screen.Map -> "Map"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                label = {
                    Text(
                        text = when (screen) {
                            Screen.Case -> "Case"
                            Screen.Clue -> "Clue"
                            Screen.Evidence -> "Evidence"
                            Screen.Interrogation -> "Talk"
                            Screen.Map -> "Map"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun ClueScreen() {
    PlaceholderScreen("CLUE ARCHIVE", "Forensic archive coming online.")
}

@Composable
private fun InterrogationScreen() {
    PlaceholderScreen("INTERROGATION", "Suspect dialogue interface coming soon.")
}

@Composable
private fun MapScreen() {
    PlaceholderScreen("INVESTIGATION MAP", "Location tracking and site analysis will appear here.")
}

@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F14)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = title, color = Color(0xFFFFF05A), style = MaterialTheme.typography.headlineMedium)
                    Text(text = subtitle, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
