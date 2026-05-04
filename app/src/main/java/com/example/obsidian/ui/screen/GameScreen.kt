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
import androidx.compose.ui.unit.dp
import com.example.obsidian.navigation.Screen
import com.example.obsidian.ui.components.CyberBottomBar

@Composable
fun GameScreen() {
    var selectedScreen: Screen by remember { mutableStateOf(Screen.Case) }

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
                    Screen.Case -> CaseScreen(onTabSelected = { selectedScreen = it })
                    Screen.Clue -> CluePlaceholderScreen()
                    Screen.Evidence -> EvidenceBoardScreen()
                    Screen.Interrogation -> InterrogationPlaceholderScreen()
                    Screen.Map -> MapPlaceholderScreen()
                }
            }

            CyberBottomBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { selectedScreen = it }
            )
        }
    }
}

@Composable
private fun CluePlaceholderScreen() {
    PlaceholderScreen("CLUE ARCHIVE", "Forensic archive coming online.")
}

@Composable
private fun EvidencePlaceholderScreen() {
    PlaceholderScreen("EVIDENCE BOARD", "Physical and digital evidence will appear here.")
}

@Composable
private fun InterrogationPlaceholderScreen() {
    PlaceholderScreen("INTERROGATION", "Suspect dialogue interface coming soon.")
}

@Composable
private fun MapPlaceholderScreen() {
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
