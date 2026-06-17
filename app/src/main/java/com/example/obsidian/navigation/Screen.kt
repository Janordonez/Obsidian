package com.example.obsidian.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Case: Screen("case_screen", "Inicio", Icons.Default.Star)
    object Clue: Screen("clue_screen", "Pistas", Icons.Default.Search)
    object Evidence: Screen("evidence_screen", "Evidencias", Icons.Default.Email)
    object Interrogation: Screen("interrogation_screen", "Interrogatorio", Icons.Default.Person)
    object Map: Screen("map_screen", "Mapa", Icons.Default.LocationOn)
    object Settings: Screen("settings_screen", "Ajustes", Icons.Default.Settings)
    object Accusation: Screen("accusation_screen", "Acusación", Icons.Default.Gavel)
    object Verdict: Screen("verdict_screen", "Veredicto", Icons.Default.Verified)
}