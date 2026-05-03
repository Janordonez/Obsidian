package com.example.obsidian.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Caso : Screen("caso", "CASO", Icons.Default.MailOutline)
    object Pistas : Screen("pistas", "PISTAS", Icons.Default.Menu)
    object Charla : Screen("charla", "CHARLA", Icons.Default.Person)
    object Mapa : Screen("mapa", "MAPA", Icons.Default.LocationOn)
    object Pruebas : Screen("pruebas", "PRUEBAS", Icons.Default.Star)
}