package com.example.obsidian.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.obsidian.ui.components.CyberBottomBar

@Composable
fun InterrogationScreen(navController: NavController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF09090B),
        bottomBar = {
            CyberBottomBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Interrogation Screen Content",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}
