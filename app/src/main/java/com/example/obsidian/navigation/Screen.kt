package com.example.obsidian.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Case: Screen("case_screen", "Case", Icons.Default.Star)
    object Clue: Screen("clue_screen", "Clue", Icons.Default.Search)
    object Evidence: Screen("evidence_screen", "Evidence", Icons.Default.Email)
    object Interrogation: Screen("interrogation_screen", "Interrogation", Icons.Default.Person)
    object Map: Screen("map_screen", "Map", Icons.Default.LocationOn)
}