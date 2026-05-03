package com.example.obsidian.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.obsidian.ui.components.CyberBottomBar

@Composable
fun MapScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CyberBottomBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF09090B)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Map Screen Content",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
