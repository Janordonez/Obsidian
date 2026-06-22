package com.example.obsidian.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.obsidian.navigation.Screen

@Composable
fun CyberBottomBar(navController: NavController) {
    CyberBottomBar(selectedScreen = Screen.Case, onScreenSelected = {})
}

@Composable
fun CyberBottomBar(
    selectedScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    val items = listOf(
        Screen.Case,
        Screen.Clue,
        Screen.Evidence,
        Screen.Interrogation,
        Screen.Map
    )

    Surface(
        color = Color(0xFF09090B).copy(alpha = 0.95f),
        modifier = Modifier.height(80.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
            items.forEach { screen ->
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
                            }
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
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F0FF),
                        selectedTextColor = Color(0xFF00F0FF),
                        indicatorColor = Color(0xFF002022),
                        unselectedIconColor = Color(0xFF52525B),
                        unselectedTextColor = Color(0xFF52525B)
                    )
                )
            }
        }
    }
}
