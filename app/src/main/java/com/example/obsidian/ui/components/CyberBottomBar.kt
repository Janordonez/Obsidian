package com.example.obsidian.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.obsidian.navigation.Screen

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

    Surface(
        color = Color(0xFF09090B).copy(alpha = 0.95f),
        modifier = Modifier.height(80.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val scale by androidx.compose.animation.core.animateFloatAsState(if (isSelected) 1f else 0.9f)

                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clickable {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            if (isSelected) {
                                drawLine(
                                    color = Color(0xFF00F0FF),
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
                    androidx.compose.material3.Icon(
                        imageVector = screen.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF00F0FF) else Color(0xFF52525B),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF52525B)
                    )
                }
            }
        }
    }
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
                        androidx.compose.material3.Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label
                        )
                    },
                    label = {
                        Text(text = screen.label)
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
