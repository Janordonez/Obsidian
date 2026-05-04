package com.example.obsidian.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.obsidian.navigation.Screen
import com.example.obsidian.ui.theme.primary

/**
 * Una barra de navegación inferior personalizada con estética "cyber" para la aplicación.
 *
 * Este componente muestra una lista de destinos de navegación definidos por la clase [Screen].
 * Presenta un diseño oscuro con efectos visuales de neón, incluyendo una animación de escala
 * para el elemento seleccionado y una línea indicadora en la parte superior.
 *
 * @param navController El [NavController] encargado de gestionar la navegación y el estado de la pila de retroceso.
 */
@Composable
fun CyberBottomBar(navController: NavController) {
    val items = listOf(
        Screen.Case,
        Screen.Clue,
        Screen.Evidence,
        Screen.Interrogation,
        Screen.Map
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Contenedor principal de la barra
    Surface(
        color = Color(0xFF09090B).copy(alpha = 0.95f), // zinc-950
        modifier = Modifier.height(80.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)) // Borde superior sutil
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route

                // Efecto de escala al interactuar
                val scale by animateFloatAsState(if (isSelected) 1f else 0.9f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        // Borde superior azul si está seleccionado
                        .drawWithContent {
                            drawContent()
                            if (isSelected) {
                                drawLine(
                                    color = primary,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                        }
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(if (isSelected) Color(0xFF002022).copy(alpha = 0.2f) else Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = null,
                        tint = if (isSelected) primary else Color(0xFF52525B), // zinc-600
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) primary else Color(0xFF52525B)
                    )
                }
            }
        }
    }
}
